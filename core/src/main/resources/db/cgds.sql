-- phpMyAdmin SQL Dump
-- version 2.11.9.2
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: Mar 25, 2010 at 11:45 AM
-- Server version: 5.0.67
-- PHP Version: 5.2.11

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET FOREIGN_KEY_CHECKS=0;

--
-- Database: `cgds`
--

-- --------------------------------------------------------

--
-- Table structure for table `type_of_cancer`
--
drop table IF EXISTS type_of_cancer;
CREATE TABLE `type_of_cancer` (
  `TYPE_OF_CANCER_ID` varchar(25) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  `CLINICAL_TRIAL_KEYWORDS` varchar(1024) NOT NULL,
  `DEDICATED_COLOR` char(31) NOT NULL,
  `SHORT_NAME` varchar(127) NOT NULL,
  PRIMARY KEY  (`TYPE_OF_CANCER_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `cancer_study`
--
drop table IF EXISTS cancer_study;
CREATE TABLE `cancer_study` (
  `CANCER_STUDY_ID` int(11) NOT NULL auto_increment,
  `CANCER_STUDY_IDENTIFIER` varchar(255),
  `TYPE_OF_CANCER_ID` varchar(25) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  `SHORT_NAME` varchar(64) NOT NULL,
  `DESCRIPTION` varchar(1024) NOT NULL,
  `PUBLIC` BOOLEAN NOT NULL,
  `PMID` varchar(20) DEFAULT NULL,
  `CITATION` varchar(200) DEFAULT NULL,
  `GROUPS` varchar(200) DEFAULT NULL,
  PRIMARY KEY  (`CANCER_STUDY_ID`),
  UNIQUE (`CANCER_STUDY_IDENTIFIER`),
  FOREIGN KEY (`TYPE_OF_CANCER_ID`) REFERENCES `type_of_cancer` (`TYPE_OF_CANCER_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

-- --------------------------------------------------------

--
-- Table structure for `entity`
--
drop table IF EXISTS entity;
CREATE TABLE `entity` (
  `INTERNAL_ID` int(11) NOT NULL auto_increment,
  `STABLE_ID` varchar(50) NOT NULL,
  `ENTITY_TYPE` varchar(50) NOT NULL,
  PRIMARY KEY (`INTERNAL_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 COMMENT='ENTITY_TYPE can be STUDY, PATIENT, SAMPLE';

--
-- Table structure for `entity_link`
--
drop table IF EXISTS entity_link;
CREATE TABLE `entity_link` (
  `INTERNAL_ID` int(11) NOT NULL auto_increment,
  `PARENT_ID` int(11) NOT NULL,
  `CHILD_ID` int(11) NOT NULL,
  PRIMARY KEY  (`INTERNAL_ID`),
  FOREIGN KEY (`PARENT_ID`) REFERENCES `entity` (`INTERNAL_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`CHILD_ID`) REFERENCES `entity` (`INTERNAL_ID`) ON DELETE CASCADE
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

--
-- Table structure for table `users`
--
drop table IF EXISTS users;
CREATE TABLE `users` (
  `EMAIL` varchar(128) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  `ENABLED` BOOLEAN NOT NULL,
  PRIMARY KEY  (`EMAIL`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `access_rights`
--
drop table IF EXISTS authorities;
CREATE TABLE `authorities` (
  `EMAIL` varchar(128) NOT NULL,
  `AUTHORITY` varchar(50) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `patient`
--
drop table IF EXISTS patient;
CREATE TABLE `patient` (
  `INTERNAL_ID` int(11) NOT NULL auto_increment,
  `STABLE_ID` varchar(50) NOT NULL,
  `CANCER_STUDY_ID` int(11) NOT NULL,
  PRIMARY KEY (`INTERNAL_ID`),
  FOREIGN KEY (`CANCER_STUDY_ID`) REFERENCES `cancer_study` (`CANCER_STUDY_ID`) ON DELETE CASCADE
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

-- --------------------------------------------------------

--
-- Table structure for table `sample`
--
drop table IF EXISTS sample;
CREATE TABLE `sample` (
  `INTERNAL_ID` int(11) NOT NULL auto_increment,
  `STABLE_ID` varchar(50) NOT NULL,
  `SAMPLE_TYPE` varchar(255) NOT NULL,
  `PATIENT_ID` int(11) NOT NULL,
  `TYPE_OF_CANCER_ID` varchar(25) NOT NULL,
  PRIMARY KEY (`INTERNAL_ID`),
  FOREIGN KEY (`PATIENT_ID`) REFERENCES `patient` (`INTERNAL_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`TYPE_OF_CANCER_ID`) REFERENCES `type_of_cancer` (`TYPE_OF_CANCER_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `patient_list`
--
drop table IF EXISTS patient_list;
CREATE TABLE `patient_list` (
  `LIST_ID` int(11) NOT NULL auto_increment,
  `STABLE_ID` varchar(255) NOT NULL,
  `CATEGORY` varchar(255) NOT NULL,
  `CANCER_STUDY_ID` int(11) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  `DESCRIPTION` mediumtext,
  PRIMARY KEY  (`LIST_ID`),
  UNIQUE (`STABLE_ID`),
  FOREIGN KEY (`CANCER_STUDY_ID`) REFERENCES `cancer_study` (`CANCER_STUDY_ID`) ON DELETE CASCADE
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `patient_list_list`
--
drop table IF EXISTS patient_list_list;
CREATE TABLE `patient_list_list` (
  `LIST_ID` int(11) NOT NULL,
  `PATIENT_ID` int(11) NOT NULL,
  PRIMARY KEY  (`LIST_ID`,`PATIENT_ID`),
  FOREIGN KEY (`LIST_ID`) REFERENCES `sample_list` (`LIST_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`PATIENT_ID`) REFERENCES `patient` (`INTERNAL_ID`) ON DELETE CASCADE
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `gene`
--
drop table IF EXISTS gene;
CREATE TABLE `gene` (
  `ENTREZ_GENE_ID` int(255) NOT NULL,
  `HUGO_GENE_SYMBOL` varchar(255) NOT NULL,
  `TYPE` varchar(50),
  `CYTOBAND` varchar(50),
  `LENGTH` int(11),
  PRIMARY KEY  (`ENTREZ_GENE_ID`),
  KEY `HUGO_GENE_SYMBOL` (`HUGO_GENE_SYMBOL`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `gene_alias`
--
drop table IF EXISTS gene_alias;
CREATE TABLE `gene_alias` (
  `ENTREZ_GENE_ID` int(255) NOT NULL,
  `GENE_ALIAS` varchar(255) NOT NULL,
  PRIMARY KEY  (`ENTREZ_GENE_ID`,`GENE_ALIAS`),
  FOREIGN KEY (`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `uniprot_id_mapping`
--
drop table IF EXISTS uniprot_id_mapping;
CREATE TABLE `uniprot_id_mapping` (
  `UNIPROT_ACC` varchar(255) NOT NULL,
  `UNIPROT_ID` varchar(255) NOT NULL,
  `ENTREZ_GENE_ID` int(255),
  PRIMARY KEY  (`ENTREZ_GENE_ID`, `UNIPROT_ID`),
  KEY (`UNIPROT_ID`),
  Key (`UNIPROT_ACC`),
  FOREIGN KEY (`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `genetic_profile`
--
drop table IF EXISTS genetic_profile;
CREATE TABLE `genetic_profile` (
  `GENETIC_PROFILE_ID` int(11) NOT NULL auto_increment,
  `STABLE_ID` varchar(255) NOT NULL,
  `CANCER_STUDY_ID` int(11) NOT NULL,
  `GENETIC_ALTERATION_TYPE` varchar(255) NOT NULL,
  `DATATYPE` varchar(255) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  `DESCRIPTION` mediumtext,
  `SHOW_PROFILE_IN_ANALYSIS_TAB` binary(1) NOT NULL,
  PRIMARY KEY  (`GENETIC_PROFILE_ID`),
  UNIQUE (`STABLE_ID`),
  FOREIGN KEY (`CANCER_STUDY_ID`) REFERENCES `cancer_study` (`CANCER_STUDY_ID`) ON DELETE CASCADE
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2 ;

--
-- Table structure for table `genetic_alteration`
--
drop table IF EXISTS genetic_alteration;
CREATE TABLE `genetic_alteration` (
  `GENETIC_PROFILE_ID` int(11) NOT NULL,
  `ENTREZ_GENE_ID` int(255) NOT NULL,
  `VALUES` longtext NOT NULL,
  KEY `QUICK_LOOK_UP` (`ENTREZ_GENE_ID`),
  KEY `QUICK_LOOK_UP2` (`ENTREZ_GENE_ID`,`GENETIC_PROFILE_ID`),
  FOREIGN KEY (`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`),
  FOREIGN KEY (`GENETIC_PROFILE_ID`) REFERENCES `genetic_profile` (`GENETIC_PROFILE_ID`) ON DELETE CASCADE
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `genetic_profile_samples`
--
drop table IF EXISTS genetic_profile_samples;
CREATE TABLE `genetic_profile_samples` (
  `GENETIC_PROFILE_ID` int(11) NOT NULL,
  `ORDERED_SAMPLE_LIST` longtext NOT NULL,
  UNIQUE (`GENETIC_PROFILE_ID`),
  FOREIGN KEY (`GENETIC_PROFILE_ID`) REFERENCES `genetic_profile` (`GENETIC_PROFILE_ID`) ON DELETE CASCADE
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `sample_profile`
--
drop table IF EXISTS sample_profile;
CREATE TABLE `sample_profile` (
  `SAMPLE_ID` int(11) NOT NULL,
  `GENETIC_PROFILE_ID` int(11) NOT NULL,
  FOREIGN KEY (`GENETIC_PROFILE_ID`) REFERENCES `genetic_profile` (`GENETIC_PROFILE_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`SAMPLE_ID`) REFERENCES `sample` (`INTERNAL_ID`) ON DELETE CASCADE
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

drop table IF EXISTS micro_rna_alteration;
CREATE TABLE `micro_rna_alteration` (
  `GENETIC_PROFILE_ID` int(11) NOT NULL,
  `MICRO_RNA_ID` varchar(50) NOT NULL,
  `VALUES` longtext NOT NULL,
  UNIQUE KEY `QUICK_LOOK_UP1` (`GENETIC_PROFILE_ID`,`MICRO_RNA_ID`),
  FOREIGN KEY (`GENETIC_PROFILE_ID`) REFERENCES `genetic_profile` (`GENETIC_PROFILE_ID`) ON DELETE CASCADE
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `micro_rna`
--
drop table IF EXISTS micro_rna;
CREATE TABLE `micro_rna` (
  `ID` varchar(50) NOT NULL,
  `VARIANT_ID` varchar(50) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

drop table IF EXISTS mutation_event;
CREATE TABLE `mutation_event` (
  `MUTATION_EVENT_ID` int(255) NOT NULL auto_increment,
  `ENTREZ_GENE_ID` int(255) NOT NULL,
  `CHR` varchar(5),
  `START_POSITION` bigint(20),
  `END_POSITION` bigint(20),
  `REFERENCE_ALLELE` varchar(255),
  `TUMOR_SEQ_ALLELE` varchar(255),
  `PROTEIN_CHANGE` varchar(255),
  `MUTATION_TYPE` varchar(255) COMMENT 'e.g. Missense, Nonsence, etc.',
  `FUNCTIONAL_IMPACT_SCORE` varchar(50) COMMENT 'Result from OMA/XVAR.',
  `FIS_VALUE` float,
  `LINK_XVAR` varchar(500) COMMENT 'Link to OMA/XVAR Landing Page for the specific mutation.',
  `LINK_PDB` varchar(500),
  `LINK_MSA` varchar(500),
  `NCBI_BUILD` varchar(10),
  `STRAND` varchar(2),
  `VARIANT_TYPE` varchar(15),
  `DB_SNP_RS` varchar(25),
  `DB_SNP_VAL_STATUS` varchar(255),
  `ONCOTATOR_DBSNP_RS` varchar(255),
  `ONCOTATOR_REFSEQ_MRNA_ID` varchar(64),
  `ONCOTATOR_CODON_CHANGE` varchar(255),
  `ONCOTATOR_UNIPROT_ENTRY_NAME` varchar(64),
  `ONCOTATOR_UNIPROT_ACCESSION` varchar(64),
  `ONCOTATOR_PROTEIN_POS_START` int(11),
  `ONCOTATOR_PROTEIN_POS_END` int(11),
  `CANONICAL_TRANSCRIPT` boolean,
  `KEYWORD` varchar(50) DEFAULT NULL COMMENT 'e.g. truncating, V200 Missense, E338del, ',
  KEY (`KEYWORD`),
  PRIMARY KEY  (`MUTATION_EVENT_ID`),
  UNIQUE (`CHR`, `START_POSITION`, `END_POSITION`, `TUMOR_SEQ_ALLELE`, `ENTREZ_GENE_ID`, `PROTEIN_CHANGE`, `MUTATION_TYPE`),
  FOREIGN KEY (`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 COMMENT='Mutation Data';

--
-- Table structure for table `mutation`
--
drop table IF EXISTS mutation;
CREATE TABLE `mutation` (
  `MUTATION_EVENT_ID` int(255) NOT NULL,
  `GENETIC_PROFILE_ID` int(11) NOT NULL,
  `SAMPLE_ID` int(11) NOT NULL,
  `ENTREZ_GENE_ID` int(255) NOT NULL, # this is included here for performance
  `CENTER` varchar(100),
  `SEQUENCER` varchar(255),
  `MUTATION_STATUS` varchar(25) COMMENT 'Germline, Somatic or LOH.',
  `VALIDATION_STATUS` varchar(25),
  `TUMOR_SEQ_ALLELE1` varchar(255),
  `TUMOR_SEQ_ALLELE2` varchar(255),
  `MATCHED_NORM_SAMPLE_BARCODE` varchar(255),
  `MATCH_NORM_SEQ_ALLELE1` varchar(255),
  `MATCH_NORM_SEQ_ALLELE2` varchar(255),
  `TUMOR_VALIDATION_ALLELE1` varchar(255),
  `TUMOR_VALIDATION_ALLELE2` varchar(255),
  `MATCH_NORM_VALIDATION_ALLELE1` varchar(255),
  `MATCH_NORM_VALIDATION_ALLELE2` varchar(255),
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
  KEY (`GENETIC_PROFILE_ID`,`ENTREZ_GENE_ID`),
  KEY (`GENETIC_PROFILE_ID`,`SAMPLE_ID`),
  KEY (`GENETIC_PROFILE_ID`),
  KEY (`ENTREZ_GENE_ID`),
  KEY (`SAMPLE_ID`),
  FOREIGN KEY (`MUTATION_EVENT_ID`) REFERENCES `mutation_event` (`MUTATION_EVENT_ID`),
  FOREIGN KEY (`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`),
  FOREIGN KEY (`GENETIC_PROFILE_ID`) REFERENCES `genetic_profile` (`GENETIC_PROFILE_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`SAMPLE_ID`) REFERENCES `sample` (`INTERNAL_ID`) ON DELETE CASCADE
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='Mutation Data Details';

drop table if EXISTS mutation_count;
CREATE TABLE `mutation_count` (
  `GENETIC_PROFILE_ID` int(11) NOT NULL,
  `SAMPLE_ID` int(11) NOT NULL,
  `MUTATION_COUNT` int NOT NULL,
  KEY (`GENETIC_PROFILE_ID`,`SAMPLE_ID`),
  FOREIGN KEY (`GENETIC_PROFILE_ID`) REFERENCES `genetic_profile` (`GENETIC_PROFILE_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`SAMPLE_ID`) REFERENCES `sample` (`INTERNAL_ID`) ON DELETE CASCADE
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `mutation_frequency`
--
drop table IF EXISTS mutation_frequency;
CREATE TABLE `mutation_frequency` (
  `ENTREZ_GENE_ID` int(11) NOT NULL,
  `SOMATIC_MUTATION_RATE` double NOT NULL,
  `CANCER_STUDY_ID` int(11) NOT NULL,
  FOREIGN KEY (`CANCER_STUDY_ID`) REFERENCES `cancer_study` (`CANCER_STUDY_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;


-- --------------------------------------------------------

--
-- Table structure for table `entity_attributes`
--
drop table IF EXISTS entity_attribute;
CREATE TABLE `entity_attribute` (
  `ENTITY_ID` int(11) NOT NULL,
  `ATTR_ID` varchar(255) NOT NULL,
  `ATTR_VALUE` varchar(255) NOT NULL,
  PRIMARY KEY (`ENTITY_ID`, `ATTR_ID`),
  FOREIGN KEY (`ENTITY_ID`) REFERENCES `entity` (`INTERNAL_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`ATTR_ID`) REFERENCES `attribute_metadata` (`ATTR_ID`) ON DELETE CASCADE
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `attributes`
--
drop table IF EXISTS attribute_metadata;
CREATE TABLE `attribute_metadata` (
  `ATTR_ID` varchar(255) NOT NULL,
  `DISPLAY_NAME` varchar(255) NOT NULL,
  `DESCRIPTION` varchar(2048) NOT NULL,
  `DATATYPE` varchar(255) NOT NULL,
  `TYPE` varchar(255) NOT NULL,
  PRIMARY KEY (`ATTR_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='DATATYPE can be NUMBER, BOOLEAN, STRING';


--
-- Table structure for table `clinical_patient`
--
drop table IF EXISTS clinical_patient;
CREATE TABLE `clinical_patient` (
  `INTERNAL_ID` int(11) NOT NULL,
  `ATTR_ID` varchar(255) NOT NULL,
  `ATTR_VALUE` varchar(255) NOT NULL,
  PRIMARY KEY (`INTERNAL_ID`, `ATTR_ID`),
  FOREIGN KEY (`INTERNAL_ID`) REFERENCES `patient` (`INTERNAL_ID`) ON DELETE CASCADE
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `clinical_sample`
--
drop table IF EXISTS clinical_sample;
CREATE TABLE `clinical_sample` (
  `INTERNAL_ID` int(11) NOT NULL,
  `ATTR_ID` varchar(255) NOT NULL,
  `ATTR_VALUE` varchar(255) NOT NULL,
  PRIMARY KEY (`INTERNAL_ID`,`ATTR_ID`),
  FOREIGN KEY (`INTERNAL_ID`) REFERENCES `sample` (`INTERNAL_ID`) ON DELETE CASCADE
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `clinical_attribute`
--
drop table IF EXISTS clinical_attribute;
CREATE TABLE `clinical_attribute` (
  `ATTR_ID` varchar(255) NOT NULL,
  `DISPLAY_NAME` varchar(255) NOT NULL,
  `DESCRIPTION` varchar(2048) NOT NULL,
  `DATATYPE` varchar(255) NOT NULL,
  `PATIENT_ATTRIBUTE` BOOLEAN NOT NULL,
  PRIMARY KEY (`ATTR_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='DATATYPE can be NUMBER, BOOLEAN, STRING';

-- --------------------------------------------------------

--
-- Table structure for table `interaction`
--
drop table IF EXISTS interaction;
CREATE TABLE `interaction` (
  `GENE_A` bigint(20) NOT NULL,
  `GENE_B` bigint(20) NOT NULL,
  `INTERACTION_TYPE` varchar(256) NOT NULL,
  `DATA_SOURCE` varchar(256) NOT NULL,
  `EXPERIMENT_TYPES` varchar(1024) NOT NULL,
  `PMIDS` varchar(1024) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table Structure for `mut_sig`
--
drop table IF EXISTS mut_sig;
CREATE TABLE `mut_sig` (
  `CANCER_STUDY_ID` int(11) NOT NULL,
  `ENTREZ_GENE_ID` bigint(20) NOT NULL,
  `RANK` int(11) NOT NULL,
  `NumBasesCovered` int(11) NOT NULL,
  `NumMutations` int(11) NOT NULL,
  `P_VALUE` float NOT NULL,
  `Q_VALUE` float NOT NULL,
  PRIMARY KEY (`CANCER_STUDY_ID`, `ENTREZ_GENE_ID`),
  FOREIGN KEY (`CANCER_STUDY_ID`) REFERENCES `cancer_study` (`CANCER_STUDY_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

drop table IF EXISTS protein_array_info;
CREATE TABLE `protein_array_info` (
  `PROTEIN_ARRAY_ID` varchar(50) NOT NULL,
  `TYPE` varchar(50) NOT NULL,
  `GENE_SYMBOL` varchar(50) NOT NULL,
  `TARGET_RESIDUE` varchar(20) default NULL,
  PRIMARY KEY (`PROTEIN_ARRAY_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

drop table IF EXISTS protein_array_target;
CREATE TABLE `protein_array_target` (
  `PROTEIN_ARRAY_ID` varchar(50) NOT NULL,
  `ENTREZ_GENE_ID` int(255) NOT NULL,
  PRIMARY KEY (`PROTEIN_ARRAY_ID`,`ENTREZ_GENE_ID`),
  FOREIGN KEY (`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`),
  FOREIGN KEY (`PROTEIN_ARRAY_ID`) REFERENCES `protein_array_info` (`PROTEIN_ARRAY_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

drop table IF EXISTS protein_array_data;
CREATE TABLE `protein_array_data` (
  `PROTEIN_ARRAY_ID` varchar(50) NOT NULL,
  `CANCER_STUDY_ID` int(11) NOT NULL,
  `SAMPLE_ID` int(11) NOT NULL,
  `ABUNDANCE` double NOT NULL,
  PRIMARY KEY (`PROTEIN_ARRAY_ID`,`CANCER_STUDY_ID`,`SAMPLE_ID`),
  FOREIGN KEY (`PROTEIN_ARRAY_ID`) REFERENCES `protein_array_info` (`PROTEIN_ARRAY_ID`),
  FOREIGN KEY (`CANCER_STUDY_ID`) REFERENCES `CANCER_STUDY` (`CANCER_STUDY_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`SAMPLE_ID`) REFERENCES `sample` (`INTERNAL_ID`) ON DELETE CASCADE
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

drop table IF EXISTS protein_array_cancer_study;
CREATE TABLE `protein_array_cancer_study` (
  `PROTEIN_ARRAY_ID` varchar(50) NOT NULL,
  `CANCER_STUDY_ID` int(11) NOT NULL,
  PRIMARY KEY (`PROTEIN_ARRAY_ID`,`CANCER_STUDY_ID`),
  FOREIGN KEY (`CANCER_STUDY_ID`) REFERENCES `cancer_study` (`CANCER_STUDY_ID`) ON DELETE CASCADE
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `sanger_cancer_census`
--
drop table IF EXISTS sanger_cancer_census;
CREATE TABLE `sanger_cancer_census` (
  `ENTREZ_GENE_ID` bigint(20) NOT NULL,
  `CANCER_SOMATIC_MUT` tinyint(1) NOT NULL,
  `CANCER_GERMLINE_MUT` tinyint(1) NOT NULL,
  `TUMOR_TYPES_SOMATIC_MUT` text NOT NULL,
  `TUMOR_TYPES_GERMLINE_MUT` text NOT NULL,
  `CANCER_SYNDROME` text NOT NULL,
  `TISSUE_TYPE` text NOT NULL,
  `MUTATION_TYPE` text NOT NULL,
  `TRANSLOCATION_PARTNER` text NOT NULL,
  `OTHER_GERMLINE_MUT` tinyint(1) NOT NULL,
  `OTHER_DISEASE` text NOT NULL,
  FOREIGN KEY (`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='Sanger Cancer Gene Census';

drop table IF EXISTS gistic;
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
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `gistic_to_gene`
--
drop table IF EXISTS gistic_to_gene;
CREATE TABLE `gistic_to_gene`  (
  `GISTIC_ROI_ID` bigint(20) NOT NULL,
  `ENTREZ_GENE_ID` bigint(20) NOT NULL,
  PRIMARY KEY(`GISTIC_ROI_ID`, `ENTREZ_GENE_ID`),
  FOREIGN KEY (`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`),
  FOREIGN KEY (`GISTIC_ROI_ID`) REFERENCES `gistic` (`GISTIC_ROI_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `text_cache`
--
drop table IF EXISTS text_cache;
CREATE TABLE `text_cache` (
  `HASH_KEY` varchar(32) NOT NULL,
  `TEXT` text NOT NULL,
  `DATE_TIME_STAMP` datetime NOT NULL,
  PRIMARY KEY (`HASH_KEY`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `pfam_graphics`
--
drop table IF EXISTS pfam_graphics;
CREATE TABLE `pfam_graphics` (
  `UNIPROT_ACC` varchar(255) NOT NULL,
  `JSON_DATA` longtext NOT NULL,
  PRIMARY KEY (`UNIPROT_ACC`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `drug_interaction`
--
drop table IF EXISTS drug_interaction;
CREATE TABLE `drug_interaction` (
  `DRUG` char(30) NOT NULL,
  `TARGET` bigint(20) NOT NULL,
  `INTERACTION_TYPE` char(50) NOT NULL,
  `DATA_SOURCE` varchar(256) NOT NULL,
  `EXPERIMENT_TYPES` varchar(1024) DEFAULT NULL,
  `PMIDS` varchar(1024) DEFAULT NULL,
  FOREIGN KEY (`DRUG`) REFERENCES `drug` (`DRUG_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `drug`
--
drop table IF EXISTS drug;
CREATE TABLE `drug` (
  `DRUG_ID` char(30) NOT NULL,
  `DRUG_RESOURCE` varchar(255) NOT NULL,
  `DRUG_NAME` varchar(255) NOT NULL,
  `DRUG_SYNONYMS` varchar(4096) DEFAULT NULL,
  `DRUG_DESCRIPTION` varchar(4096) DEFAULT NULL,
  `DRUG_XREF` varchar(4096) DEFAULT NULL,
  `DRUG_ATC_CODE` varchar(1024) DEFAULT NULL,
  `DRUG_APPROVED` integer(1) DEFAULT 0,
  `DRUG_CANCERDRUG` integer(1) DEFAULT 0,
  `DRUG_NUTRACEUTICAL` integer(1) DEFAULT 0,
  `DRUG_NUMOFTRIALS` integer DEFAULT -1,
  PRIMARY KEY  (`DRUG_ID`),
  KEY `DRUG_NAME` (`DRUG_NAME`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

drop table IF EXISTS cna_event;
CREATE TABLE `cna_event` (
  `CNA_EVENT_ID` int(255) NOT NULL auto_increment,
  `ENTREZ_GENE_ID` bigint(20) NOT NULL,
  `ALTERATION` tinyint NOT NULL,
  PRIMARY KEY  (`CNA_EVENT_ID`),
  UNIQUE (`ENTREZ_GENE_ID`, `ALTERATION`),
  FOREIGN KEY (`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

drop table IF EXISTS sample_cna_event;
CREATE TABLE `sample_cna_event` (
  `CNA_EVENT_ID` int(255) NOT NULL,
  `SAMPLE_ID` int(11) NOT NULL,
  `GENETIC_PROFILE_ID` int(11) NOT NULL,
  KEY (`GENETIC_PROFILE_ID`,`SAMPLE_ID`),
  PRIMARY KEY  (`CNA_EVENT_ID`, `SAMPLE_ID`, `GENETIC_PROFILE_ID`),
  FOREIGN KEY (`CNA_EVENT_ID`) REFERENCES `cna_event` (`CNA_EVENT_ID`),
  FOREIGN KEY (`GENETIC_PROFILE_ID`) REFERENCES `genetic_profile` (`GENETIC_PROFILE_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`SAMPLE_ID`) REFERENCES `sample` (`INTERNAL_ID`) ON DELETE CASCADE
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

drop table IF EXISTS copy_number_seg;
CREATE TABLE `copy_number_seg` (
  `SEG_ID` int(255) NOT NULL auto_increment,
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
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

drop table IF EXISTS copy_number_seg_file;
CREATE TABLE `copy_number_seg_file` (
  `SEG_FILE_ID` int(11) NOT NULL auto_increment,
  `CANCER_STUDY_ID` int(11) NOT NULL,
  `REFERENCE_GENOME_ID` varchar(10) NOT NULL,
  `DESCRIPTION` varchar(255) NOT NULL,
  `FILENAME` varchar(255) NOT NULL,
  PRIMARY KEY(`SEG_FILE_ID`),
  FOREIGN KEY (`CANCER_STUDY_ID`) REFERENCES `cancer_study` (`CANCER_STUDY_ID`) ON DELETE CASCADE
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

drop table IF EXISTS cosmic_mutation;
CREATE TABLE `cosmic_mutation` (
  `COSMIC_MUTATION_ID` varchar(30) NOT NULL,
  `CHR` varchar(5),
  `START_POSITION` bigint(20),
  `REFERENCE_ALLELE` varchar(255),
  `TUMOR_SEQ_ALLELE` varchar(255),
  `STRAND` varchar(2),
  `CODON_CHANGE` varchar(255),
  `ENTREZ_GENE_ID` int(255) NOT NULL,
  `PROTEIN_CHANGE` varchar(255) NOT NULL,
  `COUNT` int(11) NOT NULL,
  `KEYWORD` varchar(50) DEFAULT NULL,
  KEY (`KEYWORD`),
  PRIMARY KEY (`COSMIC_MUTATION_ID`),
  FOREIGN KEY (`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

drop table IF EXISTS clinical_trials; 
CREATE TABLE `clinical_trials` (
  `PROTOCOLID` char(50) NOT NULL,
  `SECONDARYID` char(50) NOT NULL,
  `TITLE` varchar(512),
  `PHASE` char(128),
  `LOCATION` varchar(256),
  `STATUS` char(50),
  PRIMARY KEY (`PROTOCOLID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

drop table IF EXISTS clinical_trial_keywords; 
CREATE TABLE `clinical_trial_keywords` (
  `PROTOCOLID` char(50) NOT NULL,
  `KEYWORD` varchar(256),
  PRIMARY KEY (`PROTOCOLID`, `KEYWORD`),
  KEY(`KEYWORD`),
  FOREIGN KEY (`PROTOCOLID`) REFERENCES `clinical_trials` (`PROTOCOLID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

drop table IF EXISTS pdb_uniprot_residue_mapping;
CREATE TABLE `pdb_uniprot_residue_mapping` (
  `ALIGNMENT_ID` int NOT NULL,
  `PDB_POSITION` int NOT NULL,
  `PDB_INSERTION_CODE` char(1) DEFAULT NULL,
  `UNIPROT_POSITION` int NOT NULL,
  `MATCH` char(1),
  KEY(`ALIGNMENT_ID`, `UNIPROT_POSITION`),
  FOREIGN KEY(`ALIGNMENT_ID`) REFERENCES `pdb_uniprot_alignment` (`ALIGNMENT_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

drop table IF EXISTS pdb_uniprot_alignment;
CREATE TABLE `pdb_uniprot_alignment` (
  `ALIGNMENT_ID` int NOT NULL,
  `PDB_ID` char(4) NOT NULL,
  `CHAIN` char(1) NOT NULL,
  `UNIPROT_ID` varchar(50) NOT NULL,
  `PDB_FROM` varchar(10) NOT NULL,
  `PDB_TO` varchar(10) NOT NULL,
  `UNIPROT_FROM` int NOT NULL,
  `UNIPROT_TO` int NOT NULL,
  `EVALUE` float,
  `IDENTITY` float,
  `IDENTP` float,
  `UNIPROT_ALIGN` text,
  `PDB_ALIGN` text,
  `MIDLINE_ALIGN` text,
  PRIMARY KEY (`ALIGNMENT_ID`),
  KEY(`UNIPROT_ID`),
  KEY(`PDB_ID`, `CHAIN`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

drop table IF EXISTS clinical_event;
CREATE TABLE `clinical_event` (
  `CLINICAL_EVENT_ID` int NOT NULL auto_increment,
  `CANCER_STUDY_ID` int NOT NULL,
  `PATIENT_ID` varchar(255) NOT NULL,
  `START_DATE` int NOT NULL,
  `STOP_DATE` int,
  `EVENT_TYPE` varchar(20) NOT NULL,
  PRIMARY KEY (`CLINICAL_EVENT_ID`),
  KEY (`CANCER_STUDY_ID`, `PATIENT_ID`),
  KEY (`CANCER_STUDY_ID`, `PATIENT_ID`, `EVENT_TYPE`),
  FOREIGN KEY (`CANCER_STUDY_ID`) REFERENCES `cancer_study` (`CANCER_STUDY_ID`) ON DELETE CASCADE
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

drop table IF EXISTS clinical_event_data;
CREATE TABLE `clinical_event_data` (
  `CLINICAL_EVENT_ID` int(255) NOT NULL,
  `KEY` varchar(255) NOT NULL,
  `VALUE` varchar(5000) NOT NULL,
  FOREIGN KEY (`CLINICAL_EVENT_ID`) REFERENCES `clinical_event` (`CLINICAL_EVENT_ID`) ON DELETE CASCADE
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
