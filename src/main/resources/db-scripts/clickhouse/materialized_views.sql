DROP TABLE IF EXISTS sample_clinical_attribute_numeric;
DROP TABLE IF EXISTS sample_clinical_attribute_categorical;
DROP TABLE IF EXISTS patient_clinical_attribute_numeric;
DROP TABLE IF EXISTS patient_clinical_attribute_categorical;
DROP TABLE IF EXISTS sample_columnstore;
DROP TABLE IF EXISTS sample_list_columnstore;
DROP TABLE IF EXISTS genomic_event;
DROP VIEW IF EXISTS sample_clinical_attribute_numeric_mv;
DROP VIEW IF EXISTS sample_clinical_attribute_categorical_mv;
DROP VIEW IF EXISTS patient_clinical_attribute_numeric_mv;
DROP VIEW IF EXISTS patient_clinical_attribute_categorical_mv;
DROP VIEW IF EXISTS sample_columnstore_mv;
DROP VIEW IF EXISTS sample_list_columnstore_mv;
DROP VIEW IF EXISTS genomic_event_mutation_mv;
DROP VIEW IF EXISTS genomic_event_cna_mv;
DROP VIEW IF EXISTS genomic_event_struct_var_mv;

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

CREATE MATERIALIZED VIEW patient_clinical_attribute_categorical_mv
    TO patient_clinical_attribute_categorical AS
SELECT concat(cs.cancer_study_identifier, '_', p.stable_id) as patient_unique_id,
       cp.attr_id                                           as attribute_name,
       cp.attr_value                                        as attribute_value,
       cs.cancer_study_identifier                           as cancer_study_identifier
FROM cancer_study cs
         INNER JOIN patient p on cs.cancer_study_id = p.cancer_study_id
         INNER JOIN clinical_patient cp on p.internal_id = cp.internal_id
WHERE NOT match(cp.attr_value, '^[\d\.]+$');

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

CREATE TABLE IF NOT EXISTS genomic_event
(
    sample_unique_id String,
    variant String,
    variant_type String,
    hugo_gene_symbol String,
    gene_panel_stable_id String,
    cancer_study_identifier String,
    genetic_profile_stable_id String
) ENGINE = MergeTree
ORDER BY
    (
        variant_type,
        sample_unique_id,
        hugo_gene_symbol
    );

Insert into genomic_event
SELECT concat(cs.cancer_study_identifier, '_', sample.stable_id) as sample_unique_id,
       me.protein_change                                         as variant,
       'mutation'                                                as variant_type,
       gene.hugo_gene_symbol                                     as hugo_gene_symbol,
       gp.stable_id                                              as gene_panel_stable_id,
       cs.cancer_study_identifier                                as cancer_study_identifier,
       g.stable_id                                               as genetic_profile_stable_id
FROM mutation
         INNER JOIN mutation_event as me on mutation.mutation_event_id = me.mutation_event_id
         INNER JOIN sample_profile sp
                    on mutation.sample_id = sp.sample_id and mutation.genetic_profile_id = sp.genetic_profile_id
         LEFT JOIN gene_panel gp on sp.panel_id = gp.internal_id
         LEFT JOIN genetic_profile g on sp.genetic_profile_id = g.genetic_profile_id
         INNER JOIN cancer_study cs on g.cancer_study_id = cs.cancer_study_id
         INNER JOIN sample on mutation.sample_id = sample.internal_id
         LEFT JOIN gene on mutation.entrez_gene_id = gene.entrez_gene_id
UNION ALL
SELECT concat(cs.cancer_study_identifier, '_', sample.stable_id) as sample_unique_id,
       toString(ce.alteration)                                   as variant,
       'cna'                                                     as variant_type,
       gene.hugo_gene_symbol                                     as hugo_gene_symbol,
       gp.stable_id                                              as gene_panel_stable_id,
       cs.cancer_study_identifier                                as cancer_study_identifier,
       g.stable_id                                              as genetic_profile_stable_id
FROM cna_event ce
         INNER JOIN sample_cna_event sce ON ce.cna_event_id = sce.cna_event_id
         INNER JOIN sample_profile sp ON sce.sample_id = sp.sample_id AND sce.genetic_profile_id = sp.genetic_profile_id
         INNER JOIN gene_panel gp ON sp.panel_id = gp.internal_id
         INNER JOIN genetic_profile g ON sp.genetic_profile_id = g.genetic_profile_id
         INNER JOIN cancer_study cs ON g.cancer_study_id = cs.cancer_study_id
         INNER JOIN sample ON sce.sample_id = sample.internal_id
         INNER JOIN gene ON ce.entrez_gene_id = gene.entrez_gene_id
UNION ALL
SELECT
    concat(cs.cancer_study_identifier, '_', s.stable_id) as sample_unique_id,
    event_info as variant,
    'structural_variant' as variant_type,
    gene2.hugo_gene_symbol as hugo_gene_symbol,
    gene_panel.stable_id as gene_panel_stable_id,
    cs.cancer_study_identifier as cancer_study_identifier,
    gp.stable_id as genetic_profile_stable_id
FROM
    structural_variant sv
        INNER JOIN genetic_profile gp ON sv.genetic_profile_id = gp.genetic_profile_id
        INNER JOIN sample s ON sv.sample_id = s.internal_id
        INNER JOIN cancer_study cs ON gp.cancer_study_id = cs.cancer_study_id
        INNER JOIN gene gene2 ON sv.site2_entrez_gene_id = gene2.entrez_gene_id
        INNER JOIN sample_profile on s.internal_id = sample_profile.sample_id
        INNER JOIN gene_panel on sample_profile.panel_id = gene_panel.internal_id
UNION ALL
SELECT
    concat(cs.cancer_study_identifier, '_', s.stable_id) as sample_unique_id,
    event_info as variant,
    'structural_variant' as variant_type,
    gene1.hugo_gene_symbol as hugo_gene_symbol,
    gene_panel.stable_id as gene_panel_stable_id,
    cs.cancer_study_identifier as cancer_study_identifier,
    gp.stable_id as genetic_profile_stable_id
FROM
    structural_variant sv
        INNER JOIN genetic_profile gp ON sv.genetic_profile_id = gp.genetic_profile_id
        INNER JOIN sample s ON sv.sample_id = s.internal_id
        INNER JOIN cancer_study cs ON gp.cancer_study_id = cs.cancer_study_id
        INNER JOIN gene gene1 ON sv.site1_entrez_gene_id = gene1.entrez_gene_id
        INNER JOIN sample_profile on s.internal_id = sample_profile.sample_id
        INNER JOIN gene_panel on sample_profile.panel_id = gene_panel.internal_id;

CREATE MATERIALIZED VIEW genomic_event_mutation_mv TO genomic_event AS
SELECT concat(cs.cancer_study_identifier, '_', sample.stable_id) as sample_unique_id,
       me.protein_change                                         as variant,
       'mutation'                                                as variant_type,
       gene.hugo_gene_symbol                                     as hugo_gene_symbol,
       gp.stable_id                                              as gene_panel_stable_id,
       cs.cancer_study_identifier                                as cancer_study_identifier,
       g.stable_id                                               as genetic_profile_stable_id
FROM mutation
         LEFT JOIN mutation_event as me on mutation.mutation_event_id = me.mutation_event_id
         LEFT JOIN sample_profile sp
                   on mutation.sample_id = sp.sample_id and mutation.genetic_profile_id = sp.genetic_profile_id
         LEFT JOIN gene_panel gp on sp.panel_id = gp.internal_id
         LEFT JOIN genetic_profile g on sp.genetic_profile_id = g.genetic_profile_id
         LEFT JOIN cancer_study cs on g.cancer_study_id = cs.cancer_study_id
         LEFT JOIN sample on mutation.sample_id = sample.internal_id
         LEFT JOIN gene on mutation.entrez_gene_id = gene.entrez_gene_id;

CREATE MATERIALIZED VIEW genomic_event_cna_mv TO genomic_event AS
SELECT concat(cs.cancer_study_identifier, '_', sample.stable_id) as sample_unique_id,
       toString(ce.alteration)                                   as variant,
       'cna'                                                     as variant_type,
       gene.hugo_gene_symbol                                     as hugo_gene_symbol,
       gp.stable_id                                              as gene_panel_stable_id,
       cs.cancer_study_identifier                                as cancer_study_identifier,
       gp.stable_id                                              as genetic_profile_stable_id
FROM cna_event ce
         INNER JOIN sample_cna_event sce ON ce.cna_event_id = sce.cna_event_id
         INNER JOIN sample_profile sp ON sce.sample_id = sp.sample_id AND sce.genetic_profile_id = sp.genetic_profile_id
         INNER JOIN gene_panel gp ON sp.panel_id = gp.internal_id
         INNER JOIN genetic_profile g ON sp.genetic_profile_id = g.genetic_profile_id
         INNER JOIN cancer_study cs ON g.cancer_study_id = cs.cancer_study_id
         INNER JOIN sample ON sce.sample_id = sample.internal_id
         INNER JOIN gene ON ce.entrez_gene_id = gene.entrez_gene_id;

CREATE MATERIALIZED VIEW genomic_event_struct_var_mv TO genomic_event AS
SELECT concat(cs.cancer_study_identifier, '_', s.stable_id) as sample_unique_id,
       event_info                                           as variant,
       'structural_variant'                                 as variant_type,
       gene1.hugo_gene_symbol                               as hugo_gene_symbol,
       gene_panel.stable_id                                 as gene_panel_stable_id,
       cs.cancer_study_identifier                           as cancer_study_identifier,
       gp.stable_id                                         as genetic_profile_stable_id
FROM structural_variant sv
         INNER JOIN genetic_profile gp ON sv.genetic_profile_id = gp.genetic_profile_id
         INNER JOIN sample s ON sv.sample_id = s.internal_id
         INNER JOIN cancer_study cs ON gp.cancer_study_id = cs.cancer_study_id
         INNER JOIN gene gene1 ON sv.site1_entrez_gene_id = gene1.entrez_gene_id
         INNER JOIN sample_profile on s.internal_id = sample_profile.sample_id
         INNER JOIN gene_panel on sample_profile.panel_id = gene_panel.internal_id
UNION ALL
SELECT concat(cs.cancer_study_identifier, '_', s.stable_id) as sample_unique_id,
       event_info                                           as variant,
       'structural_variant'                                 as variant_type,
       gene2.hugo_gene_symbol                               as hugo_gene_symbol,
       gene_panel.stable_id                                 as gene_panel_stable_id,
       cs.cancer_study_identifier                           as cancer_study_identifier,
       gp.stable_id                                         as genetic_profile_stable_id
FROM structural_variant sv
         INNER JOIN genetic_profile gp ON sv.genetic_profile_id = gp.genetic_profile_id
         INNER JOIN sample s ON sv.sample_id = s.internal_id
         INNER JOIN cancer_study cs ON gp.cancer_study_id = cs.cancer_study_id
         INNER JOIN gene gene2 ON sv.site2_entrez_gene_id = gene2.entrez_gene_id
         INNER JOIN sample_profile on s.internal_id = sample_profile.sample_id
         INNER JOIN gene_panel on sample_profile.panel_id = gene_panel.internal_id;

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
