DROP TABLE IF EXISTS genomic_event_derived;
DROP TABLE IF EXISTS sample_to_gene_panel;
DROP TABLE IF EXISTS gene_panel_to_gene;

CREATE TABLE IF NOT EXISTS genomic_event_derived
(
    sample_unique_id          String,
    hugo_gene_symbol          String,
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
    sv_event_info             String
) ENGINE = MergeTree
ORDER BY ( variant_type, hugo_gene_symbol, genetic_profile_stable_id, sample_unique_id);

CREATE TABLE sample_to_gene_panel
(
    sample_unique_id String,
    alteration_type LowCardinality(String),
    gene_panel_id LowCardinality(String),
    cancer_study_identifier LowCardinality(String)
) ENGINE = MergeTree()
ORDER BY (gene_panel_id, alteration_type, sample_unique_id);

CREATE TABLE gene_panel_to_gene
(
    gene_panel_id LowCardinality(String),
    gene String
) ENGINE = MergeTree()
ORDER BY (gene_panel_id);