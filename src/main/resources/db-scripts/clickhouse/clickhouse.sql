DROP TABLE IF EXISTS sample_to_gene_panel_derived;
DROP TABLE IF EXISTS gene_panel_to_gene_derived;
DROP TABLE IF EXISTS sample_derived;
DROP TABLE IF EXISTS genomic_event_derived;
DROP TABLE IF EXISTS clinical_data_derived;
DROP TABLE IF EXISTS clinical_event_derived;
DROP TABLE IF EXISTS genetic_alteration_derived;
DROP TABLE IF EXISTS generic_assay_data_derived;

CREATE TABLE sample_to_gene_panel_derived
(
    sample_unique_id String,
    alteration_type LowCardinality(String),
    gene_panel_id LowCardinality(String),
    cancer_study_identifier LowCardinality(String),
    genetic_profile_id LowCardinality(String)
) ENGINE = MergeTree()
ORDER BY (gene_panel_id, alteration_type, genetic_profile_id, sample_unique_id);

INSERT INTO sample_to_gene_panel_derived
SELECT
    concat(cs.cancer_study_identifier, '_', sample.stable_id) AS sample_unique_id,
    genetic_alteration_type AS alteration_type,
    -- If a mutation is found in a gene that is not in a gene panel we assume Whole Exome Sequencing WES
    ifnull(gene_panel.stable_id, 'WES') AS gene_panel_id,
    cs.cancer_study_identifier AS cancer_study_identifier,
    gp.stable_id AS genetic_profile_id
FROM sample_profile sp
         INNER JOIN genetic_profile gp ON sample_profile.genetic_profile_id = gp.genetic_profile_id
         LEFT JOIN gene_panel ON sp.panel_id = gene_panel.internal_id
         INNER JOIN sample ON sp.sample_id = sample.internal_id
         INNER JOIN cancer_study cs ON gp.cancer_study_id = cs.cancer_study_id;

CREATE TABLE gene_panel_to_gene_derived
(
    gene_panel_id LowCardinality(String),
    gene String
) ENGINE = MergeTree()
ORDER BY (gene_panel_id);

INSERT INTO gene_panel_to_gene_derived
SELECT
    gp.stable_id AS gene_panel_id,
    g.hugo_gene_symbol AS gene
FROM gene_panel gp
         INNER JOIN gene_panel_list gpl ON gp.internal_id = gpl.internal_id
         INNER JOIN gene g ON g.entrez_gene_id = gpl.gene_id
UNION ALL
SELECT
    'WES' AS gene_panel_id,
    gene.hugo_gene_symbol AS gene
FROM gene
WHERE gene.entrez_gene_id > 0 AND gene.type = 'protein-coding';

CREATE TABLE sample_derived
(
    sample_unique_id         String,
    sample_unique_id_base64  String,
    sample_stable_id         String,
    patient_unique_id        String,
    patient_unique_id_base64 String,
    patient_stable_id        String,
    cancer_study_identifier LowCardinality(String),
    internal_id             Int
)
    ENGINE = MergeTree
        ORDER BY (cancer_study_identifier, sample_unique_id);

INSERT INTO sample_derived
SELECT concat(cs.cancer_study_identifier, '_', sample.stable_id) AS sample_unique_id,
       base64Encode(sample.stable_id)                            AS sample_unique_id_base64,
       sample.stable_id                                          AS sample_stable_id,
       concat(cs.cancer_study_identifier, '_', p.stable_id)      AS patient_unique_id,
       base64Encode(p.stable_id)                                 AS patient_unique_id_base64,
       p.stable_id                                               AS patient_stable_id,
       cs.cancer_study_identifier                                AS cancer_study_identifier,
       sample.internal_id                                        AS internal_id
FROM sample
         INNER JOIN patient AS p ON sample.patient_id = p.internal_id
         INNER JOIN cancer_study AS cs ON p.cancer_study_id = cs.cancer_study_id;

CREATE TABLE IF NOT EXISTS genomic_event_derived
(
    sample_unique_id          String,
    hugo_gene_symbol          String,
    entrez_gene_id            Int32,
    gene_panel_stable_id      LowCardinality(String),
    cancer_study_identifier   LowCardinality(String),
    genetic_profile_stable_id LowCardinality(String),
    variant_type              LowCardinality(String),
    mutation_variant          String,
    mutation_type             LowCardinality(String),
    mutation_status           LowCardinality(String),
    driver_filter             LowCardinality(String),
    driver_tiers_filter       LowCardinality(String),
    cna_alteration            Nullable(Int8),
    cna_cytoband              String,
    sv_event_info             String,
    patient_unique_id         String
) ENGINE = MergeTree
      ORDER BY ( variant_type, entrez_gene_id, hugo_gene_symbol, genetic_profile_stable_id, sample_unique_id);

INSERT INTO genomic_event_derived
-- Insert Mutations
SELECT concat(cs.cancer_study_identifier, '_', sample.stable_id) AS sample_unique_id,
       gene.hugo_gene_symbol                                     AS hugo_gene_symbol,
       gene.entrez_gene_id                                       AS entrez_gene_id,
       ifNull(gp.stable_id, 'WES')                               AS gene_panel_stable_id,
       cs.cancer_study_identifier                                AS cancer_study_identifier,
       g.stable_id                                               AS genetic_profile_stable_id,
       'mutation'                                                AS variant_type,
       me.protein_change                                         AS mutation_variant,
       me.mutation_type                                          AS mutation_type,
       mutation.mutation_status                                  AS mutation_status,
       'NA'                                                      AS driver_filter,
       'NA'                                                      AS drivet_tiers_filter,
       NULL                                                      AS cna_alteration,
       ''                                                        AS cna_cytoband,
       ''                                                        AS sv_event_info,
       concat(cs.cancer_study_identifier, '_', patient.stable_id) AS patient_unique_id
FROM mutation
         INNER JOIN mutation_event AS me ON mutation.mutation_event_id = me.mutation_event_id
         INNER JOIN sample_profile sp
                    ON mutation.sample_id = sp.sample_id AND mutation.genetic_profile_id = sp.genetic_profile_id
         LEFT JOIN gene_panel gp ON sp.panel_id = gp.internal_id
         LEFT JOIN genetic_profile g ON sp.genetic_profile_id = g.genetic_profile_id
         INNER JOIN cancer_study cs ON g.cancer_study_id = cs.cancer_study_id
         INNER JOIN sample ON mutation.sample_id = sample.internal_id
         INNER JOIN patient on sample.patient_id = patient.internal_id
         LEFT JOIN gene ON mutation.entrez_gene_id = gene.entrez_gene_id
UNION ALL
-- Insert CNA Genes
SELECT concat(cs.cancer_study_identifier, '_', sample.stable_id) AS sample_unique_id,
       gene.hugo_gene_symbol                                     AS hugo_gene_symbol,
       gene.entrez_gene_id                                       AS entrez_gene_id,
       ifNull(gp.stable_id, 'WES')                               AS gene_panel_stable_id,
       cs.cancer_study_identifier                                AS cancer_study_identifier,
       g.stable_id                                               AS genetic_profile_stable_id,
       'cna'                                                     AS variant_type,
       'NA'                                                      AS mutation_variant,
       'NA'                                                      AS mutation_type,
       'NA'                                                      AS mutation_status,
       'NA'                                                      AS driver_filter,
       'NA'                                                      AS drivet_tiers_filter,
       ce.alteration                                             AS cna_alteration,
       rgg.cytoband                                              AS cna_cytoband,
       ''                                                        AS sv_event_info,
       concat(cs.cancer_study_identifier, '_', patient.stable_id) AS patient_unique_id
FROM cna_event ce
         INNER JOIN sample_cna_event sce ON ce.cna_event_id = sce.cna_event_id
         INNER JOIN sample_profile sp ON sce.sample_id = sp.sample_id AND sce.genetic_profile_id = sp.genetic_profile_id
         LEFT JOIN gene_panel gp ON sp.panel_id = gp.internal_id
         INNER JOIN genetic_profile g ON sp.genetic_profile_id = g.genetic_profile_id
         INNER JOIN cancer_study cs ON g.cancer_study_id = cs.cancer_study_id
         INNER JOIN sample ON sce.sample_id = sample.internal_id
         INNER JOIN patient on sample.patient_id = patient.internal_id
         INNER JOIN gene ON ce.entrez_gene_id = gene.entrez_gene_id
         INNER JOIN reference_genome_gene rgg ON rgg.entrez_gene_id = ce.entrez_gene_id  AND rgg.reference_genome_id = cs.reference_genome_id
UNION ALL
-- Insert Structural Variants Site1
SELECT concat(cs.cancer_study_identifier, '_', s.stable_id) AS sample_unique_id,
       gene.hugo_gene_symbol                                AS hugo_gene_symbol,
       gene.entrez_gene_id                                  AS entrez_gene_id,
       ifNull(gene_panel.stable_id, 'WES')                  AS gene_panel_stable_id,
       cs.cancer_study_identifier                           AS cancer_study_identifier,
       gp.stable_id                                         AS genetic_profile_stable_id,
       'structural_variant'                                 AS variant_type,
       'NA'                                                 AS mutation_variant,
       'NA'                                                 AS mutation_type,
       'NA'                                                 AS mutation_status,
       'NA'                                                 AS driver_filter,
       'NA'                                                 AS drivet_tiers_filter,
       NULL                                                 AS cna_alteration,
       ''                                                   AS cna_cytoband,
       event_info                                           AS sv_event_info,
       concat(cs.cancer_study_identifier, '_', patient.stable_id) AS patient_unique_id
FROM structural_variant sv
         INNER JOIN genetic_profile gp ON sv.genetic_profile_id = gp.genetic_profile_id
         INNER JOIN sample s ON sv.sample_id = s.internal_id
         INNER JOIN patient on s.patient_id = patient.internal_id
         INNER JOIN cancer_study cs ON gp.cancer_study_id = cs.cancer_study_id
         INNER JOIN gene ON sv.site1_entrez_gene_id = gene.entrez_gene_id
         INNER JOIN sample_profile ON s.internal_id = sample_profile.sample_id AND sample_profile.genetic_profile_id = sv.genetic_profile_id
         LEFT JOIN gene_panel ON sample_profile.panel_id = gene_panel.internal_id
UNION ALL
-- Insert Structural Variants Site2
SELECT concat(cs.cancer_study_identifier, '_', s.stable_id) AS sample_unique_id,
       gene.hugo_gene_symbol                                AS hugo_gene_symbol,
       gene.entrez_gene_id                                  AS entrez_gene_id,
       ifNull(gene_panel.stable_id, 'WES')                  AS gene_panel_stable_id,
       cs.cancer_study_identifier                           AS cancer_study_identifier,
       gp.stable_id                                         AS genetic_profile_stable_id,
       'structural_variant'                                 AS variant_type,
       'NA'                                                 AS mutation_variant,
       'NA'                                                 AS mutation_type,
       'NA'                                                 AS mutation_status,
       'NA'                                                 AS driver_filter,
       'NA'                                                 AS drivet_tiers_filter,
       NULL                                                 AS cna_alteration,
       ''                                                   AS cna_cytoband,
       event_info                                           AS sv_event_info,
       concat(cs.cancer_study_identifier, '_', patient.stable_id) AS patient_unique_id
FROM structural_variant sv
         INNER JOIN genetic_profile gp ON sv.genetic_profile_id = gp.genetic_profile_id
         INNER JOIN sample s ON sv.sample_id = s.internal_id
         INNER JOIN patient on s.patient_id = patient.internal_id
         INNER JOIN cancer_study cs ON gp.cancer_study_id = cs.cancer_study_id
         INNER JOIN gene ON sv.site2_entrez_gene_id = gene.entrez_gene_id
         INNER JOIN sample_profile ON s.internal_id = sample_profile.sample_id AND sample_profile.genetic_profile_id = sv.genetic_profile_id
         LEFT JOIN gene_panel ON sample_profile.panel_id = gene_panel.internal_id
WHERE
        sv.site2_entrez_gene_id != sv.site1_entrez_gene_id
   OR sv.site1_entrez_gene_id IS NULL;

CREATE TABLE IF NOT EXISTS clinical_data_derived
(
    internal_id Int,
    sample_unique_id String,
    patient_unique_id String,
    attribute_name LowCardinality(String),
    attribute_value String,
    cancer_study_identifier LowCardinality(String),
    type LowCardinality(String)
)
    ENGINE=MergeTree
        ORDER BY (type, attribute_name, sample_unique_id);

-- Insert sample attribute data
INSERT INTO TABLE clinical_data_derived
SELECT sm.internal_id             AS internal_id,
       sm.sample_unique_id        AS sample_unique_id,
       sm.patient_unique_id       AS patient_unique_id,
       cam.attr_id                AS attribute_name,
       ifNull(csamp.attr_value, '')          AS attribute_value,
       cs.cancer_study_identifier AS cancer_study_identifier,
       'sample'                   AS type
FROM sample_derived AS sm
         INNER JOIN cancer_study AS cs
                    ON sm.cancer_study_identifier = cs.cancer_study_identifier
         FULL OUTER JOIN clinical_attribute_meta AS cam
                         ON cs.cancer_study_id = cam.cancer_study_id
         FULL OUTER JOIN clinical_sample AS csamp
                         ON (sm.internal_id = csamp.internal_id) AND (csamp.attr_id = cam.attr_id)
WHERE cam.patient_attribute = 0;

-- INSERT patient attribute data
INSERT INTO TABLE clinical_data_derived
SELECT p.internal_id                                        AS internal_id,
       ''                                                   AS sample_unique_id,
       concat(cs.cancer_study_identifier, '_', p.stable_id) AS patient_unique_id,
       cam.attr_id                                          AS attribute_name,
       ifNull(clinpat.attr_value, '')                       AS attribute_value,
       cs.cancer_study_identifier                           AS cancer_study_identifier,
       'patient'                                            AS type
FROM patient AS p
         INNER JOIN cancer_study AS cs ON p.cancer_study_id = cs.cancer_study_id
         FULL OUTER JOIN clinical_attribute_meta AS cam
                         ON cs.cancer_study_id = cam.cancer_study_id
         FULL OUTER JOIN clinical_patient AS clinpat
                         ON (p.internal_id = clinpat.internal_id) AND (clinpat.attr_id = cam.attr_id)
WHERE cam.patient_attribute = 1;

CREATE TABLE clinical_event_derived
(
    patient_unique_id String,
    key String,
    value String,
    start_date Int32,
    stop_date Int32 DEFAULT 0,
    event_type LowCardinality(String),
    cancer_study_identifier LowCardinality(String)
)
ENGINE = MergeTree
    ORDER BY (event_type, patient_unique_id, cancer_study_identifier);

INSERT INTO clinical_event_derived
SELECT
    concat(cs.cancer_study_identifier, '_', p.stable_id)      AS patient_unique_id,
    ced.key AS key,
    ced.value AS value,
    ce.start_date AS start_date,
    ifNull(ce.stop_date, 0) AS stop_date,
    ce.event_type AS event_type,
    cs.cancer_study_identifier
FROM clinical_event ce
         LEFT JOIN clinical_event_data ced ON ce.clinical_event_id = ced.clinical_event_id
         INNER JOIN patient p ON ce.patient_id = p.internal_id
         INNER JOIN cancer_study cs ON p.cancer_study_id = cs.cancer_study_id;

CREATE TABLE IF NOT EXISTS genetic_alteration_derived
(
    sample_unique_id String,
    cancer_study_identifier LowCardinality(String),
    hugo_gene_symbol String,
    profile_type LowCardinality(String),
    alteration_value Nullable(String)
    )
    ENGINE = MergeTree()
    ORDER BY (cancer_study_identifier, hugo_gene_symbol, profile_type, sample_unique_id);

INSERT INTO TABLE genetic_alteration_derived
SELECT
    sample_unique_id,
    cancer_study_identifier,
    hugo_gene_symbol,
    replaceOne(stable_id, concat(sd.cancer_study_identifier, '_'), '') as profile_type,
    alteration_value
FROM
    (SELECT
         sample_id,
         hugo_gene_symbol,
         stable_id,
         alteration_value
    FROM
        (SELECT
            g.hugo_gene_symbol AS hugo_gene_symbol,
            gp.stable_id as stable_id,
            arrayMap(x -> (x = '' ? NULL : x), splitByString(',', assumeNotNull(substring(ga.values, 1, -1)))) AS alteration_value,
            arrayMap(x -> (x = '' ? NULL : toInt32(x)), splitByString(',', assumeNotNull(substring(gps.ordered_sample_list, 1, -1)))) AS sample_id
        FROM
            genetic_profile gp
            JOIN genetic_profile_samples gps ON gp.genetic_profile_id = gps.genetic_profile_id
            JOIN genetic_alteration ga ON gp.genetic_profile_id = ga.genetic_profile_id
            JOIN gene g ON ga.genetic_entity_id = g.genetic_entity_id
        WHERE
             gp.genetic_alteration_type NOT IN ('GENERIC_ASSAY', 'MUTATION_EXTENDED', 'STRUCTURAL_VARIANT'))
            ARRAY JOIN alteration_value, sample_id
    WHERE alteration_value != 'NA') AS subquery
        JOIN sample_derived sd ON sd.internal_id = subquery.sample_id;

CREATE TABLE IF NOT EXISTS generic_assay_data_derived
(
    sample_unique_id String,
    patient_unique_id String,
    genetic_entity_id String,
    value String,
    generic_assay_type String,
    profile_stable_id String,
    entity_stable_id String,
    datatype String,
    patient_level NUMERIC,
    profile_type String
)
    ENGINE = MergeTree()
    ORDER BY (profile_type, entity_stable_id, patient_unique_id, sample_unique_id);

INSERT INTO TABLE generic_assay_data_derived
SELECT
    sd.sample_unique_id as sample_unique_id,
    sd.patient_unique_id as patient_unique_id,
    genetic_entity_id,
    value,
    generic_assay_type,
    profile_stable_id,
    entity_stable_id,
    datatype,
    patient_level,
    replaceOne(profile_stable_id, concat(cs.cancer_study_identifier, '_'), '') as profile_type
FROM
    (SELECT
         sample_id,
         genetic_entity_id,
         value,
         cancer_study_id,
         generic_assay_type,
         genetic_profile_id,
         profile_stable_id,
         entity_stable_id,
         patient_level,
         datatype
     FROM
         (SELECT
              sample_id as sample_unique_id,
              gp.cancer_study_id AS cancer_study_id,
              ga.genetic_entity_id as genetic_entity_id,
              gp.genetic_profile_id as genetic_profile_id,
              gp.generic_assay_type as generic_assay_type,
              gp.stable_id as profile_stable_id,
              ge.stable_id as entity_stable_id,
              gp.datatype as datatype,
              gp.patient_level as patient_level,
              arrayMap(x -> (x = '' ? NULL : x), splitByString(',', assumeNotNull(substring(ga.values, 1, -1)))) AS value,
              arrayMap(x -> (x = '' ? NULL : toInt64(x)), splitByString(',', assumeNotNull(substring(gps.ordered_sample_list, 1, -1)))) AS sample_id
          FROM genetic_profile gp
              JOIN genetic_profile_samples gps ON gp.genetic_profile_id = gps.genetic_profile_id
              JOIN genetic_alteration ga ON gp.genetic_profile_id = ga.genetic_profile_id
              JOIN genetic_entity ge on ga.genetic_entity_id = ge.id
          WHERE
              gp.generic_assay_type IS NOT NULL
         )
             ARRAY JOIN value, sample_id) AS subquery
        JOIN cancer_study cs ON cs.cancer_study_id = subquery.cancer_study_id
        JOIN sample_derived sd ON sd.internal_id = subquery.sample_id;

OPTIMIZE TABLE sample_to_gene_panel_derived;
OPTIMIZE TABLE gene_panel_to_gene_derived;
OPTIMIZE TABLE sample_derived;
OPTIMIZE TABLE genomic_event_derived;
OPTIMIZE TABLE clinical_data_derived;
OPTIMIZE TABLE clinical_event_derived;
OPTIMIZE TABLE genetic_alteration_derived;
OPTIMIZE TABLE generic_assay_data_derived;
