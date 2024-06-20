DROP TABLE IF EXISTS sample_columnstore;
DROP TABLE IF EXISTS sample_list_columnstore;
DROP TABLE IF EXISTS genomic_event;
DROP VIEW IF EXISTS sample_columnstore_mv;
DROP VIEW IF EXISTS sample_list_columnstore_mv;

CREATE TABLE sample_clinical_attribute_numeric
(
    sample_unique_id        VARCHAR(45),
    patient_unique_id       VARCHAR(45),
    attribute_name          VARCHAR(45),
    attribute_value         FLOAT,
    cancer_study_identifier VARCHAR(45)
)
    ENGINE = MergeTree()
        ORDER BY (sample_unique_id, patient_unique_id, attribute_name, cancer_study_identifier);

INSERT INTO sample_clinical_attribute_numeric
SELECT concat(cs.cancer_study_identifier, '_', s.stable_id) as sample_unique_id,
       concat(cs.cancer_study_identifier, '_', p.stable_id) as patient_unique_id,
       clinical_sample.attr_id                              as attribute_name,
       cast(clinical_sample.attr_value as float)            as attribute_value,
       cs.cancer_study_identifier                           as cancer_study_identifier
FROM cancer_study cs
         INNER JOIN patient p on cs.cancer_study_id = p.cancer_study_id
         INNER JOIN sample s on p.internal_id = s.patient_id
         INNER JOIN clinical_sample ON s.internal_id = clinical_sample.internal_id
WHERE match(clinical_sample.attr_value, '^[\d\.]+$');

CREATE MATERIALIZED VIEW sample_clinical_attribute_numeric_mv
    TO sample_clinical_attribute_numeric AS
SELECT concat(cs.cancer_study_identifier, '_', s.stable_id) as sample_unique_id,
       concat(cs.cancer_study_identifier, '_', p.stable_id) as patient_unique_id,
       clinical_sample.attr_id                              as attribute_name,
       cast(clinical_sample.attr_value as float)            as attribute_value,
       cs.cancer_study_identifier                           as cancer_study_identifier
FROM cancer_study cs
         INNER JOIN patient p on cs.cancer_study_id = p.cancer_study_id
         INNER JOIN sample s on p.internal_id = s.patient_id
         INNER JOIN clinical_sample ON s.internal_id = clinical_sample.internal_id
WHERE match(clinical_sample.attr_value, '^[\d\.]+$');

CREATE TABLE sample_clinical_attribute_categorical
(
    sample_unique_id        VARCHAR(45),
    patient_unique_id       VARCHAR(45),
    attribute_name          VARCHAR(45),
    attribute_value         VARCHAR(45),
    cancer_study_identifier VARCHAR(45)
)
    ENGINE = MergeTree()
        ORDER BY (sample_unique_id, patient_unique_id, attribute_name, cancer_study_identifier);

INSERT INTO sample_clinical_attribute_categorical
SELECT concat(cs.cancer_study_identifier, '_', s.stable_id) as sample_unique_id,
       concat(cs.cancer_study_identifier, '_', p.stable_id) as patient_unique_id,
       cl.attr_id                                           as attribute_name,
       cl.attr_value                                        as attribute_value,
       cs.cancer_study_identifier                           as cancer_study_identifier
FROM cancer_study cs
         INNER JOIN patient p on cs.cancer_study_id = p.cancer_study_id
         INNER JOIN sample s on p.internal_id = s.patient_id
         INNER JOIN clinical_sample cl on s.internal_id = cl.internal_id
WHERE NOT match(cl.attr_value, '^[\d\.]+$');

CREATE MATERIALIZED VIEW sample_clinical_attribute_categorical_mv
    TO sample_clinical_attribute_categorical AS
SELECT concat(cs.cancer_study_identifier, '_', s.stable_id) as sample_unique_id,
       concat(cs.cancer_study_identifier, '_', p.stable_id) as patient_unique_id,
       cl.attr_id                                           as attribute_name,
       cl.attr_value                                        as attribute_value,
       cs.cancer_study_identifier                           as cancer_study_identifier
FROM cancer_study cs
         INNER JOIN patient p on cs.cancer_study_id = p.cancer_study_id
         INNER JOIN sample s on p.internal_id = s.patient_id
         INNER JOIN clinical_sample cl on s.internal_id = cl.internal_id
WHERE NOT match(cl.attr_value, '^[\d\.]+$');

CREATE TABLE patient_clinical_attribute_numeric
(
    patient_unique_id       VARCHAR(45),
    attribute_name          VARCHAR(45),
    attribute_value         FLOAT,
    cancer_study_identifier VARCHAR(45)
)
    ENGINE = MergeTree()
        ORDER BY (patient_unique_id, attribute_name, cancer_study_identifier);

INSERT INTO patient_clinical_attribute_numeric
SELECT concat(cs.cancer_study_identifier, '_', p.stable_id) as patient_unique_id,
       cp.attr_id                                           as attribute_name,
       cast(cp.attr_value as float)                         as attribute_value,
       cs.cancer_study_identifier                           as cancer_study_identifier
FROM cancer_study cs
         INNER JOIN patient p on cs.cancer_study_id = p.cancer_study_id
         INNER JOIN clinical_patient cp on p.internal_id = cp.internal_id
WHERE match(cp.attr_value, '^[\d\.]+$');

CREATE MATERIALIZED VIEW patient_clinical_attribute_numeric_mv
    TO patient_clinical_attribute_numeric AS
SELECT concat(cs.cancer_study_identifier, '_', p.stable_id) as patient_unique_id,
       cp.attr_id                                           as attribute_name,
       cast(cp.attr_value as float)                         as attribute_value,
       cs.cancer_study_identifier                           as cancer_study_identifier
FROM cancer_study cs
         INNER JOIN patient p on cs.cancer_study_id = p.cancer_study_id
         INNER JOIN clinical_patient cp on p.internal_id = cp.internal_id
WHERE match(cp.attr_value, '^[\d\.]+$');

CREATE TABLE patient_clinical_attribute_categorical
(
    patient_unique_id       VARCHAR(45),
    attribute_name          VARCHAR(45),
    attribute_value         VARCHAR(45),
    cancer_study_identifier VARCHAR(45)
)
    ENGINE = MergeTree()
        ORDER BY (patient_unique_id, attribute_name, cancer_study_identifier);

INSERT INTO patient_clinical_attribute_categorical
SELECT concat(cs.cancer_study_identifier, '_', p.stable_id) as patient_unique_id,
       cp.attr_id                                           as attribute_name,
       cp.attr_value                                        as attribute_value,
       cs.cancer_study_identifier                           as cancer_study_identifier
FROM cancer_study cs
         INNER JOIN patient p on cs.cancer_study_id = p.cancer_study_id
         INNER JOIN clinical_patient cp on p.internal_id = cp.internal_id
WHERE NOT match(cp.attr_value, '^[\d\.]+$');

--patient_clinical_attribute_categorical_mv
DROP VIEW IF EXISTS patient_clinical_attribute_categorical_mv;
CREATE MATERIALIZED VIEW patient_clinical_attribute_categorical_mv
            ENGINE = MergeTree()
                ORDER BY cancer_study_identifier
                SETTINGS allow_nullable_key = 1
            POPULATE
AS
SELECT concat(cs.cancer_study_identifier, '_', p.stable_id) AS patient_unique_id,
       cp.attr_id                                           AS attribute_name,
       cp.attr_value                                        AS attribute_value,
       cs.cancer_study_identifier                           AS cancer_study_identifier
FROM clinical_patient AS cp
         INNER JOIN patient AS p ON cp.internal_id = p.internal_id
         INNER JOIN cancer_study AS cs ON p.cancer_study_id = cs.cancer_study_id
         INNER JOIN clinical_attribute_meta AS cam
                    ON (cp.attr_id = cam.attr_id) AND (cs.cancer_study_id = cam.cancer_study_id)
WHERE cam.datatype = 'STRING'


--patient_clinical_attribute_numeric_mv
DROP VIEW IF EXISTS patient_clinical_attribute_numeric_mv;
CREATE MATERIALIZED VIEW patient_clinical_attribute_numeric_mv
            ENGINE = MergeTree()
                ORDER BY cancer_study_identifier
                SETTINGS allow_nullable_key = 1
            POPULATE
AS
SELECT concat(cs.cancer_study_identifier, '_', p.stable_id) AS patient_unique_id,
       cp.attr_id                                           AS attribute_name,
       cp.attr_value                                        AS attribute_value,
       cs.cancer_study_identifier                           AS cancer_study_identifier
FROM sling_db_2024_05_23_original.clinical_patient AS cp
         INNER JOIN sling_db_2024_05_23_original.patient AS p ON cp.internal_id = p.internal_id
         INNER JOIN sling_db_2024_05_23_original.cancer_study AS cs ON p.cancer_study_id = cs.cancer_study_id
         INNER JOIN sling_db_2024_05_23_original.clinical_attribute_meta AS cam
                    ON (cp.attr_id = cam.attr_id) AND (cs.cancer_study_id = cam.cancer_study_id)
WHERE cam.datatype = 'NUMBER'

-- sample_clinical_attribute_categorical_mv
DROP VIEW IF EXISTS sample_clinical_attribute_categorical_mv;
CREATE MATERIALIZED VIEW sample_clinical_attribute_categorical_mv
            ENGINE = MergeTree()
                ORDER BY cancer_study_identifier
                SETTINGS allow_nullable_key = 1
            POPULATE
AS
SELECT s.sample_unique_id,
       s.patient_unique_id,
       csamp.attr_id             AS attribute_name,
       csamp.attr_value          AS attribute_value,
       s.cancer_study_identifier AS cancer_study_identifier
FROM sling_db_2024_05_23_original.clinical_sample AS csamp
         INNER JOIN sling_db_2024_05_23_original.sample_mv AS s ON csamp.internal_id = s.internal_id
         INNER JOIN sling_db_2024_05_23_original.cancer_study AS cs
                    ON s.cancer_study_identifier = cs.cancer_study_identifier
         INNER JOIN sling_db_2024_05_23_original.clinical_attribute_meta AS cam
                    ON (csamp.attr_id = cam.attr_id) AND (cs.cancer_study_id = cam.cancer_study_id)
WHERE cam.datatype = 'STRING'

-- sample_clinical_attribute_numeric_mv
DROP VIEW IF EXISTS sample_clinical_attribute_numeric_mv;
CREATE MATERIALIZED VIEW sample_clinical_attribute_numeric_mv
            ENGINE = MergeTree()
                ORDER BY cancer_study_identifier
                SETTINGS allow_nullable_key = 1
            POPULATE
AS
SELECT s.sample_unique_id,
       s.patient_unique_id,
       csamp.attr_id             AS attribute_name,
       csamp.attr_value          AS attribute_value,
       s.cancer_study_identifier AS cancer_study_identifier
FROM clinical_sample AS csamp
         INNER JOIN sample_mv AS s ON csamp.internal_id = s.internal_id
         INNER JOIN cancer_study AS cs
                    ON s.cancer_study_identifier = cs.cancer_study_identifier
         INNER JOIN clinical_attribute_meta AS cam
                    ON (csamp.attr_id = cam.attr_id) AND (cs.cancer_study_id = cam.cancer_study_id)
WHERE cam.datatype = 'NUMBER'




-- sample_columnstore
CREATE TABLE IF NOT EXISTS sample_columnstore
(
    sample_unique_id         VARCHAR(45),
    sample_unique_id_base64  VARCHAR(45),
    sample_stable_id         VARCHAR(45),
    patient_unique_id        VARCHAR(45),
    patient_unique_id_base64 VARCHAR(45),
    patient_stable_id        VARCHAR(45),
    cancer_study_identifier  VARCHAR(45)
)
    ENGINE = MergeTree
        ORDER BY (sample_unique_id, patient_unique_id, cancer_study_identifier);

INSERT INTO sample_columnstore
SELECT concat(cs.cancer_study_identifier, '_', sample.stable_id) as sample_unique_id,
       base64Encode(sample.stable_id)                            as sample_unique_id_base64,
       sample.stable_id                                          as sample_stable_id,
       concat(cs.cancer_study_identifier, '_', p.stable_id)      as patient_unique_id,
       p.stable_id                                               as patient_stable_id,
       base64Encode(p.stable_id)                                 as patient_unique_id_base64,
       cs.cancer_study_identifier                                as cancer_study_identifier
FROM sample
         INNER JOIN patient p on sample.patient_id = p.internal_id
         INNER JOIN cancer_study cs on p.cancer_study_id = cs.cancer_study_id;

CREATE MATERIALIZED VIEW sample_columnstore_mv TO sample_columnstore AS
SELECT concat(cs.cancer_study_identifier, '_', sample.stable_id) as sample_unique_id,
       sample.stable_id                                          as sample_stable_id,
       base64Encode(sample.stable_id)                            as sample_unique_id_base64,
       concat(cs.cancer_study_identifier, '_', p.stable_id)      as patient_unique_id,
       p.stable_id                                               as patient_stable_id,
       base64Encode(p.stable_id)                                 as patient_unique_id_base64,
       cs.cancer_study_identifier                                as cancer_study_identifier
FROM sample
         INNER JOIN patient p on sample.patient_id = p.internal_id
         INNER JOIN cancer_study cs on p.cancer_study_id = cs.cancer_study_id;

CREATE TABLE IF NOT EXISTS sample_list_columnstore
(
    sample_unique_id        VARCHAR(45),
    sample_list_stable_id   VARCHAR(45),
    name                    VARCHAR(45),
    cancer_study_identifier VARCHAR(45)
)
    ENGINE = MergeTree
        ORDER BY (sample_unique_id, sample_list_stable_id, name, cancer_study_identifier);

INSERT INTO sample_list_columnstore
SELECT concat(cs.cancer_study_identifier, '_', s.stable_id) as sample_unique_id,
       sl.stable_id                                         as sample_list_stable_id,
       sl.name                                              as name,
       cs.cancer_study_identifier                           as cancer_study_identifier
FROM sample_list as sl
         INNER JOIN sample_list_list AS sll on sll.list_id = sl.list_id
         INNER JOIN sample AS s on s.internal_id = sll.sample_id
         INNER JOIN cancer_study cs on sl.cancer_study_id = cs.cancer_study_id;

CREATE MATERIALIZED VIEW sample_list_columnstore_mv TO sample_list_columnstore AS
SELECT concat(cs.cancer_study_identifier, '_', s.stable_id) as sample_unique_id,
       sl.stable_id                                         as sample_list_stable_id,
       sl.name                                              as name,
       cs.cancer_study_identifier                           as cancer_study_identifier
FROM sample_list as sl
         INNER JOIN sample_list_list AS sll on sll.list_id = sl.list_id
         INNER JOIN sample AS s on s.internal_id = sll.sample_id
         INNER JOIN cancer_study cs on sl.cancer_study_id = cs.cancer_study_id;



-- SAMPLE_MV
DROP VIEW IF EXISTS sample_mv;
CREATE MATERIALIZED VIEW sample_mv
        ENGINE = AggregatingMergeTree()
            ORDER BY internal_id 
            SETTINGS allow_nullable_key = 1
        POPULATE
AS

SELECT concat(cs.cancer_study_identifier, '_', sample.stable_id) AS sample_unique_id,
       base64Encode(sample.stable_id)                            AS sample_unique_id_base64,
       sample.stable_id                                          AS sample_stable_id,
       concat(cs.cancer_study_identifier, '_', p.stable_id)      AS patient_unique_id,
       p.stable_id                                               AS patient_stable_id,
       base64Encode(p.stable_id)                                 AS patient_unique_id_base64,
       cs.cancer_study_identifier                                AS cancer_study_identifier,
       sample.internal_id                                        AS internal_id
FROM sample
         INNER JOIN patient AS p ON sample.patient_id = p.internal_id
         INNER JOIN cancer_study AS cs ON p.cancer_study_id = cs.cancer_study_id;
