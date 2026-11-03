/*
DROP TABLE IF EXISTS allele_specific_copy_number;
DROP TABLE IF EXISTS alteration_driver_annotation;
DROP TABLE IF EXISTS authorities;
DROP TABLE IF EXISTS cancer_study;
DROP TABLE IF EXISTS cancer_study_tags;
DROP TABLE IF EXISTS clinical_attribute_meta;
DROP TABLE IF EXISTS clinical_data_derived;
DROP TABLE IF EXISTS clinical_event;
DROP TABLE IF EXISTS clinical_event_data;
DROP TABLE IF EXISTS clinical_event_derived;
DROP TABLE IF EXISTS clinical_patient;
DROP TABLE IF EXISTS clinical_sample;
DROP TABLE IF EXISTS cna_event;
DROP TABLE IF EXISTS copy_number_seg;
DROP TABLE IF EXISTS copy_number_seg_file;
DROP TABLE IF EXISTS data_access_tokens;
DROP TABLE IF EXISTS gene;
DROP TABLE IF EXISTS gene_alias;
DROP TABLE IF EXISTS gene_panel;
DROP TABLE IF EXISTS gene_panel_list;
DROP TABLE IF EXISTS gene_panel_to_gene_derived;
DROP TABLE IF EXISTS generic_assay_data_derived;
DROP TABLE IF EXISTS generic_entity_properties;
DROP TABLE IF EXISTS geneset;
DROP TABLE IF EXISTS geneset_gene;
DROP TABLE IF EXISTS geneset_hierarchy_leaf;
DROP TABLE IF EXISTS geneset_hierarchy_node;
DROP TABLE IF EXISTS genetic_alteration;
DROP TABLE IF EXISTS genetic_alteration_derived;
DROP TABLE IF EXISTS genetic_entity;
DROP TABLE IF EXISTS genetic_profile;
DROP TABLE IF EXISTS genetic_profile_link;
DROP TABLE IF EXISTS genetic_profile_samples;
DROP TABLE IF EXISTS genomic_event_derived;
DROP TABLE IF EXISTS gistic;
DROP TABLE IF EXISTS gistic_to_gene;
DROP TABLE IF EXISTS info;
DROP TABLE IF EXISTS mut_sig;
DROP TABLE IF EXISTS mutation;
DROP TABLE IF EXISTS mutation_count_by_keyword;
DROP TABLE IF EXISTS mutation_event;
DROP TABLE IF EXISTS patient;
DROP TABLE IF EXISTS reference_genome;
DROP TABLE IF EXISTS reference_genome_gene;
DROP TABLE IF EXISTS resource;
DROP TABLE IF EXISTS resource_definition;
DROP TABLE IF EXISTS resource_patient;
DROP TABLE IF EXISTS resource_sample;
DROP TABLE IF EXISTS resource_study;
DROP TABLE IF EXISTS sample;
DROP TABLE IF EXISTS sample_cna_event;
DROP TABLE IF EXISTS sample_derived;
DROP TABLE IF EXISTS sample_list;
DROP TABLE IF EXISTS sample_list_list;
DROP TABLE IF EXISTS sample_profile;
DROP TABLE IF EXISTS sample_to_gene_panel_derived;
DROP TABLE IF EXISTS structural_variant;
DROP TABLE IF EXISTS type_of_cancer;
DROP TABLE IF EXISTS users;
 */

CREATE TABLE allele_specific_copy_number
(
    `mutation_event_id` Nullable(Int64),
    `genetic_profile_id` Nullable(Int64),
    `sample_id` Nullable(Int64),
    `ascn_integer_copy_number` Nullable(Int64),
    `ascn_method` Nullable(String),
    `ccf_expected_copies_upper` Nullable(Float64),
    `ccf_expected_copies` Nullable(Float64),
    `clonal` Nullable(String),
    `minor_copy_number` Nullable(Int64),
    `expected_alt_copies` Nullable(Int64),
    `total_copy_number` Nullable(Int64)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE alteration_driver_annotation
(
    `alteration_event_id` Nullable(Int64),
    `genetic_profile_id` Nullable(Int64),
    `sample_id` Nullable(Int64),
    `driver_filter` Nullable(String),
    `driver_filter_annotation` Nullable(String),
    `driver_tiers_filter` Nullable(String),
    `driver_tiers_filter_annotation` Nullable(String)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE authorities
(
    `email` Nullable(String),
    `authority` Nullable(String)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE cancer_study
(
    `cancer_study_id` Nullable(Int64),
    `cancer_study_identifier` Nullable(String),
    `type_of_cancer_id` Nullable(String),
    `name` Nullable(String),
    `description` Nullable(String),
    `public` Nullable(Int32),
    `pmid` Nullable(String),
    `citation` Nullable(String),
    `groups` Nullable(String),
    `status` Nullable(Int64),
    `import_date` Nullable(DateTime64(6)),
    `reference_genome_id` Nullable(Int64)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE cancer_study_tags
(
    `cancer_study_id` Nullable(Int64),
    `tags` Nullable(String)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE clinical_attribute_meta
(
    `attr_id` Nullable(String),
    `display_name` Nullable(String),
    `description` Nullable(String),
    `datatype` Nullable(String),
    `patient_attribute` Nullable(Int32),
    `priority` Nullable(String),
    `cancer_study_id` Nullable(Int64)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE clinical_data_derived
(
    `internal_id` Int32,
    `sample_unique_id` String,
    `patient_unique_id` String,
    `attribute_name` LowCardinality(String),
    `attribute_value` String,
    `cancer_study_identifier` LowCardinality(String),
    `type` LowCardinality(String)
)
    ENGINE = MergeTree
ORDER BY (type, attribute_name, sample_unique_id);

CREATE TABLE clinical_event
(
    `clinical_event_id` Nullable(Int64),
    `patient_id` Nullable(Int64),
    `start_date` Nullable(Int64),
    `stop_date` Nullable(Int64),
    `event_type` Nullable(String)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE clinical_event_data
(
    `clinical_event_id` Nullable(Int64),
    `key` Nullable(String),
    `value` Nullable(String)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE clinical_event_derived
(
    `patient_unique_id` String,
    `key` String,
    `value` String,
    `start_date` Int32,
    `stop_date` Int32 DEFAULT 0,
    `event_type` LowCardinality(String),
    `cancer_study_identifier` LowCardinality(String)
)
    ENGINE = MergeTree
ORDER BY (event_type, patient_unique_id, cancer_study_identifier);

CREATE TABLE clinical_patient
(
    `internal_id` Int64,
    `attr_id` String,
    `attr_value` String
)
    ENGINE = ReplacingMergeTree
ORDER BY tuple(`internal_id`, `attr_id`);

CREATE TABLE clinical_sample
(
    `internal_id` Int64,
    `attr_id` String,
    `attr_value` String
)
    ENGINE = ReplacingMergeTree
ORDER BY tuple(`internal_id`, `attr_id`);

CREATE TABLE cna_event
(
    `cna_event_id` Nullable(Int64),
    `entrez_gene_id` Nullable(Int64),
    `alteration` Nullable(Int32)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE copy_number_seg
(
    `seg_id` Nullable(Int64),
    `cancer_study_id` Nullable(Int64),
    `sample_id` Nullable(Int64),
    `chr` Nullable(String),
    `start` Nullable(Int64),
    `end` Nullable(Int64),
    `num_probes` Nullable(Int64),
    `segment_mean` Nullable(Float64)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE copy_number_seg_file
(
    `seg_file_id` Nullable(Int64),
    `cancer_study_id` Nullable(Int64),
    `reference_genome_id` Nullable(String),
    `description` Nullable(String),
    `filename` Nullable(String)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE data_access_tokens
(
    `token` Nullable(String),
    `username` Nullable(String),
    `expiration` Nullable(DateTime64(6)),
    `creation` Nullable(DateTime64(6))
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE gene
(
    `entrez_gene_id` Nullable(Int64),
    `hugo_gene_symbol` Nullable(String),
    `genetic_entity_id` Nullable(Int64),
    `type` Nullable(String)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE gene_alias
(
    `entrez_gene_id` Nullable(Int64),
    `gene_alias` Nullable(String)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE gene_panel
(
    `internal_id` Nullable(Int64),
    `stable_id` Nullable(String),
    `description` Nullable(String)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE gene_panel_list
(
    `internal_id` Nullable(Int64),
    `gene_id` Nullable(Int64)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE gene_panel_to_gene_derived
(
    `gene_panel_id` LowCardinality(String),
    `gene` String
)
    ENGINE = MergeTree
ORDER BY gene_panel_id;

CREATE TABLE generic_assay_data_derived
(
    `sample_unique_id` String,
    `patient_unique_id` String,
    `genetic_entity_id` String,
    `value` String,
    `generic_assay_type` String,
    `profile_stable_id` String,
    `entity_stable_id` String,
    `datatype` String,
    `patient_level` Decimal(10, 0),
    `profile_type` String
)
    ENGINE = MergeTree
ORDER BY (profile_type, entity_stable_id, patient_unique_id, sample_unique_id);

CREATE TABLE generic_entity_properties
(
    `id` Nullable(Int64),
    `genetic_entity_id` Nullable(Int64),
    `name` Nullable(String),
    `value` Nullable(String)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE geneset
(
    `id` Nullable(Int64),
    `genetic_entity_id` Nullable(Int64),
    `external_id` Nullable(String),
    `name` Nullable(String),
    `description` Nullable(String),
    `ref_link` Nullable(String)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE geneset_gene
(
    `geneset_id` Nullable(Int64),
    `entrez_gene_id` Nullable(Int64)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE geneset_hierarchy_leaf
(
    `node_id` Nullable(Int64),
    `geneset_id` Nullable(Int64)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE geneset_hierarchy_node
(
    `node_id` Nullable(Int64),
    `node_name` Nullable(String),
    `parent_id` Nullable(Int64)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE genetic_alteration
(
    `genetic_profile_id` Int64,
    `genetic_entity_id` Int64,
    `values` Nullable(String)
)
    ENGINE = ReplacingMergeTree
ORDER BY tuple(`genetic_profile_id`, `genetic_entity_id`);

CREATE TABLE genetic_alteration_derived
(
    `sample_unique_id` String,
    `cancer_study_identifier` LowCardinality(String),
    `hugo_gene_symbol` String,
    `profile_type` LowCardinality(String),
    `alteration_value` Nullable(String)
)
    ENGINE = MergeTree
ORDER BY (cancer_study_identifier, hugo_gene_symbol, profile_type, sample_unique_id);

CREATE TABLE genetic_entity
(
    `id` Nullable(Int64),
    `entity_type` Nullable(String),
    `stable_id` Nullable(String)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE genetic_profile
(
    `genetic_profile_id` Nullable(Int64),
    `stable_id` Nullable(String),
    `cancer_study_id` Nullable(Int64),
    `genetic_alteration_type` Nullable(String),
    `generic_assay_type` Nullable(String),
    `datatype` Nullable(String),
    `name` Nullable(String),
    `description` Nullable(String),
    `show_profile_in_analysis_tab` Nullable(Int32),
    `pivot_threshold` Nullable(Float64),
    `sort_order` Nullable(String),
    `patient_level` Nullable(Int32)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE genetic_profile_link
(
    `referring_genetic_profile_id` Nullable(Int64),
    `referred_genetic_profile_id` Nullable(Int64),
    `reference_type` Nullable(String)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE genetic_profile_samples
(
    `genetic_profile_id` Int64,
    `ordered_sample_list` Nullable(String)
)
    ENGINE = ReplacingMergeTree
ORDER BY tuple(`genetic_profile_id`);

CREATE TABLE genomic_event_derived
(
    `sample_unique_id` String,
    `hugo_gene_symbol` String,
    `entrez_gene_id` Int32,
    `gene_panel_stable_id` LowCardinality(String),
    `cancer_study_identifier` LowCardinality(String),
    `genetic_profile_stable_id` LowCardinality(String),
    `variant_type` LowCardinality(String),
    `mutation_variant` String,
    `mutation_type` LowCardinality(String),
    `mutation_status` LowCardinality(String),
    `driver_filter` LowCardinality(String),
    `driver_tiers_filter` LowCardinality(String),
    `cna_alteration` Nullable(Int8),
    `cna_cytoband` String,
    `sv_event_info` String,
    `patient_unique_id` String,
    `off_panel` Bool DEFAULT false
)
    ENGINE = MergeTree
ORDER BY (variant_type, entrez_gene_id, hugo_gene_symbol, genetic_profile_stable_id, sample_unique_id);

CREATE TABLE gistic
(
    `gistic_roi_id` Nullable(Int64),
    `cancer_study_id` Nullable(Int64),
    `chromosome` Nullable(Int64),
    `cytoband` Nullable(String),
    `wide_peak_start` Nullable(Int64),
    `wide_peak_end` Nullable(Int64),
    `q_value` Nullable(Float64),
    `amp` Nullable(Int32)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE gistic_to_gene
(
    `gistic_roi_id` Nullable(Int64),
    `entrez_gene_id` Nullable(Int64)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE info
(
    `db_schema_version` Nullable(String),
    `geneset_version` Nullable(String)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE mut_sig
(
    `cancer_study_id` Nullable(Int64),
    `entrez_gene_id` Nullable(Int64),
    `rank` Nullable(Int64),
    `NumBasesCovered` Nullable(Int64),
    `NumMutations` Nullable(Int64),
    `p_value` Nullable(Float64),
    `q_value` Nullable(Float64)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE mutation
(
    `mutation_event_id` Nullable(Int64),
    `genetic_profile_id` Nullable(Int64),
    `sample_id` Nullable(Int64),
    `entrez_gene_id` Nullable(Int64),
    `center` Nullable(String),
    `sequencer` Nullable(String),
    `mutation_status` Nullable(String),
    `validation_status` Nullable(String),
    `tumor_seq_allele1` Nullable(String),
    `tumor_seq_allele2` Nullable(String),
    `matched_norm_sample_barcode` Nullable(String),
    `match_norm_seq_allele1` Nullable(String),
    `match_norm_seq_allele2` Nullable(String),
    `tumor_validation_allele1` Nullable(String),
    `tumor_validation_allele2` Nullable(String),
    `match_norm_validation_allele1` Nullable(String),
    `match_norm_validation_allele2` Nullable(String),
    `verification_status` Nullable(String),
    `sequencing_phase` Nullable(String),
    `sequence_source` Nullable(String),
    `validation_method` Nullable(String),
    `score` Nullable(String),
    `bam_file` Nullable(String),
    `tumor_alt_count` Nullable(Int64),
    `tumor_ref_count` Nullable(Int64),
    `normal_alt_count` Nullable(Int64),
    `normal_ref_count` Nullable(Int64),
    `amino_acid_change` Nullable(String),
    `annotation_json` Nullable(String)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE mutation_count_by_keyword
(
    `genetic_profile_id` Nullable(Int64),
    `keyword` Nullable(String),
    `entrez_gene_id` Nullable(Int64),
    `keyword_count` Nullable(Int64),
    `gene_count` Nullable(Int64)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE mutation_event
(
    `mutation_event_id` Nullable(Int64),
    `entrez_gene_id` Nullable(Int64),
    `chr` Nullable(String),
    `start_position` Nullable(Int64),
    `end_position` Nullable(Int64),
    `reference_allele` Nullable(String),
    `tumor_seq_allele` Nullable(String),
    `protein_change` Nullable(String),
    `mutation_type` Nullable(String),
    `ncbi_build` Nullable(String),
    `strand` Nullable(String),
    `variant_type` Nullable(String),
    `db_snp_rs` Nullable(String),
    `db_snp_val_status` Nullable(String),
    `refseq_mrna_id` Nullable(String),
    `codon_change` Nullable(String),
    `uniprot_accession` Nullable(String),
    `protein_pos_start` Nullable(Int64),
    `protein_pos_end` Nullable(Int64),
    `canonical_transcript` Nullable(Int32),
    `keyword` Nullable(String)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE patient
(
    `internal_id` Nullable(Int64),
    `stable_id` Nullable(String),
    `cancer_study_id` Nullable(Int64)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE reference_genome
(
    `reference_genome_id` Nullable(Int64),
    `species` Nullable(String),
    `name` Nullable(String),
    `build_name` Nullable(String),
    `genome_size` Nullable(Int64),
    `url` Nullable(String),
    `release_date` Nullable(DateTime64(6))
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE reference_genome_gene
(
    `entrez_gene_id` Nullable(Int64),
    `reference_genome_id` Nullable(Int64),
    `chr` Nullable(String),
    `cytoband` Nullable(String),
    `start` Nullable(Int64),
    `end` Nullable(Int64)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE resource_definition
(
    `resource_id` Nullable(String),
    `display_name` Nullable(String),
    `description` Nullable(String),
    `resource_type` Nullable(String),
    `open_by_default` Nullable(Int32),
    `priority` Nullable(Int64),
    `cancer_study_id` Nullable(Int64),
    `custom_metadata` Nullable(String)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE resource
(
    `internal_id` Int64,
    `resource_id` String,
    `cancer_study_id` Int64,
    `resource_scope` String,
    `patient_id` Nullable(String),
    `sample_id` Nullable(String),
    `url` String,
    `display_name` Nullable(String),
    `metadata` Nullable(String)
)
    ENGINE = MergeTree
ORDER BY (cancer_study_id, resource_id, patient_id, sample_id, internal_id)
    SETTINGS allow_nullable_key = 1;

CREATE TABLE resource_patient
(
    `internal_id` Nullable(Int64),
    `resource_id` Nullable(String),
    `url` Nullable(String)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE resource_sample
(
    `internal_id` Nullable(Int64),
    `resource_id` Nullable(String),
    `url` Nullable(String)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE resource_study
(
    `internal_id` Nullable(Int64),
    `resource_id` Nullable(String),
    `url` Nullable(String)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE sample
(
    `internal_id` Nullable(Int64),
    `stable_id` Nullable(String),
    `sample_type` Nullable(String),
    `patient_id` Nullable(Int64)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE sample_cna_event
(
    `cna_event_id` Nullable(Int64),
    `sample_id` Nullable(Int64),
    `genetic_profile_id` Nullable(Int64),
    `annotation_json` Nullable(String)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE sample_derived
(
    `sample_unique_id` String,
    `sample_unique_id_base64` String,
    `sample_stable_id` String,
    `patient_unique_id` String,
    `patient_unique_id_base64` String,
    `patient_stable_id` String,
    `cancer_study_identifier` LowCardinality(String),
    `internal_id` Int32,
    `patient_internal_id` Int32,
    `sample_type` String,
    `sequenced` Int32,
    `copy_number_segment_present` Int32
)
    ENGINE = MergeTree
ORDER BY (cancer_study_identifier, sample_unique_id);

CREATE TABLE sample_list
(
    `list_id` Nullable(Int64),
    `stable_id` Nullable(String),
    `category` Nullable(String),
    `cancer_study_id` Nullable(Int64),
    `name` Nullable(String),
    `description` Nullable(String)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE sample_list_list
(
    `list_id` Nullable(Int64),
    `sample_id` Nullable(Int64)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE sample_profile
(
    `sample_id` Int64,
    `genetic_profile_id` Int64,
    `panel_id` Nullable(Int64)
)
    ENGINE = ReplacingMergeTree
ORDER BY tuple(`sample_id`, `genetic_profile_id`);

CREATE TABLE sample_to_gene_panel_derived
(
    `sample_unique_id` String,
    `alteration_type` LowCardinality(String),
    `gene_panel_id` LowCardinality(String),
    `cancer_study_identifier` LowCardinality(String),
    `genetic_profile_id` LowCardinality(String)
)
    ENGINE = MergeTree
ORDER BY (gene_panel_id, alteration_type, genetic_profile_id, sample_unique_id);

CREATE TABLE structural_variant
(
    `internal_id` Nullable(Int64),
    `genetic_profile_id` Nullable(Int64),
    `sample_id` Nullable(Int64),
    `site1_entrez_gene_id` Nullable(Int64),
    `site1_ensembl_transcript_id` Nullable(String),
    `site1_chromosome` Nullable(String),
    `site1_region` Nullable(String),
    `site1_region_number` Nullable(Int64),
    `site1_contig` Nullable(String),
    `site1_position` Nullable(Int64),
    `site1_description` Nullable(String),
    `site2_entrez_gene_id` Nullable(Int64),
    `site2_ensembl_transcript_id` Nullable(String),
    `site2_chromosome` Nullable(String),
    `site2_region` Nullable(String),
    `site2_region_number` Nullable(Int64),
    `site2_contig` Nullable(String),
    `site2_position` Nullable(Int64),
    `site2_description` Nullable(String),
    `site2_effect_on_frame` Nullable(String),
    `ncbi_build` Nullable(String),
    `dna_support` Nullable(String),
    `rna_support` Nullable(String),
    `normal_read_count` Nullable(Int64),
    `tumor_read_count` Nullable(Int64),
    `normal_variant_count` Nullable(Int64),
    `tumor_variant_count` Nullable(Int64),
    `normal_paired_end_read_count` Nullable(Int64),
    `tumor_paired_end_read_count` Nullable(Int64),
    `normal_split_read_count` Nullable(Int64),
    `tumor_split_read_count` Nullable(Int64),
    `annotation` Nullable(String),
    `breakpoint_type` Nullable(String),
    `connection_type` Nullable(String),
    `event_info` Nullable(String),
    `class` Nullable(String),
    `length` Nullable(Int64),
    `comments` Nullable(String),
    `sv_status` Nullable(String),
    `annotation_json` Nullable(String)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE type_of_cancer
(
    `type_of_cancer_id` Nullable(String),
    `name` Nullable(String),
    `dedicated_color` Nullable(String),
    `short_name` Nullable(String),
    `parent` Nullable(String)
)
    ENGINE = MergeTree
ORDER BY tuple();

CREATE TABLE users
(
    `email` Nullable(String),
    `name` Nullable(String),
    `enabled` Nullable(Int32)
)
    ENGINE = MergeTree
ORDER BY tuple();
