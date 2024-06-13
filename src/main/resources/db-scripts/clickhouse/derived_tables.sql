-- clinical_data_derived
DROP TABLE IF EXISTS clinical_data_derived
CREATE TABLE IF NOT EXISTS clinical_data_derived
(
    sample_unique_id String,
    patient_unique_id String,
    attribute_name String,
    attribute_value String,
    cancer_study_identifier String,
    type String
)
    ENGINE=MergeTree
    ORDER BY sample_unique_id

-- Insert sample attribute data
INSERT INTO TABLE clinical_data_derived
SELECT sm.sample_unique_id        AS sample_unique_id,
       sm.patient_unique_id       AS patient_unique_id,
       cam.attr_id                AS attribute_name,
       csamp.attr_value           AS attribute_value,
       cs.cancer_study_identifier AS cancer_study_identifier,
       'sample'                   AS type
FROM sling_db_2024_05_23_original.sample_mv AS sm
         INNER JOIN sling_db_2024_05_23_original.cancer_study AS cs
                    ON sm.cancer_study_identifier = cs.cancer_study_identifier
         FULL OUTER JOIN sling_db_2024_05_23_original.clinical_attribute_meta AS cam
                         ON cs.cancer_study_id = cam.cancer_study_id
         FULL OUTER JOIN sling_db_2024_05_23_original.clinical_sample AS csamp
                         ON (sm.internal_id = csamp.internal_id) AND (csamp.attr_id = cam.attr_id)
WHERE cam.patient_attribute = 0

-- INSERT patient attribute data
INSERT INTO TABLE clinical_data_derived
SELECT NULL                                                 AS sample_unique_id,
       concat(cs.cancer_study_identifier, '_', p.stable_id) AS patient_unique_id,
       cam.attr_id                                          AS attribute_name,
       clinpat.attr_value                                   AS attribute_value,
       cs.cancer_study_identifier                           AS cancer_study_identifier,
       'patient'                                            AS type
FROM sling_db_2024_05_23_original.patient AS p
         INNER JOIN sling_db_2024_05_23_original.cancer_study AS cs ON p.cancer_study_id = cs.cancer_study_id
         FULL OUTER JOIN sling_db_2024_05_23_original.clinical_attribute_meta AS cam
                         ON cs.cancer_study_id = cam.cancer_study_id
         FULL OUTER JOIN sling_db_2024_05_23_original.clinical_patient AS clinpat
                         ON (p.internal_id = clinpat.internal_id) AND (clinpat.attr_id = cam.attr_id)
WHERE cam.patient_attribute = 1
