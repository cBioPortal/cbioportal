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
