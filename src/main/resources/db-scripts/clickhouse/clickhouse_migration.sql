
INSERT INTO sample_to_gene_panel
select
    concat(cs.cancer_study_identifier, '_', sample.stable_id) as sample_unique_id,
    genetic_alteration_type as alteration_type,
    ifnull(gene_panel.stable_id, 'WES') as gene_panel_id,
    cs.cancer_study_identifier as cancer_study_identifier
from sample_profile sp
         inner join genetic_profile gp on sample_profile.genetic_profile_id = gp.genetic_profile_id
         left join gene_panel on sp.panel_id = gene_panel.internal_id
         inner join sample on sp.sample_id = sample.internal_id
         inner join cancer_study cs on gp.cancer_study_id = cs.cancer_study_id;

INSERT INTO gene_panel_to_gene
select
    gp.stable_id as gene_panel_id,
    g.hugo_gene_symbol as gene
from gene_panel gp
         inner join gene_panel_list gpl ON gp.internal_id = gpl.internal_id
         inner join gene g ON g.entrez_gene_id = gpl.gene_id
UNION ALL
select
    'WES' as gene_panel_id,
    gene.hugo_gene_symbol as gene
from gene
where gene.entrez_gene_id > 0;

INSERT INTO genomic_event_derived
SELECT concat(cs.cancer_study_identifier, '_', sample.stable_id) as sample_unique_id,
       gene.hugo_gene_symbol                                     as hugo_gene_symbol,
       gp.stable_id                                              as gene_panel_stable_id,
       cs.cancer_study_identifier                                as cancer_study_identifier,
       g.stable_id                                               as genetic_profile_stable_id,
       'mutation'                                                as variant_type,
       me.protein_change                                         as mutation_variant,
       me.mutation_type                                          as mutation_type,
       mutation.mutation_status                                  as mutation_status,
       'NA'                                                      as driver_filter,
       'NA'                                                      as drivet_tiers_filter,
       NULL                                                      as cna_alteration,
       ''                                                        as cna_cytoband,
       ''                                                        as sv_event_info
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
       gene.hugo_gene_symbol                                     as hugo_gene_symbol,
       gp.stable_id                                              as gene_panel_stable_id,
       cs.cancer_study_identifier                                as cancer_study_identifier,
       g.stable_id                                               as genetic_profile_stable_id,
       'cna'                                                     as variant_type,
       'NA'                                                      as mutation_variant,
       'NA'                                                      as mutation_type,
       'NA'                                                      as mutation_status,
       'NA'                                                      as driver_filter,
       'NA'                                                      as drivet_tiers_filter,
       ce.alteration                                             as cna_alteration,
       rgg.cytoband                                              as cna_cytoband,
       ''                                                        as sv_event_info
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
SELECT concat(cs.cancer_study_identifier, '_', s.stable_id) as sample_unique_id,
       gene2.hugo_gene_symbol                               as hugo_gene_symbol,
       gene_panel.stable_id                                 as gene_panel_stable_id,
       cs.cancer_study_identifier                           as cancer_study_identifier,
       gp.stable_id                                         as genetic_profile_stable_id,
       'structural_variant'                                 as variant_type,
       'NA'                                                 as mutation_variant,
       'NA'                                                 as mutation_type,
       'NA'                                                 as mutation_status,
       'NA'                                                 as driver_filter,
       'NA'                                                 as drivet_tiers_filter,
       NULL                                                 as cna_alteration,
       ''                                                   as cna_cytoband,
       event_info                                           as sv_event_info
FROM structural_variant sv
         INNER JOIN genetic_profile gp ON sv.genetic_profile_id = gp.genetic_profile_id
         INNER JOIN sample s ON sv.sample_id = s.internal_id
         INNER JOIN cancer_study cs ON gp.cancer_study_id = cs.cancer_study_id
         INNER JOIN gene gene2 ON sv.site2_entrez_gene_id = gene2.entrez_gene_id
         INNER JOIN sample_profile on s.internal_id = sample_profile.sample_id
         INNER JOIN gene_panel on sample_profile.panel_id = gene_panel.internal_id
UNION ALL
SELECT concat(cs.cancer_study_identifier, '_', s.stable_id) as sample_unique_id,
       gene1.hugo_gene_symbol                               as hugo_gene_symbol,
       gene_panel.stable_id                                 as gene_panel_stable_id,
       cs.cancer_study_identifier                           as cancer_study_identifier,
       gp.stable_id                                         as genetic_profile_stable_id,
       'structural_variant'                                 as variant_type,
       'NA'                                                 as mutation_variant,
       'NA'                                                 as mutation_type,
       'NA'                                                 as mutation_status,
       'NA'                                                 as driver_filter,
       'NA'                                                 as drivet_tiers_filter,
       NULL                                                 as cna_alteration,
       ''                                                   as cna_cytoband,
       event_info                                           as sv_event_info
FROM structural_variant sv
         INNER JOIN genetic_profile gp ON sv.genetic_profile_id = gp.genetic_profile_id
         INNER JOIN sample s ON sv.sample_id = s.internal_id
         INNER JOIN cancer_study cs ON gp.cancer_study_id = cs.cancer_study_id
         INNER JOIN gene gene1 ON sv.site1_entrez_gene_id = gene1.entrez_gene_id
         INNER JOIN sample_profile on s.internal_id = sample_profile.sample_id
         INNER JOIN gene_panel on sample_profile.panel_id = gene_panel.internal_id;