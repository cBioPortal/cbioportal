DROP TABLE IF EXISTS sample_list_columnstore;
DROP VIEW IF EXISTS sample_list_columnstore_mv;

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
