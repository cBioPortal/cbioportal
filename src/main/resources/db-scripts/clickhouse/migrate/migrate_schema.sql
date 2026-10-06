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
## description: Rebuild the de-identified WSI snapshot tables and slide-access projection
-- WSI data is insert-only and is rebuilt in the inactive blue/green database. Drop both the
-- legacy release-based layout and any partially created snapshot tables so this section cannot
-- advance the schema version while leaving an incompatible WSI table behind.
DROP TABLE IF EXISTS wsi_slide_placement SYNC;
DROP TABLE IF EXISTS wsi_slide SYNC;
DROP TABLE IF EXISTS wsi_block SYNC;
DROP TABLE IF EXISTS wsi_part SYNC;
DROP TABLE IF EXISTS wsi_patient SYNC;
DROP TABLE IF EXISTS wsi_release_patient SYNC;
DROP TABLE IF EXISTS wsi_release SYNC;

CREATE TABLE IF NOT EXISTS wsi_patient (
    cancer_study_id Int64,
    patient_id Int64,
    reference_sample_id Nullable(Int64)
) ENGINE = MergeTree()
ORDER BY (cancer_study_id, patient_id);

CREATE TABLE IF NOT EXISTS wsi_part (
    cancer_study_id Int64,
    patient_id Int64,
    part_key String,
    part_number Nullable(String),
    part_designator Nullable(String),
    part_type Nullable(String),
    part_description Nullable(String),
    subspecialty Nullable(String),
    path_dx_title Nullable(String)
) ENGINE = MergeTree()
ORDER BY (cancer_study_id, patient_id, part_key);

CREATE TABLE IF NOT EXISTS wsi_block (
    cancer_study_id Int64,
    patient_id Int64,
    part_key String,
    block_key String,
    block_number Nullable(String),
    block_label Nullable(String)
) ENGINE = MergeTree()
ORDER BY (cancer_study_id, patient_id, part_key, block_key);

CREATE TABLE IF NOT EXISTS wsi_slide (
    cancer_study_id Int64,
    patient_id Int64,
    image_id String,
    stain_name Nullable(String),
    stain_group Nullable(String),
    is_hne Bool,
    is_ihc Bool,
    magnification Nullable(String),
    file_size_bytes Nullable(UInt64),
    can_serve_tiles Bool,
    barcode Nullable(String),
    slide_type Nullable(String),
    source_url Nullable(String),
    tile_metadata_json Nullable(String),
    thumbnail_url Nullable(String),
    thumbnail_width Nullable(UInt32),
    thumbnail_height Nullable(UInt32),
    thumbnail_content_type Nullable(String),
    PROJECTION wsi_slide_by_access (
        SELECT
            cancer_study_id,
            image_id,
            can_serve_tiles,
            source_url,
            tile_metadata_json,
            thumbnail_url,
            thumbnail_width,
            thumbnail_height,
            thumbnail_content_type
        ORDER BY (cancer_study_id, image_id)
    )
) ENGINE = MergeTree()
ORDER BY (cancer_study_id, patient_id, image_id);

CREATE TABLE IF NOT EXISTS wsi_slide_placement (
    cancer_study_id Int64,
    patient_id Int64,
    image_id String,
    part_key String,
    block_key String,
    sample_id Nullable(Int64),
    match_level String,
    specimen_key String
) ENGINE = MergeTree()
ORDER BY (cancer_study_id, patient_id, image_id, part_key, block_key);

## db_schema_version: 3.2.0
## description: Enforce the non-null WSI stain contract in an inactive blue/green database
-- Version 3.1.0 was exercised by beta before upstream merge and is immutable. Rebuild the WSI
-- snapshot at a new version so databases that already recorded 3.1.0 cannot silently retain its
-- nullable slide_type. The WSI tables are hydrated only after this migration completes.
DROP TABLE IF EXISTS wsi_slide_placement SYNC;
DROP TABLE IF EXISTS wsi_slide SYNC;
DROP TABLE IF EXISTS wsi_block SYNC;
DROP TABLE IF EXISTS wsi_part SYNC;
DROP TABLE IF EXISTS wsi_patient SYNC;

CREATE TABLE IF NOT EXISTS wsi_patient (
    cancer_study_id Int64,
    patient_id Int64,
    reference_sample_id Nullable(Int64)
) ENGINE = MergeTree()
ORDER BY (cancer_study_id, patient_id);

CREATE TABLE IF NOT EXISTS wsi_part (
    cancer_study_id Int64,
    patient_id Int64,
    part_key String,
    part_number Nullable(String),
    part_designator Nullable(String),
    part_type Nullable(String),
    part_description Nullable(String),
    subspecialty Nullable(String),
    path_dx_title Nullable(String)
) ENGINE = MergeTree()
ORDER BY (cancer_study_id, patient_id, part_key);

CREATE TABLE IF NOT EXISTS wsi_block (
    cancer_study_id Int64,
    patient_id Int64,
    part_key String,
    block_key String,
    block_number Nullable(String),
    block_label Nullable(String)
) ENGINE = MergeTree()
ORDER BY (cancer_study_id, patient_id, part_key, block_key);

CREATE TABLE IF NOT EXISTS wsi_slide (
    cancer_study_id Int64,
    patient_id Int64,
    image_id String,
    stain_name Nullable(String),
    stain_group Nullable(String),
    is_hne Bool,
    is_ihc Bool,
    magnification Nullable(String),
    file_size_bytes Nullable(UInt64),
    can_serve_tiles Bool,
    barcode Nullable(String),
    slide_type String,
    source_url Nullable(String),
    tile_metadata_json Nullable(String),
    thumbnail_url Nullable(String),
    thumbnail_width Nullable(UInt32),
    thumbnail_height Nullable(UInt32),
    thumbnail_content_type Nullable(String),
    CONSTRAINT wsi_slide_type_valid CHECK slide_type IN ('H&E', 'IHC', 'Other'),
    CONSTRAINT wsi_slide_stain_flags_valid CHECK NOT (is_hne AND is_ihc)
        AND (slide_type != 'H&E' OR is_hne)
        AND (slide_type != 'IHC' OR is_ihc)
        AND (slide_type != 'Other' OR NOT is_hne AND NOT is_ihc),
    PROJECTION wsi_slide_by_access (
        SELECT
            cancer_study_id,
            image_id,
            can_serve_tiles,
            source_url,
            tile_metadata_json,
            thumbnail_url,
            thumbnail_width,
            thumbnail_height,
            thumbnail_content_type
        ORDER BY (cancer_study_id, image_id)
    )
) ENGINE = MergeTree()
ORDER BY (cancer_study_id, patient_id, image_id);

CREATE TABLE IF NOT EXISTS wsi_slide_placement (
    cancer_study_id Int64,
    patient_id Int64,
    image_id String,
    part_key String,
    block_key String,
    sample_id Nullable(Int64),
    match_level String,
    specimen_key String
) ENGINE = MergeTree()
ORDER BY (cancer_study_id, patient_id, image_id, part_key, block_key);

## db_schema_version: 3.3.0
## description: Distinguish positively identified non-H&E/IHC slides from unknown classifications
ALTER TABLE wsi_slide DROP CONSTRAINT IF EXISTS wsi_slide_type_valid;
ALTER TABLE wsi_slide DROP CONSTRAINT IF EXISTS wsi_slide_stain_flags_valid;
ALTER TABLE wsi_slide ADD CONSTRAINT wsi_slide_type_valid
    CHECK slide_type IN ('H&E', 'IHC', 'Other', 'Unknown');
ALTER TABLE wsi_slide ADD CONSTRAINT wsi_slide_stain_flags_valid
    CHECK NOT (is_hne AND is_ihc)
        AND (slide_type != 'H&E' OR is_hne)
        AND (slide_type != 'IHC' OR is_ihc)
        AND (slide_type NOT IN ('Other', 'Unknown') OR NOT is_hne AND NOT is_ihc);

## db_schema_version: 3.4.0
## description: Store WSI timing provenance, including undated associations, outside clinical events
CREATE TABLE IF NOT EXISTS wsi_slide_timing (
    cancer_study_id Int64,
    patient_id Int64,
    image_id String,
    timeline_start_days Nullable(Int64),
    timeline_date_status String,
    timeline_date_kind String,
    timeline_date_source Nullable(String),
    timeline_date_reason Nullable(String),
    timeline_coordinate_system Nullable(String),
    timepoint_source Nullable(String)
) ENGINE = MergeTree()
ORDER BY (cancer_study_id, patient_id, image_id);
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
-- section is safe to re-run: rows already present are excluded via NOT IN.
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
    toInt64(cityHash64(rs.resource_id, s.stable_id, rs.url)),
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
WHERE toInt64(cityHash64(rs.resource_id, s.stable_id, rs.url)) NOT IN (
    SELECT resource_data_id FROM resource_data
);

INSERT INTO resource_data
    (resource_data_id, resource_id, cancer_study_id, entity_type,
     patient_id, sample_id, url, display_name, type, metadata)
SELECT
    toInt64(cityHash64(rp.resource_id, pt.stable_id, rp.url)),
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
WHERE toInt64(cityHash64(rp.resource_id, pt.stable_id, rp.url)) NOT IN (
    SELECT resource_data_id FROM resource_data
);

INSERT INTO resource_data
    (resource_data_id, resource_id, cancer_study_id, entity_type,
     patient_id, sample_id, url, display_name, type, metadata)
SELECT
    toInt64(cityHash64(rst.resource_id, toString(rst.internal_id), rst.url)),
    rst.resource_id,
    toInt32(rst.internal_id),
    'STUDY',
    NULL, NULL,
    rst.url,
    NULL, NULL, NULL
FROM resource_study rst
WHERE toInt64(cityHash64(rst.resource_id, toString(rst.internal_id), rst.url)) NOT IN (
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
