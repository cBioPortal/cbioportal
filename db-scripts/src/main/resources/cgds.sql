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

DROP TABLE IF EXISTS `info`;
DROP TABLE IF EXISTS `clinical_event_data`;
DROP TABLE IF EXISTS `clinical_event`;
DROP TABLE IF EXISTS `pdb_uniprot_residue_mapping`;
DROP TABLE IF EXISTS `pdb_uniprot_alignment`;
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
DROP TABLE IF EXISTS `resource_definition`;
DROP TABLE IF EXISTS `resource_sample`;
DROP TABLE IF EXISTS `resource_patient`;
DROP TABLE IF EXISTS `resource_study`;
DROP TABLE IF EXISTS `mutation_count_by_keyword`;
DROP TABLE IF EXISTS `allele_specific_copy_number`;
DROP TABLE IF EXISTS `mutation`;
DROP TABLE IF EXISTS `mutation_event`;
DROP TABLE IF EXISTS `structural_variant`;
DROP TABLE IF EXISTS `sample_profile`;
DROP TABLE IF EXISTS `gene_panel_list`;
DROP TABLE IF EXISTS `gene_panel`;
DROP TABLE IF EXISTS `genetic_profile_samples`;
DROP TABLE IF EXISTS `genetic_alteration`;
DROP TABLE IF EXISTS `genetic_profile_link`;
DROP TABLE IF EXISTS `alteration_driver_annotation`;
DROP TABLE IF EXISTS `genetic_profile`;
DROP TABLE IF EXISTS `gene_alias`;
DROP TABLE IF EXISTS `geneset_gene`;
DROP TABLE IF EXISTS `reference_genome_gene`;
DROP TABLE IF EXISTS `gene`;
DROP TABLE IF EXISTS `sample_list_list`;
DROP TABLE IF EXISTS `sample_list`;
DROP TABLE IF EXISTS `sample`;
DROP TABLE IF EXISTS `patient`;
DROP TABLE IF EXISTS `authorities`;
DROP TABLE IF EXISTS `data_access_tokens`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `cancer_study_tags`;
DROP TABLE IF EXISTS `cancer_study`;
DROP TABLE IF EXISTS `type_of_cancer`;
DROP TABLE IF EXISTS `geneset_hierarchy_leaf`;
DROP TABLE IF EXISTS `geneset_hierarchy_node`;
DROP TABLE IF EXISTS `geneset`;
DROP TABLE IF EXISTS `generic_entity_properties`;
DROP TABLE IF EXISTS `genetic_entity`;
DROP TABLE IF EXISTS `reference_genome`;

-- --------------------------------------------------------
CREATE TABLE `type_of_cancer` (
  `TYPE_OF_CANCER_ID` varchar(63) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  `DEDICATED_COLOR` char(31) NOT NULL,
  `SHORT_NAME` varchar(127),
  `PARENT` varchar(63),
  PRIMARY KEY (`TYPE_OF_CANCER_ID`)
);

-- --------------------------------------------------------
CREATE TABLE `reference_genome` (
    `REFERENCE_GENOME_ID` int(4) NOT NULL AUTO_INCREMENT,
    `SPECIES` varchar(64) NOT NULL,
    `NAME` varchar(64) NOT NULL,
    `BUILD_NAME` varchar(64) NOT NULL,
    `GENOME_SIZE` bigint(20) NULL,
    `URL` varchar(256) NOT NULL,
    `RELEASE_DATE` datetime DEFAULT NULL,
    PRIMARY KEY (`REFERENCE_GENOME_ID`),
    UNIQUE INDEX `BUILD_NAME_UNIQUE` (`BUILD_NAME` ASC)
);

-- --------------------------------------------------------
CREATE TABLE `cancer_study` (
  `CANCER_STUDY_ID` int(11) NOT NULL auto_increment,
  `CANCER_STUDY_IDENTIFIER` varchar(255),
  `TYPE_OF_CANCER_ID` varchar(63) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  `DESCRIPTION` varchar(1024) NOT NULL,
  `PUBLIC` BOOLEAN NOT NULL,
  `PMID` varchar(1024) DEFAULT NULL,
  `CITATION` varchar(200) DEFAULT NULL,
  `GROUPS` varchar(200) DEFAULT NULL,
  `STATUS` int(1) DEFAULT NULL,
  `IMPORT_DATE` datetime DEFAULT NULL,
  `REFERENCE_GENOME_ID` int(4) DEFAULT 1,
  PRIMARY KEY (`CANCER_STUDY_ID`),
  UNIQUE (`CANCER_STUDY_IDENTIFIER`),
  FOREIGN KEY (`TYPE_OF_CANCER_ID`) REFERENCES `type_of_cancer` (`TYPE_OF_CANCER_ID`),
  FOREIGN KEY (`REFERENCE_GENOME_ID`) REFERENCES `reference_genome` (`REFERENCE_GENOME_ID`) ON DELETE RESTRICT
);

-- --------------------------------------------------------
CREATE TABLE `cancer_study_tags` (
  `CANCER_STUDY_ID` int(11) NOT NULL,
  `TAGS` text NOT NULL,
  PRIMARY KEY (`CANCER_STUDY_ID`),
  FOREIGN KEY (`CANCER_STUDY_ID`) REFERENCES `cancer_study` (`CANCER_STUDY_ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `users` (
  `EMAIL` varchar(128) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  `ENABLED` BOOLEAN NOT NULL,
  PRIMARY KEY (`EMAIL`)
);

-- --------------------------------------------------------
CREATE TABLE `authorities` (
  `EMAIL` varchar(128) NOT NULL,
  `AUTHORITY` varchar(255) NOT NULL
);

-- --------------------------------------------------------
CREATE TABLE `patient` (
  `INTERNAL_ID` int(11) NOT NULL auto_increment,
  `STABLE_ID` varchar(50) NOT NULL,
  `CANCER_STUDY_ID` int(11) NOT NULL,
  PRIMARY KEY (`INTERNAL_ID`),
  FOREIGN KEY (`CANCER_STUDY_ID`) REFERENCES `cancer_study` (`CANCER_STUDY_ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `sample` (
  `INTERNAL_ID` int(11) NOT NULL auto_increment,
  `STABLE_ID` varchar(63) NOT NULL,
  `SAMPLE_TYPE` varchar(255) NOT NULL,
  `PATIENT_ID` int(11) NOT NULL,
  PRIMARY KEY (`INTERNAL_ID`),
  FOREIGN KEY (`PATIENT_ID`) REFERENCES `patient` (`INTERNAL_ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `sample_list` (
  `LIST_ID` int(11) NOT NULL auto_increment,
  `STABLE_ID` varchar(255) NOT NULL,
  `CATEGORY` varchar(255) NOT NULL,
  `CANCER_STUDY_ID` int(11) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  `DESCRIPTION` mediumtext,
  PRIMARY KEY (`LIST_ID`),
  UNIQUE (`STABLE_ID`),
  FOREIGN KEY (`CANCER_STUDY_ID`) REFERENCES `cancer_study` (`CANCER_STUDY_ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `sample_list_list` (
  `LIST_ID` int(11) NOT NULL,
  `SAMPLE_ID` int(11) NOT NULL,
  PRIMARY KEY (`LIST_ID`,`SAMPLE_ID`),
  FOREIGN KEY (`SAMPLE_ID`) REFERENCES `sample` (`INTERNAL_ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------

CREATE TABLE `genetic_entity` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `ENTITY_TYPE` varchar(45) NOT NULL,
  `STABLE_ID` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
);

-- --------------------------------------------------------
CREATE TABLE `gene` (
  `ENTREZ_GENE_ID` int(11) NOT NULL,
  `HUGO_GENE_SYMBOL` varchar(255) NOT NULL,
  `GENETIC_ENTITY_ID` int(11) NOT NULL,
  `TYPE` varchar(50),
  PRIMARY KEY (`ENTREZ_GENE_ID`),
  UNIQUE KEY `GENETIC_ENTITY_ID_UNIQUE` (`GENETIC_ENTITY_ID`),
  KEY `HUGO_GENE_SYMBOL` (`HUGO_GENE_SYMBOL`),
  FOREIGN KEY (`GENETIC_ENTITY_ID`) REFERENCES `genetic_entity` (`ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `gene_alias` (
  `ENTREZ_GENE_ID` int(11) NOT NULL,
  `GENE_ALIAS` varchar(255) NOT NULL,
  PRIMARY KEY (`ENTREZ_GENE_ID`,`GENE_ALIAS`),
  FOREIGN KEY (`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`)
);

-- --------------------------------------------------------
CREATE TABLE `geneset` (
  `ID` INT(11) NOT NULL auto_increment,
  `GENETIC_ENTITY_ID` INT NOT NULL,
  `EXTERNAL_ID` VARCHAR(200) NOT NULL,
  `NAME` VARCHAR(200) NOT NULL,
  `DESCRIPTION` VARCHAR(300) NOT NULL,
  `REF_LINK` TEXT,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `NAME_UNIQUE` (`NAME` ASC),
  UNIQUE INDEX `EXTERNAL_ID_COLL_UNIQUE` (`EXTERNAL_ID` ASC),
  UNIQUE INDEX `GENESET_GENETIC_ENTITY_ID_UNIQUE` (`GENETIC_ENTITY_ID` ASC),
  FOREIGN KEY (`GENETIC_ENTITY_ID`) REFERENCES `genetic_entity` (`ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `geneset_gene` (
  `GENESET_ID` INT(11) NOT NULL,
  `ENTREZ_GENE_ID` INT(11) NOT NULL,
  PRIMARY KEY (`GENESET_ID`, `ENTREZ_GENE_ID`),
  FOREIGN KEY (`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`GENESET_ID`) REFERENCES `geneset` (`ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `geneset_hierarchy_node` (
  `NODE_ID` BIGINT(20) NOT NULL auto_increment,
  `NODE_NAME` VARCHAR(200) NOT NULL,
  `PARENT_ID` BIGINT NULL DEFAULT NULL,
  PRIMARY KEY (`NODE_ID`),
  UNIQUE INDEX `NODE_NAME_UNIQUE` (`NODE_NAME` ASC, `PARENT_ID` ASC)
);

-- --------------------------------------------------------
CREATE TABLE `geneset_hierarchy_leaf` (
  `NODE_ID` BIGINT NOT NULL,
  `GENESET_ID` INT NOT NULL,
  PRIMARY KEY (`NODE_ID`, `GENESET_ID`),
  FOREIGN KEY (`NODE_ID`) REFERENCES `geneset_hierarchy_node` (`NODE_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`GENESET_ID`) REFERENCES `geneset` (`ID`) ON DELETE CASCADE
);

-- ------------------------------------------------------
CREATE TABLE `generic_entity_properties` (
  `ID` INT(11) NOT NULL auto_increment,
  `GENETIC_ENTITY_ID` INT NOT NULL,
  `NAME` varchar(255) NOT NULL,
  `VALUE` varchar(5000) NOT NULL,
  UNIQUE (`GENETIC_ENTITY_ID`, `NAME`),
  PRIMARY KEY (`ID`),
  FOREIGN KEY (`GENETIC_ENTITY_ID`) REFERENCES `genetic_entity` (`ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `genetic_profile` (
  `GENETIC_PROFILE_ID` int(11) NOT NULL AUTO_INCREMENT,
  `STABLE_ID` varchar(255) NOT NULL,
  `CANCER_STUDY_ID` int(11) NOT NULL,
  `GENETIC_ALTERATION_TYPE` varchar(255) NOT NULL,
  `GENERIC_ASSAY_TYPE` varchar(255) DEFAULT NULL,
  `DATATYPE` varchar(255) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  `DESCRIPTION` mediumtext,
  `SHOW_PROFILE_IN_ANALYSIS_TAB` tinyint(1) NOT NULL,
  `PIVOT_THRESHOLD` FLOAT DEFAULT NULL,
  `SORT_ORDER` ENUM('ASC','DESC') DEFAULT NULL,
  `PATIENT_LEVEL` boolean DEFAULT 0,
  PRIMARY KEY (`GENETIC_PROFILE_ID`),
  UNIQUE (`STABLE_ID`),
  FOREIGN KEY (`CANCER_STUDY_ID`) REFERENCES `cancer_study` (`CANCER_STUDY_ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `genetic_profile_link` (
  `REFERRING_GENETIC_PROFILE_ID` INT NOT NULL,
  `REFERRED_GENETIC_PROFILE_ID` INT NOT NULL,
  `REFERENCE_TYPE` VARCHAR(45) NULL, -- COMMENT 'Values: AGGREGATION (e.g. for GSVA) or STATISTIC (e.g. for Z-SCORES)
  PRIMARY KEY (`REFERRING_GENETIC_PROFILE_ID`, `REFERRED_GENETIC_PROFILE_ID`),
  FOREIGN KEY (`REFERRING_GENETIC_PROFILE_ID` ) REFERENCES `genetic_profile` (`GENETIC_PROFILE_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`REFERRED_GENETIC_PROFILE_ID` ) REFERENCES `genetic_profile` (`GENETIC_PROFILE_ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- --------------------------------------------------------
CREATE TABLE `genetic_alteration` (
  `GENETIC_PROFILE_ID` int(11) NOT NULL,
  `GENETIC_ENTITY_ID` int(11) NOT NULL,
  `VALUES` longtext NOT NULL,
  PRIMARY KEY (`GENETIC_PROFILE_ID`,`GENETIC_ENTITY_ID`),
  FOREIGN KEY (`GENETIC_PROFILE_ID`) REFERENCES `genetic_profile` (`GENETIC_PROFILE_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`GENETIC_ENTITY_ID`) REFERENCES `genetic_entity` (`ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `genetic_profile_samples` (
  `GENETIC_PROFILE_ID` int(11) NOT NULL,
  `ORDERED_SAMPLE_LIST` longtext NOT NULL,
  UNIQUE (`GENETIC_PROFILE_ID`),
  FOREIGN KEY (`GENETIC_PROFILE_ID`) REFERENCES `genetic_profile` (`GENETIC_PROFILE_ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `gene_panel` (
  `INTERNAL_ID` int(11) NOT NULL auto_increment,
  `STABLE_ID` varchar(255) NOT NULL,
  `DESCRIPTION` mediumtext,
  PRIMARY KEY (`INTERNAL_ID`),
  UNIQUE (`STABLE_ID`)
);

-- --------------------------------------------------------
CREATE TABLE `gene_panel_list` (
  `INTERNAL_ID` int(11) NOT NULL,
  `GENE_ID` int(11) NOT NULL,
  PRIMARY KEY (`INTERNAL_ID`, `GENE_ID`),
  FOREIGN KEY (`INTERNAL_ID`) REFERENCES `gene_panel` (`INTERNAL_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `sample_profile` (
  `SAMPLE_ID` int(11) NOT NULL,
  `GENETIC_PROFILE_ID` int(11) NOT NULL,
  `PANEL_ID` int(11) DEFAULT NULL,
  UNIQUE KEY `UQ_SAMPLE_ID_GENETIC_PROFILE_ID` (`SAMPLE_ID`,`GENETIC_PROFILE_ID`), -- Constraint to allow each sample only once in each profile
  KEY (`SAMPLE_ID`),
  FOREIGN KEY (`GENETIC_PROFILE_ID`) REFERENCES `genetic_profile` (`GENETIC_PROFILE_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`SAMPLE_ID`) REFERENCES `sample` (`INTERNAL_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`PANEL_ID`) REFERENCES `gene_panel` (`INTERNAL_ID`) ON DELETE RESTRICT
);

-- --------------------------------------------------------
CREATE TABLE `structural_variant` (
  `INTERNAL_ID` int(11) NOT NULL auto_increment,
  `GENETIC_PROFILE_ID` int(11) NOT NULL,
  `SAMPLE_ID` int(11) NOT NULL,
  `SITE1_ENTREZ_GENE_ID` int(11),
  `SITE1_ENSEMBL_TRANSCRIPT_ID` varchar(25),
  `SITE1_CHROMOSOME` varchar(5),
  `SITE1_REGION` varchar(25),
  `SITE1_REGION_NUMBER` int(11),
  `SITE1_CONTIG` varchar(100),
  `SITE1_POSITION` int(11),
  `SITE1_DESCRIPTION` varchar(255),
  `SITE2_ENTREZ_GENE_ID` int(11),
  `SITE2_ENSEMBL_TRANSCRIPT_ID` varchar(25),
  `SITE2_CHROMOSOME` varchar(5),
  `SITE2_REGION` varchar(25),
  `SITE2_REGION_NUMBER` int(11),
  `SITE2_CONTIG` varchar(100),
  `SITE2_POSITION` int(11),
  `SITE2_DESCRIPTION` varchar(255),
  `SITE2_EFFECT_ON_FRAME` varchar(25),
  `NCBI_BUILD` varchar(10),
  `DNA_SUPPORT` varchar(3),
  `RNA_SUPPORT` varchar(3),
  `NORMAL_READ_COUNT` int(11),
  `TUMOR_READ_COUNT` int(11),
  `NORMAL_VARIANT_COUNT` int(11),
  `TUMOR_VARIANT_COUNT` int(11),
  `NORMAL_PAIRED_END_READ_COUNT` int(11),
  `TUMOR_PAIRED_END_READ_COUNT` int(11),
  `NORMAL_SPLIT_READ_COUNT` int(11),
  `TUMOR_SPLIT_READ_COUNT` int(11),
  `ANNOTATION` varchar(255),
  `BREAKPOINT_TYPE` varchar(25),
  `CONNECTION_TYPE` varchar(25),
  `EVENT_INFO` varchar(255),
  `CLASS` varchar(25),
  `LENGTH` int(11),
  `COMMENTS` varchar(255),
  `SV_STATUS` varchar(25) NOT NULL DEFAULT 'SOMATIC' COMMENT 'GERMLINE or SOMATIC.',
  `ANNOTATION_JSON` JSON,
  PRIMARY KEY (`INTERNAL_ID`),
  FOREIGN KEY (`SAMPLE_ID`) REFERENCES `sample` (`INTERNAL_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`SITE1_ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`SITE2_ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`GENETIC_PROFILE_ID`) REFERENCES `genetic_profile` (`GENETIC_PROFILE_ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `alteration_driver_annotation` (
  `ALTERATION_EVENT_ID` int(255) NOT NULL,
  `GENETIC_PROFILE_ID` int(11) NOT NULL,
  `SAMPLE_ID` int(11) NOT NULL,
  `DRIVER_FILTER` VARCHAR(20),
  `DRIVER_FILTER_ANNOTATION` VARCHAR(80),
  `DRIVER_TIERS_FILTER` VARCHAR(50),
  `DRIVER_TIERS_FILTER_ANNOTATION` VARCHAR(80),
  PRIMARY KEY (`ALTERATION_EVENT_ID`, `GENETIC_PROFILE_ID`, `SAMPLE_ID`),
  FOREIGN KEY (`GENETIC_PROFILE_ID`) REFERENCES `genetic_profile` (`GENETIC_PROFILE_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`SAMPLE_ID`) REFERENCES `sample` (`INTERNAL_ID`) ON DELETE CASCADE,
  INDEX (`DRIVER_FILTER`),
  INDEX (`DRIVER_TIERS_FILTER`)
) COMMENT='Alteration driver annotation';

-- --------------------------------------------------------
CREATE TABLE `mutation_event` (
  `MUTATION_EVENT_ID` int(255) NOT NULL auto_increment,
  `ENTREZ_GENE_ID` int(11) NOT NULL,
  `CHR` varchar(5),
  `START_POSITION` bigint(20),
  `END_POSITION` bigint(20),
  `REFERENCE_ALLELE` text,
  `TUMOR_SEQ_ALLELE` text,
  `PROTEIN_CHANGE` varchar(255),
  `MUTATION_TYPE` varchar(255) COMMENT 'e.g. Missense, Nonsence, etc.',
  `NCBI_BUILD` varchar(10),
  `STRAND` varchar(2),
  `VARIANT_TYPE` varchar(15),
  `DB_SNP_RS` varchar(25),
  `DB_SNP_VAL_STATUS` varchar(255),
  `REFSEQ_MRNA_ID` varchar(64),
  `CODON_CHANGE` varchar(255),
  `UNIPROT_ACCESSION` varchar(64),
  `PROTEIN_POS_START` int(11),
  `PROTEIN_POS_END` int(11),
  `CANONICAL_TRANSCRIPT` boolean,
  `KEYWORD` varchar(255) DEFAULT NULL COMMENT 'e.g. truncating, V200 Missense, E338del, ',
  KEY (`KEYWORD`),
  PRIMARY KEY (`MUTATION_EVENT_ID`),
  KEY `KEY_MUTATION_EVENT_DETAILS` (`CHR`, `START_POSITION`, `END_POSITION`, `TUMOR_SEQ_ALLELE`(240), `ENTREZ_GENE_ID`, `PROTEIN_CHANGE`, `MUTATION_TYPE`),
  FOREIGN KEY (`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`),
  INDEX (`MUTATION_TYPE`)
) COMMENT='Mutation Data';

-- --------------------------------------------------------
CREATE TABLE `mutation` (
  `MUTATION_EVENT_ID` int(255) NOT NULL,
  `GENETIC_PROFILE_ID` int(11) NOT NULL,
  `SAMPLE_ID` int(11) NOT NULL,
  `ENTREZ_GENE_ID` int(11) NOT NULL,
  `CENTER` varchar(100),
  `SEQUENCER` varchar(255),
  `MUTATION_STATUS` varchar(25) COMMENT 'Germline, Somatic or LOH.',
  `VALIDATION_STATUS` varchar(25),
  `TUMOR_SEQ_ALLELE1` TEXT,
  `TUMOR_SEQ_ALLELE2` TEXT,
  `MATCHED_NORM_SAMPLE_BARCODE` varchar(255),
  `MATCH_NORM_SEQ_ALLELE1` TEXT,
  `MATCH_NORM_SEQ_ALLELE2` TEXT,
  `TUMOR_VALIDATION_ALLELE1` TEXT,
  `TUMOR_VALIDATION_ALLELE2` TEXT,
  `MATCH_NORM_VALIDATION_ALLELE1` TEXT,
  `MATCH_NORM_VALIDATION_ALLELE2` TEXT,
  `VERIFICATION_STATUS` varchar(10),
  `SEQUENCING_PHASE` varchar(100),
  `SEQUENCE_SOURCE` varchar(255) NOT NULL,
  `VALIDATION_METHOD` varchar(255),
  `SCORE` varchar(100),
  `BAM_FILE` varchar(255),
  `TUMOR_ALT_COUNT` int(11),
  `TUMOR_REF_COUNT` int(11),
  `NORMAL_ALT_COUNT` int(11),
  `NORMAL_REF_COUNT` int(11),
  `AMINO_ACID_CHANGE` varchar(255),
  `ANNOTATION_JSON` JSON,
  UNIQUE KEY `UQ_MUTATION_EVENT_ID_GENETIC_PROFILE_ID_SAMPLE_ID` (`MUTATION_EVENT_ID`,`GENETIC_PROFILE_ID`,`SAMPLE_ID`), -- Constraint to block duplicated mutation entries
  KEY (`GENETIC_PROFILE_ID`,`ENTREZ_GENE_ID`),
  KEY (`GENETIC_PROFILE_ID`,`SAMPLE_ID`),
  KEY (`GENETIC_PROFILE_ID`),
  KEY (`ENTREZ_GENE_ID`),
  KEY (`SAMPLE_ID`),
  KEY (`MUTATION_EVENT_ID`),
  FOREIGN KEY (`MUTATION_EVENT_ID`) REFERENCES `mutation_event` (`MUTATION_EVENT_ID`),
  FOREIGN KEY (`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`),
  FOREIGN KEY (`GENETIC_PROFILE_ID`) REFERENCES `genetic_profile` (`GENETIC_PROFILE_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`SAMPLE_ID`) REFERENCES `sample` (`INTERNAL_ID`) ON DELETE CASCADE
) COMMENT='Mutation Data Details';

-- --------------------------------------------------------
CREATE TABLE `mutation_count_by_keyword` (
    `GENETIC_PROFILE_ID` int(11) NOT NULL,
    `KEYWORD` varchar(255) DEFAULT NULL,
    `ENTREZ_GENE_ID` int(11) NOT NULL,
    `KEYWORD_COUNT` int NOT NULL,
    `GENE_COUNT` int NOT NULL,
    KEY (`GENETIC_PROFILE_ID`,`KEYWORD`),
    FOREIGN KEY (`GENETIC_PROFILE_ID`) REFERENCES `genetic_profile` (`GENETIC_PROFILE_ID`) ON DELETE CASCADE,
    FOREIGN KEY (`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `clinical_patient` (
  `INTERNAL_ID` int(11) NOT NULL,
  `ATTR_ID` varchar(255) NOT NULL,
  `ATTR_VALUE` varchar(255) NOT NULL,
  PRIMARY KEY (`INTERNAL_ID`, `ATTR_ID`),
  FOREIGN KEY (`INTERNAL_ID`) REFERENCES `patient` (`INTERNAL_ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `clinical_sample` (
  `INTERNAL_ID` int(11) NOT NULL,
  `ATTR_ID` varchar(255) NOT NULL,
  `ATTR_VALUE` varchar(255) NOT NULL,
  PRIMARY KEY (`INTERNAL_ID`,`ATTR_ID`),
  FOREIGN KEY (`INTERNAL_ID`) REFERENCES `sample` (`INTERNAL_ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `clinical_attribute_meta` (
  `ATTR_ID` varchar(255) NOT NULL,
  `DISPLAY_NAME` varchar(255) NOT NULL,
  `DESCRIPTION` varchar(2048) NOT NULL,
  `DATATYPE` varchar(255) NOT NULL COMMENT 'NUMBER, BOOLEAN, or STRING',
  `PATIENT_ATTRIBUTE` BOOLEAN NOT NULL,
  `PRIORITY` varchar(255) NOT NULL,
  `CANCER_STUDY_ID` int(11) NOT NULL,
  PRIMARY KEY (`ATTR_ID`,`CANCER_STUDY_ID`),
  FOREIGN KEY (`CANCER_STUDY_ID`) REFERENCES `cancer_study` (`CANCER_STUDY_ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `mut_sig` (
  `CANCER_STUDY_ID` int(11) NOT NULL,
  `ENTREZ_GENE_ID` int(11) NOT NULL,
  `RANK` int(11) NOT NULL,
  `NumBasesCovered` int(11) NOT NULL,
  `NumMutations` int(11) NOT NULL,
  `P_VALUE` float NOT NULL,
  `Q_VALUE` float NOT NULL,
  PRIMARY KEY (`CANCER_STUDY_ID`, `ENTREZ_GENE_ID`),
  FOREIGN KEY (`CANCER_STUDY_ID`) REFERENCES `cancer_study` (`CANCER_STUDY_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`)
);

-- --------------------------------------------------------
CREATE TABLE `gistic` (
  `GISTIC_ROI_ID` bigint(20) NOT NULL auto_increment,
  `CANCER_STUDY_ID` int(11) NOT NULL,
  `CHROMOSOME` int(11) NOT NULL,
  `CYTOBAND` varchar(255) NOT NULL,
  `WIDE_PEAK_START` int(11) NOT NULL,
  `WIDE_PEAK_END` int(11) NOT NULL,
  `Q_VALUE` double NOT NULL,
  `AMP` tinyint(1) NOT NULL,
  PRIMARY KEY (`GISTIC_ROI_ID`),
  FOREIGN KEY (`CANCER_STUDY_ID`) REFERENCES `cancer_study` (`CANCER_STUDY_ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `gistic_to_gene` (
  `GISTIC_ROI_ID` bigint(20) NOT NULL,
  `ENTREZ_GENE_ID` int(11) NOT NULL,
  PRIMARY KEY(`GISTIC_ROI_ID`, `ENTREZ_GENE_ID`),
  FOREIGN KEY (`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`),
  FOREIGN KEY (`GISTIC_ROI_ID`) REFERENCES `gistic` (`GISTIC_ROI_ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `cna_event` (
  `CNA_EVENT_ID` int(255) NOT NULL auto_increment,
  `ENTREZ_GENE_ID` int(11) NOT NULL,
  `ALTERATION` tinyint NOT NULL,
  PRIMARY KEY (`CNA_EVENT_ID`),
  UNIQUE (`ENTREZ_GENE_ID`, `ALTERATION`),
  FOREIGN KEY (`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`),
  INDEX (`ALTERATION`)
);

-- --------------------------------------------------------
CREATE TABLE `sample_cna_event` (
  `CNA_EVENT_ID` int(255) NOT NULL,
  `SAMPLE_ID` int(11) NOT NULL,
  `GENETIC_PROFILE_ID` int(11) NOT NULL,
  `ANNOTATION_JSON` JSON,
  KEY (`GENETIC_PROFILE_ID`,`SAMPLE_ID`),
  PRIMARY KEY (`CNA_EVENT_ID`, `SAMPLE_ID`, `GENETIC_PROFILE_ID`),
  FOREIGN KEY (`CNA_EVENT_ID`) REFERENCES `cna_event` (`CNA_EVENT_ID`),
  FOREIGN KEY (`GENETIC_PROFILE_ID`) REFERENCES `genetic_profile` (`GENETIC_PROFILE_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`SAMPLE_ID`) REFERENCES `sample` (`INTERNAL_ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `copy_number_seg` (
  `SEG_ID` bigint(20) NOT NULL auto_increment,
  `CANCER_STUDY_ID` int(11) NOT NULL,
  `SAMPLE_ID` int(11) NOT NULL,
  `CHR` varchar(5) NOT NULL,
  `START` int(11) NOT NULL,
  `END` int(11) NOT NULL,
  `NUM_PROBES` int(11) NOT NULL,
  `SEGMENT_MEAN` double NOT NULL,
  KEY (`CANCER_STUDY_ID`,`SAMPLE_ID`),
  PRIMARY KEY (`SEG_ID`),
  FOREIGN KEY (`CANCER_STUDY_ID`) REFERENCES `cancer_study` (`CANCER_STUDY_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`SAMPLE_ID`) REFERENCES `sample` (`INTERNAL_ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `copy_number_seg_file` (
  `SEG_FILE_ID` int(11) NOT NULL auto_increment,
  `CANCER_STUDY_ID` int(11) NOT NULL,
  `REFERENCE_GENOME_ID` varchar(10) NOT NULL,
  `DESCRIPTION` varchar(255) NOT NULL,
  `FILENAME` varchar(255) NOT NULL,
  PRIMARY KEY(`SEG_FILE_ID`),
  FOREIGN KEY (`CANCER_STUDY_ID`) REFERENCES `cancer_study` (`CANCER_STUDY_ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `cosmic_mutation` (
  `COSMIC_MUTATION_ID` varchar(30) NOT NULL,
  `CHR` varchar(5),
  `START_POSITION` bigint(20),
  `REFERENCE_ALLELE` varchar(255),
  `TUMOR_SEQ_ALLELE` varchar(255),
  `STRAND` varchar(2),
  `CODON_CHANGE` varchar(255),
  `ENTREZ_GENE_ID` int(11) NOT NULL,
  `PROTEIN_CHANGE` varchar(255) NOT NULL,
  `COUNT` int(11) NOT NULL,
  `KEYWORD` varchar(50) DEFAULT NULL,
  KEY (`KEYWORD`),
  PRIMARY KEY (`COSMIC_MUTATION_ID`),
  FOREIGN KEY (`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`)
);

-- --------------------------------------------------------
CREATE TABLE `clinical_event` (
  `CLINICAL_EVENT_ID` int NOT NULL auto_increment,
  `PATIENT_ID`  int(11) NOT NULL,
  `START_DATE` int NOT NULL,
  `STOP_DATE` int,
  `EVENT_TYPE` varchar(20) NOT NULL,
  PRIMARY KEY (`CLINICAL_EVENT_ID`),
  KEY (`PATIENT_ID`, `EVENT_TYPE`),
  FOREIGN KEY (`PATIENT_ID`) REFERENCES `patient` (`INTERNAL_ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `clinical_event_data` (
  `CLINICAL_EVENT_ID` int(255) NOT NULL,
  `KEY` varchar(255) NOT NULL,
  `VALUE` varchar(5000) NOT NULL,
  FOREIGN KEY (`CLINICAL_EVENT_ID`) REFERENCES `clinical_event` (`CLINICAL_EVENT_ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `reference_genome_gene` (
    `ENTREZ_GENE_ID` int(11) NOT NULL,
    `REFERENCE_GENOME_ID` int(4) NOT NULL,
    `CHR` varchar(5) DEFAULT NULL,
    `CYTOBAND` varchar(64) DEFAULT NULL,
    `START` bigint(20) DEFAULT NULL,
    `END` bigint(20) DEFAULT NULL,
    PRIMARY KEY (`ENTREZ_GENE_ID`,`REFERENCE_GENOME_ID`),
    FOREIGN KEY (`REFERENCE_GENOME_ID`) REFERENCES `reference_genome` (`REFERENCE_GENOME_ID`) ON DELETE CASCADE,
    FOREIGN KEY (`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `data_access_tokens` (
    `TOKEN` varchar(50) NOT NULL,
    `USERNAME` varchar(128) NOT NULL,
    `EXPIRATION` datetime NOT NULL,
    `CREATION` datetime NOT NULL,
    PRIMARY KEY (`TOKEN`),
    FOREIGN KEY (`USERNAME`) REFERENCES `users` (`EMAIL`) ON DELETE CASCADE
);
-- --------------------------------------------------------
CREATE TABLE `allele_specific_copy_number` (
    `MUTATION_EVENT_ID` int(255) NOT NULL,
    `GENETIC_PROFILE_ID` int(11) NOT NULL,
    `SAMPLE_ID` int(11) NOT NULL,
    `ASCN_INTEGER_COPY_NUMBER` int DEFAULT NULL,
    `ASCN_METHOD` varchar(24) NOT NULL,
    `CCF_EXPECTED_COPIES_UPPER` float DEFAULT NULL,
    `CCF_EXPECTED_COPIES` float DEFAULT NULL,
    `CLONAL` varchar(16) DEFAULT NULL,
    `MINOR_COPY_NUMBER` int DEFAULT NULL,
    `EXPECTED_ALT_COPIES` int DEFAULT NULL,
    `TOTAL_COPY_NUMBER` int DEFAULT NULL,
    UNIQUE KEY `UQ_ASCN_MUTATION_EVENT_ID_GENETIC_PROFILE_ID_SAMPLE_ID` (`MUTATION_EVENT_ID`,`GENETIC_PROFILE_ID`,`SAMPLE_ID`), -- Constraint to block duplicated mutation entries
    FOREIGN KEY (`MUTATION_EVENT_ID`) REFERENCES `mutation_event` (`MUTATION_EVENT_ID`),
    FOREIGN KEY (`GENETIC_PROFILE_ID`) REFERENCES `genetic_profile` (`GENETIC_PROFILE_ID`) ON DELETE CASCADE,
    FOREIGN KEY (`SAMPLE_ID`) REFERENCES `sample` (`INTERNAL_ID`) ON DELETE CASCADE
);
-- --------------------------------------------------------
CREATE TABLE `info` (
  `DB_SCHEMA_VERSION` varchar(24),
  `GENESET_VERSION` varchar(24)
);

-- --------------------------------------------------------
CREATE TABLE `resource_definition` (
  `RESOURCE_ID` varchar(255) NOT NULL,
  `DISPLAY_NAME` varchar(255) NOT NULL,
  `DESCRIPTION` varchar(2048) DEFAULT NULL,
  `RESOURCE_TYPE` ENUM('STUDY', 'PATIENT', 'SAMPLE') NOT NULL,
  `OPEN_BY_DEFAULT` BOOLEAN DEFAULT 0,
  `PRIORITY` int(11) NOT NULL,
  `CANCER_STUDY_ID` int(11) NOT NULL,
  PRIMARY KEY (`RESOURCE_ID`,`CANCER_STUDY_ID`),
  FOREIGN KEY (`CANCER_STUDY_ID`) REFERENCES `cancer_study` (`CANCER_STUDY_ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `resource_sample` (
  `INTERNAL_ID` int(11) NOT NULL,
  `RESOURCE_ID` varchar(255) NOT NULL,
  `URL` varchar(255) NOT NULL,
  PRIMARY KEY (`INTERNAL_ID`, `RESOURCE_ID`, `URL`),
  FOREIGN KEY (`INTERNAL_ID`) REFERENCES `sample` (`INTERNAL_ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `resource_patient` (
  `INTERNAL_ID` int(11) NOT NULL,
  `RESOURCE_ID` varchar(255) NOT NULL,
  `URL` varchar(255) NOT NULL,
  PRIMARY KEY (`INTERNAL_ID`, `RESOURCE_ID`, `URL`),
  FOREIGN KEY (`INTERNAL_ID`) REFERENCES `patient` (`INTERNAL_ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `resource_study` (
  `INTERNAL_ID` int(11) NOT NULL,
  `RESOURCE_ID` varchar(255) NOT NULL,
  `URL` varchar(255) NOT NULL,
  PRIMARY KEY (`INTERNAL_ID`, `RESOURCE_ID`, `URL`),
  FOREIGN KEY (`INTERNAL_ID`) REFERENCES `cancer_study` (`CANCER_STUDY_ID`) ON DELETE CASCADE
);

-- THIS MUST BE KEPT IN SYNC WITH db.version PROPERTY IN pom.xml
INSERT INTO info VALUES ('2.13.1', NULL);


SET FOREIGN_KEY_CHECKS=0;

INSERT INTO type_of_cancer (TYPE_OF_CANCER_ID,NAME,DEDICATED_COLOR,SHORT_NAME,PARENT) VALUES ('brca','Breast Invasive Carcinoma','HotPink','Breast','tissue');
INSERT INTO type_of_cancer (TYPE_OF_CANCER_ID,NAME,DEDICATED_COLOR,SHORT_NAME,PARENT) VALUES ('acc','Adrenocortical Carcinoma','Purple','ACC','adrenal_gland');

INSERT INTO `reference_genome` VALUES (1, 'human', 'hg19', 'GRCh37', NULL, 'http://hgdownload.cse.ucsc.edu/goldenPath/hg19/bigZips', '2009-02-01 00:00:00');
INSERT INTO `reference_genome` VALUES (2, 'human', 'hg38', 'GRCh38', NULL, 'http://hgdownload.cse.ucsc.edu/goldenPath/hg38/bigZips', '2013-12-01 00:00:00');

INSERT INTO cancer_study (CANCER_STUDY_ID,CANCER_STUDY_IDENTIFIER,TYPE_OF_CANCER_ID,NAME,DESCRIPTION,PUBLIC,PMID,CITATION,GROUPS,STATUS,IMPORT_DATE,REFERENCE_GENOME_ID) VALUES(1,'study_tcga_pub','brca','Breast Invasive Carcinoma (TCGA, Nature 2012)','<a href=\"http://cancergenome.nih.gov/\">The Cancer Genome Atlas (TCGA)</a> Breast Invasive Carcinoma project. 825 cases.<br><i>Nature 2012.</i> <a href=\"http://tcga-data.nci.nih.gov/tcga/\">Raw data via the TCGA Data Portal</a>.',1,'23000897,26451490','TCGA, Nature 2012, ...','SU2C-PI3K;PUBLIC;GDAC',0,'2011-12-18 13:17:17',1);
INSERT INTO cancer_study (CANCER_STUDY_ID,CANCER_STUDY_IDENTIFIER,TYPE_OF_CANCER_ID,NAME,DESCRIPTION,PUBLIC,PMID,CITATION,GROUPS,STATUS,IMPORT_DATE,REFERENCE_GENOME_ID) VALUES(2,'acc_tcga','acc','Adrenocortical Carcinoma (TCGA, Provisional)','TCGA Adrenocortical Carcinoma; raw data at the <A HREF="https://tcga-data.nci.nih.gov/">NCI</A>.',1,'23000897','TCGA, Nature 2012','SU2C-PI3K;PUBLIC;GDAC',0,'2013-10-12 11:11:15',1);

INSERT INTO cancer_study_tags (CANCER_STUDY_ID,TAGS) VALUES(1,'{"Analyst": {"Name": "Jack", "Email": "jack@something.com"}, "Load id": 35}');
INSERT INTO cancer_study_tags (CANCER_STUDY_ID,TAGS) VALUES(2,'{"Load id": 36}');

INSERT INTO genetic_entity (ID,ENTITY_TYPE,STABLE_ID) VALUES (1,'GENE', NULL);
INSERT INTO genetic_entity (ID,ENTITY_TYPE,STABLE_ID) VALUES (2,'GENE', NULL);
INSERT INTO genetic_entity (ID,ENTITY_TYPE,STABLE_ID) VALUES (3,'GENE', NULL);
INSERT INTO genetic_entity (ID,ENTITY_TYPE,STABLE_ID) VALUES (4,'GENE', NULL);
INSERT INTO genetic_entity (ID,ENTITY_TYPE,STABLE_ID) VALUES (5,'GENE', NULL);
INSERT INTO genetic_entity (ID,ENTITY_TYPE,STABLE_ID) VALUES (6,'GENE', NULL);
INSERT INTO genetic_entity (ID,ENTITY_TYPE,STABLE_ID) VALUES (7,'GENE', NULL);
INSERT INTO genetic_entity (ID,ENTITY_TYPE,STABLE_ID) VALUES (8,'GENE', NULL);
INSERT INTO genetic_entity (ID,ENTITY_TYPE,STABLE_ID) VALUES (9,'GENE', NULL);
INSERT INTO genetic_entity (ID,ENTITY_TYPE,STABLE_ID) VALUES (10,'GENE', NULL);
INSERT INTO genetic_entity (ID,ENTITY_TYPE,STABLE_ID) VALUES (11,'GENE', NULL);
INSERT INTO genetic_entity (ID,ENTITY_TYPE,STABLE_ID) VALUES (12,'GENE', NULL);
INSERT INTO genetic_entity (ID,ENTITY_TYPE,STABLE_ID) VALUES (13,'GENE', NULL);
INSERT INTO genetic_entity (ID,ENTITY_TYPE,STABLE_ID) VALUES (14,'GENE', NULL);
INSERT INTO genetic_entity (ID,ENTITY_TYPE,STABLE_ID) VALUES (15,'GENE', NULL);
INSERT INTO genetic_entity (ID,ENTITY_TYPE,STABLE_ID) VALUES (16,'GENE', NULL);
INSERT INTO genetic_entity (ID,ENTITY_TYPE,STABLE_ID) VALUES (17,'GENESET', NULL);
INSERT INTO genetic_entity (ID,ENTITY_TYPE,STABLE_ID) VALUES (18,'GENESET', NULL);
INSERT INTO genetic_entity (ID,ENTITY_TYPE,STABLE_ID) VALUES (19,'GENERIC_ASSAY','17-AAG');
INSERT INTO genetic_entity (ID,ENTITY_TYPE,STABLE_ID) VALUES (20,'GENERIC_ASSAY','AEW541');
INSERT INTO genetic_entity (ID,ENTITY_TYPE,STABLE_ID) VALUES (28,'GENERIC_ASSAY','mean_1');
INSERT INTO genetic_entity (ID,ENTITY_TYPE,STABLE_ID) VALUES (29,'GENERIC_ASSAY','mean_2');

INSERT INTO gene (ENTREZ_GENE_ID,HUGO_GENE_SYMBOL,GENETIC_ENTITY_ID,TYPE) VALUES(207,'AKT1',1,'protein-coding');
INSERT INTO gene (ENTREZ_GENE_ID,HUGO_GENE_SYMBOL,GENETIC_ENTITY_ID,TYPE) VALUES(208,'AKT2',2,'protein-coding');
INSERT INTO gene (ENTREZ_GENE_ID,HUGO_GENE_SYMBOL,GENETIC_ENTITY_ID,TYPE) VALUES(10000,'AKT3',3,'protein-coding');
INSERT INTO gene (ENTREZ_GENE_ID,HUGO_GENE_SYMBOL,GENETIC_ENTITY_ID,TYPE) VALUES(369,'ARAF',4,'protein-coding');
INSERT INTO gene (ENTREZ_GENE_ID,HUGO_GENE_SYMBOL,GENETIC_ENTITY_ID,TYPE) VALUES(472,'ATM',5,'protein-coding');
INSERT INTO gene (ENTREZ_GENE_ID,HUGO_GENE_SYMBOL,GENETIC_ENTITY_ID,TYPE) VALUES(673,'BRAF',6,'protein-coding');
INSERT INTO gene (ENTREZ_GENE_ID,HUGO_GENE_SYMBOL,GENETIC_ENTITY_ID,TYPE) VALUES(672,'BRCA1',7,'protein-coding');
INSERT INTO gene (ENTREZ_GENE_ID,HUGO_GENE_SYMBOL,GENETIC_ENTITY_ID,TYPE) VALUES(675,'BRCA2',8,'protein-coding');
INSERT INTO gene (ENTREZ_GENE_ID,HUGO_GENE_SYMBOL,GENETIC_ENTITY_ID,TYPE) VALUES(3265,'HRAS',9,'protein-coding');
INSERT INTO gene (ENTREZ_GENE_ID,HUGO_GENE_SYMBOL,GENETIC_ENTITY_ID,TYPE) VALUES(3845,'KRAS',10,'protein-coding');
INSERT INTO gene (ENTREZ_GENE_ID,HUGO_GENE_SYMBOL,GENETIC_ENTITY_ID,TYPE) VALUES(4893,'NRAS',11,'protein-coding');
INSERT INTO gene (ENTREZ_GENE_ID,HUGO_GENE_SYMBOL,GENETIC_ENTITY_ID,TYPE) VALUES(79501,'OR4F5',12,'protein-coding');
INSERT INTO gene (ENTREZ_GENE_ID,HUGO_GENE_SYMBOL,GENETIC_ENTITY_ID,TYPE) VALUES(148398,'SAMD11',13,'protein-coding');
INSERT INTO gene (ENTREZ_GENE_ID,HUGO_GENE_SYMBOL,GENETIC_ENTITY_ID,TYPE) VALUES(26155,'NOC2L',14,'protein-coding');
INSERT INTO gene (ENTREZ_GENE_ID,HUGO_GENE_SYMBOL,GENETIC_ENTITY_ID,TYPE) VALUES(2064,'ERBB2',15,'protein-coding');
INSERT INTO gene (ENTREZ_GENE_ID,HUGO_GENE_SYMBOL,GENETIC_ENTITY_ID,TYPE) VALUES(2886,'GRB7',16,'protein-coding');
INSERT INTO cosmic_mutation (COSMIC_MUTATION_ID,PROTEIN_CHANGE,ENTREZ_GENE_ID,COUNT,KEYWORD) VALUES(3677745,'D45A',79501,1,'OR4F5 D45 missense');
INSERT INTO cosmic_mutation (COSMIC_MUTATION_ID,PROTEIN_CHANGE,ENTREZ_GENE_ID,COUNT,KEYWORD) VALUES(426644,'G145C',79501,1,'OR4F5 G145 missense');
INSERT INTO cosmic_mutation (COSMIC_MUTATION_ID,PROTEIN_CHANGE,ENTREZ_GENE_ID,COUNT,KEYWORD) VALUES(460103,'P23P',148398,1,'SAMD11 P23 silent');
INSERT INTO cosmic_mutation (COSMIC_MUTATION_ID,PROTEIN_CHANGE,ENTREZ_GENE_ID,COUNT,KEYWORD) VALUES(4010395,'S146S',26155,1,'NOC2L S146 silent');
INSERT INTO cosmic_mutation (COSMIC_MUTATION_ID,PROTEIN_CHANGE,ENTREZ_GENE_ID,COUNT,KEYWORD) VALUES(1290240,'M1T',26155,1,'NOC2L truncating');
INSERT INTO cosmic_mutation (COSMIC_MUTATION_ID,PROTEIN_CHANGE,ENTREZ_GENE_ID,COUNT,KEYWORD) VALUES(4010425,'Q197*',26155,1,'NOC2L truncating');

INSERT INTO gene_alias (ENTREZ_GENE_ID,GENE_ALIAS) VALUES (207,'AKT alias');
INSERT INTO gene_alias (ENTREZ_GENE_ID,GENE_ALIAS) VALUES (207,'AKT alias2');
INSERT INTO gene_alias (ENTREZ_GENE_ID,GENE_ALIAS) VALUES (675,'BRCA1 alias');

INSERT INTO reference_genome_gene (ENTREZ_GENE_ID,CYTOBAND,`START`,`END`,CHR,REFERENCE_GENOME_ID) VALUES(207,'14q32.33',105235686,105262088,14,1);
INSERT INTO reference_genome_gene (ENTREZ_GENE_ID,CYTOBAND,`START`,`END`,CHR,REFERENCE_GENOME_ID) VALUES(207,'14q32.33',104769349,104795751,14,2);
INSERT INTO reference_genome_gene (ENTREZ_GENE_ID,CYTOBAND,`START`,`END`,CHR,REFERENCE_GENOME_ID) VALUES(208,'19q13.2', 40736224, 40791443,19,1);
INSERT INTO reference_genome_gene (ENTREZ_GENE_ID,CYTOBAND,`START`,`END`,CHR,REFERENCE_GENOME_ID) VALUES(208,'19q13.2', 40230317, 40285536,19,2);

INSERT INTO genetic_profile (GENETIC_PROFILE_ID,STABLE_ID,CANCER_STUDY_ID,GENETIC_ALTERATION_TYPE,DATATYPE,NAME,DESCRIPTION,SHOW_PROFILE_IN_ANALYSIS_TAB) VALUES (2,'study_tcga_pub_gistic',1,'COPY_NUMBER_ALTERATION','DISCRETE','Putative copy-number alterations from GISTIC','Putative copy-number from GISTIC 2.0. Values: -2 = homozygous deletion; -1 = hemizygous deletion; 0 = neutral / no change; 1 = gain; 2 = high level amplification.',1);
INSERT INTO genetic_profile (GENETIC_PROFILE_ID,STABLE_ID,CANCER_STUDY_ID,GENETIC_ALTERATION_TYPE,DATATYPE,NAME,DESCRIPTION,SHOW_PROFILE_IN_ANALYSIS_TAB) VALUES (3,'study_tcga_pub_mrna',1,'MRNA_EXPRESSION','Z-SCORE','mRNA expression (microarray)','Expression levels (Agilent microarray).',0);
INSERT INTO genetic_profile (GENETIC_PROFILE_ID,STABLE_ID,CANCER_STUDY_ID,GENETIC_ALTERATION_TYPE,DATATYPE,NAME,DESCRIPTION,SHOW_PROFILE_IN_ANALYSIS_TAB) VALUES (10,'study_tcga_pub_m_na',1,'MRNA_EXPRESSION','Z-SCORE','mRNA expression (microarray)','Expression levels (Agilent microarray).',0);
INSERT INTO genetic_profile (GENETIC_PROFILE_ID,STABLE_ID,CANCER_STUDY_ID,GENETIC_ALTERATION_TYPE,DATATYPE,NAME,DESCRIPTION,SHOW_PROFILE_IN_ANALYSIS_TAB) VALUES (4,'study_tcga_pub_log2CNA',1,'COPY_NUMBER_ALTERATION','LOG2-VALUE','Log2 copy-number values','Log2 copy-number VALUESfor each gene (from Affymetrix SNP6).',0);
INSERT INTO genetic_profile (GENETIC_PROFILE_ID,STABLE_ID,CANCER_STUDY_ID,GENETIC_ALTERATION_TYPE,DATATYPE,NAME,DESCRIPTION,SHOW_PROFILE_IN_ANALYSIS_TAB) VALUES (5,'study_tcga_pub_methylation_hm27',1,'METHYLATION','CONTINUOUS','Methylation (HM27)','Methylation beta-VALUES (HM27 platform). For genes with multiple methylation probes, the probe least correlated with expression is selected.',0);
INSERT INTO genetic_profile (GENETIC_PROFILE_ID,STABLE_ID,CANCER_STUDY_ID,GENETIC_ALTERATION_TYPE,DATATYPE,NAME,DESCRIPTION,SHOW_PROFILE_IN_ANALYSIS_TAB) VALUES (6,'study_tcga_pub_mutations',1,'MUTATION_EXTENDED','MAF','Mutations','Mutation data from whole exome sequencing.',1);
INSERT INTO genetic_profile (GENETIC_PROFILE_ID,STABLE_ID,CANCER_STUDY_ID,GENETIC_ALTERATION_TYPE,DATATYPE,NAME,DESCRIPTION,SHOW_PROFILE_IN_ANALYSIS_TAB) VALUES (7,'study_tcga_pub_sv',1,'STRUCTURAL_VARIANT','SV','Structural Variants','Structural Variants detected by Illumina HiSeq sequencing.',1);
INSERT INTO genetic_profile (GENETIC_PROFILE_ID,STABLE_ID,CANCER_STUDY_ID,GENETIC_ALTERATION_TYPE,DATATYPE,NAME,DESCRIPTION,SHOW_PROFILE_IN_ANALYSIS_TAB) VALUES (8,'acc_tcga_mutations',2,'MUTATION_EXTENDED','MAF','Mutations','Mutation data from whole exome sequencing.',1);
INSERT INTO genetic_profile (GENETIC_PROFILE_ID,STABLE_ID,CANCER_STUDY_ID,GENETIC_ALTERATION_TYPE,DATATYPE,NAME,DESCRIPTION,SHOW_PROFILE_IN_ANALYSIS_TAB) VALUES (13,'acc_tcga_sv',2,'STRUCTURAL_VARIANT','SV','Structural Variants','Structural Variants detected by Illumina HiSeq sequencing.',1);
INSERT INTO genetic_profile (GENETIC_PROFILE_ID,STABLE_ID,CANCER_STUDY_ID,GENETIC_ALTERATION_TYPE,DATATYPE,NAME,DESCRIPTION,SHOW_PROFILE_IN_ANALYSIS_TAB) VALUES (9,'study_tcga_pub_gsva_scores',1,'GENESET_SCORE','GSVA-SCORE','GSVA scores','GSVA scores for oncogenic signature gene sets from MsigDB calculated with GSVA version 1.22.4, R version 3.3.2.',1);
INSERT INTO genetic_profile (GENETIC_PROFILE_ID,STABLE_ID,CANCER_STUDY_ID,GENETIC_ALTERATION_TYPE,DATATYPE,NAME,DESCRIPTION,SHOW_PROFILE_IN_ANALYSIS_TAB,GENERIC_ASSAY_TYPE) VALUES (11,'study_tcga_pub_treatment_ic50',1,'GENERIC_ASSAY','IC50','Treatment IC50 values','Treatment response IC50 values',1,'TREATMENT_RESPONSE');
INSERT INTO genetic_profile (GENETIC_PROFILE_ID,STABLE_ID,CANCER_STUDY_ID,GENETIC_ALTERATION_TYPE,DATATYPE,NAME,DESCRIPTION,SHOW_PROFILE_IN_ANALYSIS_TAB,GENERIC_ASSAY_TYPE) VALUES (12,'study_tcga_pub_mutational_signature',1,'GENERIC_ASSAY','LIMIT-VALUE','mutational_signature values','mutational_signature values',1,'MUTATIONAL_SIGNATURE');

INSERT INTO genetic_profile_samples (GENETIC_PROFILE_ID,ORDERED_SAMPLE_LIST) VALUES (2,'1,2,3,4,5,6,7,8,9,10,11,12,13,14,');
INSERT INTO genetic_profile_samples (GENETIC_PROFILE_ID,ORDERED_SAMPLE_LIST) VALUES (3,'2,3,6,8,9,10,12,13,');
INSERT INTO genetic_profile_samples (GENETIC_PROFILE_ID,ORDERED_SAMPLE_LIST) VALUES (4,'1,2,3,4,5,6,7,8,9,10,11,12,13,14,');
INSERT INTO genetic_profile_samples (GENETIC_PROFILE_ID,ORDERED_SAMPLE_LIST) VALUES (5,'2,');
INSERT INTO genetic_profile_samples (GENETIC_PROFILE_ID,ORDERED_SAMPLE_LIST) VALUES (11,'1,2,3,4,5,6,7,8,9,10,11,12,13,14,');

INSERT INTO patient (INTERNAL_ID,STABLE_ID,CANCER_STUDY_ID) VALUES (1,'TCGA-A1-A0SB',1);
INSERT INTO patient (INTERNAL_ID,STABLE_ID,CANCER_STUDY_ID) VALUES (2,'TCGA-A1-A0SD',1);
INSERT INTO patient (INTERNAL_ID,STABLE_ID,CANCER_STUDY_ID) VALUES (3,'TCGA-A1-A0SE',1);
INSERT INTO patient (INTERNAL_ID,STABLE_ID,CANCER_STUDY_ID) VALUES (4,'TCGA-A1-A0SF',1);
INSERT INTO patient (INTERNAL_ID,STABLE_ID,CANCER_STUDY_ID) VALUES (5,'TCGA-A1-A0SG',1);
INSERT INTO patient (INTERNAL_ID,STABLE_ID,CANCER_STUDY_ID) VALUES (6,'TCGA-A1-A0SH',1);
INSERT INTO patient (INTERNAL_ID,STABLE_ID,CANCER_STUDY_ID) VALUES (7,'TCGA-A1-A0SI',1);
INSERT INTO patient (INTERNAL_ID,STABLE_ID,CANCER_STUDY_ID) VALUES (8,'TCGA-A1-A0SJ',1);
INSERT INTO patient (INTERNAL_ID,STABLE_ID,CANCER_STUDY_ID) VALUES (9,'TCGA-A1-A0SK',1);
INSERT INTO patient (INTERNAL_ID,STABLE_ID,CANCER_STUDY_ID) VALUES (10,'TCGA-A1-A0SM',1);
INSERT INTO patient (INTERNAL_ID,STABLE_ID,CANCER_STUDY_ID) VALUES (11,'TCGA-A1-A0SN',1);
INSERT INTO patient (INTERNAL_ID,STABLE_ID,CANCER_STUDY_ID) VALUES (12,'TCGA-A1-A0SO',1);
INSERT INTO patient (INTERNAL_ID,STABLE_ID,CANCER_STUDY_ID) VALUES (13,'TCGA-A1-A0SP',1);
INSERT INTO patient (INTERNAL_ID,STABLE_ID,CANCER_STUDY_ID) VALUES (14,'TCGA-A1-A0SQ',1);
INSERT INTO patient (INTERNAL_ID,STABLE_ID,CANCER_STUDY_ID) VALUES (15,'TCGA-A1-B0SO',2);
INSERT INTO patient (INTERNAL_ID,STABLE_ID,CANCER_STUDY_ID) VALUES (16,'TCGA-A1-B0SP',2);
INSERT INTO patient (INTERNAL_ID,STABLE_ID,CANCER_STUDY_ID) VALUES (17,'TCGA-A1-B0SQ',2);
INSERT INTO patient (INTERNAL_ID,STABLE_ID,CANCER_STUDY_ID) VALUES (18,'TCGA-A1-A0SB',2);
INSERT INTO genetic_profile_samples (GENETIC_PROFILE_ID,ORDERED_SAMPLE_LIST) VALUES(10,'1,2,3,4,5,6,7,8,9,10,11,');

INSERT INTO sample (INTERNAL_ID,STABLE_ID,SAMPLE_TYPE,PATIENT_ID) VALUES (1,'TCGA-A1-A0SB-01','Primary Solid Tumor',1);
INSERT INTO sample (INTERNAL_ID,STABLE_ID,SAMPLE_TYPE,PATIENT_ID) VALUES (2,'TCGA-A1-A0SD-01','Primary Solid Tumor',2);
INSERT INTO sample (INTERNAL_ID,STABLE_ID,SAMPLE_TYPE,PATIENT_ID) VALUES (3,'TCGA-A1-A0SE-01','Primary Solid Tumor',3);
INSERT INTO sample (INTERNAL_ID,STABLE_ID,SAMPLE_TYPE,PATIENT_ID) VALUES (4,'TCGA-A1-A0SF-01','Primary Solid Tumor',4);
INSERT INTO sample (INTERNAL_ID,STABLE_ID,SAMPLE_TYPE,PATIENT_ID) VALUES (5,'TCGA-A1-A0SG-01','Primary Solid Tumor',5);
INSERT INTO sample (INTERNAL_ID,STABLE_ID,SAMPLE_TYPE,PATIENT_ID) VALUES (6,'TCGA-A1-A0SH-01','Primary Solid Tumor',6);
INSERT INTO sample (INTERNAL_ID,STABLE_ID,SAMPLE_TYPE,PATIENT_ID) VALUES (7,'TCGA-A1-A0SI-01','Primary Solid Tumor',7);
INSERT INTO sample (INTERNAL_ID,STABLE_ID,SAMPLE_TYPE,PATIENT_ID) VALUES (8,'TCGA-A1-A0SJ-01','Primary Solid Tumor',8);
INSERT INTO sample (INTERNAL_ID,STABLE_ID,SAMPLE_TYPE,PATIENT_ID) VALUES (9,'TCGA-A1-A0SK-01','Primary Solid Tumor',9);
INSERT INTO sample (INTERNAL_ID,STABLE_ID,SAMPLE_TYPE,PATIENT_ID) VALUES (10,'TCGA-A1-A0SM-01','Primary Solid Tumor',10);
INSERT INTO sample (INTERNAL_ID,STABLE_ID,SAMPLE_TYPE,PATIENT_ID) VALUES (11,'TCGA-A1-A0SN-01','Primary Solid Tumor',11);
INSERT INTO sample (INTERNAL_ID,STABLE_ID,SAMPLE_TYPE,PATIENT_ID) VALUES (12,'TCGA-A1-A0SO-01','Primary Solid Tumor',12);
INSERT INTO sample (INTERNAL_ID,STABLE_ID,SAMPLE_TYPE,PATIENT_ID) VALUES (13,'TCGA-A1-A0SP-01','Primary Solid Tumor',13);
INSERT INTO sample (INTERNAL_ID,STABLE_ID,SAMPLE_TYPE,PATIENT_ID) VALUES (14,'TCGA-A1-A0SQ-01','Primary Solid Tumor',14);
INSERT INTO sample (INTERNAL_ID,STABLE_ID,SAMPLE_TYPE,PATIENT_ID) VALUES (15,'TCGA-A1-B0SO-01','Primary Solid Tumor',15);
INSERT INTO sample (INTERNAL_ID,STABLE_ID,SAMPLE_TYPE,PATIENT_ID) VALUES (16,'TCGA-A1-B0SP-01','Primary Solid Tumor',16);
INSERT INTO sample (INTERNAL_ID,STABLE_ID,SAMPLE_TYPE,PATIENT_ID) VALUES (17,'TCGA-A1-B0SQ-01','Primary Solid Tumor',17);
INSERT INTO sample (INTERNAL_ID,STABLE_ID,SAMPLE_TYPE,PATIENT_ID) VALUES (18,'TCGA-A1-A0SB-02','Primary Solid Tumor',1);
INSERT INTO sample (INTERNAL_ID,STABLE_ID,SAMPLE_TYPE,PATIENT_ID) VALUES (19,'TCGA-A1-A0SB-01','Primary Solid Tumor',18);


INSERT INTO mutation_event (MUTATION_EVENT_ID,ENTREZ_GENE_ID,CHR,START_POSITION,END_POSITION,REFERENCE_ALLELE,TUMOR_SEQ_ALLELE,PROTEIN_CHANGE,MUTATION_TYPE,NCBI_BUILD,STRAND,VARIANT_TYPE,DB_SNP_RS,DB_SNP_VAL_STATUS,REFSEQ_MRNA_ID,CODON_CHANGE,UNIPROT_ACCESSION,PROTEIN_POS_START,PROTEIN_POS_END,CANONICAL_TRANSCRIPT,KEYWORD) VALUES (2038,672,'17',41244748,41244748,'G','A','Q934*','Nonsense_Mutation','37','+','SNP','rs80357223','unknown','NM_007294','c.(2800-2802)CAG>TAG','P38398',934,934,1,'BRCA1 truncating');
INSERT INTO mutation_event (MUTATION_EVENT_ID,ENTREZ_GENE_ID,CHR,START_POSITION,END_POSITION,REFERENCE_ALLELE,TUMOR_SEQ_ALLELE,PROTEIN_CHANGE,MUTATION_TYPE,NCBI_BUILD,STRAND,VARIANT_TYPE,DB_SNP_RS,DB_SNP_VAL_STATUS,REFSEQ_MRNA_ID,CODON_CHANGE,UNIPROT_ACCESSION,PROTEIN_POS_START,PROTEIN_POS_END,CANONICAL_TRANSCRIPT,KEYWORD) VALUES (22604,672,'17',41258504,41258504,'A','C','C61G','Missense_Mutation','37','+','SNP','rs28897672','byCluster','NM_007294','c.(181-183)TGT>GGT','P38398',61,61,1,'BRCA1 C61 missense');
INSERT INTO mutation_event (MUTATION_EVENT_ID,ENTREZ_GENE_ID,CHR,START_POSITION,END_POSITION,REFERENCE_ALLELE,TUMOR_SEQ_ALLELE,PROTEIN_CHANGE,MUTATION_TYPE,NCBI_BUILD,STRAND,VARIANT_TYPE,DB_SNP_RS,DB_SNP_VAL_STATUS,REFSEQ_MRNA_ID,CODON_CHANGE,UNIPROT_ACCESSION,PROTEIN_POS_START,PROTEIN_POS_END,CANONICAL_TRANSCRIPT,KEYWORD) VALUES (2039,672,'17',41276033,41276033,'C','T','C27_splice','Splice_Site','37','+','SNP','rs80358010','byCluster','NM_007294','c.e2+1','NA',-1,-1,1,'BRCA1 truncating');
INSERT INTO mutation_event (MUTATION_EVENT_ID,ENTREZ_GENE_ID,CHR,START_POSITION,END_POSITION,REFERENCE_ALLELE,TUMOR_SEQ_ALLELE,PROTEIN_CHANGE,MUTATION_TYPE,NCBI_BUILD,STRAND,VARIANT_TYPE,DB_SNP_RS,DB_SNP_VAL_STATUS,REFSEQ_MRNA_ID,CODON_CHANGE,UNIPROT_ACCESSION,PROTEIN_POS_START,PROTEIN_POS_END,CANONICAL_TRANSCRIPT,KEYWORD) VALUES (2040,207,'17',41244748,41244748,'G','A','Q934*','Nonsense_Mutation','37','+','SNP','rs80357223','unknown','NM_007294','c.(2800-2802)CAG>TAG','P38398',934,934,1,'BRCA1 truncating');
INSERT INTO mutation_event (MUTATION_EVENT_ID,ENTREZ_GENE_ID,CHR,START_POSITION,END_POSITION,REFERENCE_ALLELE,TUMOR_SEQ_ALLELE,PROTEIN_CHANGE,MUTATION_TYPE,NCBI_BUILD,STRAND,VARIANT_TYPE,DB_SNP_RS,DB_SNP_VAL_STATUS,REFSEQ_MRNA_ID,CODON_CHANGE,UNIPROT_ACCESSION,PROTEIN_POS_START,PROTEIN_POS_END,CANONICAL_TRANSCRIPT,KEYWORD) VALUES (2041,207,'17',41258504,41258504,'A','C','C61G','Missense_Mutation','37','+','SNP','rs28897672','byCluster','NM_007294','c.(181-183)TGT>GGT','P38398',61,61,1,'BRCA1 C61 missense');
INSERT INTO mutation_event (MUTATION_EVENT_ID,ENTREZ_GENE_ID,CHR,START_POSITION,END_POSITION,REFERENCE_ALLELE,TUMOR_SEQ_ALLELE,PROTEIN_CHANGE,MUTATION_TYPE,NCBI_BUILD,STRAND,VARIANT_TYPE,DB_SNP_RS,DB_SNP_VAL_STATUS,REFSEQ_MRNA_ID,CODON_CHANGE,UNIPROT_ACCESSION,PROTEIN_POS_START,PROTEIN_POS_END,CANONICAL_TRANSCRIPT,KEYWORD) VALUES (2042,208,'17',41276033,41276033,'C','T','C27_splice','Splice_Site','37','+','SNP','rs80358010','byCluster','NM_007294','c.e2+1','NA',-1,-1,1,'BRCA1 truncating');

INSERT INTO alteration_driver_annotation (ALTERATION_EVENT_ID,GENETIC_PROFILE_ID,SAMPLE_ID, DRIVER_FILTER, DRIVER_FILTER_ANNOTATION, DRIVER_TIERS_FILTER, DRIVER_TIERS_FILTER_ANNOTATION) VALUES (2038,6,6, 'Putative_Driver', 'Pathogenic', 'Tier 1', 'Highly Actionable');
INSERT INTO alteration_driver_annotation (ALTERATION_EVENT_ID,GENETIC_PROFILE_ID,SAMPLE_ID, DRIVER_FILTER, DRIVER_FILTER_ANNOTATION, DRIVER_TIERS_FILTER, DRIVER_TIERS_FILTER_ANNOTATION) VALUES (22604,6,6, 'Putative_Passenger', 'Pathogenic', 'Tier 2', 'Potentially Actionable');
INSERT INTO alteration_driver_annotation (ALTERATION_EVENT_ID,GENETIC_PROFILE_ID,SAMPLE_ID, DRIVER_FILTER, DRIVER_FILTER_ANNOTATION, DRIVER_TIERS_FILTER, DRIVER_TIERS_FILTER_ANNOTATION) VALUES (2039,6,12, 'Putative_Passenger', 'Pathogenic', 'Tier 1', 'Highly Actionable');
INSERT INTO alteration_driver_annotation (ALTERATION_EVENT_ID,GENETIC_PROFILE_ID,SAMPLE_ID, DRIVER_FILTER, DRIVER_FILTER_ANNOTATION, DRIVER_TIERS_FILTER, DRIVER_TIERS_FILTER_ANNOTATION) VALUES (2038,6,7, 'Putative_Driver', 'Pathogenic', 'Tier 2', 'Potentially Actionable');
INSERT INTO alteration_driver_annotation (ALTERATION_EVENT_ID,GENETIC_PROFILE_ID,SAMPLE_ID, DRIVER_FILTER, DRIVER_FILTER_ANNOTATION, DRIVER_TIERS_FILTER, DRIVER_TIERS_FILTER_ANNOTATION) VALUES (2039,6,13, 'Putative_Driver', 'Pathogenic', 'Tier 1', 'Highly Actionable');
INSERT INTO alteration_driver_annotation (ALTERATION_EVENT_ID,GENETIC_PROFILE_ID,SAMPLE_ID, DRIVER_FILTER, DRIVER_FILTER_ANNOTATION, DRIVER_TIERS_FILTER, DRIVER_TIERS_FILTER_ANNOTATION) VALUES (2040,6,1, 'Putative_Driver', 'Pathogenic', 'Tier 1', 'Highly Actionable');
INSERT INTO alteration_driver_annotation (ALTERATION_EVENT_ID,GENETIC_PROFILE_ID,SAMPLE_ID, DRIVER_FILTER, DRIVER_FILTER_ANNOTATION, DRIVER_TIERS_FILTER, DRIVER_TIERS_FILTER_ANNOTATION) VALUES (2041,6,2, 'Putative_Passenger', 'Pathogenic', 'Tier 2', 'Potentially Actionable');
INSERT INTO alteration_driver_annotation (ALTERATION_EVENT_ID,GENETIC_PROFILE_ID,SAMPLE_ID, DRIVER_FILTER, DRIVER_FILTER_ANNOTATION, DRIVER_TIERS_FILTER, DRIVER_TIERS_FILTER_ANNOTATION) VALUES (2042,6,3, 'Putative_Passenger', 'Pathogenic', 'Tier 1', 'Highly Actionable');
INSERT INTO alteration_driver_annotation (ALTERATION_EVENT_ID,GENETIC_PROFILE_ID,SAMPLE_ID, DRIVER_FILTER, DRIVER_FILTER_ANNOTATION, DRIVER_TIERS_FILTER, DRIVER_TIERS_FILTER_ANNOTATION) VALUES (2042,8,15, 'Putative_Driver', 'Pathogenic', 'Tier 1', 'Highly Actionable');

INSERT INTO mutation (MUTATION_EVENT_ID,GENETIC_PROFILE_ID,SAMPLE_ID,ENTREZ_GENE_ID,CENTER,SEQUENCER,MUTATION_STATUS,VALIDATION_STATUS,TUMOR_SEQ_ALLELE1,TUMOR_SEQ_ALLELE2,MATCHED_NORM_SAMPLE_BARCODE,MATCH_NORM_SEQ_ALLELE1,MATCH_NORM_SEQ_ALLELE2,TUMOR_VALIDATION_ALLELE1,TUMOR_VALIDATION_ALLELE2,MATCH_NORM_VALIDATION_ALLELE1,MATCH_NORM_VALIDATION_ALLELE2,VERIFICATION_STATUS,SEQUENCING_PHASE,SEQUENCE_SOURCE,VALIDATION_METHOD,SCORE,BAM_FILE,TUMOR_ALT_COUNT,TUMOR_REF_COUNT,NORMAL_ALT_COUNT,NORMAL_REF_COUNT,AMINO_ACID_CHANGE,ANNOTATION_JSON) VALUES (2038,6,6,672,'genome.wustl.edu','IlluminaGAIIx','NA','Unknown','G','A','TCGA-A1-A0SH-10A-03D-A099-09','G','A','NA','NA','NA','NA','Unknown','Phase_IV','Capture','NA','1','dbGAP',1,0,-1,-1,'cyclases/Protein','{"zygosity":{"status": "heterozygous"}}');
INSERT INTO mutation (MUTATION_EVENT_ID,GENETIC_PROFILE_ID,SAMPLE_ID,ENTREZ_GENE_ID,CENTER,SEQUENCER,MUTATION_STATUS,VALIDATION_STATUS,TUMOR_SEQ_ALLELE1,TUMOR_SEQ_ALLELE2,MATCHED_NORM_SAMPLE_BARCODE,MATCH_NORM_SEQ_ALLELE1,MATCH_NORM_SEQ_ALLELE2,TUMOR_VALIDATION_ALLELE1,TUMOR_VALIDATION_ALLELE2,MATCH_NORM_VALIDATION_ALLELE1,MATCH_NORM_VALIDATION_ALLELE2,VERIFICATION_STATUS,SEQUENCING_PHASE,SEQUENCE_SOURCE,VALIDATION_METHOD,SCORE,BAM_FILE,TUMOR_ALT_COUNT,TUMOR_REF_COUNT,NORMAL_ALT_COUNT,NORMAL_REF_COUNT,AMINO_ACID_CHANGE,ANNOTATION_JSON) VALUES (22604,6,6,672,'genome.wustl.edu','IlluminaGAIIx','Germline','Unknown','A','C','TCGA-A1-A0SH-10A-03D-A099-09','A','C','NA','NA','NA','NA','Unknown','Phase_IV','Capture','NA','1','dbGAP',-1,-1,-1,-1,'cyclases/Protein','{"zygosity":{"status": "heterozygous"}}');
INSERT INTO mutation (MUTATION_EVENT_ID,GENETIC_PROFILE_ID,SAMPLE_ID,ENTREZ_GENE_ID,CENTER,SEQUENCER,MUTATION_STATUS,VALIDATION_STATUS,TUMOR_SEQ_ALLELE1,TUMOR_SEQ_ALLELE2,MATCHED_NORM_SAMPLE_BARCODE,MATCH_NORM_SEQ_ALLELE1,MATCH_NORM_SEQ_ALLELE2,TUMOR_VALIDATION_ALLELE1,TUMOR_VALIDATION_ALLELE2,MATCH_NORM_VALIDATION_ALLELE1,MATCH_NORM_VALIDATION_ALLELE2,VERIFICATION_STATUS,SEQUENCING_PHASE,SEQUENCE_SOURCE,VALIDATION_METHOD,SCORE,BAM_FILE,TUMOR_ALT_COUNT,TUMOR_REF_COUNT,NORMAL_ALT_COUNT,NORMAL_REF_COUNT,AMINO_ACID_CHANGE,ANNOTATION_JSON) VALUES (2039,6,12,672,'genome.wustl.edu','IlluminaGAIIx','Germline','Unknown','T','T','TCGA-A1-A0SO-10A-03D-A099-09','T','T','NA','NA','NA','NA','Unknown','Phase_IV','Capture','NA','1','dbGAP',-1,-1,-1,-1,'cyclases/Protein','{"zygosity":{"status": "heterozygous"}}');
INSERT INTO mutation (MUTATION_EVENT_ID,GENETIC_PROFILE_ID,SAMPLE_ID,ENTREZ_GENE_ID,CENTER,SEQUENCER,MUTATION_STATUS,VALIDATION_STATUS,TUMOR_SEQ_ALLELE1,TUMOR_SEQ_ALLELE2,MATCHED_NORM_SAMPLE_BARCODE,MATCH_NORM_SEQ_ALLELE1,MATCH_NORM_SEQ_ALLELE2,TUMOR_VALIDATION_ALLELE1,TUMOR_VALIDATION_ALLELE2,MATCH_NORM_VALIDATION_ALLELE1,MATCH_NORM_VALIDATION_ALLELE2,VERIFICATION_STATUS,SEQUENCING_PHASE,SEQUENCE_SOURCE,VALIDATION_METHOD,SCORE,BAM_FILE,TUMOR_ALT_COUNT,TUMOR_REF_COUNT,NORMAL_ALT_COUNT,NORMAL_REF_COUNT,AMINO_ACID_CHANGE,ANNOTATION_JSON) VALUES (2038,6,7,672,'genome.wustl.edu','IlluminaGAIIx','Germline','Unknown','G','A','TCGA-A1-A0SH-10A-03D-A099-09','G','A','NA','NA','NA','NA','Unknown','Phase_IV','Capture','NA','1','dbGAP',-1,-1,-1,-1,'cyclases/Protein','{"zygosity":{"status": "heterozygous"}}');
INSERT INTO mutation (MUTATION_EVENT_ID,GENETIC_PROFILE_ID,SAMPLE_ID,ENTREZ_GENE_ID,CENTER,SEQUENCER,MUTATION_STATUS,VALIDATION_STATUS,TUMOR_SEQ_ALLELE1,TUMOR_SEQ_ALLELE2,MATCHED_NORM_SAMPLE_BARCODE,MATCH_NORM_SEQ_ALLELE1,MATCH_NORM_SEQ_ALLELE2,TUMOR_VALIDATION_ALLELE1,TUMOR_VALIDATION_ALLELE2,MATCH_NORM_VALIDATION_ALLELE1,MATCH_NORM_VALIDATION_ALLELE2,VERIFICATION_STATUS,SEQUENCING_PHASE,SEQUENCE_SOURCE,VALIDATION_METHOD,SCORE,BAM_FILE,TUMOR_ALT_COUNT,TUMOR_REF_COUNT,NORMAL_ALT_COUNT,NORMAL_REF_COUNT,AMINO_ACID_CHANGE,ANNOTATION_JSON) VALUES (2039,6,13,672,'genome.wustl.edu','IlluminaGAIIx','Germline','Unknown','T','T','TCGA-A1-A0SO-10A-03D-A099-09','T','T','NA','NA','NA','NA','Unknown','Phase_IV','Capture','NA','1','dbGAP',-1,-1,-1,-1,'cyclases/Protein','{"zygosity":{"status": "heterozygous"}}');
INSERT INTO mutation (MUTATION_EVENT_ID,GENETIC_PROFILE_ID,SAMPLE_ID,ENTREZ_GENE_ID,CENTER,SEQUENCER,MUTATION_STATUS,VALIDATION_STATUS,TUMOR_SEQ_ALLELE1,TUMOR_SEQ_ALLELE2,MATCHED_NORM_SAMPLE_BARCODE,MATCH_NORM_SEQ_ALLELE1,MATCH_NORM_SEQ_ALLELE2,TUMOR_VALIDATION_ALLELE1,TUMOR_VALIDATION_ALLELE2,MATCH_NORM_VALIDATION_ALLELE1,MATCH_NORM_VALIDATION_ALLELE2,VERIFICATION_STATUS,SEQUENCING_PHASE,SEQUENCE_SOURCE,VALIDATION_METHOD,SCORE,BAM_FILE,TUMOR_ALT_COUNT,TUMOR_REF_COUNT,NORMAL_ALT_COUNT,NORMAL_REF_COUNT,AMINO_ACID_CHANGE,ANNOTATION_JSON) VALUES (2040,6,1,207,'genome.wustl.edu','IlluminaGAIIx','Germline','Unknown','G','A','TCGA-A1-A0SH-10A-03D-A099-09','G','A','NA','NA','NA','NA','Unknown','Phase_IV','Capture','NA','1','dbGAP',-1,-1,-1,-1,'cyclases/Protein','{"zygosity":{"status": "heterozygous"}}');
INSERT INTO mutation (MUTATION_EVENT_ID,GENETIC_PROFILE_ID,SAMPLE_ID,ENTREZ_GENE_ID,CENTER,SEQUENCER,MUTATION_STATUS,VALIDATION_STATUS,TUMOR_SEQ_ALLELE1,TUMOR_SEQ_ALLELE2,MATCHED_NORM_SAMPLE_BARCODE,MATCH_NORM_SEQ_ALLELE1,MATCH_NORM_SEQ_ALLELE2,TUMOR_VALIDATION_ALLELE1,TUMOR_VALIDATION_ALLELE2,MATCH_NORM_VALIDATION_ALLELE1,MATCH_NORM_VALIDATION_ALLELE2,VERIFICATION_STATUS,SEQUENCING_PHASE,SEQUENCE_SOURCE,VALIDATION_METHOD,SCORE,BAM_FILE,TUMOR_ALT_COUNT,TUMOR_REF_COUNT,NORMAL_ALT_COUNT,NORMAL_REF_COUNT,AMINO_ACID_CHANGE,ANNOTATION_JSON) VALUES (2041,6,2,207,'genome.wustl.edu','IlluminaGAIIx','Germline','Unknown','A','C','TCGA-A1-A0SH-10A-03D-A099-09','A','C','NA','NA','NA','NA','Unknown','Phase_IV','Capture','NA','1','dbGAP',0,-1,-1,-1,'cyclases/Protein','{"zygosity":{"status": "heterozygous"}}');
INSERT INTO mutation (MUTATION_EVENT_ID,GENETIC_PROFILE_ID,SAMPLE_ID,ENTREZ_GENE_ID,CENTER,SEQUENCER,MUTATION_STATUS,VALIDATION_STATUS,TUMOR_SEQ_ALLELE1,TUMOR_SEQ_ALLELE2,MATCHED_NORM_SAMPLE_BARCODE,MATCH_NORM_SEQ_ALLELE1,MATCH_NORM_SEQ_ALLELE2,TUMOR_VALIDATION_ALLELE1,TUMOR_VALIDATION_ALLELE2,MATCH_NORM_VALIDATION_ALLELE1,MATCH_NORM_VALIDATION_ALLELE2,VERIFICATION_STATUS,SEQUENCING_PHASE,SEQUENCE_SOURCE,VALIDATION_METHOD,SCORE,BAM_FILE,TUMOR_ALT_COUNT,TUMOR_REF_COUNT,NORMAL_ALT_COUNT,NORMAL_REF_COUNT,AMINO_ACID_CHANGE,ANNOTATION_JSON) VALUES (2042,6,3,208,'genome.wustl.edu','IlluminaGAIIx','Germline','Unknown','T','T','TCGA-A1-A0SO-10A-03D-A099-09','T','T','NA','NA','NA','NA','Unknown','Phase_IV','Capture','NA','1','dbGAP',-1,-1,-1,-1,'cyclases/Protein','{"zygosity":{"status": "heterozygous"}}');
INSERT INTO mutation (MUTATION_EVENT_ID,GENETIC_PROFILE_ID,SAMPLE_ID,ENTREZ_GENE_ID,CENTER,SEQUENCER,MUTATION_STATUS,VALIDATION_STATUS,TUMOR_SEQ_ALLELE1,TUMOR_SEQ_ALLELE2,MATCHED_NORM_SAMPLE_BARCODE,MATCH_NORM_SEQ_ALLELE1,MATCH_NORM_SEQ_ALLELE2,TUMOR_VALIDATION_ALLELE1,TUMOR_VALIDATION_ALLELE2,MATCH_NORM_VALIDATION_ALLELE1,MATCH_NORM_VALIDATION_ALLELE2,VERIFICATION_STATUS,SEQUENCING_PHASE,SEQUENCE_SOURCE,VALIDATION_METHOD,SCORE,BAM_FILE,TUMOR_ALT_COUNT,TUMOR_REF_COUNT,NORMAL_ALT_COUNT,NORMAL_REF_COUNT,AMINO_ACID_CHANGE,ANNOTATION_JSON) VALUES (2042,8,15,208,'genome.wustl.edu','IlluminaGAIIx','Germline','Unknown','T','T','TCGA-A1-A0SO-10A-03D-A099-09','T','T','NA','NA','NA','NA','Unknown','Phase_IV','Capture','NA','1','dbGAP',-1,-1,-1,-1,'cyclases/Protein','{"zygosity":{"status": "heterozygous"}}');

INSERT INTO gene_panel (INTERNAL_ID,STABLE_ID,DESCRIPTION) VALUES (1,'TESTPANEL1','A test panel consisting of a few genes');
INSERT INTO gene_panel (INTERNAL_ID,STABLE_ID,DESCRIPTION) VALUES (2,'TESTPANEL2','Another test panel consisting of a few genes');

INSERT INTO gene_panel_list (INTERNAL_ID,GENE_ID) VALUES (1,207);
INSERT INTO gene_panel_list (INTERNAL_ID,GENE_ID) VALUES (1,369);
INSERT INTO gene_panel_list (INTERNAL_ID,GENE_ID) VALUES (1,672);
INSERT INTO gene_panel_list (INTERNAL_ID,GENE_ID) VALUES (2,207);
INSERT INTO gene_panel_list (INTERNAL_ID,GENE_ID) VALUES (2,208);
INSERT INTO gene_panel_list (INTERNAL_ID,GENE_ID) VALUES (2,4893);

INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (1,2,1);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (1,3,1);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (1,4,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (1,6,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (2,2,2);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (2,3,1);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (2,4,2);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (2,5,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (2,6,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (3,2,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (3,3,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (3,4,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (3,6,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (4,2,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (4,4,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (5,2,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (5,4,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (6,2,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (6,3,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (6,4,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (6,6,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (7,2,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (7,4,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (7,6,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (8,2,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (8,3,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (8,4,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (8,6,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (9,2,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (9,3,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (9,4,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (9,6,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (10,2,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (10,3,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (10,4,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (10,6,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (11,2,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (11,4,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (12,2,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (12,3,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (12,4,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (12,6,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (13,2,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (13,3,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (13,4,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (13,6,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (14,2,NULL);
INSERT INTO sample_profile (SAMPLE_ID,GENETIC_PROFILE_ID,PANEL_ID) VALUES (14,4,NULL);

INSERT INTO sample_list (LIST_ID,STABLE_ID,CATEGORY,CANCER_STUDY_ID,NAME,DESCRIPTION) VALUES (1,'study_tcga_pub_all','other',1,'All Tumors','All tumor samples');
INSERT INTO sample_list (LIST_ID,STABLE_ID,CATEGORY,CANCER_STUDY_ID,NAME,DESCRIPTION) VALUES (2,'study_tcga_pub_acgh','other',1,'Tumors aCGH','All tumors with aCGH data');
INSERT INTO sample_list (LIST_ID,STABLE_ID,CATEGORY,CANCER_STUDY_ID,NAME,DESCRIPTION) VALUES (3,'study_tcga_pub_cnaseq','other',1,'Tumors with sequencing and aCGH data','All tumor samples that have CNA and sequencing data');
INSERT INTO sample_list (LIST_ID,STABLE_ID,CATEGORY,CANCER_STUDY_ID,NAME,DESCRIPTION) VALUES (4,'study_tcga_pub_complete','other',1,'Complete samples (mutations, copy-number, expression)','Samples with complete data');
INSERT INTO sample_list (LIST_ID,STABLE_ID,CATEGORY,CANCER_STUDY_ID,NAME,DESCRIPTION) VALUES (5,'study_tcga_pub_log2CNA','other',1,'Tumors log2 copy-number','All tumors with log2 copy-number data');
INSERT INTO sample_list (LIST_ID,STABLE_ID,CATEGORY,CANCER_STUDY_ID,NAME,DESCRIPTION) VALUES (6,'study_tcga_pub_methylation_hm27','all_cases_with_mutation_data',1,'Tumors with methylation data','All samples with methylation (HM27) data');
INSERT INTO sample_list (LIST_ID,STABLE_ID,CATEGORY,CANCER_STUDY_ID,NAME,DESCRIPTION) VALUES (7,'study_tcga_pub_mrna','other',1,'Tumors with mRNA data (Agilent microarray)','All samples with mRNA expression data');
INSERT INTO sample_list (LIST_ID,STABLE_ID,CATEGORY,CANCER_STUDY_ID,NAME,DESCRIPTION) VALUES (8,'study_tcga_pub_sequenced','other',1,'Sequenced Tumors','All sequenced samples');
INSERT INTO sample_list (LIST_ID,STABLE_ID,CATEGORY,CANCER_STUDY_ID,NAME,DESCRIPTION) VALUES (9,'study_tcga_pub_cna','other',1,'Tumor Samples with CNA data','All tumors with CNA data');
INSERT INTO sample_list (LIST_ID,STABLE_ID,CATEGORY,CANCER_STUDY_ID,NAME,DESCRIPTION) VALUES (10,'study_tcga_pub_rna_seq_v2_mrna','other',1,'Tumor Samples with mRNA data (RNA Seq V2)','All samples with mRNA expression data');
INSERT INTO sample_list (LIST_ID,STABLE_ID,CATEGORY,CANCER_STUDY_ID,NAME,DESCRIPTION) VALUES (11,'study_tcga_pub_microrna','other',1,'Tumors with microRNA data (microRNA-Seq)','All samples with microRNA data');
INSERT INTO sample_list (LIST_ID,STABLE_ID,CATEGORY,CANCER_STUDY_ID,NAME,DESCRIPTION) VALUES (12,'study_tcga_pub_rppa','other',1,'Tumor Samples with RPPA data','Tumors with reverse phase protein array (RPPA) data for about 200 antibodies');
INSERT INTO sample_list (LIST_ID,STABLE_ID,CATEGORY,CANCER_STUDY_ID,NAME,DESCRIPTION) VALUES (13,'study_tcga_pub_3way_complete','other',1,'All Complete Tumors','All tumor samples that have mRNA,CNA and sequencing data');
INSERT INTO sample_list (LIST_ID,STABLE_ID,CATEGORY,CANCER_STUDY_ID,NAME,DESCRIPTION) VALUES (14,'acc_tcga_all','other',2,'All Tumors','All tumor samples');

INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (1,1);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (1,2);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (1,3);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (1,4);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (1,5);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (1,6);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (1,7);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (1,8);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (1,9);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (1,10);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (1,11);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (1,12);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (1,13);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (1,14);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (2,1);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (2,2);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (2,3);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (2,4);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (2,5);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (2,6);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (2,7);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (2,8);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (2,9);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (2,10);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (2,11);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (2,12);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (2,13);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (2,14);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (3,2);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (3,3);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (3,6);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (3,8);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (3,9);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (3,10);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (3,12);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (4,2);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (4,3);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (4,6);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (4,8);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (4,9);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (4,10);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (4,12);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (5,1);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (5,2);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (5,3);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (5,4);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (5,5);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (5,6);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (5,7);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (5,8);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (5,9);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (5,10);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (5,11);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (5,12);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (5,13);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (5,14);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (6,2);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (7,2);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (7,3);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (7,6);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (7,8);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (7,9);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (7,10);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (7,12);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (7,13);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (8,2);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (8,3);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (8,6);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (8,8);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (8,9);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (8,10);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (8,12);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (9,2);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (9,3);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (9,6);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (9,8);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (9,9);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (9,10);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (9,12);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (10,2);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (10,3);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (10,6);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (10,8);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (10,9);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (10,10);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (10,12);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (13,2);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (13,3);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (13,6);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (13,8);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (13,9);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (13,10);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (13,12);
INSERT INTO sample_list_list (LIST_ID,SAMPLE_ID) VALUES (14,15);

INSERT INTO copy_number_seg (SEG_ID,CANCER_STUDY_ID,SAMPLE_ID,CHR,`START`,`END`,NUM_PROBES,SEGMENT_MEAN) VALUES (50236594,1,1,'1',324556,180057677,291,0.0519);
INSERT INTO copy_number_seg (SEG_ID,CANCER_STUDY_ID,SAMPLE_ID,CHR,`START`,`END`,NUM_PROBES,SEGMENT_MEAN) VALUES (50236595,1,1,'2',224556,327677,391,0.0219);
INSERT INTO copy_number_seg (SEG_ID,CANCER_STUDY_ID,SAMPLE_ID,CHR,`START`,`END`,NUM_PROBES,SEGMENT_MEAN) VALUES (50236593,1,2,	'2',1402650,190262486,207,0.0265);
INSERT INTO copy_number_seg (SEG_ID,CANCER_STUDY_ID,SAMPLE_ID,CHR,`START`,`END`,NUM_PROBES,SEGMENT_MEAN) VALUES (50236592,1,3,	'3',1449872,194238390,341,0.0347);
INSERT INTO copy_number_seg (SEG_ID,CANCER_STUDY_ID,SAMPLE_ID,CHR,`START`,`END`,NUM_PROBES,SEGMENT_MEAN) VALUES (50236500,2,15,	'2',14492,19423390,41,0.047);

INSERT INTO clinical_patient (INTERNAL_ID,ATTR_ID,ATTR_VALUE) VALUES (1,'RETROSPECTIVE_COLLECTION','NO');
INSERT INTO clinical_patient (INTERNAL_ID,ATTR_ID,ATTR_VALUE) VALUES (1,'FORM_COMPLETION_DATE','2013-12-5');
INSERT INTO clinical_patient (INTERNAL_ID,ATTR_ID,ATTR_VALUE) VALUES (1,'OTHER_PATIENT_ID','286CF147-B7F7-4A05-8E41-7FBD3717AD71');
INSERT INTO clinical_patient (INTERNAL_ID,ATTR_ID,ATTR_VALUE) VALUES (2,'PROSPECTIVE_COLLECTION','YES');
INSERT INTO clinical_patient (INTERNAL_ID,ATTR_ID,ATTR_VALUE) VALUES (15,'DFS_MONTHS','5.72');
INSERT INTO clinical_patient (INTERNAL_ID,ATTR_ID,ATTR_VALUE) VALUES (15,'DFS_STATUS','1:Recurred/Progressed');
INSERT INTO clinical_patient (INTERNAL_ID,ATTR_ID,ATTR_VALUE) VALUES (15,'OS_MONTHS','12.3');
INSERT INTO clinical_patient (INTERNAL_ID,ATTR_ID,ATTR_VALUE) VALUES (15,'OS_STATUS','0:LIVING');
INSERT INTO clinical_patient (INTERNAL_ID,ATTR_ID,ATTR_VALUE) VALUES (18,'RETROSPECTIVE_COLLECTION','NO');

INSERT INTO clinical_sample (INTERNAL_ID,ATTR_ID,ATTR_VALUE) VALUES (1,'OTHER_SAMPLE_ID','5C631CE8-F96A-4C35-A459-556FC4AB21E1');
INSERT INTO clinical_sample (INTERNAL_ID,ATTR_ID,ATTR_VALUE) VALUES (1,'DAYS_TO_COLLECTION','276');
INSERT INTO clinical_sample (INTERNAL_ID,ATTR_ID,ATTR_VALUE) VALUES (1,'IS_FFPE','NO');
INSERT INTO clinical_sample (INTERNAL_ID,ATTR_ID,ATTR_VALUE) VALUES (1,'SAMPLE_TYPE','Secondary Tumor');
INSERT INTO clinical_sample (INTERNAL_ID,ATTR_ID,ATTR_VALUE) VALUES (2,'OCT_EMBEDDED','false');
INSERT INTO clinical_sample (INTERNAL_ID,ATTR_ID,ATTR_VALUE) VALUES (2,'DAYS_TO_COLLECTION','277');
INSERT INTO clinical_sample (INTERNAL_ID,ATTR_ID,ATTR_VALUE) VALUES (2,'PATHOLOGY_REPORT_FILE_NAME','TCGA-GC-A3BM.F3408556-9259-4700-B9A0-F41E516B420C.pdf');
INSERT INTO clinical_sample (INTERNAL_ID,ATTR_ID,ATTR_VALUE) VALUES (2,'SAMPLE_TYPE','Primary Tumor');
INSERT INTO clinical_sample (INTERNAL_ID,ATTR_ID,ATTR_VALUE) VALUES (15,'OTHER_SAMPLE_ID','91E7F41C-17B3-4724-96EF-D3C207B964E1');
INSERT INTO clinical_sample (INTERNAL_ID,ATTR_ID,ATTR_VALUE) VALUES (15,'DAYS_TO_COLLECTION','111');
INSERT INTO clinical_sample (INTERNAL_ID,ATTR_ID,ATTR_VALUE) VALUES (19,'DAYS_TO_COLLECTION','111');


INSERT INTO clinical_attribute_meta (ATTR_ID,DISPLAY_NAME,DESCRIPTION,DATATYPE,PATIENT_ATTRIBUTE,PRIORITY,CANCER_STUDY_ID) VALUES ('RETROSPECTIVE_COLLECTION','Tissue Retrospective Collection Indicator','Text indicator for the time frame of tissue procurement,indicating that the tissue was obtained and stored prior to the initiation of the project.','STRING',1,'1',1);
INSERT INTO clinical_attribute_meta (ATTR_ID,DISPLAY_NAME,DESCRIPTION,DATATYPE,PATIENT_ATTRIBUTE,PRIORITY,CANCER_STUDY_ID) VALUES ('PROSPECTIVE_COLLECTION','Tissue Prospective Collection Indicator','Text indicator for the time frame of tissue procurement,indicating that the tissue was procured in parallel to the project.','STRING',1,'1',1);
INSERT INTO clinical_attribute_meta (ATTR_ID,DISPLAY_NAME,DESCRIPTION,DATATYPE,PATIENT_ATTRIBUTE,PRIORITY,CANCER_STUDY_ID) VALUES ('FORM_COMPLETION_DATE','Form completion date','Form completion date','STRING',1,'1',1);
INSERT INTO clinical_attribute_meta (ATTR_ID,DISPLAY_NAME,DESCRIPTION,DATATYPE,PATIENT_ATTRIBUTE,PRIORITY,CANCER_STUDY_ID) VALUES ('OTHER_PATIENT_ID','Other Patient ID','Legacy DMP patient identifier (DMPnnnn)','STRING',1,'1',1);
INSERT INTO clinical_attribute_meta (ATTR_ID,DISPLAY_NAME,DESCRIPTION,DATATYPE,PATIENT_ATTRIBUTE,PRIORITY,CANCER_STUDY_ID) VALUES ('DFS_MONTHS','Disease Free (Months)','Disease free (months) since initial treatment.','NUMBER',1,'1',1);
INSERT INTO clinical_attribute_meta (ATTR_ID,DISPLAY_NAME,DESCRIPTION,DATATYPE,PATIENT_ATTRIBUTE,PRIORITY,CANCER_STUDY_ID) VALUES ('DFS_STATUS','Disease Free Status','Disease free status since initial treatment.','STRING',1,'1',1);
INSERT INTO clinical_attribute_meta (ATTR_ID,DISPLAY_NAME,DESCRIPTION,DATATYPE,PATIENT_ATTRIBUTE,PRIORITY,CANCER_STUDY_ID) VALUES ('OS_MONTHS','Overall Survival (Months)','Overall survival in months since initial diagonosis.','NUMBER',1,'1',1);
INSERT INTO clinical_attribute_meta (ATTR_ID,DISPLAY_NAME,DESCRIPTION,DATATYPE,PATIENT_ATTRIBUTE,PRIORITY,CANCER_STUDY_ID) VALUES ('OS_STATUS','Overall Survival Status','Overall patient survival status.','STRING',1,'1',1);
INSERT INTO clinical_attribute_meta (ATTR_ID,DISPLAY_NAME,DESCRIPTION,DATATYPE,PATIENT_ATTRIBUTE,PRIORITY,CANCER_STUDY_ID) VALUES ('OTHER_SAMPLE_ID','Other Sample ID','Legacy DMP sample identifier (DMPnnnn)','STRING',0,'1',1);
INSERT INTO clinical_attribute_meta (ATTR_ID,DISPLAY_NAME,DESCRIPTION,DATATYPE,PATIENT_ATTRIBUTE,PRIORITY,CANCER_STUDY_ID) VALUES ('DAYS_TO_COLLECTION','Days to Sample Collection.','Days to sample collection.','NUMBER',0,'1',1);
INSERT INTO clinical_attribute_meta (ATTR_ID,DISPLAY_NAME,DESCRIPTION,DATATYPE,PATIENT_ATTRIBUTE,PRIORITY,CANCER_STUDY_ID) VALUES ('IS_FFPE','Is FFPE','If the sample is from FFPE','STRING',0,'1',1);
INSERT INTO clinical_attribute_meta (ATTR_ID,DISPLAY_NAME,DESCRIPTION,DATATYPE,PATIENT_ATTRIBUTE,PRIORITY,CANCER_STUDY_ID) VALUES ('OCT_EMBEDDED','Oct embedded','Oct embedded','STRING',0,'1',1);
INSERT INTO clinical_attribute_meta (ATTR_ID,DISPLAY_NAME,DESCRIPTION,DATATYPE,PATIENT_ATTRIBUTE,PRIORITY,CANCER_STUDY_ID) VALUES ('PATHOLOGY_REPORT_FILE_NAME','Pathology Report File Name','Pathology Report File Name','STRING',0,'1',1);
INSERT INTO clinical_attribute_meta (ATTR_ID,DISPLAY_NAME,DESCRIPTION,DATATYPE,PATIENT_ATTRIBUTE,PRIORITY,CANCER_STUDY_ID) VALUES ('SAMPLE_TYPE','Sample Type','The type of sample (i.e.,normal,primary,met,recurrence).','STRING',0,'1',1);
INSERT INTO clinical_attribute_meta (ATTR_ID,DISPLAY_NAME,DESCRIPTION,DATATYPE,PATIENT_ATTRIBUTE,PRIORITY,CANCER_STUDY_ID) VALUES ('RETROSPECTIVE_COLLECTION','Tissue Retrospective Collection Indicator','Text indicator for the time frame of tissue procurement,indicating that the tissue was obtained and stored prior to the initiation of the project.','STRING',1,'1',2);
INSERT INTO clinical_attribute_meta (ATTR_ID,DISPLAY_NAME,DESCRIPTION,DATATYPE,PATIENT_ATTRIBUTE,PRIORITY,CANCER_STUDY_ID) VALUES ('PROSPECTIVE_COLLECTION','Tissue Prospective Collection Indicator','Text indicator for the time frame of tissue procurement,indicating that the tissue was procured in parallel to the project.','STRING',1,'1',2);
INSERT INTO clinical_attribute_meta (ATTR_ID,DISPLAY_NAME,DESCRIPTION,DATATYPE,PATIENT_ATTRIBUTE,PRIORITY,CANCER_STUDY_ID) VALUES ('FORM_COMPLETION_DATE','Form completion date','Form completion date','STRING',1,'1',2);
INSERT INTO clinical_attribute_meta (ATTR_ID,DISPLAY_NAME,DESCRIPTION,DATATYPE,PATIENT_ATTRIBUTE,PRIORITY,CANCER_STUDY_ID) VALUES ('OTHER_PATIENT_ID','Other Patient ID','Legacy DMP patient identifier (DMPnnnn)','STRING',1,'1',2);
INSERT INTO clinical_attribute_meta (ATTR_ID,DISPLAY_NAME,DESCRIPTION,DATATYPE,PATIENT_ATTRIBUTE,PRIORITY,CANCER_STUDY_ID) VALUES ('DFS_MONTHS','Disease Free (Months)','Disease free (months) since initial treatment.','NUMBER',1,'1',2);
INSERT INTO clinical_attribute_meta (ATTR_ID,DISPLAY_NAME,DESCRIPTION,DATATYPE,PATIENT_ATTRIBUTE,PRIORITY,CANCER_STUDY_ID) VALUES ('DFS_STATUS','Disease Free Status','Disease free status since initial treatment.','STRING',1,'1',2);
INSERT INTO clinical_attribute_meta (ATTR_ID,DISPLAY_NAME,DESCRIPTION,DATATYPE,PATIENT_ATTRIBUTE,PRIORITY,CANCER_STUDY_ID) VALUES ('OS_MONTHS','Overall Survival (Months)','Overall survival in months since initial diagonosis.','NUMBER',1,'1',2);
INSERT INTO clinical_attribute_meta (ATTR_ID,DISPLAY_NAME,DESCRIPTION,DATATYPE,PATIENT_ATTRIBUTE,PRIORITY,CANCER_STUDY_ID) VALUES ('OS_STATUS','Overall Survival Status','Overall patient survival status.','STRING',1,'1',2);
INSERT INTO clinical_attribute_meta (ATTR_ID,DISPLAY_NAME,DESCRIPTION,DATATYPE,PATIENT_ATTRIBUTE,PRIORITY,CANCER_STUDY_ID) VALUES ('OTHER_SAMPLE_ID','Other Sample ID','Legacy DMP sample identifier (DMPnnnn)','STRING',0,'1',2);
INSERT INTO clinical_attribute_meta (ATTR_ID,DISPLAY_NAME,DESCRIPTION,DATATYPE,PATIENT_ATTRIBUTE,PRIORITY,CANCER_STUDY_ID) VALUES ('DAYS_TO_COLLECTION','Days to Sample Collection.','Days to sample collection.','STRING',0,'1',2);
INSERT INTO clinical_attribute_meta (ATTR_ID,DISPLAY_NAME,DESCRIPTION,DATATYPE,PATIENT_ATTRIBUTE,PRIORITY,CANCER_STUDY_ID) VALUES ('IS_FFPE','Is FFPE','If the sample is from FFPE','STRING',0,'1',2);
INSERT INTO clinical_attribute_meta (ATTR_ID,DISPLAY_NAME,DESCRIPTION,DATATYPE,PATIENT_ATTRIBUTE,PRIORITY,CANCER_STUDY_ID) VALUES ('OCT_EMBEDDED','Oct embedded','Oct embedded','STRING',0,'1',2);
INSERT INTO clinical_attribute_meta (ATTR_ID,DISPLAY_NAME,DESCRIPTION,DATATYPE,PATIENT_ATTRIBUTE,PRIORITY,CANCER_STUDY_ID) VALUES ('PATHOLOGY_REPORT_FILE_NAME','Pathology Report File Name','Pathology Report File Name','STRING',0,'1',2);
INSERT INTO clinical_attribute_meta (ATTR_ID,DISPLAY_NAME,DESCRIPTION,DATATYPE,PATIENT_ATTRIBUTE,PRIORITY,CANCER_STUDY_ID) VALUES ('SAMPLE_TYPE','Sample Type','The type of sample (i.e.,normal,primary,met,recurrence).','STRING',0,'1',2);

-- Add genes, genetic entities and structural variants for structural_variant
INSERT INTO genetic_entity (ID,ENTITY_TYPE) VALUES(21,'GENE');
INSERT INTO genetic_entity (ID,ENTITY_TYPE) VALUES(22,'GENE');
INSERT INTO genetic_entity (ID,ENTITY_TYPE) VALUES(23,'GENE');
INSERT INTO genetic_entity (ID,ENTITY_TYPE) VALUES(24,'GENE');
INSERT INTO genetic_entity (ID,ENTITY_TYPE) VALUES(25,'GENE');
INSERT INTO genetic_entity (ID,ENTITY_TYPE) VALUES(26,'GENE');
INSERT INTO genetic_entity (ID,ENTITY_TYPE) VALUES(27,'GENE');

INSERT INTO gene (ENTREZ_GENE_ID,HUGO_GENE_SYMBOL,GENETIC_ENTITY_ID,TYPE) VALUES(57670,'KIAA1549',21,'protein-coding');
INSERT INTO gene (ENTREZ_GENE_ID,HUGO_GENE_SYMBOL,GENETIC_ENTITY_ID,TYPE) VALUES(8031,'NCOA4',22,'protein-coding');
INSERT INTO gene (ENTREZ_GENE_ID,HUGO_GENE_SYMBOL,GENETIC_ENTITY_ID,TYPE) VALUES(5979,'RET',23,'protein-coding');
INSERT INTO gene (ENTREZ_GENE_ID,HUGO_GENE_SYMBOL,GENETIC_ENTITY_ID,TYPE) VALUES(27436,'EML4',24,'protein-coding');
INSERT INTO gene (ENTREZ_GENE_ID,HUGO_GENE_SYMBOL,GENETIC_ENTITY_ID,TYPE) VALUES(238,'ALK',25,'protein-coding');
INSERT INTO gene (ENTREZ_GENE_ID,HUGO_GENE_SYMBOL,GENETIC_ENTITY_ID,TYPE) VALUES(7113,'TMPRSS2',26,'protein-coding');
INSERT INTO gene (ENTREZ_GENE_ID,HUGO_GENE_SYMBOL,GENETIC_ENTITY_ID,TYPE) VALUES(2078,'ERG',27,'protein-coding');

INSERT INTO structural_variant (GENETIC_PROFILE_ID,SAMPLE_ID,SITE1_ENTREZ_GENE_ID,SITE1_ENSEMBL_TRANSCRIPT_ID,SITE1_CHROMOSOME,SITE1_REGION,SITE1_REGION_NUMBER,SITE1_CONTIG,SITE1_POSITION,SITE1_DESCRIPTION,SITE2_ENTREZ_GENE_ID,SITE2_ENSEMBL_TRANSCRIPT_ID,SITE2_CHROMOSOME,SITE2_REGION,SITE2_REGION_NUMBER,SITE2_CONTIG,SITE2_POSITION,SITE2_DESCRIPTION,NCBI_BUILD,DNA_SUPPORT,RNA_SUPPORT,TUMOR_READ_COUNT,TUMOR_VARIANT_COUNT,ANNOTATION,EVENT_INFO,COMMENTS,SV_STATUS)
VALUES(7,1,57670,'ENST00000242365','7','exon',-1,'q13.4',138536968,'KIAA1549-BRAF.K16B10.COSF509_1',673,'ENST00000288602','7','exon',-1,'p13.1',140482957,'KIAA1549-BRAF.K16B10.COSF509_2','GRCh37','no','yes',100000,90000,'KIAA1549-BRAF.K16B10.COSF509','Fusion','Gain-of-Function','SOMATIC');
INSERT INTO structural_variant (GENETIC_PROFILE_ID,SAMPLE_ID,SITE1_ENTREZ_GENE_ID,SITE1_ENSEMBL_TRANSCRIPT_ID,SITE1_CHROMOSOME,SITE1_REGION,SITE1_REGION_NUMBER,SITE1_CONTIG,SITE1_POSITION,SITE1_DESCRIPTION,SITE2_ENTREZ_GENE_ID,SITE2_ENSEMBL_TRANSCRIPT_ID,SITE2_CHROMOSOME,SITE2_REGION,SITE2_REGION_NUMBER,SITE2_CONTIG,SITE2_POSITION,SITE2_DESCRIPTION,NCBI_BUILD,DNA_SUPPORT,RNA_SUPPORT,TUMOR_READ_COUNT,TUMOR_VARIANT_COUNT,ANNOTATION,EVENT_INFO,COMMENTS,SV_STATUS)
VALUES(7,2,57670,'ENST00000242365','7','exon',-1,'q13.4',138536968,'KIAA1549-BRAF.K16B10.COSF509_1',673,'ENST00000288602','7','exon',-1,'p13.1',140482957,'KIAA1549-BRAF.K16B10.COSF509_2','GRCh37','no','yes',100000,90000,'KIAA1549-BRAF.K16B10.COSF509','Fusion','Gain-of-Function','GERMLINE');
INSERT INTO structural_variant (GENETIC_PROFILE_ID,SAMPLE_ID,SITE1_ENTREZ_GENE_ID,SITE1_ENSEMBL_TRANSCRIPT_ID,SITE1_CHROMOSOME,SITE1_REGION,SITE1_REGION_NUMBER,SITE1_CONTIG,SITE1_POSITION,SITE1_DESCRIPTION,SITE2_ENTREZ_GENE_ID,SITE2_ENSEMBL_TRANSCRIPT_ID,SITE2_CHROMOSOME,SITE2_REGION,SITE2_REGION_NUMBER,SITE2_CONTIG,SITE2_POSITION,SITE2_DESCRIPTION,NCBI_BUILD,DNA_SUPPORT,RNA_SUPPORT,TUMOR_READ_COUNT,TUMOR_VARIANT_COUNT,ANNOTATION,EVENT_INFO,COMMENTS,SV_STATUS)
VALUES(7,1,8031,'ENST00000344348','10','exon',-1,'q13.4',51582939,'NCOA4-RET.N7R12_1',5979,'ENST00000340058','10','exon',-1,'p13.1',43612031,'NCOA4-RET.N7R12_2','GRCh37','no','yes',100001,80000,'NCOA4-RET.N7R1','Fusion','Gain-of-Function','SOMATIC');
INSERT INTO structural_variant (GENETIC_PROFILE_ID,SAMPLE_ID,SITE1_ENTREZ_GENE_ID,SITE1_ENSEMBL_TRANSCRIPT_ID,SITE1_CHROMOSOME,SITE1_REGION,SITE1_REGION_NUMBER,SITE1_CONTIG,SITE1_POSITION,SITE1_DESCRIPTION,SITE2_ENTREZ_GENE_ID,SITE2_ENSEMBL_TRANSCRIPT_ID,SITE2_CHROMOSOME,SITE2_REGION,SITE2_REGION_NUMBER,SITE2_CONTIG,SITE2_POSITION,SITE2_DESCRIPTION,NCBI_BUILD,DNA_SUPPORT,RNA_SUPPORT,TUMOR_READ_COUNT,TUMOR_VARIANT_COUNT,ANNOTATION,EVENT_INFO,COMMENTS,SV_STATUS)
VALUES(7,1,27436,'ENST00000318522','2','exon',-1,'q13.4',42492091,'EML4-ALK.E6bA20.AB374362_1',238,'ENST00000389048','2','exon',-1,'p13.1',29446394,'EML4-ALK.E6bA20.AB374362_2','GRCh37','no','yes',100002,70000,'EML4-ALK.E6bA20.AB374362','Fusion','Gain-of-Function','GERMLINE');
INSERT INTO structural_variant (GENETIC_PROFILE_ID,SAMPLE_ID,SITE1_ENTREZ_GENE_ID,SITE1_ENSEMBL_TRANSCRIPT_ID,SITE1_CHROMOSOME,SITE1_REGION,SITE1_REGION_NUMBER,SITE1_CONTIG,SITE1_POSITION,SITE1_DESCRIPTION,SITE2_ENTREZ_GENE_ID,SITE2_ENSEMBL_TRANSCRIPT_ID,SITE2_CHROMOSOME,SITE2_REGION,SITE2_REGION_NUMBER,SITE2_CONTIG,SITE2_POSITION,SITE2_DESCRIPTION,NCBI_BUILD,DNA_SUPPORT,RNA_SUPPORT,TUMOR_READ_COUNT,TUMOR_VARIANT_COUNT,ANNOTATION,EVENT_INFO,COMMENTS,SV_STATUS)
VALUES(7,2,27436,'ENST00000318522','2','exon',-1,'q13.4',42492091,'EML4-ALK.E6bA20.AB374362_1',238,'ENST00000389048','2','exon',-1,'p13.1',29446394,'EML4-ALK.E6bA20.AB374362_2','GRCh37','no','yes',100002,70000,'EML4-ALK.E6bA20.AB374362-2','Fusion','Gain-of-Function','SOMATIC');
INSERT INTO structural_variant (GENETIC_PROFILE_ID,SAMPLE_ID,SITE1_ENTREZ_GENE_ID,SITE1_ENSEMBL_TRANSCRIPT_ID,SITE1_CHROMOSOME,SITE1_REGION,SITE1_REGION_NUMBER,SITE1_CONTIG,SITE1_POSITION,SITE1_DESCRIPTION,SITE2_ENTREZ_GENE_ID,SITE2_ENSEMBL_TRANSCRIPT_ID,SITE2_CHROMOSOME,SITE2_REGION,SITE2_REGION_NUMBER,SITE2_CONTIG,SITE2_POSITION,SITE2_DESCRIPTION,NCBI_BUILD,DNA_SUPPORT,RNA_SUPPORT,TUMOR_READ_COUNT,TUMOR_VARIANT_COUNT,ANNOTATION,EVENT_INFO,COMMENTS,SV_STATUS)
VALUES(7,1,7113,'ENST00000332149','21','exon',-1,'q13.4',42880007,'TMPRSS2-ERG.T1E2.COSF23.1_1',2078,'ENST00000442448','21','exon',-1,'p13.1',39956869,'TMPRSS2-ERG.T1E2.COSF23.1_2','GRCh37','no','yes',100003,60000,'TMPRSS2-ERG.T1E2.COSF23.1','Fusion','Gain-of-Function','SOMATIC');
INSERT INTO structural_variant (GENETIC_PROFILE_ID,SAMPLE_ID,SITE1_ENTREZ_GENE_ID,SITE1_ENSEMBL_TRANSCRIPT_ID,SITE1_CHROMOSOME,SITE1_REGION,SITE1_REGION_NUMBER,SITE1_CONTIG,SITE1_POSITION,SITE1_DESCRIPTION,SITE2_ENTREZ_GENE_ID,SITE2_ENSEMBL_TRANSCRIPT_ID,SITE2_CHROMOSOME,SITE2_REGION,SITE2_REGION_NUMBER,SITE2_CONTIG,SITE2_POSITION,SITE2_DESCRIPTION,NCBI_BUILD,DNA_SUPPORT,RNA_SUPPORT,TUMOR_READ_COUNT,TUMOR_VARIANT_COUNT,ANNOTATION,EVENT_INFO,COMMENTS,SV_STATUS)
VALUES(7,2,57670,'ENST00000242365','7','exon',-1,'q13.4',138536968,'KIAA1549-BRAF.K16B10.COSF509_1',673,'ENST00000288602','7','exon',-1,'p13.1',140482957,'KIAA1549-BRAF.K16B10.COSF509_2','GRCh37','no','yes',100000,90000,'KIAA1549-BRAF.K16B10.COSF509','Fusion','Gain-of-Function','SOMATIC');
INSERT INTO structural_variant (GENETIC_PROFILE_ID,SAMPLE_ID,SITE1_ENTREZ_GENE_ID,SITE1_ENSEMBL_TRANSCRIPT_ID,SITE1_CHROMOSOME,SITE1_REGION,SITE1_REGION_NUMBER,SITE1_CONTIG,SITE1_POSITION,SITE1_DESCRIPTION,SITE2_ENTREZ_GENE_ID,SITE2_ENSEMBL_TRANSCRIPT_ID,SITE2_CHROMOSOME,SITE2_REGION,SITE2_REGION_NUMBER,SITE2_CONTIG,SITE2_POSITION,SITE2_DESCRIPTION,NCBI_BUILD,DNA_SUPPORT,RNA_SUPPORT,TUMOR_READ_COUNT,TUMOR_VARIANT_COUNT,ANNOTATION,EVENT_INFO,COMMENTS,SV_STATUS)
VALUES(13,15,57670,'ENST00000242365','7','exon',-1,'q13.4',138536968,'KIAA1549-BRAF.K16B10.COSF509_1',673,'ENST00000288602','7','exon',-1,'p13.1',140482957,'KIAA1549-BRAF.K16B10.COSF509_2','GRCh37','no','yes',100000,90000,'KIAA1549-BRAF.K16B10.COSF509','Fusion','Gain-of-Function','SOMATIC');
INSERT INTO structural_variant (GENETIC_PROFILE_ID,SAMPLE_ID,SITE1_ENTREZ_GENE_ID,SITE1_ENSEMBL_TRANSCRIPT_ID,SITE1_CHROMOSOME,SITE1_REGION,SITE1_REGION_NUMBER,SITE1_CONTIG,SITE1_POSITION,SITE1_DESCRIPTION,SITE2_ENTREZ_GENE_ID,SITE2_ENSEMBL_TRANSCRIPT_ID,SITE2_CHROMOSOME,SITE2_REGION,SITE2_REGION_NUMBER,SITE2_CONTIG,SITE2_POSITION,SITE2_DESCRIPTION,NCBI_BUILD,DNA_SUPPORT,RNA_SUPPORT,TUMOR_READ_COUNT,TUMOR_VARIANT_COUNT,ANNOTATION,EVENT_INFO,COMMENTS,SV_STATUS)
VALUES(13,15,8031,'ENST00000344348','10','exon',-1,'q13.4',51582939,'NCOA4-RET.N7R12_1',5979,'ENST00000340058','10','exon',-1,'p13.1',43612031,'NCOA4-RET.N7R12_2','GRCh37','no','yes',100001,80000,'NCOA4-RET.N7R1-2','Fusion','Gain-of-Function','SOMATIC');
INSERT INTO structural_variant (GENETIC_PROFILE_ID,SAMPLE_ID,SITE1_ENTREZ_GENE_ID,SITE1_ENSEMBL_TRANSCRIPT_ID,SITE1_CHROMOSOME,SITE1_REGION,SITE1_REGION_NUMBER,SITE1_CONTIG,SITE1_POSITION,SITE1_DESCRIPTION,SITE2_ENTREZ_GENE_ID,SITE2_ENSEMBL_TRANSCRIPT_ID,SITE2_CHROMOSOME,SITE2_REGION,SITE2_REGION_NUMBER,SITE2_CONTIG,SITE2_POSITION,SITE2_DESCRIPTION,NCBI_BUILD,DNA_SUPPORT,RNA_SUPPORT,TUMOR_READ_COUNT,TUMOR_VARIANT_COUNT,ANNOTATION,EVENT_INFO,COMMENTS,SV_STATUS)
VALUES(13,15,7113,'ENST00000332149','21','exon',-1,'q13.4',42880007,'TMPRSS2-ERG.T1E2.COSF23.1_1',2078,'ENST00000442448','21','exon',-1,'p13.1',39956869,'TMPRSS2-ERG.T1E2.COSF23.1_2','GRCh37','no','yes',100003,60000,'TMPRSS2-ERG.T1E2.COSF23.1','Fusion','Gain-of-Function','Germline');
INSERT INTO structural_variant (GENETIC_PROFILE_ID,SAMPLE_ID,SITE1_ENTREZ_GENE_ID,SITE1_ENSEMBL_TRANSCRIPT_ID,SITE1_CHROMOSOME,SITE1_REGION,SITE1_REGION_NUMBER,SITE1_CONTIG,SITE1_POSITION,SITE1_DESCRIPTION,SITE2_ENTREZ_GENE_ID,SITE2_ENSEMBL_TRANSCRIPT_ID,SITE2_CHROMOSOME,SITE2_REGION,SITE2_REGION_NUMBER,SITE2_CONTIG,SITE2_POSITION,SITE2_DESCRIPTION,NCBI_BUILD,DNA_SUPPORT,RNA_SUPPORT,TUMOR_READ_COUNT,TUMOR_VARIANT_COUNT,ANNOTATION,EVENT_INFO,COMMENTS,SV_STATUS)
VALUES(13,15,8031,'ENST00000344348','10','exon',-1,'q13.4',51582939,'NCOA4-NULL',NULL,'ENST00000340058_NULL','10','exon',-1,'p13.1',43612031,'NCOA4-NULL','GRCh37','no','yes',100001,80000,'NCOA4-NULL','Fusion','Gain-of-Function','SOMATIC');

INSERT INTO alteration_driver_annotation (ALTERATION_EVENT_ID,GENETIC_PROFILE_ID,SAMPLE_ID, DRIVER_FILTER, DRIVER_FILTER_ANNOTATION, DRIVER_TIERS_FILTER, DRIVER_TIERS_FILTER_ANNOTATION)
VALUES (1,7,1, 'Putative_Passenger', 'Pathogenic', 'Tier 1', 'Potentially Actionable');

INSERT INTO alteration_driver_annotation (ALTERATION_EVENT_ID,GENETIC_PROFILE_ID,SAMPLE_ID, DRIVER_FILTER, DRIVER_FILTER_ANNOTATION, DRIVER_TIERS_FILTER, DRIVER_TIERS_FILTER_ANNOTATION)
VALUES (3,7,1, 'Putative_Driver', 'Pathogenic', 'Class 2', 'Highly Actionable');
INSERT INTO alteration_driver_annotation (ALTERATION_EVENT_ID,GENETIC_PROFILE_ID,SAMPLE_ID, DRIVER_FILTER, DRIVER_FILTER_ANNOTATION, DRIVER_TIERS_FILTER, DRIVER_TIERS_FILTER_ANNOTATION)
VALUES (5,7,2, 'Putative_Driver', 'Pathogenic', 'Class 3', 'Highly Actionable');

INSERT INTO mut_sig (CANCER_STUDY_ID,ENTREZ_GENE_ID,RANK,NumBasesCovered,NumMutations,P_VALUE,Q_VALUE) VALUES (1,207,1,998421,17,0.00000315,0.00233);
INSERT INTO mut_sig (CANCER_STUDY_ID,ENTREZ_GENE_ID,RANK,NumBasesCovered,NumMutations,P_VALUE,Q_VALUE) VALUES (1,208,2,3200341,351,0.000000012,0.00000000000212);

INSERT INTO genetic_alteration (GENETIC_PROFILE_ID,GENETIC_ENTITY_ID,`VALUES`) VALUES (2,1,'-0.4674,-0.6270,-1.2266,-1.2479,-1.2262,0.6962,-0.3338,-0.1264,0.7559,-1.1267,-0.5893,-1.1546,-1.0027,-1.3157,');
INSERT INTO genetic_alteration (GENETIC_PROFILE_ID,GENETIC_ENTITY_ID,`VALUES`) VALUES (2,2,'1.4146,-0.0662,-0.8585,-1.6576,-0.3552,-0.8306,0.8102,0.1146,0.3498,0.0349,0.4927,-0.8665,-0.4754,-0.7221,');
INSERT INTO genetic_alteration (GENETIC_PROFILE_ID,GENETIC_ENTITY_ID,`VALUES`) VALUES (3,2,'-0.8097,0.7360,-1.0225,-0.8922,0.7247,0.3537,1.2702,-0.1419,');

INSERT INTO cna_event (CNA_EVENT_ID,ENTREZ_GENE_ID,ALTERATION) VALUES (1,207,-2);
INSERT INTO cna_event (CNA_EVENT_ID,ENTREZ_GENE_ID,ALTERATION) VALUES (2,208,2);
INSERT INTO cna_event (CNA_EVENT_ID,ENTREZ_GENE_ID,ALTERATION) VALUES (3,207,2);

INSERT INTO sample_cna_event (CNA_EVENT_ID,SAMPLE_ID,GENETIC_PROFILE_ID, ANNOTATION_JSON) VALUES (1,1,2, '{"columnName":{"fieldName":"fieldValue"}}');
INSERT INTO sample_cna_event (CNA_EVENT_ID,SAMPLE_ID,GENETIC_PROFILE_ID, ANNOTATION_JSON) VALUES (2,1,2, '{"columnName":{"fieldName":"fieldValue"}}');
INSERT INTO sample_cna_event (CNA_EVENT_ID,SAMPLE_ID,GENETIC_PROFILE_ID, ANNOTATION_JSON) VALUES (3,2,2, '{"columnName":{"fieldName":"fieldValue"}}');

INSERT INTO alteration_driver_annotation (ALTERATION_EVENT_ID,GENETIC_PROFILE_ID,SAMPLE_ID, DRIVER_FILTER, DRIVER_FILTER_ANNOTATION, DRIVER_TIERS_FILTER, DRIVER_TIERS_FILTER_ANNOTATION) VALUES (1,2,1, 'Putative_Driver', 'Pathogenic', 'Tier 1', 'Highly Actionable');
INSERT INTO alteration_driver_annotation (ALTERATION_EVENT_ID,GENETIC_PROFILE_ID,SAMPLE_ID, DRIVER_FILTER, DRIVER_FILTER_ANNOTATION, DRIVER_TIERS_FILTER, DRIVER_TIERS_FILTER_ANNOTATION) VALUES (3,2,2, 'Putative_Passenger', 'Pathogenic', 'Tier 2', 'Potentially Actionable');

INSERT INTO gistic (GISTIC_ROI_ID,CANCER_STUDY_ID,CHROMOSOME,CYTOBAND,WIDE_PEAK_START,WIDE_PEAK_END,Q_VALUE,AMP) VALUES (1,1,1,'1q32.32',123,136,0.0208839997649193,0);
INSERT INTO gistic (GISTIC_ROI_ID,CANCER_STUDY_ID,CHROMOSOME,CYTOBAND,WIDE_PEAK_START,WIDE_PEAK_END,Q_VALUE,AMP) VALUES (2,1,2,'2q30.32',324234,324280,0.000323799991747364,1);
INSERT INTO gistic (GISTIC_ROI_ID,CANCER_STUDY_ID,CHROMOSOME,CYTOBAND,WIDE_PEAK_START,WIDE_PEAK_END,Q_VALUE,AMP) VALUES (3,2,1,'1q3.32',123,136,0.000000129710002738648,0);

INSERT INTO gistic_to_gene (GISTIC_ROI_ID,ENTREZ_GENE_ID) VALUES (1,207);
INSERT INTO gistic_to_gene (GISTIC_ROI_ID,ENTREZ_GENE_ID) VALUES (1,208);
INSERT INTO gistic_to_gene (GISTIC_ROI_ID,ENTREZ_GENE_ID) VALUES (2,207);
INSERT INTO gistic_to_gene (GISTIC_ROI_ID,ENTREZ_GENE_ID) VALUES (3,208);

INSERT INTO clinical_event (CLINICAL_EVENT_ID,PATIENT_ID,START_DATE,STOP_DATE,EVENT_TYPE) VALUES (1,1,123,NULL,'STATUS');
INSERT INTO clinical_event (CLINICAL_EVENT_ID,PATIENT_ID,START_DATE,STOP_DATE,EVENT_TYPE) VALUES (2,1,233,345,'SPECIMEN');
INSERT INTO clinical_event (CLINICAL_EVENT_ID,PATIENT_ID,START_DATE,STOP_DATE,EVENT_TYPE) VALUES (3,2,213,445,'TREATMENT');
INSERT INTO clinical_event (CLINICAL_EVENT_ID,PATIENT_ID,START_DATE,STOP_DATE,EVENT_TYPE) VALUES (4,2,211,441,'SEQENCING');

INSERT INTO clinical_event_data (CLINICAL_EVENT_ID,`KEY`,`VALUE`) VALUES (1,'STATUS','radiographic_progression');
INSERT INTO clinical_event_data (CLINICAL_EVENT_ID,`KEY`,`VALUE`) VALUES (1,'SAMPLE_ID','TCGA-A1-A0SB-01');
INSERT INTO clinical_event_data (CLINICAL_EVENT_ID,`KEY`,`VALUE`) VALUES (2,'SURGERY','OA II Initial');
INSERT INTO clinical_event_data (CLINICAL_EVENT_ID,`KEY`,`VALUE`) VALUES (2,'SAMPLE_ID','TCGA-A1-A0SB-01');
INSERT INTO clinical_event_data (CLINICAL_EVENT_ID,`KEY`,`VALUE`) VALUES (3,'EVENT_TYPE_DETAILED','AA III Recurrence1');
INSERT INTO clinical_event_data (CLINICAL_EVENT_ID,`KEY`,`VALUE`) VALUES (3,'AGENT','Madeupanib');
INSERT INTO clinical_event_data (CLINICAL_EVENT_ID,`KEY`,`VALUE`) VALUES (3,'AGENT_TARGET','Directly to forehead, Elbow');
INSERT INTO clinical_event_data (CLINICAL_EVENT_ID,`KEY`,`VALUE`) VALUES (3,'SAMPLE_ID','TCGA-A1-A0SD-01');
INSERT INTO clinical_event_data (CLINICAL_EVENT_ID,`KEY`,`VALUE`) VALUES (4,'SAMPLE_ID','TCGA-A1-A0SD-01');

INSERT INTO geneset (ID,GENETIC_ENTITY_ID,EXTERNAL_ID,NAME,DESCRIPTION,REF_LINK) VALUES (1,17,'MORF_ATRX','MORF ATRX name','Morf description','https://morf_link');
INSERT INTO geneset (ID,GENETIC_ENTITY_ID,EXTERNAL_ID,NAME,DESCRIPTION,REF_LINK) VALUES (2,18,'HINATA_NFKB_MATRIX','HINATA NFKB MATRIX name','Hinata description','https://hinata_link');

INSERT INTO geneset_gene (GENESET_ID,ENTREZ_GENE_ID) VALUES (1,207);
INSERT INTO geneset_gene (GENESET_ID,ENTREZ_GENE_ID) VALUES (1,208);
INSERT INTO geneset_gene (GENESET_ID,ENTREZ_GENE_ID) VALUES (1,10000);
INSERT INTO geneset_gene (GENESET_ID,ENTREZ_GENE_ID) VALUES (2,369);
INSERT INTO geneset_gene (GENESET_ID,ENTREZ_GENE_ID) VALUES (2,472);

INSERT INTO genetic_alteration (GENETIC_PROFILE_ID,GENETIC_ENTITY_ID,`VALUES`) VALUES (9,17,'-0.0670,-0.6270,-1.2266,-1.2079,-1.2262,0.6962,-0.3338,-0.1260,0.7559,-1.1267,-0.5893,-1.1506,-1.0027,-1.3157,');
INSERT INTO genetic_alteration (GENETIC_PROFILE_ID,GENETIC_ENTITY_ID,`VALUES`) VALUES (9,18,'1.0106,-0.0662,-0.8585,-1.6576,-0.3552,-0.8306,0.8102,0.1106,0.3098,0.0309,0.0927,-0.8665,-0.0750,-0.7221,');


-- Root node ->  sub node A -> parent node 1 -> MORF_ATRX
--    "              "             "         -> HINATA_NFKB_MATRIX
--    "              "      -> parent node 2 -> HINATA_NFKB_MATRIX
-- Root node ->  sub node B -> x (dead branch)
INSERT INTO geneset_hierarchy_node (NODE_ID,NODE_NAME,PARENT_ID) VALUES (1,'Root node',NULL);
INSERT INTO geneset_hierarchy_node (NODE_ID,NODE_NAME,PARENT_ID) VALUES (2,'Sub node A',1);
INSERT INTO geneset_hierarchy_node (NODE_ID,NODE_NAME,PARENT_ID) VALUES (3,'Sub node B',1);
INSERT INTO geneset_hierarchy_node (NODE_ID,NODE_NAME,PARENT_ID) VALUES (4,'Parent node 1',2);
INSERT INTO geneset_hierarchy_node (NODE_ID,NODE_NAME,PARENT_ID) VALUES (5,'Parent node 2',2);

INSERT INTO geneset_hierarchy_leaf (NODE_ID,GENESET_ID) VALUES (4,1);
INSERT INTO geneset_hierarchy_leaf (NODE_ID,GENESET_ID) VALUES (4,2);
INSERT INTO geneset_hierarchy_leaf (NODE_ID,GENESET_ID) VALUES (5,2);

INSERT INTO mutation_count_by_keyword (GENETIC_PROFILE_ID,KEYWORD,ENTREZ_GENE_ID,KEYWORD_COUNT,GENE_COUNT) VALUES (6, 'AKT1 truncating', 207, 54, 64);
INSERT INTO mutation_count_by_keyword (GENETIC_PROFILE_ID,KEYWORD,ENTREZ_GENE_ID,KEYWORD_COUNT,GENE_COUNT) VALUES (6, NULL, 207, 21, 22);
INSERT INTO mutation_count_by_keyword (GENETIC_PROFILE_ID,KEYWORD,ENTREZ_GENE_ID,KEYWORD_COUNT,GENE_COUNT) VALUES (6, 'ARAF G1513 missense', 369, 1, 2);
INSERT INTO mutation_count_by_keyword (GENETIC_PROFILE_ID,KEYWORD,ENTREZ_GENE_ID,KEYWORD_COUNT,GENE_COUNT) VALUES (6, 'ARAF G1514 missense', 369, 4, 7);
INSERT INTO mutation_count_by_keyword (GENETIC_PROFILE_ID,KEYWORD,ENTREZ_GENE_ID,KEYWORD_COUNT,GENE_COUNT) VALUES (8, 'NOC2L truncating', 26155, 1, 3);

INSERT INTO users (EMAIL, NAME, ENABLED) VALUES ('mockemail@email.com', 'MOCK USER', 1);
INSERT INTO users (EMAIL, NAME, ENABLED) VALUES ('mockemail2@email.com', 'MOCK USER 2', 1);
INSERT INTO users (EMAIL, NAME, ENABLED) VALUES ('mockemail3@email.com', 'MOCK USER 3', 1);
INSERT INTO users (EMAIL, NAME, ENABLED) VALUES ('mockemail4@email.com', 'MOCK USER 4', 1);

INSERT INTO data_access_tokens (TOKEN, USERNAME, EXPIRATION, CREATION) VALUES ('6c9a641e-9719-4b09-974c-f17e089b37e8', 'mockemail@email.com', '2018-11-12 11:11:15', '2018-10-12 11:11:15');
INSERT INTO data_access_tokens (TOKEN, USERNAME, EXPIRATION, CREATION) VALUES ('6c9a641e-9719-fake-data-f17e089b37e8', 'mockemail2@email.com', '2018-5-14 11:11:15', '2018-4-14 11:11:15');
INSERT INTO data_access_tokens (TOKEN, USERNAME, EXPIRATION, CREATION) VALUES ('12345678-119e-4bC9-9a4c-f123kl9b37e8', 'mockemail3@email.com', '2017-1-12 11:11:15', '2016-12-12 11:11:15');
INSERT INTO data_access_tokens (TOKEN, USERNAME, EXPIRATION, CREATION) VALUES ('6c9a641e-9719-4b09-974c-4rb1tr4ry5tr', 'mockemail3@email.com', '2017-10-9 11:11:15', '2017-9-9 11:11:15');
INSERT INTO data_access_tokens (TOKEN, USERNAME, EXPIRATION, CREATION) VALUES ('1337rand-ki1n-4bna-974c-s4sk3n4rut0l', 'mockemail3@email.com', '2018-8-25 11:11:15', '2018-7-25 11:11:15');
INSERT INTO data_access_tokens (TOKEN, USERNAME, EXPIRATION, CREATION) VALUES ('12445678-119e-4bC9-9a4c-f124kl9b47e8', 'mockemail4@email.com', '2017-1-12 11:11:15', '2016-12-12 11:11:15');
INSERT INTO data_access_tokens (TOKEN, USERNAME, EXPIRATION, CREATION) VALUES ('6cokl41e-9719-4b09-974c-4rb1tr4ry5tr', 'mockemail4@email.com', '2017-10-9 11:11:15', '2017-9-9 11:11:15');
INSERT INTO data_access_tokens (TOKEN, USERNAME, EXPIRATION, CREATION) VALUES ('1447rand-ki1n-4bna-974c-s4sk4n4rut0l', 'mockemail4@email.com', '2018-8-25 11:11:15', '2018-7-25 11:11:15');

-- treatment test data
INSERT INTO generic_entity_properties (ID,GENETIC_ENTITY_ID,NAME,`VALUE`) VALUES (1,19,'NAME','Tanespimycin');
INSERT INTO generic_entity_properties (ID,GENETIC_ENTITY_ID,NAME,`VALUE`) VALUES (2,19,'DESCRIPTION','Hsp90 inhibitor');
INSERT INTO generic_entity_properties (ID,GENETIC_ENTITY_ID,NAME,`VALUE`) VALUES (3,19,'URL','https://en.wikipedia.org/wiki/Tanespimycin');
INSERT INTO generic_entity_properties (ID,GENETIC_ENTITY_ID,NAME,`VALUE`) VALUES (4,20,'NAME','Larotrectinib');
INSERT INTO generic_entity_properties (ID,GENETIC_ENTITY_ID,NAME,`VALUE`) VALUES (5,20,'DESCRIPTION','TrkA/B/C inhibitor');
INSERT INTO generic_entity_properties (ID,GENETIC_ENTITY_ID,NAME,`VALUE`) VALUES (6,20,'URL','https://en.wikipedia.org/wiki/Larotrectinib');
INSERT INTO genetic_alteration (GENETIC_PROFILE_ID,GENETIC_ENTITY_ID,`VALUES`) VALUES (11,19,'-0.0670,-0.6270,-1.2266,-1.2079,-1.2262,0.6962,-0.3338,-0.1260,0.7559,-1.1267,-0.5893,-1.1506,-1.0027,-1.3157,');
INSERT INTO genetic_alteration (GENETIC_PROFILE_ID,GENETIC_ENTITY_ID,`VALUES`) VALUES (11,20,'1.0106,-0.0662,-0.8585,-1.6576,-0.3552,-0.8306,0.8102,0.1106,0.3098,0.0309,0.0927,-0.8665,-0.0750,-0.7221,');

-- allele specific copy number data
INSERT INTO allele_specific_copy_number (MUTATION_EVENT_ID, GENETIC_PROFILE_ID, SAMPLE_ID, ASCN_INTEGER_COPY_NUMBER, ASCN_METHOD, CCF_EXPECTED_COPIES_UPPER, CCF_EXPECTED_COPIES, CLONAL, MINOR_COPY_NUMBER, EXPECTED_ALT_COPIES, TOTAL_COPY_NUMBER) VALUES (2040, 6, 1, 3, 'FACETS', 1.25, 1.75, 'CLONAL', 2, 1, 4);
INSERT INTO allele_specific_copy_number (MUTATION_EVENT_ID, GENETIC_PROFILE_ID, SAMPLE_ID, ASCN_INTEGER_COPY_NUMBER, ASCN_METHOD, CCF_EXPECTED_COPIES_UPPER, CCF_EXPECTED_COPIES, CLONAL, MINOR_COPY_NUMBER, EXPECTED_ALT_COPIES, TOTAL_COPY_NUMBER) VALUES (2038, 6, 6, 1, 'FACETS', 1.25, 1.75, 'SUBCLONAL', 1, 1, 2);
-- generic assay test data
-- mutational signature test data
INSERT INTO generic_entity_properties (ID,GENETIC_ENTITY_ID,NAME,`VALUE`) VALUES (7,28,'name','mean_1');
INSERT INTO generic_entity_properties (ID,GENETIC_ENTITY_ID,NAME,`VALUE`) VALUES (8,28,'description','description of mean_1');
INSERT INTO generic_entity_properties (ID,GENETIC_ENTITY_ID,NAME,`VALUE`) VALUES (9,29,'name','mean_2');
INSERT INTO generic_entity_properties (ID,GENETIC_ENTITY_ID,NAME,`VALUE`) VALUES (10,29,'description','description of mean_2');

INSERT INTO genetic_alteration (GENETIC_PROFILE_ID,GENETIC_ENTITY_ID,`VALUES`) VALUES (12,28,'-0.0670,-0.6270,-1.2266,-1.2079,-1.2262,0.6962,-0.3338,-0.1260,0.7559,-1.1267,-0.5893,-1.1506,-1.0027,-1.3157,');
INSERT INTO genetic_alteration (GENETIC_PROFILE_ID,GENETIC_ENTITY_ID,`VALUES`) VALUES (12,29,'1.0106,-0.0662,-0.8585,-1.6576,-0.3552,-0.8306,0.8102,0.1106,0.3098,0.0309,0.0927,-0.8665,-0.0750,-0.7221,');
