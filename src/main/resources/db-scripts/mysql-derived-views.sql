DROP TABLE IF EXISTS sample_to_gene_panel_derived;
DROP TABLE IF EXISTS gene_panel_to_gene_derived;
DROP TABLE IF EXISTS sample_derived;
DROP TABLE IF EXISTS genomic_event_derived;
DROP TABLE IF EXISTS clinical_data_derived;
DROP TABLE IF EXISTS clinical_event_derived;
DROP TABLE IF EXISTS genetic_profile_samples_transposed;
DROP TABLE IF EXISTS genetic_alteration_transposed;
DROP TABLE IF EXISTS genetic_sample_alteration_transposed;
DROP TABLE IF EXISTS genetic_alteration_derived;
DROP TABLE IF EXISTS genetic_generic_assay_data_derived;

CREATE VIEW sample_to_gene_panel_derived AS
SELECT
    CONCAT(cs.cancer_study_identifier, '_', sample.stable_id) AS sample_unique_id,
    genetic_alteration_type AS alteration_type,
    IFNULL(gene_panel.stable_id, 'WES') AS gene_panel_id,
    cs.cancer_study_identifier AS cancer_study_identifier,
    gp.stable_id AS genetic_profile_id
FROM sample_profile sp
INNER JOIN genetic_profile gp ON sp.genetic_profile_id = gp.genetic_profile_id
LEFT JOIN gene_panel ON sp.panel_id = gene_panel.internal_id
INNER JOIN sample ON sp.sample_id = sample.internal_id
INNER JOIN cancer_study cs ON gp.cancer_study_id = cs.cancer_study_id;

CREATE VIEW gene_panel_to_gene_derived AS
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

CREATE VIEW sample_derived AS
SELECT
    CONCAT(cs.cancer_study_identifier, '_', sample.stable_id) AS sample_unique_id,
    TO_BASE64(sample.stable_id) AS sample_unique_id_base64,
    sample.stable_id AS sample_stable_id,
    CONCAT(cs.cancer_study_identifier, '_', p.stable_id) AS patient_unique_id,
    TO_BASE64(p.stable_id) AS patient_unique_id_base64,
    p.stable_id AS patient_stable_id,
    cs.cancer_study_identifier AS cancer_study_identifier,
    sample.internal_id AS internal_id
FROM sample
INNER JOIN patient AS p ON sample.patient_id = p.internal_id
INNER JOIN cancer_study AS cs ON p.cancer_study_id = cs.cancer_study_id;

CREATE VIEW genomic_event_derived AS
SELECT
    CONCAT(cs.cancer_study_identifier, '_', sample.stable_id) AS sample_unique_id,
    gene.hugo_gene_symbol AS hugo_gene_symbol,
    gene.entrez_gene_id AS entrez_gene_id,
    IFNULL(gp.stable_id, 'WES') AS gene_panel_stable_id,
    cs.cancer_study_identifier AS cancer_study_identifier,
    g.stable_id AS genetic_profile_stable_id,
    'mutation' AS variant_type,
    me.protein_change AS mutation_variant,
    me.mutation_type AS mutation_type,
    mutation.mutation_status AS mutation_status,
    'NA' AS driver_filter,
    'NA' AS driver_tiers_filter,
    NULL AS cna_alteration,
    '' AS cna_cytoband,
    '' AS sv_event_info,
    CONCAT(cs.cancer_study_identifier, '_', patient.stable_id) AS patient_unique_id
FROM mutation
INNER JOIN mutation_event AS me ON mutation.mutation_event_id = me.mutation_event_id
INNER JOIN sample_profile sp ON mutation.sample_id = sp.sample_id AND mutation.genetic_profile_id = sp.genetic_profile_id
LEFT JOIN gene_panel gp ON sp.panel_id = gp.internal_id
LEFT JOIN genetic_profile g ON sp.genetic_profile_id = g.genetic_profile_id
INNER JOIN cancer_study cs ON g.cancer_study_id = cs.cancer_study_id
INNER JOIN sample ON mutation.sample_id = sample.internal_id
INNER JOIN patient ON sample.patient_id = patient.internal_id
LEFT JOIN gene ON mutation.entrez_gene_id = gene.entrez_gene_id
UNION ALL
SELECT
    CONCAT(cs.cancer_study_identifier, '_', sample.stable_id) AS sample_unique_id,
    gene.hugo_gene_symbol AS hugo_gene_symbol,
    gene.entrez_gene_id AS entrez_gene_id,
    IFNULL(gp.stable_id, 'WES') AS gene_panel_stable_id,
    cs.cancer_study_identifier AS cancer_study_identifier,
    g.stable_id AS genetic_profile_stable_id,
    'cna' AS variant_type,
    'NA' AS mutation_variant,
    'NA' AS mutation_type,
    'NA' AS mutation_status,
    'NA' AS driver_filter,
    'NA' AS driver_tiers_filter,
    ce.alteration AS cna_alteration,
    rgg.cytoband AS cna_cytoband,
    '' AS sv_event_info,
    CONCAT(cs.cancer_study_identifier, '_', patient.stable_id) AS patient_unique_id
FROM cna_event ce
INNER JOIN sample_cna_event sce ON ce.cna_event_id = sce.cna_event_id
INNER JOIN sample_profile sp ON sce.sample_id = sp.sample_id AND sce.genetic_profile_id = sp.genetic_profile_id
LEFT JOIN gene_panel gp ON sp.panel_id = gp.internal_id
INNER JOIN genetic_profile g ON sp.genetic_profile_id = g.genetic_profile_id
INNER JOIN cancer_study cs ON g.cancer_study_id = cs.cancer_study_id
INNER JOIN sample ON sce.sample_id = sample.internal_id
INNER JOIN patient ON sample.patient_id = patient.internal_id
INNER JOIN gene ON ce.entrez_gene_id = gene.entrez_gene_id
INNER JOIN reference_genome_gene rgg ON rgg.entrez_gene_id = ce.entrez_gene_id AND rgg.reference_genome_id = cs.reference_genome_id
UNION ALL
SELECT
    CONCAT(cs.cancer_study_identifier, '_', s.stable_id) AS sample_unique_id,
    gene.hugo_gene_symbol AS hugo_gene_symbol,
    gene.entrez_gene_id AS entrez_gene_id,
    IFNULL(gene_panel.stable_id, 'WES') AS gene_panel_stable_id,
    cs.cancer_study_identifier AS cancer_study_identifier,
    gp.stable_id AS genetic_profile_stable_id,
    'structural_variant' AS variant_type,
    'NA' AS mutation_variant,
    'NA' AS mutation_type,
    'NA' AS mutation_status,
    'NA' AS driver_filter,
    'NA' AS driver_tiers_filter,
    NULL AS cna_alteration,
    '' AS cna_cytoband,
    sv.event_info AS sv_event_info,
    CONCAT(cs.cancer_study_identifier, '_', patient.stable_id) AS patient_unique_id
FROM structural_variant sv
INNER JOIN genetic_profile gp ON sv.genetic_profile_id = gp.genetic_profile_id
INNER JOIN sample s ON sv.sample_id = s.internal_id
INNER JOIN patient ON s.patient_id = patient.internal_id
INNER JOIN cancer_study cs ON gp.cancer_study_id = cs.cancer_study_id
INNER JOIN gene ON sv.site1_entrez_gene_id = gene.entrez_gene_id
INNER JOIN sample_profile ON s.internal_id = sample_profile.sample_id AND sample_profile.genetic_profile_id = sv.genetic_profile_id
LEFT JOIN gene_panel ON sample_profile.panel_id = gene_panel.internal_id
UNION ALL
SELECT
    CONCAT(cs.cancer_study_identifier, '_', s.stable_id) AS sample_unique_id,
    gene.hugo_gene_symbol AS hugo_gene_symbol,
    gene.entrez_gene_id AS entrez_gene_id,
    IFNULL(gene_panel.stable_id, 'WES') AS gene_panel_stable_id,
    cs.cancer_study_identifier AS cancer_study_identifier,
    gp.stable_id AS genetic_profile_stable_id,
    'structural_variant' AS variant_type,
    'NA' AS mutation_variant,
    'NA' AS mutation_type,
    'NA' AS mutation_status,
    'NA' AS driver_filter,
    'NA' AS driver_tiers_filter,
    NULL AS cna_alteration,
    '' AS cna_cytoband,
    sv.event_info AS sv_event_info,
    CONCAT(cs.cancer_study_identifier, '_', patient.stable_id) AS patient_unique_id
FROM structural_variant sv
INNER JOIN genetic_profile gp ON sv.genetic_profile_id = gp.genetic_profile_id
INNER JOIN sample s ON sv.sample_id = s.internal_id
INNER JOIN patient ON s.patient_id = patient.internal_id
INNER JOIN cancer_study cs ON gp.cancer_study_id = cs.cancer_study_id
INNER JOIN gene ON sv.site2_entrez_gene_id = gene.entrez_gene_id
INNER JOIN sample_profile ON s.internal_id = sample_profile.sample_id AND sample_profile.genetic_profile_id = sv.genetic_profile_id
LEFT JOIN gene_panel ON sample_profile.panel_id = gene_panel.internal_id
WHERE sv.site2_entrez_gene_id != sv.site1_entrez_gene_id OR sv.site1_entrez_gene_id IS NULL;

CREATE VIEW clinical_data_derived AS
SELECT
    sm.internal_id AS internal_id,
    sm.sample_unique_id AS sample_unique_id,
    sm.patient_unique_id AS patient_unique_id,
    cam.attr_id AS attribute_name,
    IFNULL(csamp.attr_value, '') AS attribute_value,
    cs.cancer_study_identifier AS cancer_study_identifier,
    'sample' AS type
FROM sample_derived AS sm
INNER JOIN cancer_study AS cs ON sm.cancer_study_identifier = cs.cancer_study_identifier
LEFT JOIN clinical_attribute_meta AS cam ON cs.cancer_study_id = cam.cancer_study_id
LEFT JOIN clinical_sample AS csamp ON sm.internal_id = csamp.internal_id AND csamp.attr_id = cam.attr_id
WHERE cam.patient_attribute = 0
UNION ALL
SELECT
    p.internal_id AS internal_id,
    '' AS sample_unique_id,
    CONCAT(cs.cancer_study_identifier, '_', p.stable_id) AS patient_unique_id,
    cam.attr_id AS attribute_name,
    IFNULL(clinpat.attr_value, '') AS attribute_value,
    cs.cancer_study_identifier AS cancer_study_identifier,
    'patient' AS type
FROM patient AS p
INNER JOIN cancer_study AS cs ON p.cancer_study_id = cs.cancer_study_id
LEFT JOIN clinical_attribute_meta AS cam ON cs.cancer_study_id = cam.cancer_study_id
LEFT JOIN clinical_patient AS clinpat ON p.internal_id = clinpat.internal_id AND clinpat.attr_id = cam.attr_id
WHERE cam.patient_attribute = 1;

CREATE VIEW clinical_event_derived AS
SELECT
    CONCAT(cs.cancer_study_identifier, '_', p.stable_id) AS patient_unique_id,
    ced.`key` AS `key`,
    ced.`value` AS `value`,
    ce.start_date AS start_date,
    IFNULL(ce.stop_date, 0) AS stop_date,
    ce.event_type AS event_type,
    cs.cancer_study_identifier AS cancer_study_identifier
FROM clinical_event ce
LEFT JOIN clinical_event_data ced ON ce.clinical_event_id = ced.clinical_event_id
INNER JOIN patient p ON ce.patient_id = p.internal_id
INNER JOIN cancer_study cs ON p.cancer_study_id = cs.cancer_study_id;

CREATE VIEW genetic_profile_samples_transposed AS
SELECT 
    gps.GENETIC_PROFILE_ID, 
    jt.value AS SAMPLE_ID, 
    jt.idx AS `POSITION`
FROM genetic_profile_samples gps
JOIN JSON_TABLE(
    CONCAT('[', TRIM(TRAILING ',' FROM gps.ORDERED_SAMPLE_LIST), ']'),
    '$[*]' COLUMNS (
        idx FOR ORDINALITY,  -- Position in the CSV
        value INT PATH '$'
    )
) AS jt;

CREATE VIEW genetic_alteration_transposed AS
SELECT 
    ga.GENETIC_PROFILE_ID,
    ga.GENETIC_ENTITY_ID,
    jt.value AS alteration_value, 
    jt.idx AS `POSITION`
FROM genetic_alteration ga
JOIN JSON_TABLE(
    CONCAT('["', REPLACE(TRIM(TRAILING ',' FROM ga.VALUES), ',', '","'), '"]'),
    '$[*]' COLUMNS (
        idx FOR ORDINALITY,  -- Position in the CSV
        value VARCHAR(256) PATH '$'
    )
) AS jt;

CREATE VIEW genetic_sample_alteration_transposed AS
SELECT 
    gpst.GENETIC_PROFILE_ID,
    gpst.SAMPLE_ID,
    gat.GENETIC_ENTITY_ID,
    gat.alteration_value
FROM genetic_profile_samples_transposed gpst
INNER JOIN genetic_alteration_transposed gat ON gat.GENETIC_PROFILE_ID = gpst.GENETIC_PROFILE_ID AND gat.`POSITION` = gpst.`POSITION`;

CREATE VIEW genetic_alteration_derived AS
SELECT
    sd.sample_unique_id AS sample_unique_id,
    sd.cancer_study_identifier AS cancer_study_identifier,
    g.hugo_gene_symbol AS hugo_gene_symbol,
    REPLACE(gp.stable_id, CONCAT(sd.cancer_study_identifier, '_'), '') AS profile_type,
    ga.alteration_value AS alteration_value
FROM genetic_sample_alteration_transposed ga
JOIN genetic_profile gp ON ga.genetic_profile_id = gp.genetic_profile_id
JOIN gene g ON ga.genetic_entity_id = g.genetic_entity_id
JOIN sample_derived sd ON sd.internal_id = ga.sample_id
WHERE gp.genetic_alteration_type NOT IN ('GENERIC_ASSAY', 'MUTATION_EXTENDED', 'STRUCTURAL_VARIANT');

CREATE VIEW generic_assay_data_derived AS
SELECT
    sd.sample_unique_id AS sample_unique_id,
    sd.patient_unique_id AS patient_unique_id,
    ga.genetic_entity_id AS genetic_entity_id,
    ga.alteration_value AS `value`,
    gp.generic_assay_type AS generic_assay_type,
    gp.stable_id AS profile_stable_id,
    ge.stable_id AS entity_stable_id,
    gp.datatype AS datatype,
    gp.patient_level AS patient_level,
    REPLACE(gp.stable_id, CONCAT(cs.cancer_study_identifier, '_'), '') AS profile_type
FROM genetic_sample_alteration_transposed ga
JOIN genetic_profile gp ON ga.genetic_profile_id = gp.genetic_profile_id
JOIN genetic_entity ge ON ga.genetic_entity_id = ge.id
JOIN cancer_study cs ON gp.cancer_study_id = cs.cancer_study_id
JOIN sample_derived sd ON sd.internal_id = ga.sample_id
WHERE gp.generic_assay_type IS NOT NULL;