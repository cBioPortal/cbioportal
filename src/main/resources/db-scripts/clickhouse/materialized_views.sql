DROP TABLE IF EXISTS sample_columnstore;
DROP TABLE IF EXISTS sample_list_columnstore;
DROP VIEW IF EXISTS sample_columnstore_mv;
DROP VIEW IF EXISTS sample_list_columnstore_mv;

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
