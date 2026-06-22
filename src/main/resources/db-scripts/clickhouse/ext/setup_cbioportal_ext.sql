-- ============================================================
-- Setup: cbioportal_ext database with resource_data table
-- and resource_data_unified view (cross-database UNION ALL).
--
-- Run this ONCE against your ClickHouse Cloud service.
-- The view reads legacy data from the main 'cbioportal' database
-- without any modification to that database's schema.
--
-- Usage:
--   clickhouse-client \
--     --host  <host>.clickhouse.cloud \
--     --port  9440 --secure \
--     --user  default \
--     --password <password> \
--     --multiquery < setup_cbioportal_ext.sql
--
-- Prerequisites:
--   - The main 'cbioportal' database exists with tables:
--     resource_sample, resource_patient, resource_study,
--     sample, patient, cancer_study
--   - The ClickHouse user has SELECT privileges on cbioportal.*
--
-- Future migration (when pipeline team is ready):
--   Run migrate_resource_data.sql to backfill legacy data and
--   replace the UNION ALL view with a simple table alias.
-- ============================================================

-- Step 1: Create the separate database
CREATE DATABASE IF NOT EXISTS cbioportal_ext;

-- Step 2: Create the resource_data table (new imports go here)
CREATE TABLE IF NOT EXISTS cbioportal_ext.resource_data
(
    `RESOURCE_DATA_ID` Int64,
    `RESOURCE_ID`      String,
    `CANCER_STUDY_ID`  Int32,
    `ENTITY_TYPE`      String,           -- 'SAMPLE' | 'PATIENT' | 'STUDY'
    `PATIENT_ID`       Nullable(String),
    `SAMPLE_ID`        Nullable(String),
    `URL`              String,
    `DISPLAY_NAME`     Nullable(String),
    `TYPE`             Nullable(String),
    `METADATA`         Nullable(String), -- JSON object string
    `PRIORITY`         Int32 DEFAULT 0
) ENGINE = MergeTree()
  ORDER BY (CANCER_STUDY_ID, RESOURCE_ID, RESOURCE_DATA_ID);

-- Step 3: Create the unified view (cross-database UNION ALL)
-- This view exposes ALL resource data:
--   - New rows from resource_data (in cbioportal_ext)
--   - Legacy rows from the three split tables (in cbioportal, via cross-DB references)
CREATE OR REPLACE VIEW cbioportal_ext.resource_data_unified AS
    -- New data written by the importer
    SELECT
        RESOURCE_DATA_ID,
        RESOURCE_ID,
        CANCER_STUDY_ID,
        ENTITY_TYPE,
        PATIENT_ID,
        SAMPLE_ID,
        URL,
        DISPLAY_NAME,
        TYPE,
        METADATA,
        PRIORITY
    FROM cbioportal_ext.resource_data

    UNION ALL

    -- Legacy SAMPLE-level resources (cross-database JOINs to cbioportal.*)
    SELECT
        toInt64(cityHash64(rs.resource_id, s.stable_id, rs.url)) AS RESOURCE_DATA_ID,
        rs.resource_id  AS RESOURCE_ID,
        toInt32(cs.cancer_study_id) AS CANCER_STUDY_ID,
        'SAMPLE'        AS ENTITY_TYPE,
        p.stable_id     AS PATIENT_ID,
        s.stable_id     AS SAMPLE_ID,
        rs.url          AS URL,
        NULL            AS DISPLAY_NAME,
        NULL            AS TYPE,
        NULL            AS METADATA,
        0               AS PRIORITY
    FROM cbioportal.resource_sample rs
    INNER JOIN cbioportal.sample       s  ON rs.internal_id   = s.internal_id
    INNER JOIN cbioportal.patient      p  ON s.patient_id     = p.internal_id
    INNER JOIN cbioportal.cancer_study cs ON p.cancer_study_id = cs.cancer_study_id

    UNION ALL

    -- Legacy PATIENT-level resources
    SELECT
        toInt64(cityHash64(rp.resource_id, pt.stable_id, rp.url)) AS RESOURCE_DATA_ID,
        rp.resource_id  AS RESOURCE_ID,
        toInt32(cs.cancer_study_id) AS CANCER_STUDY_ID,
        'PATIENT'       AS ENTITY_TYPE,
        pt.stable_id    AS PATIENT_ID,
        NULL            AS SAMPLE_ID,
        rp.url          AS URL,
        NULL            AS DISPLAY_NAME,
        NULL            AS TYPE,
        NULL            AS METADATA,
        0               AS PRIORITY
    FROM cbioportal.resource_patient rp
    INNER JOIN cbioportal.patient      pt ON rp.internal_id     = pt.internal_id
    INNER JOIN cbioportal.cancer_study cs ON pt.cancer_study_id = cs.cancer_study_id

    UNION ALL

    -- Legacy STUDY-level resources
    SELECT
        toInt64(cityHash64(rst.resource_id, toString(rst.internal_id), rst.url)) AS RESOURCE_DATA_ID,
        rst.resource_id      AS RESOURCE_ID,
        toInt32(rst.internal_id) AS CANCER_STUDY_ID,
        'STUDY'              AS ENTITY_TYPE,
        NULL                 AS PATIENT_ID,
        NULL                 AS SAMPLE_ID,
        rst.url              AS URL,
        NULL                 AS DISPLAY_NAME,
        NULL                 AS TYPE,
        NULL                 AS METADATA,
        0                    AS PRIORITY
    FROM cbioportal.resource_study rst;

-- Step 4: Create cross-DB views for tables referenced in mapper JOINs
CREATE OR REPLACE VIEW cbioportal_ext.resource_definition AS
    SELECT * FROM cbioportal.resource_definition;

CREATE OR REPLACE VIEW cbioportal_ext.cancer_study AS
    SELECT * FROM cbioportal.cancer_study;

-- Step 5: Sanity check
SELECT
    'cbioportal.resource_sample' AS source, count() AS rows FROM cbioportal.resource_sample
UNION ALL SELECT 'cbioportal.resource_patient', count() FROM cbioportal.resource_patient
UNION ALL SELECT 'cbioportal.resource_study', count() FROM cbioportal.resource_study
UNION ALL SELECT 'cbioportal_ext.resource_data', count() FROM cbioportal_ext.resource_data
UNION ALL SELECT 'cbioportal_ext.resource_data_unified (total)', count() FROM cbioportal_ext.resource_data_unified;
