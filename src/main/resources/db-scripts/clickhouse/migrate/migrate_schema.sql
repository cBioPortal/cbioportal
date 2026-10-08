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
## description: Reserved: native WSI snapshot tables (keyed by image id) were withdrawn before release
-- No changes. This version once created wsi_patient, wsi_part, wsi_block, wsi_slide and
-- wsi_slide_placement; 3.7.0 drops them from databases that applied it.

## db_schema_version: 3.2.0
## description: Reserved: native WSI snapshot tables were withdrawn before release
-- No changes. This version once rebuilt the native WSI tables; 3.7.0 drops them.

## db_schema_version: 3.3.0
## description: Reserved: native WSI snapshot tables were withdrawn before release
-- No changes. This version once replaced the wsi_slide constraints; 3.7.0 drops the table.

## db_schema_version: 3.4.0
## description: Reserved: WSI slide timing moves to the release that puts slides on the patient Summary timeline
-- No changes. This version once created wsi_slide_timing; 3.7.0 drops it.

## db_schema_version: 3.5.0
## description: Add unified resource_data table and backfill from legacy resource_sample/patient/study tables
-- Sorting key: patient_id and sample_id sit ahead of resource_data_id so the resource table's
-- default sort (ORDER BY patient_id, sample_id) is read in key order rather than sorting the whole
-- result set, which keeps paging cost independent of how large the resource is.
-- Both are Nullable (patient-level rows carry no sample; study-level rows carry neither), which
-- MergeTree only permits with allow_nullable_key.
CREATE TABLE IF NOT EXISTS resource_data
(
    `resource_data_id` Int64,
    `resource_id`      String,
    `cancer_study_id`  Int32,
    `entity_type`      String,
    `patient_id`       Nullable(String),
    `sample_id`        Nullable(String),
    `url`              String,
    `display_name`     Nullable(String),
    `type`             Nullable(String),
    `metadata`         Nullable(String)
) ENGINE = MergeTree ORDER BY (cancer_study_id, resource_id, patient_id, sample_id, resource_data_id)
  SETTINGS allow_nullable_key = 1;

-- Backfill is guarded by a deterministic resource_data_id (hash of the natural key) so this
-- section is safe to re-run: rows already present are excluded via NOT IN. The study id is part
-- of that key: stable ids are unique only within a study and the same URL can be attached in more
-- than one, so hashing without it mints the same id for two studies' rows -- and because the
-- importer deletes a resource's stale rows by id, re-importing one study would then delete the
-- other study's rows. Study-level rows key on the study's own internal_id, which already carries
-- it.
-- Recreate the legacy tables if they are missing, so this section can be retried after a run
-- that reached the drops below but died before migrate_db.py advanced db_schema_version. On a
-- first run they already exist and this is a no-op; on a retry they come back empty, the
-- backfill finds nothing new, and the drops remove them again.
CREATE TABLE IF NOT EXISTS resource_sample (`internal_id` Int64, `resource_id` String, `url` String) ENGINE = MergeTree ORDER BY (internal_id, resource_id, url);
CREATE TABLE IF NOT EXISTS resource_patient (`internal_id` Int64, `resource_id` String, `url` String) ENGINE = MergeTree ORDER BY (internal_id, resource_id, url);
CREATE TABLE IF NOT EXISTS resource_study (`internal_id` Int64, `resource_id` String, `url` String) ENGINE = MergeTree ORDER BY (internal_id, resource_id, url);

INSERT INTO resource_data
    (resource_data_id, resource_id, cancer_study_id, entity_type,
     patient_id, sample_id, url, display_name, type, metadata)
SELECT
    toInt64(cityHash64(cs.cancer_study_id, rs.resource_id, s.stable_id, rs.url)),
    rs.resource_id,
    toInt32(cs.cancer_study_id),
    'SAMPLE',
    p.stable_id,
    s.stable_id,
    rs.url,
    NULL, NULL, NULL
FROM resource_sample rs
INNER JOIN sample       s  ON rs.internal_id    = s.internal_id
INNER JOIN patient      p  ON s.patient_id      = p.internal_id
INNER JOIN cancer_study cs ON p.cancer_study_id = cs.cancer_study_id
WHERE toInt64(cityHash64(cs.cancer_study_id, rs.resource_id, s.stable_id, rs.url)) NOT IN (
    SELECT resource_data_id FROM resource_data
);

INSERT INTO resource_data
    (resource_data_id, resource_id, cancer_study_id, entity_type,
     patient_id, sample_id, url, display_name, type, metadata)
SELECT
    toInt64(cityHash64(cs.cancer_study_id, rp.resource_id, pt.stable_id, rp.url)),
    rp.resource_id,
    toInt32(cs.cancer_study_id),
    'PATIENT',
    pt.stable_id,
    NULL,
    rp.url,
    NULL, NULL, NULL
FROM resource_patient rp
INNER JOIN patient      pt ON rp.internal_id     = pt.internal_id
INNER JOIN cancer_study cs ON pt.cancer_study_id = cs.cancer_study_id
WHERE toInt64(cityHash64(cs.cancer_study_id, rp.resource_id, pt.stable_id, rp.url)) NOT IN (
    SELECT resource_data_id FROM resource_data
);

INSERT INTO resource_data
    (resource_data_id, resource_id, cancer_study_id, entity_type,
     patient_id, sample_id, url, display_name, type, metadata)
SELECT
    toInt64(cityHash64(rst.internal_id, rst.resource_id, rst.url)),
    rst.resource_id,
    toInt32(rst.internal_id),
    'STUDY',
    NULL, NULL,
    rst.url,
    NULL, NULL, NULL
FROM resource_study rst
WHERE toInt64(cityHash64(rst.internal_id, rst.resource_id, rst.url)) NOT IN (
    SELECT resource_data_id FROM resource_data
);

-- Nothing reads the legacy split tables any more: the importer writes only resource_data, and
-- the API paths that used to read them (the legacy resource endpoints, the study resource
-- counts) were repointed. Dropping them leaves an upgraded database with the same schema a
-- fresh install gets from schema.sql, which no longer creates them.
DROP TABLE IF EXISTS resource_sample;
DROP TABLE IF EXISTS resource_patient;
DROP TABLE IF EXISTS resource_study;

## db_schema_version: 3.6.0
## description: Remove WSI data that exposes real image ids (wsi-serving-v5 opaque slide_key)
-- Pathology timeline events imported under wsi-serving-v4 carry real image ids (the IMAGE_IDS
-- attribute, and image-derived part/block keys inside LINKOUT specimenKey). Remove them;
-- re-importing the v5 timeline restores the links. position() is a literal substring match.
ALTER TABLE clinical_event_data DELETE
WHERE key = 'IMAGE_IDS'
   OR (key = 'LINKOUT' AND (position(value, 'image%3A') > 0 OR position(value, 'image:') > 0));
-- WSI resource rows written before slide_key existed name the slide by its real image id in url,
-- display_name and public metadata. The portal can neither list nor serve them (it addresses
-- slides only by slide_key), so delete them; re-importing the converted v3 resources restores the
-- slides. Both mutations are idempotent and safe to re-run.
ALTER TABLE resource_data DELETE
WHERE resource_id IN ('WSI_SAMPLE', 'WSI_PATIENT')
  AND JSONExtractString(ifNull(metadata, '{}'), 'slide_key') = '';

## db_schema_version: 3.7.0
## description: Remove pathology image ids and object URIs from the database (wsi-serving-v6 sealed source)
-- The pathology image id must not be stored in cBioPortal, and object URIs embed it. Drop the
-- withdrawn native WSI tables, which are keyed by image id, from databases that applied an earlier
-- 3.1.0-3.4.0, and the release-based tables that preceded them. Nothing reads any of them.
DROP TABLE IF EXISTS wsi_slide_timing SYNC;
DROP TABLE IF EXISTS wsi_slide_placement SYNC;
DROP TABLE IF EXISTS wsi_slide SYNC;
DROP TABLE IF EXISTS wsi_block SYNC;
DROP TABLE IF EXISTS wsi_part SYNC;
DROP TABLE IF EXISTS wsi_patient SYNC;
DROP TABLE IF EXISTS wsi_release_patient SYNC;
DROP TABLE IF EXISTS wsi_release SYNC;
-- WSI resource rows imported under wsi-serving-v5 keep the image id and the source/thumbnail
-- URIs in metadata (wsi_serving, or the legacy top-level image_id). v6 rows carry only the opaque
-- wsi_serving.sealed_source, so delete the older rows; re-importing the study's v6 WSI resources
-- restores the slides. The mutation is idempotent and safe to re-run.
ALTER TABLE resource_data DELETE
WHERE resource_id IN ('WSI_SAMPLE', 'WSI_PATIENT')
  AND (JSONHas(ifNull(metadata, '{}'), 'image_id')
    OR JSONHas(ifNull(metadata, '{}'), 'wsi_serving', 'image_id')
    OR JSONHas(ifNull(metadata, '{}'), 'wsi_serving', 'source_url')
    OR JSONHas(ifNull(metadata, '{}'), 'wsi_serving', 'thumbnail_url'));
