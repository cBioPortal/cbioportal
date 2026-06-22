-- ============================================================
-- Migration: backfill resource_data and simplify the view.
--
-- Run this when the pipeline team is ready to do a full migration.
-- After this script completes:
--   - All legacy data is copied into cbioportal_ext.resource_data
--   - The view becomes a simple SELECT (no more cross-DB JOINs)
--   - Performance improves (no UNION ALL overhead on every query)
--
-- Usage:
--   clickhouse-client \
--     --host  <host>.clickhouse.cloud \
--     --port  9440 --secure \
--     --user  default \
--     --password <password> \
--     --multiquery < migrate_resource_data.sql
-- ============================================================

-- 1. Backfill SAMPLE-level resources
INSERT INTO cbioportal_ext.resource_data
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
FROM cbioportal.resource_sample rs
INNER JOIN cbioportal.sample       s  ON rs.internal_id   = s.internal_id
INNER JOIN cbioportal.patient      p  ON s.patient_id     = p.internal_id
INNER JOIN cbioportal.cancer_study cs ON p.cancer_study_id = cs.cancer_study_id
WHERE toInt64(cityHash64(rs.resource_id, s.stable_id, rs.url)) NOT IN (
    SELECT RESOURCE_DATA_ID FROM cbioportal_ext.resource_data
);

-- 2. Backfill PATIENT-level resources
INSERT INTO cbioportal_ext.resource_data
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
FROM cbioportal.resource_patient rp
INNER JOIN cbioportal.patient      pt ON rp.internal_id     = pt.internal_id
INNER JOIN cbioportal.cancer_study cs ON pt.cancer_study_id = cs.cancer_study_id
WHERE toInt64(cityHash64(rp.resource_id, pt.stable_id, rp.url)) NOT IN (
    SELECT RESOURCE_DATA_ID FROM cbioportal_ext.resource_data
);

-- 3. Backfill STUDY-level resources
INSERT INTO cbioportal_ext.resource_data
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
FROM cbioportal.resource_study rst
WHERE toInt64(cityHash64(rst.resource_id, toString(rst.internal_id), rst.url)) NOT IN (
    SELECT RESOURCE_DATA_ID FROM cbioportal_ext.resource_data
);

-- 4. Verify row counts
SELECT
    'cbioportal.resource_sample'  AS source, count() AS rows FROM cbioportal.resource_sample
UNION ALL SELECT 'cbioportal.resource_patient', count() FROM cbioportal.resource_patient
UNION ALL SELECT 'cbioportal.resource_study', count() FROM cbioportal.resource_study
UNION ALL SELECT 'cbioportal_ext.resource_data (after backfill)', count() FROM cbioportal_ext.resource_data;

-- 5. Replace the UNION ALL view with a direct table read.
--    All data is now in resource_data — no need for cross-DB JOINs.
CREATE OR REPLACE VIEW cbioportal_ext.resource_data_unified AS
    SELECT
        RESOURCE_DATA_ID, RESOURCE_ID, CANCER_STUDY_ID, ENTITY_TYPE,
        PATIENT_ID, SAMPLE_ID, URL, DISPLAY_NAME, TYPE, METADATA, PRIORITY
    FROM cbioportal_ext.resource_data;
