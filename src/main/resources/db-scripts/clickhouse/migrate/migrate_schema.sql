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

## db_schema_version: 3.0.1
## description: Add unified resource_data table and backfill from legacy resource_sample/patient/study tables
CREATE TABLE IF NOT EXISTS resource_data
(
    `RESOURCE_DATA_ID` Int64,
    `RESOURCE_ID`      String,
    `CANCER_STUDY_ID`  Int32,
    `ENTITY_TYPE`      String,
    `PATIENT_ID`       Nullable(String),
    `SAMPLE_ID`        Nullable(String),
    `URL`              String,
    `DISPLAY_NAME`     Nullable(String),
    `TYPE`             Nullable(String),
    `METADATA`         Nullable(String),
    `PRIORITY`         Int32
) ENGINE = MergeTree ORDER BY (CANCER_STUDY_ID, RESOURCE_ID, RESOURCE_DATA_ID);

-- Backfill is guarded by a deterministic RESOURCE_DATA_ID (hash of the natural key) so this
-- section is safe to re-run: rows already present are excluded via NOT IN.
INSERT INTO resource_data
    (RESOURCE_DATA_ID, RESOURCE_ID, CANCER_STUDY_ID, ENTITY_TYPE,
     PATIENT_ID, SAMPLE_ID, URL, DISPLAY_NAME, TYPE, METADATA, PRIORITY)
SELECT
    toInt64(cityHash64(rs.resource_id, s.stable_id, rs.url)),
    rs.resource_id,
    toInt32(cs.cancer_study_id),
    'SAMPLE',
    p.stable_id,
    s.stable_id,
    rs.url,
    NULL, NULL, NULL, 0
FROM resource_sample rs
INNER JOIN sample       s  ON rs.internal_id    = s.internal_id
INNER JOIN patient      p  ON s.patient_id      = p.internal_id
INNER JOIN cancer_study cs ON p.cancer_study_id = cs.cancer_study_id
WHERE toInt64(cityHash64(rs.resource_id, s.stable_id, rs.url)) NOT IN (
    SELECT RESOURCE_DATA_ID FROM resource_data
);

INSERT INTO resource_data
    (RESOURCE_DATA_ID, RESOURCE_ID, CANCER_STUDY_ID, ENTITY_TYPE,
     PATIENT_ID, SAMPLE_ID, URL, DISPLAY_NAME, TYPE, METADATA, PRIORITY)
SELECT
    toInt64(cityHash64(rp.resource_id, pt.stable_id, rp.url)),
    rp.resource_id,
    toInt32(cs.cancer_study_id),
    'PATIENT',
    pt.stable_id,
    NULL,
    rp.url,
    NULL, NULL, NULL, 0
FROM resource_patient rp
INNER JOIN patient      pt ON rp.internal_id     = pt.internal_id
INNER JOIN cancer_study cs ON pt.cancer_study_id = cs.cancer_study_id
WHERE toInt64(cityHash64(rp.resource_id, pt.stable_id, rp.url)) NOT IN (
    SELECT RESOURCE_DATA_ID FROM resource_data
);

INSERT INTO resource_data
    (RESOURCE_DATA_ID, RESOURCE_ID, CANCER_STUDY_ID, ENTITY_TYPE,
     PATIENT_ID, SAMPLE_ID, URL, DISPLAY_NAME, TYPE, METADATA, PRIORITY)
SELECT
    toInt64(cityHash64(rst.resource_id, toString(rst.internal_id), rst.url)),
    rst.resource_id,
    toInt32(rst.internal_id),
    'STUDY',
    NULL, NULL,
    rst.url,
    NULL, NULL, NULL, 0
FROM resource_study rst
WHERE toInt64(cityHash64(rst.resource_id, toString(rst.internal_id), rst.url)) NOT IN (
    SELECT RESOURCE_DATA_ID FROM resource_data
);
