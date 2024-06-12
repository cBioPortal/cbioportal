DROP TABLE IF EXISTS genomic_event_mutation;
DROP TABLE IF EXISTS genomic_event;
DROP TABLE IF EXISTS sample_to_gene_panel;
DROP TABLE IF EXISTS gene_panel_to_gene;

CREATE TABLE IF NOT EXISTS genomic_event
(
    sample_unique_id          String,
    variant                   String,
    variant_type              String,
    hugo_gene_symbol          String,
    gene_panel_stable_id      String,
    cancer_study_identifier   String,
    genetic_profile_stable_id String
) ENGINE = MergeTree
ORDER BY ( variant_type, sample_unique_id, hugo_gene_symbol);

CREATE TABLE IF NOT EXISTS genomic_event_mutation
(
    sample_unique_id          String,
    variant                   String,
    hugo_gene_symbol          String,
    gene_panel_stable_id      String,
    cancer_study_identifier   String,
    genetic_profile_stable_id String,
    mutation_type             String,
    mutation_status           String,
    driver_filter             String,
    driver_tiers_filter       String
) ENGINE = MergeTree
ORDER BY ( hugo_gene_symbol, genetic_profile_stable_id);

CREATE TABLE sample_to_gene_panel
(
    sample_unique_id String,
    alteration_type String,
    gene_panel_id String,
    cancer_study_identifier String
) ENGINE = MergeTree()
ORDER BY (gene_panel_id, alteration_type, sample_unique_id);

CREATE TABLE gene_panel_to_gene
(
    gene_panel_id String,
    gene String
) ENGINE = MergeTree()
ORDER BY (gene_panel_id);