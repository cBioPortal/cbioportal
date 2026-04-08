--
-- Copyright (c) 2016 - 2026 Memorial Sloan Kettering Cancer Center.
--
-- This library is distributed in the hope that it will be useful, but WITHOUT
-- ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
-- FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
-- is on an "as is" basis, and Memorial Sloan Kettering Cancer Center has no
-- obligations to provide maintenance, support, updates, enhancements or
-- modifications. In no event shall Memorial Sloan Kettering Cancer Center be
-- liable to any party for direct, indirect, special, incidental or
-- consequential damages, including lost profits, arising out of the use of this
-- software and its documentation, even if Memorial Sloan Kettering Cancer
-- Center has been advised of the possibility of such damage.
--
-- This file is part of cBioPortal.
--
-- cBioPortal is free software: you can redistribute it and/or modify
-- it under the terms of the GNU Affero General Public License as
-- published by the Free Software Foundation, either version 3 of the
-- License.
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU Affero General Public License for more details.
--
-- You should have received a copy of the GNU Affero General Public License
-- along with this program.  If not, see <http://www.gnu.org/licenses/>.
-- ----------------------------------------------------------------------------
--
-- This cBioPortal database table schema initialization script will set up
-- the essential database tables for cBioPortal. The database name is not
-- specified in this file. Near the end of this script, the table named
-- 'info' is populated with a single record which includes setting a value
-- for property db_schema_version, which corresponds to the version number
-- of this file.
--
-- This file is intended for use with a ClickHouse DMBS, and uses ClickHouse
-- specific specifications, such as setting ENGINE types and ORDER BY
-- specifications for tables. These are needed for good performance during
-- cBioPortal web service functionality and data import functionality.
--
-- --------------------------------------------------------
DROP TABLE IF EXISTS allele_specific_copy_number;
DROP TABLE IF EXISTS alteration_driver_annotation;
DROP TABLE IF EXISTS authorities;
DROP TABLE IF EXISTS cancer_study;
DROP TABLE IF EXISTS cancer_study_tags;
DROP TABLE IF EXISTS clinical_attribute_meta;
DROP TABLE IF EXISTS clinical_event;
DROP TABLE IF EXISTS clinical_event_data;
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
DROP TABLE IF EXISTS generic_entity_properties;
DROP TABLE IF EXISTS geneset;
DROP TABLE IF EXISTS geneset_gene;
DROP TABLE IF EXISTS geneset_hierarchy_leaf;
DROP TABLE IF EXISTS geneset_hierarchy_node;
DROP TABLE IF EXISTS genetic_alteration;
DROP TABLE IF EXISTS genetic_entity;
DROP TABLE IF EXISTS genetic_profile;
DROP TABLE IF EXISTS genetic_profile_link;
DROP TABLE IF EXISTS genetic_profile_samples;
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
DROP TABLE IF EXISTS resource_definition;
DROP TABLE IF EXISTS resource_patient;
DROP TABLE IF EXISTS resource_sample;
DROP TABLE IF EXISTS resource_study;
DROP TABLE IF EXISTS sample;
DROP TABLE IF EXISTS sample_cna_event;
DROP TABLE IF EXISTS sample_list;
DROP TABLE IF EXISTS sample_list_list;
DROP TABLE IF EXISTS sample_profile;
DROP TABLE IF EXISTS structural_variant;
DROP TABLE IF EXISTS type_of_cancer;
DROP TABLE IF EXISTS users;

CREATE TABLE allele_specific_copy_number (
    `mutation_event_id` Int64,
    `genetic_profile_id` Int64,
    `sample_id` Int64,
    `ascn_integer_copy_number` Nullable(Int64),
    `ascn_method` String,
    `ccf_expected_copies_upper` Nullable(Float64),
    `ccf_expected_copies` Nullable(Float64),
    `clonal` Nullable(String),
    `minor_copy_number` Nullable(Int64),
    `expected_alt_copies` Nullable(Int64),
    `total_copy_number` Nullable(Int64)
) ENGINE = MergeTree ORDER BY (mutation_event_id, genetic_profile_id, sample_id);

CREATE TABLE alteration_driver_annotation (
    `alteration_event_id` Int64,
    `genetic_profile_id` Int64,
    `sample_id` Int64,
    `driver_filter` Nullable(String),
    `driver_filter_annotation` Nullable(String),
    `driver_tiers_filter` Nullable(String),
    `driver_tiers_filter_annotation` Nullable(String)
) ENGINE = MergeTree ORDER BY (alteration_event_id, genetic_profile_id, sample_id);

CREATE TABLE authorities (
    `email` String,
    `authority` String
) ENGINE = MergeTree ORDER BY (email);

CREATE TABLE cancer_study (
    `cancer_study_id` Int64,
    `cancer_study_identifier` Nullable(String),
    `type_of_cancer_id` String,
    `name` String,
    `description` String,
    `public` Int32,
    `pmid` Nullable(String),
    `citation` Nullable(String),
    `groups` Nullable(String),
    `status` Nullable(Int64),
    `import_date` Nullable(DateTime64(6)),
    `reference_genome_id` Nullable(Int64)
) ENGINE = MergeTree ORDER BY (cancer_study_id);

CREATE TABLE cancer_study_tags (
    `cancer_study_id` Int64,
    `tags` String
) ENGINE = MergeTree ORDER BY (cancer_study_id);

CREATE TABLE clinical_attribute_meta (
    `attr_id` String,
    `display_name` String,
    `description` String,
    `datatype` String,
    `patient_attribute` Int32,
    `priority` String,
    `cancer_study_id` Int64
) ENGINE = MergeTree ORDER BY (attr_id, cancer_study_id);

CREATE TABLE clinical_event (
    `clinical_event_id` Int64,
    `patient_id` Int64,
    `start_date` Int64,
    `stop_date` Nullable(Int64),
    `event_type` String
) ENGINE = MergeTree ORDER BY (clinical_event_id);

CREATE TABLE clinical_event_data (
    `clinical_event_id` Int64,
    `key` String,
    `value` String
) ENGINE = MergeTree ORDER BY (clinical_event_id);

CREATE TABLE clinical_patient (
    `internal_id` Int64,
    `attr_id` String,
    `attr_value` String
) ENGINE = ReplacingMergeTree ORDER BY (internal_id, attr_id);

CREATE TABLE clinical_sample (
    `internal_id` Int64,
    `attr_id` String,
    `attr_value` String
) ENGINE = ReplacingMergeTree ORDER BY (internal_id, attr_id);

CREATE TABLE cna_event (
    `cna_event_id` Int64,
    `entrez_gene_id` Int64,
    `alteration` Int32
) ENGINE = MergeTree ORDER BY (cna_event_id);

CREATE TABLE copy_number_seg (
    `seg_id` Int64,
    `cancer_study_id` Int64,
    `sample_id` Int64,
    `chr` String,
    `start` Int64,
    `end` Int64,
    `num_probes` Int64,
    `segment_mean` Float64
) ENGINE = MergeTree ORDER BY (seg_id);

CREATE TABLE copy_number_seg_file (
    `seg_file_id` Int64,
    `cancer_study_id` Int64,
    `reference_genome_id` String,
    `description` String,
    `filename` String
) ENGINE = MergeTree ORDER BY (seg_file_id);

CREATE TABLE data_access_tokens (
    `token` String,
    `username` String,
    `expiration` DateTime64(6),
    `creation` DateTime64(6)
) ENGINE = MergeTree ORDER BY (token);

CREATE TABLE gene (
    `entrez_gene_id` Int64,
    `hugo_gene_symbol` String,
    `genetic_entity_id` Int64,
    `type` Nullable(String)
) ENGINE = MergeTree ORDER BY (entrez_gene_id);

CREATE TABLE gene_alias (
    `entrez_gene_id` Int64,
    `gene_alias` String
) ENGINE = MergeTree ORDER BY (entrez_gene_id, gene_alias);

CREATE TABLE gene_panel (
    `internal_id` Int64,
    `stable_id` String,
    `description` Nullable(String)
) ENGINE = MergeTree ORDER BY (internal_id);

CREATE TABLE gene_panel_list (
    `internal_id` Int64,
    `gene_id` Int64
) ENGINE = MergeTree ORDER BY (internal_id, gene_id);

CREATE TABLE generic_entity_properties (
    `id` Int64,
    `genetic_entity_id` Int64,
    `name` String,
    `value` String
) ENGINE = MergeTree ORDER BY (id);

CREATE TABLE geneset (
    `id` Int64,
    `genetic_entity_id` Int64,
    `external_id` String,
    `name` String,
    `description` String,
    `ref_link` Nullable(String)
) ENGINE = MergeTree ORDER BY (id);

CREATE TABLE geneset_gene (
    `geneset_id` Int64,
    `entrez_gene_id` Int64
) ENGINE = MergeTree ORDER BY (geneset_id, entrez_gene_id);

CREATE TABLE geneset_hierarchy_leaf (
    `node_id` Int64,
    `geneset_id` Int64
) ENGINE = MergeTree ORDER BY (node_id, geneset_id);

CREATE TABLE geneset_hierarchy_node (
    `node_id` Int64,
    `node_name` String,
    `parent_id` Nullable(Int64)
) ENGINE = MergeTree ORDER BY (node_id);

CREATE TABLE genetic_alteration (
    `genetic_profile_id` Int64,
    `genetic_entity_id` Int64,
    `values` String
) ENGINE = ReplacingMergeTree ORDER BY (genetic_profile_id, genetic_entity_id);

CREATE TABLE genetic_entity (
    `id` Int64,
    `entity_type` String,
    `stable_id` Nullable(String)
) ENGINE = MergeTree ORDER BY (id);

CREATE TABLE genetic_profile (
    `genetic_profile_id` Int64,
    `stable_id` String,
    `cancer_study_id` Int64,
    `genetic_alteration_type` String,
    `generic_assay_type` Nullable(String),
    `datatype` String,
    `name` String,
    `description` Nullable(String),
    `show_profile_in_analysis_tab` Int32,
    `pivot_threshold` Nullable(Float64),
    `sort_order` Nullable(String),
    `patient_level` Nullable(Int32)
) ENGINE = MergeTree ORDER BY (genetic_profile_id);

CREATE TABLE genetic_profile_link (
    `referring_genetic_profile_id` Int64,
    `referred_genetic_profile_id` Int64,
    `reference_type` Nullable(String)
) ENGINE = MergeTree ORDER BY (referring_genetic_profile_id, referred_genetic_profile_id);

CREATE TABLE genetic_profile_samples (
    `genetic_profile_id` Int64,
    `ordered_sample_list` String
) ENGINE = ReplacingMergeTree ORDER BY (genetic_profile_id);

CREATE TABLE gistic (
    `gistic_roi_id` Int64,
    `cancer_study_id` Int64,
    `chromosome` Int64,
    `cytoband` String,
    `wide_peak_start` Int64,
    `wide_peak_end` Int64,
    `q_value` Float64,
    `amp` Int32
) ENGINE = MergeTree ORDER BY (gistic_roi_id);

CREATE TABLE gistic_to_gene (
    `gistic_roi_id` Int64,
    `entrez_gene_id` Int64
) ENGINE = MergeTree ORDER BY (gistic_roi_id, entrez_gene_id);

CREATE TABLE info (
    `db_schema_version` Nullable(String),
    `geneset_version` Nullable(String),
    `derived_table_schema_version` Nullable(String),
    `gene_table_version` Nullable(String)
) ENGINE = MergeTree ORDER BY tuple();

CREATE TABLE mut_sig (
    `cancer_study_id` Int64,
    `entrez_gene_id` Int64,
    `rank` Int64,
    `NumBasesCovered` Int64,
    `NumMutations` Int64,
    `p_value` Float64,
    `q_value` Float64
) ENGINE = MergeTree ORDER BY (cancer_study_id, entrez_gene_id);

CREATE TABLE mutation (
    `mutation_event_id` Int64 COMMENT 'References mutation_event.mutation_event_id.',
    `genetic_profile_id` Int64 COMMENT 'References genetic_profile.genetic_profile_id.',
    `sample_id` Int64 COMMENT 'References sample.internal_id.',
    `entrez_gene_id` Int64 COMMENT 'References gene.entrez_gene_id.',
    `center` Nullable(String) COMMENT 'Center where sequencing was performed.',
    `sequencer` Nullable(String) COMMENT 'Sequencing platform used.',
    `mutation_status` Nullable(String) COMMENT 'Mutation status: Germline,
    Somatic,
    or LOH.',
    `validation_status` Nullable(String) COMMENT 'Validation status.',
    `tumor_seq_allele1` Nullable(String) COMMENT 'Tumor allele 1 sequence.',
    `tumor_seq_allele2` Nullable(String) COMMENT 'Tumor allele 2 sequence.',
    `matched_norm_sample_barcode` Nullable(String) COMMENT 'Matched normal sample barcode.',
    `match_norm_seq_allele1` Nullable(String) COMMENT 'Matched normal allele 1 sequence.',
    `match_norm_seq_allele2` Nullable(String) COMMENT 'Matched normal allele 2 sequence.',
    `tumor_validation_allele1` Nullable(String) COMMENT 'Tumor validation allele 1 sequence.',
    `tumor_validation_allele2` Nullable(String) COMMENT 'Tumor validation allele 2 sequence.',
    `match_norm_validation_allele1` Nullable(String) COMMENT 'Matched normal validation allele 1.',
    `match_norm_validation_allele2` Nullable(String) COMMENT 'Matched normal validation allele 2.',
    `verification_status` Nullable(String) COMMENT 'Verification status.',
    `sequencing_phase` Nullable(String) COMMENT 'Sequencing phase.',
    `sequence_source` String COMMENT 'Source of sequencing data.',
    `validation_method` Nullable(String) COMMENT 'Validation method used.',
    `score` Nullable(String) COMMENT 'Score or quality metric.',
    `bam_file` Nullable(String) COMMENT 'Associated BAM file.',
    `tumor_alt_count` Nullable(Int64) COMMENT 'Tumor alternate allele count.',
    `tumor_ref_count` Nullable(Int64) COMMENT 'Tumor reference allele count.',
    `normal_alt_count` Nullable(Int64) COMMENT 'Normal alternate allele count.',
    `normal_ref_count` Nullable(Int64) COMMENT 'Normal reference allele count.',
    `amino_acid_change` Nullable(String) COMMENT 'Amino acid change from mutation.',
    `annotation_json` Nullable(String) COMMENT 'JSON-formatted annotations.'
) ENGINE = MergeTree ORDER BY (genetic_profile_id, entrez_gene_id) COMMENT 'Mutation observations in specific samples and profiles. References mutation_event, gene, genetic_profile, and sample.';

CREATE TABLE mutation_count_by_keyword (
    `genetic_profile_id` Int64,
    `keyword` Nullable(String),
    `entrez_gene_id` Int64,
    `keyword_count` Int64,
    `gene_count` Int64
) ENGINE = MergeTree ORDER BY (genetic_profile_id, entrez_gene_id);

CREATE TABLE mutation_event (
    `mutation_event_id` Int64,
    `entrez_gene_id` Int64,
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
) ENGINE = MergeTree ORDER BY (mutation_event_id);

CREATE TABLE patient (
    `internal_id` Int64,
    `stable_id` String,
    `cancer_study_id` Int64
) ENGINE = MergeTree ORDER BY (internal_id);

CREATE TABLE reference_genome (
    `reference_genome_id` Int64,
    `species` String,
    `name` String,
    `build_name` String,
    `genome_size` Nullable(Int64),
    `url` String,
    `release_date` Nullable(DateTime64(6))
) ENGINE = MergeTree ORDER BY (reference_genome_id);

CREATE TABLE reference_genome_gene (
    `entrez_gene_id` Int64,
    `reference_genome_id` Int64,
    `chr` Nullable(String),
    `cytoband` Nullable(String),
    `start` Nullable(Int64),
    `end` Nullable(Int64)
) ENGINE = MergeTree ORDER BY (entrez_gene_id, reference_genome_id);

CREATE TABLE resource_definition (
    `resource_id` String,
    `display_name` String,
    `description` Nullable(String),
    `resource_type` String,
    `open_by_default` Nullable(Int32),
    `priority` Int64,
    `cancer_study_id` Int64,
    `custom_metadata` Nullable(String)
) ENGINE = MergeTree ORDER BY (resource_id, cancer_study_id);

CREATE TABLE resource_patient (
    `internal_id` Int64,
    `resource_id` String,
    `url` String
) ENGINE = MergeTree ORDER BY (internal_id, resource_id, url);

CREATE TABLE resource_sample (
    `internal_id` Int64,
    `resource_id` String,
    `url` String
) ENGINE = MergeTree ORDER BY (internal_id, resource_id, url);

CREATE TABLE resource_study (
    `internal_id` Int64,
    `resource_id` String,
    `url` String
) ENGINE = MergeTree ORDER BY (internal_id, resource_id, url);

CREATE TABLE sample (
    `internal_id` Int64,
    `stable_id` String,
    `sample_type` String,
    `patient_id` Int64
) ENGINE = MergeTree ORDER BY (internal_id);

CREATE TABLE sample_cna_event (
    `cna_event_id` Int64 COMMENT 'References cna_event.cna_event_id.',
    `sample_id` Int64 COMMENT 'References sample.internal_id.',
    `genetic_profile_id` Int64 COMMENT 'References genetic_profile.genetic_profile_id.',
    `annotation_json` Nullable(String) COMMENT 'JSON-formatted annotation details.'
) ENGINE = MergeTree PRIMARY KEY (genetic_profile_id, cna_event_id, sample_id) ORDER BY (genetic_profile_id, cna_event_id, sample_id) COMMENT 'Observed CNA events per sample and profile. References cna_event, sample, and genetic_profile.';

CREATE TABLE sample_list (
    `list_id` Int64,
    `stable_id` String,
    `category` String,
    `cancer_study_id` Int64,
    `name` String,
    `description` Nullable(String)
) ENGINE = MergeTree ORDER BY (list_id);

CREATE TABLE sample_list_list (
    `list_id` Int64,
    `sample_id` Int64
) ENGINE = MergeTree ORDER BY (list_id, sample_id);

CREATE TABLE sample_profile (
    `sample_id` Int64,
    `genetic_profile_id` Int64,
    `panel_id` Nullable(Int64)
) ENGINE = ReplacingMergeTree ORDER BY (sample_id, genetic_profile_id);

CREATE TABLE structural_variant (
    `internal_id` Int64,
    `genetic_profile_id` Int64,
    `sample_id` Int64,
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
    `sv_status` String,
    `annotation_json` Nullable(String)
) ENGINE = MergeTree ORDER BY (`sample_id`, `genetic_profile_id`);

CREATE TABLE type_of_cancer (
    `type_of_cancer_id` String,
    `name` String,
    `dedicated_color` String,
    `short_name` Nullable(String),
    `parent` Nullable(String)
) ENGINE = MergeTree ORDER BY (type_of_cancer_id);

CREATE TABLE users (
    `email` String,
    `name` String,
    `enabled` Int32
) ENGINE = MergeTree ORDER BY (email);

INSERT INTO info (`db_schema_version`, `geneset_version`, `derived_table_schema_version`, `gene_table_version`) VALUES ('2.14.5', 'msigdb_v2025.1.Hs', '1.0.9', 'hgnc_v7_2025.10.7');
