-- ClickHouse base-table migration file.
--
-- Format: one section per db_schema_version, applied strictly in ascending order by
-- migrate_db.py (see migrate_db.py in this directory for the runner).
--
--   ## db_schema_version: <version>
--   ## description: <one line>
--   <SQL statements for this section>
--
-- Rules for writing a section:
--   1. Sections should be idempotent wherever possible (IF EXISTS / IF NOT EXISTS) — ClickHouse
--      has no transactions, so a crash mid-section ideally leaves it safe to re-run from the top.
--      Not always achievable; use judgment, and note in the description when a section isn't.
--   2. Never DROP a column in the same section that reads from it.
--   3. For ORDER BY / primary-key changes: create a new table, INSERT ... SELECT, RENAME —
--      ClickHouse cannot ALTER these in place.
--   4. Don't write db_schema_version updates yourself — migrate_db.py advances
--      info.db_schema_version automatically after a section's SQL succeeds (and waits for that
--      and any other mutations the section triggered to finish before treating it as applied).
--   5. If a section changes a table that feeds a derived table (see
--      db-scripts/clickhouse/populate_derived_tables.sql), no extra bookkeeping is needed here —
--      run migrate_db.py with --populate-derived-tables and it repopulates derived tables
--      automatically after any migration that actually applied something.
--
-- Sections for versions already recorded in the target database's info.db_schema_version are
-- skipped automatically — do not remove or renumber old sections once released.

## db_schema_version: 3.0.0
## description: ClickHouse-native migration era begins; collapse derived_table_schema_version into db_schema_version
ALTER TABLE info DROP COLUMN IF EXISTS derived_table_schema_version;

## db_schema_version: 3.1.0
## description: Add unified resource table and backfill from legacy resource_sample/patient/study tables
CREATE TABLE IF NOT EXISTS resource (
    `internal_id` Int64,
    `resource_id` String,
    `cancer_study_id` Int64,
    `resource_scope` String,
    `patient_id` Nullable(String),
    `sample_id` Nullable(String),
    `url` String,
    `display_name` Nullable(String),
    `metadata` Nullable(String)
) ENGINE = MergeTree ORDER BY (cancer_study_id, resource_id, patient_id, sample_id, internal_id)
  SETTINGS allow_nullable_key = 1;

-- create source tables in case this is a re-attempt at migration and they are missing
CREATE TABLE IF NOT EXISTS resource_sample (`internal_id` Int64, `resource_id` String, `url` String) ENGINE = MergeTree ORDER BY (internal_id, resource_id, url);
CREATE TABLE IF NOT EXISTS resource_patient (`internal_id` Int64, `resource_id` String, `url` String) ENGINE = MergeTree ORDER BY (internal_id, resource_id, url);
CREATE TABLE IF NOT EXISTS resource_study (`internal_id` Int64, `resource_id` String, `url` String) ENGINE = MergeTree ORDER BY (internal_id, resource_id, url);

-- Fail before anything is written if a legacy row references a sample, patient or study that is
-- not present. The backfills below join on those tables, so such a row is dropped rather than
-- migrated, and the DROPs at the end remove the only copy.
SELECT throwIf(
    (SELECT count() FROM resource_sample) != (
        SELECT count()
        FROM resource_sample rs
        INNER JOIN sample s ON rs.internal_id = s.internal_id
        INNER JOIN patient p ON s.patient_id = p.internal_id
        INNER JOIN cancer_study cs ON p.cancer_study_id = cs.cancer_study_id),
    'resource_sample holds rows whose sample, patient or study is missing; load them before migrating'
);
SELECT throwIf(
    (SELECT count() FROM resource_patient) != (
        SELECT count()
        FROM resource_patient rp
        INNER JOIN patient pt ON rp.internal_id = pt.internal_id
        INNER JOIN cancer_study cs ON pt.cancer_study_id = cs.cancer_study_id),
    'resource_patient holds rows whose patient or study is missing; load them before migrating'
);
SELECT throwIf(
    (SELECT count() FROM resource_study) != (
        SELECT count()
        FROM resource_study rst
        INNER JOIN cancer_study cs ON rst.internal_id = cs.cancer_study_id),
    'resource_study holds rows whose study is missing; load it before migrating'
);

INSERT INTO resource
    (internal_id, resource_id, cancer_study_id, resource_scope,
     patient_id, sample_id, url, display_name, metadata)
SELECT
    (SELECT ifNull(max(r.internal_id), 0) FROM resource r) + row_number() OVER (),
    rs.resource_id,
    cs.cancer_study_id,
    'SAMPLE',
    p.stable_id,
    s.stable_id,
    rs.url,
    NULL, NULL
FROM resource_sample rs
INNER JOIN sample       s  ON rs.internal_id    = s.internal_id
INNER JOIN patient      p  ON s.patient_id      = p.internal_id
INNER JOIN cancer_study cs ON p.cancer_study_id = cs.cancer_study_id
WHERE (cs.cancer_study_id, rs.resource_id, s.stable_id, rs.url) NOT IN (
    SELECT (cancer_study_id, resource_id, sample_id, url) FROM resource WHERE resource_scope = 'SAMPLE'
);

INSERT INTO resource
    (internal_id, resource_id, cancer_study_id, resource_scope,
     patient_id, sample_id, url, display_name, metadata)
SELECT
    (SELECT ifNull(max(r.internal_id), 0) FROM resource r) + row_number() OVER (),
    rp.resource_id,
    cs.cancer_study_id,
    'PATIENT',
    pt.stable_id,
    NULL,
    rp.url,
    NULL, NULL
FROM resource_patient rp
INNER JOIN patient      pt ON rp.internal_id     = pt.internal_id
INNER JOIN cancer_study cs ON pt.cancer_study_id = cs.cancer_study_id
WHERE (cs.cancer_study_id, rp.resource_id, pt.stable_id, rp.url) NOT IN (
    SELECT (cancer_study_id, resource_id, patient_id, url) FROM resource WHERE resource_scope = 'PATIENT'
);

INSERT INTO resource
    (internal_id, resource_id, cancer_study_id, resource_scope,
     patient_id, sample_id, url, display_name, metadata)
SELECT
    (SELECT ifNull(max(r.internal_id), 0) FROM resource r) + row_number() OVER (),
    rst.resource_id,
    rst.internal_id,
    'STUDY',
    NULL, NULL,
    rst.url,
    NULL, NULL
FROM resource_study rst
WHERE (rst.internal_id, rst.resource_id, rst.url) NOT IN (
    SELECT (cancer_study_id, resource_id, url) FROM resource WHERE resource_scope = 'STUDY'
);

-- Fail if rows are not each assigned a unique internal_id
SELECT throwIf(
    count() != uniqExact(internal_id),
    'resource backfill produced duplicate internal_id values'
) FROM resource;

DROP TABLE IF EXISTS resource_sample;
DROP TABLE IF EXISTS resource_patient;
DROP TABLE IF EXISTS resource_study;

## db_schema_version: 3.2.0
## description: Add allele-specific copy number (ASCN) tables ascn_seg (segment-level) and ascn_gene_level (gene-level)
CREATE TABLE IF NOT EXISTS ascn_seg
(
    `seg_id` Int64,
    `cancer_study_id` Int64,
    `sample_id` Int64,
    `chr` LowCardinality(String),
    `start` Int64,
    `end` Int64,
    `log_ratio` Nullable(Float64),
    `tcn` Nullable(Float64),
    `lcn` Nullable(Float64),
    `cellular_fraction` Nullable(Float64),
    `purity` Nullable(Float64),
    `ploidy` Nullable(Float64),
    `dip_log_r` Nullable(Float64)
)
    ENGINE = MergeTree
ORDER BY (cancer_study_id, sample_id, chr, start);

CREATE TABLE IF NOT EXISTS ascn_gene_level
(
    `gene_id` Int64,
    `cancer_study_id` Int64,
    `sample_id` Int64,
    `hugo_gene_symbol` String,
    `entrez_gene_id` Int64,
    `chr` LowCardinality(String),
    `start` Int64,
    `end` Int64,
    `tcn` Nullable(Float64),
    `lcn` Nullable(Float64),
    `cellular_fraction` Nullable(Float64),
    `purity` Nullable(Float64),
    `ploidy` Nullable(Float64),
    `dip_log_r` Nullable(Float64),
    INDEX idx_ascn_gene_level_hugo_symbol hugo_gene_symbol TYPE bloom_filter GRANULARITY 4
)
    ENGINE = MergeTree
ORDER BY (cancer_study_id, sample_id, hugo_gene_symbol);
