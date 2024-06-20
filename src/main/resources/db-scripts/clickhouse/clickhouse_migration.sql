
INSERT INTO sample_to_gene_panel
SELECT
    concat(cs.cancer_study_identifier, '_', sample.stable_id) AS sample_unique_id,
    genetic_alteration_type AS alteration_type,
    -- If a mutation is found in a gene that is not in a gene panel we assume Whole Exome Sequencing WES
    ifnull(gene_panel.stable_id, 'WES') AS gene_panel_id,
    cs.cancer_study_identifier AS cancer_study_identifier
FROM sample_profile sp
         INNER JOIN genetic_profile gp ON sample_profile.genetic_profile_id = gp.genetic_profile_id
         LEFT JOIN gene_panel ON sp.panel_id = gene_panel.internal_id
         INNER JOIN sample ON sp.sample_id = sample.internal_id
         INNER JOIN cancer_study cs ON gp.cancer_study_id = cs.cancer_study_id;

INSERT INTO gene_panel_to_gene
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
WHERE gene.entrez_gene_id > 0;

INSERT INTO genomic_event_derived
SELECT concat(cs.cancer_study_identifier, '_', sample.stable_id) AS sample_unique_id,
       gene.hugo_gene_symbol                                     AS hugo_gene_symbol,
       gp.stable_id                                              AS gene_panel_stable_id,
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
       ''                                                        AS sv_event_info
FROM mutation
         INNER JOIN mutation_event AS me ON mutation.mutation_event_id = me.mutation_event_id
         INNER JOIN sample_profile sp
                    ON mutation.sample_id = sp.sample_id AND mutation.genetic_profile_id = sp.genetic_profile_id
         LEFT JOIN gene_panel gp ON sp.panel_id = gp.internal_id
         LEFT JOIN genetic_profile g ON sp.genetic_profile_id = g.genetic_profile_id
         INNER JOIN cancer_study cs ON g.cancer_study_id = cs.cancer_study_id
         INNER JOIN sample ON mutation.sample_id = sample.internal_id
         LEFT JOIN gene ON mutation.entrez_gene_id = gene.entrez_gene_id
UNION ALL
SELECT concat(cs.cancer_study_identifier, '_', sample.stable_id) AS sample_unique_id,
       gene.hugo_gene_symbol                                     AS hugo_gene_symbol,
       gp.stable_id                                              AS gene_panel_stable_id,
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
       ''                                                        AS sv_event_info
FROM cna_event ce
         INNER JOIN sample_cna_event sce ON ce.cna_event_id = sce.cna_event_id
         INNER JOIN sample_profile sp ON sce.sample_id = sp.sample_id AND sce.genetic_profile_id = sp.genetic_profile_id
         INNER JOIN gene_panel gp ON sp.panel_id = gp.internal_id
         INNER JOIN genetic_profile g ON sp.genetic_profile_id = g.genetic_profile_id
         INNER JOIN cancer_study cs ON g.cancer_study_id = cs.cancer_study_id
         INNER JOIN sample ON sce.sample_id = sample.internal_id
         INNER JOIN gene ON ce.entrez_gene_id = gene.entrez_gene_id
         INNER JOIN reference_genome_gene rgg ON rgg.entrez_gene_id = ce.entrez_gene_id
UNION ALL
SELECT concat(cs.cancer_study_identifier, '_', s.stable_id) AS sample_unique_id,
       gene2.hugo_gene_symbol                               AS hugo_gene_symbol,
       gene_panel.stable_id                                 AS gene_panel_stable_id,
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
       event_info                                           AS sv_event_info
FROM structural_variant sv
         INNER JOIN genetic_profile gp ON sv.genetic_profile_id = gp.genetic_profile_id
         INNER JOIN sample s ON sv.sample_id = s.internal_id
         INNER JOIN cancer_study cs ON gp.cancer_study_id = cs.cancer_study_id
         INNER JOIN gene gene2 ON sv.site2_entrez_gene_id = gene2.entrez_gene_id
         INNER JOIN sample_profile ON s.internal_id = sample_profile.sample_id
         INNER JOIN gene_panel ON sample_profile.panel_id = gene_panel.internal_id
UNION ALL
SELECT concat(cs.cancer_study_identifier, '_', s.stable_id) AS sample_unique_id,
       gene1.hugo_gene_symbol                               AS hugo_gene_symbol,
       gene_panel.stable_id                                 AS gene_panel_stable_id,
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
       event_info                                           AS sv_event_info
FROM structural_variant sv
         INNER JOIN genetic_profile gp ON sv.genetic_profile_id = gp.genetic_profile_id
         INNER JOIN sample s ON sv.sample_id = s.internal_id
         INNER JOIN cancer_study cs ON gp.cancer_study_id = cs.cancer_study_id
         INNER JOIN gene gene1 ON sv.site1_entrez_gene_id = gene1.entrez_gene_id
         INNER JOIN sample_profile ON s.internal_id = sample_profile.sample_id
         INNER JOIN gene_panel ON sample_profile.panel_id = gene_panel.internal_id;