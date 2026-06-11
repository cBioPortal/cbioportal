-- ============================================================
-- Migration: add resource_data table to an existing ClickHouse
-- database (v2.14.6 equivalent for ClickHouse deployments).
--
-- Run this against your ClickHouse Cloud test service ONCE,
-- after restoring / copying the production backup.
--
-- Usage:
--   clickhouse-client \
--     --host  <host>.clickhouse.cloud \
--     --port  9440 --secure \
--     --user  default \
--     --password <password> \
--     --database cbioportal \
--     --multiquery < migrate_resource_data.sql
-- ============================================================

-- 1. Create the unified resource_data table
--    (safe to re-run: CREATE TABLE IF NOT EXISTS)
CREATE TABLE IF NOT EXISTS resource_data
(
    RESOURCE_DATA_ID  Int32,
    RESOURCE_ID       String,
    CANCER_STUDY_ID   Int32,
    ENTITY_TYPE       String,              -- 'SAMPLE' | 'PATIENT' | 'STUDY'
    PATIENT_ID        Nullable(String),
    SAMPLE_ID         Nullable(String),
    URL               String,
    DISPLAY_NAME      Nullable(String),
    TYPE              Nullable(String),    -- e.g. IMAGE, LINK, PDF
    METADATA          Nullable(String),   -- JSON object string
    PRIORITY          Int32
) ENGINE = MergeTree()
  ORDER BY (CANCER_STUDY_ID, RESOURCE_ID, RESOURCE_DATA_ID);

-- 2. Back-fill from legacy split tables (resource_sample, resource_patient, resource_study)
--    Skip if those tables are empty or don't exist in this environment.
--
--    ClickHouse does not enforce FK constraints; joins are done explicitly.

-- 2a. SAMPLE-level resources
INSERT INTO resource_data
    (RESOURCE_DATA_ID, RESOURCE_ID, CANCER_STUDY_ID, ENTITY_TYPE,
     PATIENT_ID, SAMPLE_ID, URL, DISPLAY_NAME, TYPE, METADATA, PRIORITY)
SELECT
    rowNumberInAllBlocks() + toInt32(now()),   -- synthetic unique ID
    rs.RESOURCE_ID,
    cs.CANCER_STUDY_ID,
    'SAMPLE',
    p.STABLE_ID,     -- patient_id
    s.STABLE_ID,     -- sample_id
    rs.URL,
    NULL, NULL, NULL,
    0
FROM resource_sample rs
INNER JOIN sample        s  ON rs.INTERNAL_ID = s.INTERNAL_ID
INNER JOIN patient       p  ON s.PATIENT_ID   = p.INTERNAL_ID
INNER JOIN cancer_study  cs ON p.CANCER_STUDY_ID = cs.CANCER_STUDY_ID
-- Skip rows already migrated on a previous run
WHERE (cs.CANCER_STUDY_ID, rs.RESOURCE_ID, s.STABLE_ID) NOT IN (
    SELECT CANCER_STUDY_ID, RESOURCE_ID, SAMPLE_ID
    FROM resource_data
    WHERE ENTITY_TYPE = 'SAMPLE' AND SAMPLE_ID IS NOT NULL
);

-- 2b. PATIENT-level resources
INSERT INTO resource_data
    (RESOURCE_DATA_ID, RESOURCE_ID, CANCER_STUDY_ID, ENTITY_TYPE,
     PATIENT_ID, SAMPLE_ID, URL, DISPLAY_NAME, TYPE, METADATA, PRIORITY)
SELECT
    rowNumberInAllBlocks() + toInt32(now()) + 1000000,
    rp.RESOURCE_ID,
    cs.CANCER_STUDY_ID,
    'PATIENT',
    pt.STABLE_ID,    -- patient_id
    NULL,            -- no sample
    rp.URL,
    NULL, NULL, NULL,
    0
FROM resource_patient rp
INNER JOIN patient      pt ON rp.INTERNAL_ID     = pt.INTERNAL_ID
INNER JOIN cancer_study cs ON pt.CANCER_STUDY_ID = cs.CANCER_STUDY_ID
WHERE (cs.CANCER_STUDY_ID, rp.RESOURCE_ID, pt.STABLE_ID) NOT IN (
    SELECT CANCER_STUDY_ID, RESOURCE_ID, PATIENT_ID
    FROM resource_data
    WHERE ENTITY_TYPE = 'PATIENT' AND PATIENT_ID IS NOT NULL AND SAMPLE_ID IS NULL
);

-- 2c. STUDY-level resources
INSERT INTO resource_data
    (RESOURCE_DATA_ID, RESOURCE_ID, CANCER_STUDY_ID, ENTITY_TYPE,
     PATIENT_ID, SAMPLE_ID, URL, DISPLAY_NAME, TYPE, METADATA, PRIORITY)
SELECT
    rowNumberInAllBlocks() + toInt32(now()) + 2000000,
    rst.RESOURCE_ID,
    rst.INTERNAL_ID,   -- INTERNAL_ID IS cancer_study_id for resource_study
    'STUDY',
    NULL, NULL,
    rst.URL,
    NULL, NULL, NULL,
    0
FROM resource_study rst
WHERE (rst.INTERNAL_ID, rst.RESOURCE_ID) NOT IN (
    SELECT CANCER_STUDY_ID, RESOURCE_ID
    FROM resource_data
    WHERE ENTITY_TYPE = 'STUDY'
);

-- 3. Verify row counts
SELECT
    'resource_sample'  AS source_table, count() AS rows FROM resource_sample
UNION ALL SELECT 'resource_patient', count() FROM resource_patient
UNION ALL SELECT 'resource_study',   count() FROM resource_study
UNION ALL SELECT 'resource_data (migrated)', count() FROM resource_data;
