--
-- Copyright (c) 2016 - 2022 Memorial Sloan Kettering Cancer Center.
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
-- Database: `cgds`
--
-- --------------------------------------------------------
-- Database table schemas and version number
-- --------------------------------------------------------
-- The order of the following DROP TABLE statements is
-- significant. If a table has a FOREIGN_KEY referring to
-- another table, then it should be dropped before the
-- other table is dropped. The approach taken here is to
-- order the CREATE TABLE statements so that refererenced
-- tables are created before referring tables.
-- DROP TABLE statements are here in the reverse order.
-- --------------------------------------------------------

DROP TABLE IF EXISTS `resource_study`;
DROP TABLE IF EXISTS `resource_patient`;
DROP TABLE IF EXISTS `resource_sample`;
DROP TABLE IF EXISTS `resource_definition`;
DROP TABLE IF EXISTS `info`;
DROP TABLE IF EXISTS `allele_specific_copy_number`;
DROP TABLE IF EXISTS `data_access_tokens`;
DROP TABLE IF EXISTS `reference_genome_gene`;
DROP TABLE IF EXISTS `clinical_event_data`;
DROP TABLE IF EXISTS `clinical_event`;
DROP TABLE IF EXISTS `cosmic_mutation`;
DROP TABLE IF EXISTS `copy_number_seg_file`;
DROP TABLE IF EXISTS `copy_number_seg`;
DROP TABLE IF EXISTS `sample_cna_event`;
DROP TABLE IF EXISTS `cna_event`;
DROP TABLE IF EXISTS `gistic_to_gene`;
DROP TABLE IF EXISTS `gistic`;
DROP TABLE IF EXISTS `mut_sig`;
DROP TABLE IF EXISTS `clinical_attribute_meta`;
DROP TABLE IF EXISTS `clinical_sample`;
DROP TABLE IF EXISTS `clinical_patient`;
DROP TABLE IF EXISTS `mutation_count_by_keyword`;
DROP TABLE IF EXISTS `mutation`;
DROP TABLE IF EXISTS `mutation_event`;
DROP TABLE IF EXISTS `alteration_driver_annotation`;
DROP TABLE IF EXISTS `structural_variant`;
DROP TABLE IF EXISTS `sample_profile`;
DROP TABLE IF EXISTS `gene_panel_list`;
DROP TABLE IF EXISTS `gene_panel`;
DROP TABLE IF EXISTS `genetic_profile_samples`;
DROP TABLE IF EXISTS `genetic_alteration`;
DROP TABLE IF EXISTS `genetic_profile_link`;
DROP TABLE IF EXISTS `genetic_profile`;
DROP TABLE IF EXISTS `generic_entity_properties`;
DROP TABLE IF EXISTS `geneset_hierarchy_leaf`;
DROP TABLE IF EXISTS `geneset_hierarchy_node`;
DROP TABLE IF EXISTS `geneset_gene`;
DROP TABLE IF EXISTS `geneset`;
DROP TABLE IF EXISTS `gene_alias`;
DROP TABLE IF EXISTS `gene`;
DROP TABLE IF EXISTS `genetic_entity`;
DROP TABLE IF EXISTS `sample_list_list`;
DROP TABLE IF EXISTS `sample_list`;
DROP TABLE IF EXISTS `sample`;
DROP TABLE IF EXISTS `patient`;
DROP TABLE IF EXISTS `authorities`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `cancer_study_tags`;
DROP TABLE IF EXISTS `cancer_study`;
DROP TABLE IF EXISTS `reference_genome`;

-- --------------------------------------------------------
CREATE TABLE `type_of_cancer` (
  `type_of_cancer_id` varchar(63) NOT NULL,
  `name` varchar(255) NOT NULL,
  `dedicated_color` char(31) NOT NULL,
  `short_name` varchar(127) DEFAULT NULL,
  `parent` varchar(63) DEFAULT NULL,
  PRIMARY KEY (`type_of_cancer_id`)
);

-- --------------------------------------------------------
CREATE TABLE `reference_genome` (
  `reference_genome_id` int(4) NOT NULL AUTO_INCREMENT,
  `species` varchar(64) NOT NULL,
  `name` varchar(64) NOT NULL,
  `build_name` varchar(64) NOT NULL,
  `genome_size` bigint(20) DEFAULT NULL,
  `url` varchar(256) NOT NULL,
  `release_date` datetime DEFAULT NULL,
  PRIMARY KEY (`reference_genome_id`),
  UNIQUE KEY `build_name_unique` (`build_name`)
);

-- --------------------------------------------------------
CREATE TABLE `cancer_study` (
  `cancer_study_id` int(11) NOT NULL AUTO_INCREMENT,
  `cancer_study_identifier` varchar(255) DEFAULT NULL,
  `type_of_cancer_id` varchar(63) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` varchar(1024) NOT NULL,
  `public` tinyint(1) NOT NULL,
  `pmid` varchar(1024) DEFAULT NULL,
  `citation` varchar(200) DEFAULT NULL,
  `groups` varchar(200) DEFAULT NULL,
  `status` int(1) DEFAULT NULL,
  `import_date` datetime DEFAULT NULL,
  `reference_genome_id` int(4) DEFAULT '1',
  PRIMARY KEY (`cancer_study_id`),
  UNIQUE KEY (`cancer_study_identifier`),
  FOREIGN KEY (`type_of_cancer_id`) REFERENCES `type_of_cancer` (`type_of_cancer_id`),
  FOREIGN KEY (`reference_genome_id`) REFERENCES `reference_genome` (`reference_genome_id`) ON DELETE RESTRICT
);

-- --------------------------------------------------------
CREATE TABLE `cancer_study_tags` (
  `cancer_study_id` int(11) NOT NULL,
  `tags` text NOT NULL,
  PRIMARY KEY (`cancer_study_id`),
  FOREIGN KEY (`cancer_study_id`) REFERENCES `cancer_study` (`cancer_study_id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `users` (
  `email` varchar(128) NOT NULL,
  `name` varchar(255) NOT NULL,
  `enabled` tinyint(1) NOT NULL,
  PRIMARY KEY (`email`)
);

-- --------------------------------------------------------
CREATE TABLE `authorities` (
  `email` varchar(128) NOT NULL,
  `authority` varchar(255) NOT NULL
);

-- --------------------------------------------------------
CREATE TABLE `patient` (
  `internal_id` int(11) NOT NULL AUTO_INCREMENT,
  `stable_id` varchar(50) NOT NULL,
  `cancer_study_id` int(11) NOT NULL,
  PRIMARY KEY (`internal_id`),
  FOREIGN KEY (`cancer_study_id`) REFERENCES `cancer_study` (`cancer_study_id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `sample` (
  `internal_id` int(11) NOT NULL AUTO_INCREMENT,
  `stable_id` varchar(63) NOT NULL,
  `sample_type` varchar(255) NOT NULL,
  `patient_id` int(11) NOT NULL,
  PRIMARY KEY (`internal_id`),
  FOREIGN KEY (`patient_id`) REFERENCES `patient` (`internal_id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `sample_list` (
  `list_id` int(11) NOT NULL AUTO_INCREMENT,
  `stable_id` varchar(255) NOT NULL,
  `category` varchar(255) NOT NULL,
  `cancer_study_id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` mediumtext,
  PRIMARY KEY (`list_id`),
  UNIQUE KEY (`stable_id`),
  FOREIGN KEY (`cancer_study_id`) REFERENCES `cancer_study` (`cancer_study_id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `sample_list_list` (
  `list_id` int(11) NOT NULL,
  `sample_id` int(11) NOT NULL,
  PRIMARY KEY (`list_id`,`sample_id`),
  FOREIGN KEY (`sample_id`) REFERENCES `sample` (`internal_id`) ON DELETE CASCADE
);

-- --------------------------------------------------------

CREATE TABLE `genetic_entity` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `entity_type` varchar(45) NOT NULL,
  `stable_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

-- --------------------------------------------------------
CREATE TABLE `gene` (
  `entrez_gene_id` int(11) NOT NULL,
  `hugo_gene_symbol` varchar(255) NOT NULL,
  `genetic_entity_id` int(11) NOT NULL,
  `type` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`entrez_gene_id`),
  UNIQUE KEY `genetic_entity_id_unique` (`genetic_entity_id`),
  KEY `hugo_gene_symbol` (`hugo_gene_symbol`),
  FOREIGN KEY (`genetic_entity_id`) REFERENCES `genetic_entity` (`id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `gene_alias` (
  `entrez_gene_id` int(11) NOT NULL,
  `gene_alias` varchar(255) NOT NULL,
  PRIMARY KEY (`entrez_gene_id`,`gene_alias`),
  FOREIGN KEY (`entrez_gene_id`) REFERENCES `gene` (`entrez_gene_id`)
);

-- --------------------------------------------------------
CREATE TABLE `geneset` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `genetic_entity_id` int(11) NOT NULL,
  `external_id` varchar(200) NOT NULL,
  `name` varchar(200) NOT NULL,
  `description` varchar(300) NOT NULL,
  `ref_link` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_unique` (`name`),
  UNIQUE KEY `external_id_coll_unique` (`external_id`),
  UNIQUE KEY `geneset_genetic_entity_id_unique` (`genetic_entity_id`),
  FOREIGN KEY (`genetic_entity_id`) REFERENCES `genetic_entity` (`id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `geneset_gene` (
  `geneset_id` int(11) NOT NULL,
  `entrez_gene_id` int(11) NOT NULL,
  PRIMARY KEY (`geneset_id`,`entrez_gene_id`),
  FOREIGN KEY (`entrez_gene_id`) REFERENCES `gene` (`entrez_gene_id`) ON DELETE CASCADE,
  FOREIGN KEY (`geneset_id`) REFERENCES `geneset` (`id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `geneset_hierarchy_node` (
  `node_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `node_name` varchar(200) NOT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`node_id`),
  UNIQUE KEY `node_name_unique` (`node_name`,`parent_id`)
);

-- --------------------------------------------------------
CREATE TABLE `geneset_hierarchy_leaf` (
  `node_id` bigint(20) NOT NULL,
  `geneset_id` int(11) NOT NULL,
  PRIMARY KEY (`node_id`,`geneset_id`),
  FOREIGN KEY (`node_id`) REFERENCES `geneset_hierarchy_node` (`node_id`) ON DELETE CASCADE,
  FOREIGN KEY (`geneset_id`) REFERENCES `geneset` (`id`) ON DELETE CASCADE
);

-- ------------------------------------------------------
CREATE TABLE `generic_entity_properties` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `genetic_entity_id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `value` varchar(5000) NOT NULL,
  UNIQUE KEY (`genetic_entity_id`,`name`),
  PRIMARY KEY (`id`),
  FOREIGN KEY (`genetic_entity_id`) REFERENCES `genetic_entity` (`id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `genetic_profile` (
  `genetic_profile_id` int(11) NOT NULL AUTO_INCREMENT,
  `stable_id` varchar(255) NOT NULL,
  `cancer_study_id` int(11) NOT NULL,
  `genetic_alteration_type` varchar(255) NOT NULL,
  `generic_assay_type` varchar(255) DEFAULT NULL,
  `datatype` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` mediumtext,
  `show_profile_in_analysis_tab` tinyint(1) NOT NULL,
  `pivot_threshold` float DEFAULT NULL,
  `sort_order` enum('ASC','DESC') DEFAULT NULL,
  `patient_level` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`genetic_profile_id`),
  UNIQUE KEY (`stable_id`),
  FOREIGN KEY (`cancer_study_id`) REFERENCES `cancer_study` (`cancer_study_id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `genetic_profile_link` (
  `referring_genetic_profile_id` int(11) NOT NULL,
  `referred_genetic_profile_id` int(11) NOT NULL,
  `reference_type` varchar(45) DEFAULT NULL, -- COMMENT 'Values: AGGREGATION (e.g. for GSVA) or STATISTIC (e.g. for Z-SCORES)'
  PRIMARY KEY (`referring_genetic_profile_id`,`referred_genetic_profile_id`),
  FOREIGN KEY (`referring_genetic_profile_id` ) REFERENCES `genetic_profile` (`genetic_profile_id`) ON DELETE CASCADE,
  FOREIGN KEY (`referred_genetic_profile_id` ) REFERENCES `genetic_profile` (`genetic_profile_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- --------------------------------------------------------
CREATE TABLE `genetic_alteration` (
  `genetic_profile_id` int(11) NOT NULL,
  `genetic_entity_id` int(11) NOT NULL,
  `values` longtext NOT NULL,
  PRIMARY KEY (`genetic_profile_id`,`genetic_entity_id`),
  FOREIGN KEY (`genetic_profile_id`) REFERENCES `genetic_profile` (`genetic_profile_id`) ON DELETE CASCADE,
  FOREIGN KEY (`genetic_entity_id`) REFERENCES `genetic_entity` (`id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `genetic_profile_samples` (
  `genetic_profile_id` int(11) NOT NULL,
  `ordered_sample_list` longtext NOT NULL,
  UNIQUE KEY (`genetic_profile_id`),
  FOREIGN KEY (`genetic_profile_id`) REFERENCES `genetic_profile` (`genetic_profile_id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `gene_panel` (
  `internal_id` int(11) NOT NULL AUTO_INCREMENT,
  `stable_id` varchar(255) NOT NULL,
  `description` mediumtext,
  PRIMARY KEY (`internal_id`),
  UNIQUE KEY (`stable_id`)
);

-- --------------------------------------------------------
CREATE TABLE `gene_panel_list` (
  `internal_id` int(11) NOT NULL,
  `gene_id` int(11) NOT NULL,
  PRIMARY KEY (`internal_id`,`gene_id`),
  FOREIGN KEY (`internal_id`) REFERENCES `gene_panel` (`internal_id`) ON DELETE CASCADE,
  FOREIGN KEY (`gene_id`) REFERENCES `gene` (`entrez_gene_id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `sample_profile` (
  `sample_id` int(11) NOT NULL,
  `genetic_profile_id` int(11) NOT NULL,
  `panel_id` int(11) DEFAULT NULL,
  UNIQUE KEY `uq_sample_id_genetic_profile_id` (`sample_id`,`genetic_profile_id`), -- Constraint to allow each sample only once in each profile
  KEY (`sample_id`),
  FOREIGN KEY (`genetic_profile_id`) REFERENCES `genetic_profile` (`genetic_profile_id`) ON DELETE CASCADE,
  FOREIGN KEY (`sample_id`) REFERENCES `sample` (`internal_id`) ON DELETE CASCADE,
  FOREIGN KEY (`panel_id`) REFERENCES `gene_panel` (`internal_id`) ON DELETE RESTRICT
);

-- --------------------------------------------------------
CREATE TABLE `structural_variant` (
  `internal_id` int(11) NOT NULL AUTO_INCREMENT,
  `genetic_profile_id` int(11) NOT NULL,
  `sample_id` int(11) NOT NULL,
  `site1_entrez_gene_id` int(11) DEFAULT NULL,
  `site1_ensembl_transcript_id` varchar(25) DEFAULT NULL,
  `site1_chromosome` varchar(5) DEFAULT NULL,
  `site1_region` varchar(25) DEFAULT NULL,
  `site1_region_number` int(11) DEFAULT NULL,
  `site1_contig` varchar(100) DEFAULT NULL,
  `site1_position` int(11) DEFAULT NULL,
  `site1_description` varchar(255) DEFAULT NULL,
  `site2_entrez_gene_id` int(11) DEFAULT NULL,
  `site2_ensembl_transcript_id` varchar(25) DEFAULT NULL,
  `site2_chromosome` varchar(5) DEFAULT NULL,
  `site2_region` varchar(25) DEFAULT NULL,
  `site2_region_number` int(11) DEFAULT NULL,
  `site2_contig` varchar(100) DEFAULT NULL,
  `site2_position` int(11) DEFAULT NULL,
  `site2_description` varchar(255) DEFAULT NULL,
  `site2_effect_on_frame` varchar(25) DEFAULT NULL,
  `ncbi_build` varchar(10) DEFAULT NULL,
  `dna_support` varchar(3) DEFAULT NULL,
  `rna_support` varchar(3) DEFAULT NULL,
  `normal_read_count` int(11) DEFAULT NULL,
  `tumor_read_count` int(11) DEFAULT NULL,
  `normal_variant_count` int(11) DEFAULT NULL,
  `tumor_variant_count` int(11) DEFAULT NULL,
  `normal_paired_end_read_count` int(11) DEFAULT NULL,
  `tumor_paired_end_read_count` int(11) DEFAULT NULL,
  `normal_split_read_count` int(11) DEFAULT NULL,
  `tumor_split_read_count` int(11) DEFAULT NULL,
  `annotation` varchar(255) DEFAULT NULL,
  `breakpoint_type` varchar(25) DEFAULT NULL,
  `connection_type` varchar(25) DEFAULT NULL,
  `event_info` varchar(255) DEFAULT NULL,
  `class` varchar(25) DEFAULT NULL,
  `length` int(11) DEFAULT NULL,
  `comments` varchar(255) DEFAULT NULL,
  `sv_status` varchar(25) NOT NULL DEFAULT 'SOMATIC' COMMENT 'GERMLINE or SOMATIC.',
  `annotation_json` json DEFAULT NULL,
  PRIMARY KEY (`internal_id`),
  FOREIGN KEY (`sample_id`) REFERENCES `sample` (`internal_id`) ON DELETE CASCADE,
  FOREIGN KEY (`site1_entrez_gene_id`) REFERENCES `gene` (`entrez_gene_id`) ON DELETE CASCADE,
  FOREIGN KEY (`site2_entrez_gene_id`) REFERENCES `gene` (`entrez_gene_id`) ON DELETE CASCADE,
  FOREIGN KEY (`genetic_profile_id`) REFERENCES `genetic_profile` (`genetic_profile_id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `alteration_driver_annotation` (
  `alteration_event_id` int(255) NOT NULL,
  `genetic_profile_id` int(11) NOT NULL,
  `sample_id` int(11) NOT NULL,
  `driver_filter` varchar(20) DEFAULT NULL,
  `driver_filter_annotation` varchar(80) DEFAULT NULL,
  `driver_tiers_filter` varchar(50) DEFAULT NULL,
  `driver_tiers_filter_annotation` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`alteration_event_id`,`genetic_profile_id`,`sample_id`),
  FOREIGN KEY (`genetic_profile_id`) REFERENCES `genetic_profile` (`genetic_profile_id`) ON DELETE CASCADE,
  FOREIGN KEY (`sample_id`) REFERENCES `sample` (`internal_id`) ON DELETE CASCADE,
  KEY (`driver_filter`),
  KEY (`driver_tiers_filter`)
) COMMENT='Alteration driver annotation';

-- --------------------------------------------------------
CREATE TABLE `mutation_event` (
  `mutation_event_id` int(255) NOT NULL AUTO_INCREMENT,
  `entrez_gene_id` int(11) NOT NULL,
  `chr` varchar(5) DEFAULT NULL,
  `start_position` bigint(20) DEFAULT NULL,
  `end_position` bigint(20) DEFAULT NULL,
  `reference_allele` text,
  `tumor_seq_allele` text,
  `protein_change` varchar(255) DEFAULT NULL,
  `mutation_type` varchar(255) DEFAULT NULL COMMENT 'e.g. Missense, Nonsence, etc.',
  `ncbi_build` varchar(10) DEFAULT NULL,
  `strand` varchar(2) DEFAULT NULL,
  `variant_type` varchar(15) DEFAULT NULL,
  `db_snp_rs` varchar(25) DEFAULT NULL,
  `db_snp_val_status` varchar(255) DEFAULT NULL,
  `refseq_mrna_id` varchar(64) DEFAULT NULL,
  `codon_change` varchar(255) DEFAULT NULL,
  `uniprot_accession` varchar(64) DEFAULT NULL,
  `protein_pos_start` int(11) DEFAULT NULL,
  `protein_pos_end` int(11) DEFAULT NULL,
  `canonical_transcript` tinyint(1) DEFAULT NULL,
  `keyword` varchar(255) DEFAULT NULL COMMENT 'e.g. truncating, V200 Missense, E338del, ',
  KEY (`keyword`),
  PRIMARY KEY (`mutation_event_id`),
  KEY `key_mutation_event_details` (`chr`,`start_position`,`end_position`,`tumor_seq_allele`(240),`entrez_gene_id`,`protein_change`,`mutation_type`),
  FOREIGN KEY (`entrez_gene_id`) REFERENCES `gene` (`entrez_gene_id`),
  KEY (`mutation_type`)
) COMMENT='Mutation Data';

-- --------------------------------------------------------
CREATE TABLE `mutation` (
  `mutation_event_id` int(255) NOT NULL,
  `genetic_profile_id` int(11) NOT NULL,
  `sample_id` int(11) NOT NULL,
  `entrez_gene_id` int(11) NOT NULL,
  `center` varchar(100) DEFAULT NULL,
  `sequencer` varchar(255) DEFAULT NULL,
  `mutation_status` varchar(25) DEFAULT NULL COMMENT 'Germline, Somatic or LOH.',
  `validation_status` varchar(25) DEFAULT NULL,
  `tumor_seq_allele1` text,
  `tumor_seq_allele2` text,
  `matched_norm_sample_barcode` varchar(255) DEFAULT NULL,
  `match_norm_seq_allele1` text,
  `match_norm_seq_allele2` text,
  `tumor_validation_allele1` text,
  `tumor_validation_allele2` text,
  `match_norm_validation_allele1` text,
  `match_norm_validation_allele2` text,
  `verification_status` varchar(10) DEFAULT NULL,
  `sequencing_phase` varchar(100) DEFAULT NULL,
  `sequence_source` varchar(255) NOT NULL,
  `validation_method` varchar(255) DEFAULT NULL,
  `score` varchar(100) DEFAULT NULL,
  `bam_file` varchar(255) DEFAULT NULL,
  `tumor_alt_count` int(11) DEFAULT NULL,
  `tumor_ref_count` int(11) DEFAULT NULL,
  `normal_alt_count` int(11) DEFAULT NULL,
  `normal_ref_count` int(11) DEFAULT NULL,
  `amino_acid_change` varchar(255) DEFAULT NULL,
  `annotation_json` json DEFAULT NULL,
  UNIQUE KEY `uq_mutation_event_id_genetic_profile_id_sample_id` (`mutation_event_id`,`genetic_profile_id`,`sample_id`), -- Constraint to block duplicated mutation entries
  KEY (`genetic_profile_id`,`entrez_gene_id`),
  KEY (`genetic_profile_id`,`sample_id`),
  KEY (`genetic_profile_id`),
  KEY (`entrez_gene_id`),
  KEY (`sample_id`),
  KEY (`mutation_event_id`),
  FOREIGN KEY (`mutation_event_id`) REFERENCES `mutation_event` (`mutation_event_id`),
  FOREIGN KEY (`entrez_gene_id`) REFERENCES `gene` (`entrez_gene_id`),
  FOREIGN KEY (`genetic_profile_id`) REFERENCES `genetic_profile` (`genetic_profile_id`) ON DELETE CASCADE,
  FOREIGN KEY (`sample_id`) REFERENCES `sample` (`internal_id`) ON DELETE CASCADE
) COMMENT='Mutation Data Details';

-- --------------------------------------------------------
CREATE TABLE `mutation_count_by_keyword` (
  `genetic_profile_id` int(11) NOT NULL,
  `keyword` varchar(255) DEFAULT NULL,
  `entrez_gene_id` int(11) NOT NULL,
  `keyword_count` int(11) NOT NULL,
  `gene_count` int(11) NOT NULL,
  KEY (`genetic_profile_id`,`keyword`),
  FOREIGN KEY (`genetic_profile_id`) REFERENCES `genetic_profile` (`genetic_profile_id`) ON DELETE CASCADE,
  FOREIGN KEY (`entrez_gene_id`) REFERENCES `gene` (`entrez_gene_id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `clinical_patient` (
  `internal_id` int(11) NOT NULL,
  `attr_id` varchar(255) NOT NULL,
  `attr_value` varchar(255) NOT NULL,
  PRIMARY KEY (`internal_id`,`attr_id`),
  FOREIGN KEY (`internal_id`) REFERENCES `patient` (`internal_id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `clinical_sample` (
  `internal_id` int(11) NOT NULL,
  `attr_id` varchar(255) NOT NULL,
  `attr_value` varchar(255) NOT NULL,
  PRIMARY KEY (`internal_id`,`attr_id`),
  FOREIGN KEY (`internal_id`) REFERENCES `sample` (`internal_id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `clinical_attribute_meta` (
  `attr_id` varchar(255) NOT NULL,
  `display_name` varchar(255) NOT NULL,
  `description` varchar(2048) NOT NULL,
  `datatype` varchar(255) NOT NULL COMMENT 'NUMBER, BOOLEAN, or STRING',
  `patient_attribute` tinyint(1) NOT NULL,
  `priority` varchar(255) NOT NULL,
  `cancer_study_id` int(11) NOT NULL,
  PRIMARY KEY (`attr_id`,`cancer_study_id`),
  FOREIGN KEY (`cancer_study_id`) REFERENCES `cancer_study` (`cancer_study_id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `mut_sig` (
  `cancer_study_id` int(11) NOT NULL,
  `entrez_gene_id` int(11) NOT NULL,
  `rank` int(11) NOT NULL,
  `numbasescovered` int(11) NOT NULL,
  `nummutations` int(11) NOT NULL,
  `p_value` float NOT NULL,
  `q_value` float NOT NULL,
  PRIMARY KEY (`cancer_study_id`,`entrez_gene_id`),
  FOREIGN KEY (`cancer_study_id`) REFERENCES `cancer_study` (`cancer_study_id`) ON DELETE CASCADE,
  FOREIGN KEY (`entrez_gene_id`) REFERENCES `gene` (`entrez_gene_id`)
);

-- --------------------------------------------------------
CREATE TABLE `gistic` (
  `gistic_roi_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cancer_study_id` int(11) NOT NULL,
  `chromosome` int(11) NOT NULL,
  `cytoband` varchar(255) NOT NULL,
  `wide_peak_start` int(11) NOT NULL,
  `wide_peak_end` int(11) NOT NULL,
  `q_value` double NOT NULL,
  `amp` tinyint(1) NOT NULL,
  PRIMARY KEY (`gistic_roi_id`),
  FOREIGN KEY (`cancer_study_id`) REFERENCES `cancer_study` (`cancer_study_id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `gistic_to_gene` (
  `gistic_roi_id` bigint(20) NOT NULL,
  `entrez_gene_id` int(11) NOT NULL,
  PRIMARY KEY (`gistic_roi_id`,`entrez_gene_id`),
  FOREIGN KEY (`entrez_gene_id`) REFERENCES `gene` (`entrez_gene_id`),
  FOREIGN KEY (`gistic_roi_id`) REFERENCES `gistic` (`gistic_roi_id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `cna_event` (
  `cna_event_id` int(255) NOT NULL AUTO_INCREMENT,
  `entrez_gene_id` int(11) NOT NULL,
  `alteration` tinyint(4) NOT NULL,
  PRIMARY KEY (`cna_event_id`),
  UNIQUE KEY (`entrez_gene_id`,`alteration`),
  FOREIGN KEY (`entrez_gene_id`) REFERENCES `gene` (`entrez_gene_id`),
  KEY (`alteration`)
);

-- --------------------------------------------------------
CREATE TABLE `sample_cna_event` (
  `cna_event_id` int(255) NOT NULL,
  `sample_id` int(11) NOT NULL,
  `genetic_profile_id` int(11) NOT NULL,
  `annotation_json` json DEFAULT NULL,
  KEY (`genetic_profile_id`,`sample_id`),
  PRIMARY KEY (`cna_event_id`,`sample_id`,`genetic_profile_id`),
  FOREIGN KEY (`cna_event_id`) REFERENCES `cna_event` (`cna_event_id`),
  FOREIGN KEY (`genetic_profile_id`) REFERENCES `genetic_profile` (`genetic_profile_id`) ON DELETE CASCADE,
  FOREIGN KEY (`sample_id`) REFERENCES `sample` (`internal_id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `copy_number_seg` (
  `seg_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cancer_study_id` int(11) NOT NULL,
  `sample_id` int(11) NOT NULL,
  `chr` varchar(5) NOT NULL,
  `start` int(11) NOT NULL,
  `end` int(11) NOT NULL,
  `num_probes` int(11) NOT NULL,
  `segment_mean` double NOT NULL,
  KEY (`cancer_study_id`,`sample_id`),
  PRIMARY KEY (`seg_id`),
  FOREIGN KEY (`cancer_study_id`) REFERENCES `cancer_study` (`cancer_study_id`) ON DELETE CASCADE,
  FOREIGN KEY (`sample_id`) REFERENCES `sample` (`internal_id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `copy_number_seg_file` (
  `seg_file_id` int(11) NOT NULL AUTO_INCREMENT,
  `cancer_study_id` int(11) NOT NULL,
  `reference_genome_id` varchar(10) NOT NULL,
  `description` varchar(255) NOT NULL,
  `filename` varchar(255) NOT NULL,
  PRIMARY KEY(`seg_file_id`),
  FOREIGN KEY (`cancer_study_id`) REFERENCES `cancer_study` (`cancer_study_id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `cosmic_mutation` (
  `cosmic_mutation_id` varchar(30) NOT NULL,
  `chr` varchar(5) DEFAULT NULL,
  `start_position` bigint(20) DEFAULT NULL,
  `reference_allele` varchar(255) DEFAULT NULL,
  `tumor_seq_allele` varchar(255) DEFAULT NULL,
  `strand` varchar(2) DEFAULT NULL,
  `codon_change` varchar(255) DEFAULT NULL,
  `entrez_gene_id` int(11) NOT NULL,
  `protein_change` varchar(255) NOT NULL,
  `count` int(11) NOT NULL,
  `keyword` varchar(50) DEFAULT NULL,
  KEY (`keyword`),
  PRIMARY KEY (`cosmic_mutation_id`),
  FOREIGN KEY (`entrez_gene_id`) REFERENCES `gene` (`entrez_gene_id`)
);

-- --------------------------------------------------------
CREATE TABLE `clinical_event` (
  `clinical_event_id` int(11) NOT NULL AUTO_INCREMENT,
  `patient_id` int(11) NOT NULL,
  `start_date` int(11) NOT NULL,
  `stop_date` int(11) DEFAULT NULL,
  `event_type` varchar(20) NOT NULL,
  PRIMARY KEY (`clinical_event_id`),
  KEY (`patient_id`,`event_type`),
  FOREIGN KEY (`patient_id`) REFERENCES `patient` (`internal_id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `clinical_event_data` (
  `clinical_event_id` int(255) NOT NULL,
  `key` varchar(255) NOT NULL,
  `value` varchar(5000) NOT NULL,
  FOREIGN KEY (`clinical_event_id`) REFERENCES `clinical_event` (`clinical_event_id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `reference_genome_gene` (
  `entrez_gene_id` int(11) NOT NULL,
  `reference_genome_id` int(4) NOT NULL,
  `chr` varchar(5) DEFAULT NULL,
  `cytoband` varchar(64) DEFAULT NULL,
  `start` bigint(20) DEFAULT NULL,
  `end` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`entrez_gene_id`,`reference_genome_id`),
  FOREIGN KEY (`reference_genome_id`) REFERENCES `reference_genome` (`reference_genome_id`) ON DELETE CASCADE,
  FOREIGN KEY (`entrez_gene_id`) REFERENCES `gene` (`entrez_gene_id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `data_access_tokens` (
  `token` varchar(50) NOT NULL,
  `username` varchar(128) NOT NULL,
  `expiration` datetime NOT NULL,
  `creation` datetime NOT NULL,
  PRIMARY KEY (`token`),
  FOREIGN KEY (`username`) REFERENCES `users` (`email`) ON DELETE CASCADE
);
-- --------------------------------------------------------
CREATE TABLE `allele_specific_copy_number` (
  `mutation_event_id` int(255) NOT NULL,
  `genetic_profile_id` int(11) NOT NULL,
  `sample_id` int(11) NOT NULL,
  `ascn_integer_copy_number` int(11) DEFAULT NULL,
  `ascn_method` varchar(24) NOT NULL,
  `ccf_expected_copies_upper` float DEFAULT NULL,
  `ccf_expected_copies` float DEFAULT NULL,
  `clonal` varchar(16) DEFAULT NULL,
  `minor_copy_number` int(11) DEFAULT NULL,
  `expected_alt_copies` int(11) DEFAULT NULL,
  `total_copy_number` int(11) DEFAULT NULL,
  UNIQUE KEY `uq_ascn_mutation_event_id_genetic_profile_id_sample_id` (`mutation_event_id`,`genetic_profile_id`,`sample_id`), -- Constraint to block duplicated mutation entries
  FOREIGN KEY (`mutation_event_id`) REFERENCES `mutation_event` (`mutation_event_id`),
  FOREIGN KEY (`genetic_profile_id`) REFERENCES `genetic_profile` (`genetic_profile_id`) ON DELETE CASCADE,
  FOREIGN KEY (`sample_id`) REFERENCES `sample` (`internal_id`) ON DELETE CASCADE
);
-- --------------------------------------------------------
CREATE TABLE `info` (
  `db_schema_version` varchar(24) DEFAULT NULL,
  `geneset_version` varchar(24) DEFAULT NULL
);

-- --------------------------------------------------------
CREATE TABLE `resource_definition` (
  `resource_id` varchar(255) NOT NULL,
  `display_name` varchar(255) NOT NULL,
  `description` varchar(2048) DEFAULT NULL,
  `resource_type` enum('STUDY','PATIENT','SAMPLE') NOT NULL,
  `open_by_default` tinyint(1) DEFAULT '0',
  `priority` int(11) NOT NULL,
  `cancer_study_id` int(11) NOT NULL,
  PRIMARY KEY (`resource_id`,`cancer_study_id`),
  FOREIGN KEY (`cancer_study_id`) REFERENCES `cancer_study` (`cancer_study_id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `resource_sample` (
  `internal_id` int(11) NOT NULL,
  `resource_id` varchar(255) NOT NULL,
  `url` varchar(255) NOT NULL,
  PRIMARY KEY (`internal_id`,`resource_id`,`url`),
  FOREIGN KEY (`internal_id`) REFERENCES `sample` (`internal_id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `resource_patient` (
  `internal_id` int(11) NOT NULL,
  `resource_id` varchar(255) NOT NULL,
  `url` varchar(255) NOT NULL,
  PRIMARY KEY (`internal_id`,`resource_id`,`url`),
  FOREIGN KEY (`internal_id`) REFERENCES `patient` (`internal_id`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `resource_study` (
  `internal_id` int(11) NOT NULL,
  `resource_id` varchar(255) NOT NULL,
  `url` varchar(255) NOT NULL,
  PRIMARY KEY (`internal_id`,`resource_id`,`url`),
  FOREIGN KEY (`internal_id`) REFERENCES `cancer_study` (`cancer_study_id`) ON DELETE CASCADE
);

-- THIS MUST BE KEPT IN SYNC WITH db.version PROPERTY IN pom.xml
INSERT INTO info VALUES ('3.0.0', NULL);

