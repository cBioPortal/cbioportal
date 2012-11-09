-- phpMyAdmin SQL Dump
-- version 2.11.9.2
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: Mar 25, 2010 at 11:45 AM
-- Server version: 5.0.67
-- PHP Version: 5.2.11

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

--
-- Database: `cgds`
--

-- --------------------------------------------------------

--
-- Table structure for table `cancer_study`
--
drop table IF EXISTS cancer_study;
CREATE TABLE `cancer_study` (
  `CANCER_STUDY_ID` int(11) NOT NULL auto_increment,
  `CANCER_STUDY_IDENTIFIER` varchar(25),
  `TYPE_OF_CANCER_ID` varchar(25) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  `DESCRIPTION` varchar(1024) NOT NULL,
  `PUBLIC` BOOLEAN NOT NULL,
  `PMID` varchar(20) DEFAULT NULL,
  `CITATION` varchar(200) DEFAULT NULL,
  PRIMARY KEY  (`CANCER_STUDY_ID`),
  UNIQUE (`CANCER_STUDY_IDENTIFIER`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--
drop table IF EXISTS users;
CREATE TABLE `users` (
  `EMAIL` varchar(128) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  `ENABLED` BOOLEAN NOT NULL,
  PRIMARY KEY  (`EMAIL`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

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
-- Table structure for table `type_of_cancer`
--
drop table IF EXISTS type_of_cancer;
CREATE TABLE `type_of_cancer` (
  `TYPE_OF_CANCER_ID` varchar(25) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  PRIMARY KEY  (`TYPE_OF_CANCER_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `case_list`
--
drop table IF EXISTS case_list;
CREATE TABLE `case_list` (
  `LIST_ID` int(11) NOT NULL auto_increment,
  `STABLE_ID` varchar(50) NOT NULL,
  `CATEGORY` varchar(255) NOT NULL,
  `CANCER_STUDY_ID` int(11) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  `DESCRIPTION` mediumtext,
  PRIMARY KEY  (`LIST_ID`),
  UNIQUE (`STABLE_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `case_list_list`
--
drop table IF EXISTS case_list_list;
CREATE TABLE `case_list_list` (
  `LIST_ID` int(11) NOT NULL,
  `CASE_ID` varchar(255) NOT NULL,
  PRIMARY KEY  (`LIST_ID`,`CASE_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `gene`
--
drop table IF EXISTS gene;
CREATE TABLE `gene` (
  `ENTREZ_GENE_ID` int(255) NOT NULL,
  `HUGO_GENE_SYMBOL` varchar(255) NOT NULL,
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
  PRIMARY KEY  (`ENTREZ_GENE_ID`,`GENE_ALIAS`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `uniprot_id_mapping`
--
drop table IF EXISTS uniprot_id_mapping;
CREATE TABLE `uniprot_id_mapping` (
  `ENTREZ_GENE_ID` int(255) NOT NULL,
  `UNIPROT_ID` varchar(255) NOT NULL,
  PRIMARY KEY  (`ENTREZ_GENE_ID`, `UNIPROT_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `genetic_alteration`
--
drop table IF EXISTS genetic_alteration;
CREATE TABLE `genetic_alteration` (
  `GENETIC_PROFILE_ID` int(11) NOT NULL,
  `ENTREZ_GENE_ID` int(255) NOT NULL,
  `VALUES` longtext NOT NULL,
  KEY `QUICK_LOOK_UP` (`ENTREZ_GENE_ID`),
  KEY `QUICK_LOOK_UP2` (`ENTREZ_GENE_ID`,`GENETIC_PROFILE_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

drop table IF EXISTS micro_rna_alteration;
CREATE TABLE `micro_rna_alteration` (
  `GENETIC_PROFILE_ID` int(11) NOT NULL,
  `MICRO_RNA_ID` varchar(50) NOT NULL,
  `VALUES` longtext NOT NULL,
  UNIQUE KEY `QUICK_LOOK_UP1` (`GENETIC_PROFILE_ID`,`MICRO_RNA_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `genetic_profile`
--
drop table IF EXISTS genetic_profile;
CREATE TABLE `genetic_profile` (
  `GENETIC_PROFILE_ID` int(11) NOT NULL auto_increment,
  `STABLE_ID` varchar(50) NOT NULL,
  `CANCER_STUDY_ID` int(11) NOT NULL,
  `GENETIC_ALTERATION_TYPE` varchar(255) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  `DESCRIPTION` mediumtext,
  `SHOW_PROFILE_IN_ANALYSIS_TAB` binary(1) NOT NULL,
  PRIMARY KEY  (`GENETIC_PROFILE_ID`),
  UNIQUE (`STABLE_ID`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2 ;

--
-- Table structure for table `genetic_profile_cases`
--
drop table IF EXISTS genetic_profile_cases;
CREATE TABLE `genetic_profile_cases` (
  `GENETIC_PROFILE_ID` int(11) NOT NULL,
  `ORDERED_CASE_LIST` longtext NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

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

--
-- Table structure for table `mutation`
--
drop table IF EXISTS mutation;
CREATE TABLE `mutation` (
  `GENETIC_PROFILE_ID` int(11) NOT NULL,
  `CASE_ID` varchar(255) NOT NULL,
  `ENTREZ_GENE_ID` int(255) NOT NULL,
  `CENTER` varchar(100) NOT NULL,
  `SEQUENCER` varchar(255) NOT NULL,
  `MUTATION_STATUS` varchar(25) NOT NULL COMMENT 'Germline, Somatic or LOH.',
  `VALIDATION_STATUS` varchar(25) NOT NULL,
  `CHR` varchar(5) NOT NULL,
  `START_POSITION` bigint(20) NOT NULL,
  `END_POSITION` bigint(20) NOT NULL,
  `PROTEIN_CHANGE` varchar(255) NOT NULL,
  `MUTATION_TYPE` varchar(255) NOT NULL COMMENT 'e.g. Missense, Nonsence, etc.',
  `FUNCTIONAL_IMPACT_SCORE` varchar(10) NOT NULL COMMENT 'Result from OMA/XVAR.',
  `LINK_XVAR` varchar(500) NOT NULL COMMENT 'Link to OMA/XVAR Landing Page for the specific mutation.',
  `LINK_PDB` varchar(500) NOT NULL,
  `LINK_MSA` varchar(500) NOT NULL,
  `NCBI_BUILD` varchar(10) NOT NULL,
  `STRAND` varchar(2) NOT NULL,
  `VARIANT_TYPE` varchar(15) NOT NULL,
  `REFERENCE_ALLELE` varchar(255) NOT NULL,
  `TUMOR_SEQ_ALLELE1` varchar(255) NOT NULL,
  `TUMOR_SEQ_ALLELE2` varchar(255) NOT NULL,
  `DB_SNP_RS` varchar(25),
  `DB_SNP_VAL_STATUS` varchar(255),
  `MATCHED_NORM_SAMPLE_BARCODE` varchar(255) NOT NULL,
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
  `ONCOTATOR_DBSNP_RS` varchar(256),
  `ONCOTATOR_COSMIC_OVERLAPPING` varchar(3072),
  KEY `QUICK_LOOK_UP2` (`GENETIC_PROFILE_ID`,`ENTREZ_GENE_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='Mutation Data Details';

-- --------------------------------------------------------

--
-- Table structure for table `mutation_frequency`
--
drop table IF EXISTS mutation_frequency;
CREATE TABLE `mutation_frequency` (
  `ENTREZ_GENE_ID` int(11) NOT NULL,
  `SOMATIC_MUTATION_RATE` double NOT NULL,
  `CANCER_STUDY_ID` int(11) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;


-- --------------------------------------------------------

--
-- Table structure for table `case_profile`
--
drop table IF EXISTS case_profile;
CREATE TABLE `case_profile` (
  `CASE_ID` varchar(255) NOT NULL,
  `GENETIC_PROFILE_ID` int(11) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

drop table IF EXISTS clinical;
CREATE TABLE `clinical` (
  `CASE_ID` varchar(255) NOT NULL,
  `OVERALL_SURVIVAL_MONTHS` double default NULL,
  `OVERALL_SURVIVAL_STATUS` varchar(50) default NULL,
  `DISEASE_FREE_SURVIVAL_MONTHS` double default NULL,
  `DISEASE_FREE_SURVIVAL_STATUS` varchar(50) default NULL,
  `AGE_AT_DIAGNOSIS` double default NULL,
  PRIMARY KEY (`CASE_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

drop table IF EXISTS clinical_free_form;
CREATE TABLE `clinical_free_form` (
  `CANCER_STUDY_ID` int(11) NOT NULL,
  `CASE_ID` varchar(256) NOT NULL,
  `PARAM_NAME` varchar(256) NOT NULL,
  `PARAM_VALUE` varchar(256) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

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
  `Q_VALUE` float NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

drop table IF EXISTS protein_array_info;
CREATE TABLE `protein_array_info` (
  `PROTEIN_ARRAY_ID` varchar(50) NOT NULL,
  `TYPE` varchar(50) NOT NULL,
  `GENE_SYMBOL` varchar(50) NOT NULL,
  `TARGET_RESIDUE` varchar(20) default NULL,
  PRIMARY KEY (`PROTEIN_ARRAY_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

drop table IF EXISTS protein_array_target;
CREATE TABLE `protein_array_target` (
  `PROTEIN_ARRAY_ID` varchar(50) NOT NULL,
  `ENTREZ_GENE_ID` int(255) NOT NULL,
  PRIMARY KEY (`PROTEIN_ARRAY_ID`,`ENTREZ_GENE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

drop table IF EXISTS protein_array_data;
CREATE TABLE `protein_array_data` (
  `PROTEIN_ARRAY_ID` varchar(50) NOT NULL,
  `CASE_ID` varchar(255) NOT NULL,
  `ABUNDANCE` double NOT NULL,
  PRIMARY KEY (`PROTEIN_ARRAY_ID`,`CASE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

drop table IF EXISTS protein_array_cancer_study;
CREATE TABLE `protein_array_cancer_study` (
  `PROTEIN_ARRAY_ID` varchar(50) NOT NULL,
  `CANCER_STUDY_ID` int(11) NOT NULL,
  PRIMARY KEY (`PROTEIN_ARRAY_ID`,`CANCER_STUDY_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

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
  `OTHER_DISEASE` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Sanger Cancer Gene Census';

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
  PRIMARY KEY (`GISTIC_ROI_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table `text_cache`
--
drop table IF EXISTS text_cache;
CREATE TABLE `text_cache` (
  `HASH_KEY` varchar(32) NOT NULL,
  `TEXT` text NOT NULL,
  `DATE_TIME_STAMP` datetime NOT NULL,
  PRIMARY KEY (`HASH_KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

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
  `PMIDS` varchar(1024) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `gistic_to_gene`
--
drop table IF EXISTS gistic_to_gene;
CREATE TABLE `gistic_to_gene`  (
  `GISTIC_ROI_ID` bigint(20) NOT NULL,
  `ENTREZ_GENE_ID` bigint(20) NOT NULL,
  PRIMARY KEY(`GISTIC_ROI_ID`, `ENTREZ_GENE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table `drug`
--
drop table IF EXISTS drug;
CREATE TABLE `drug` (
  `DRUG_ID` char(30) NOT NULL,
  `DRUG_RESOURCE` varchar(30) NOT NULL,
  `DRUG_NAME` varchar(255) NOT NULL,
  `DRUG_SYNONYMS` varchar(2048) DEFAULT NULL,
  `DRUG_DESCRIPTION` varchar(4096) DEFAULT NULL,
  `DRUG_XREF` varchar(255) DEFAULT NULL,
  `DRUG_APPROVED` integer(1) DEFAULT 0,
  `DRUG_ATC_CODE` varchar(1024) DEFAULT NULL,
  PRIMARY KEY  (`DRUG_ID`),
  KEY `DRUG_NAME` (`DRUG_NAME`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

drop table IF EXISTS _case;
CREATE TABLE `_case` (
  `CASE_ID` varchar(255) NOT NULL,
  `CANCER_STUDY_ID` int(11) NOT NULL,
  PRIMARY KEY (`CASE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

drop table IF EXISTS case_cna_event;
CREATE TABLE `case_cna_event` (
  `CNA_EVENT_ID` int(255) NOT NULL,
  `CASE_ID` varchar(255) NOT NULL,
  `GENETIC_PROFILE_ID` int(11) NOT NULL,
  PRIMARY KEY  (`CNA_EVENT_ID`, `CASE_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

drop table IF EXISTS cna_event;
CREATE TABLE `cna_event` (
  `CNA_EVENT_ID` int(255) NOT NULL auto_increment,
  `ENTREZ_GENE_ID` bigint(20) NOT NULL,
  `ALTERATION` tinyint NOT NULL,
  PRIMARY KEY  (`CNA_EVENT_ID`),
  UNIQUE (`ENTREZ_GENE_ID`, `ALTERATION`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

drop table IF EXISTS case_mutation_event;
CREATE TABLE `case_mutation_event` (
  `GENETIC_PROFILE_ID` int(11) NOT NULL,
  `CASE_ID` varchar(255) NOT NULL,
  `MUTATION_EVENT_ID` int(255) NOT NULL,
  `VALIDATION_STATUS` varchar(25) NOT NULL,
  PRIMARY KEY  (`MUTATION_EVENT_ID`, `CASE_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='Mutation Data for patient view';

drop table IF EXISTS mutation_event;
CREATE TABLE `mutation_event` (
  `MUTATION_EVENT_ID` int(255) NOT NULL auto_increment,
  `ENTREZ_GENE_ID` int(255) NOT NULL,
  `MUTATION_STATUS` varchar(25) NOT NULL COMMENT 'Germline, Somatic or LOH.',
  `AMINO_ACID_CHANGE` varchar(255) NOT NULL,
  `MUTATION_TYPE` varchar(255) NOT NULL COMMENT 'e.g. Missense, Nonsence, etc.',
  `CHR` varchar(5) NOT NULL,
  `START_POSITION` bigint(20) NOT NULL,
  `END_POSITION` bigint(20) NOT NULL,
  `FUNCTIONAL_IMPACT_SCORE` varchar(5) NOT NULL COMMENT 'Result from OMA/XVAR.',
  `LINK_XVAR` varchar(500) NOT NULL COMMENT 'Link to OMA/XVAR Landing Page for the specific mutation.',
  `LINK_PDB` varchar(500) NOT NULL,
  `LINK_MSA` varchar(500) NOT NULL,
  `KEYWORD` varchar(50) DEFAULT NULL COMMENT 'e.g. truncating, V200 Missense, E338del, ',
  PRIMARY KEY  (`MUTATION_EVENT_ID`),
  UNIQUE (`ENTREZ_GENE_ID`, `MUTATION_STATUS`, `AMINO_ACID_CHANGE`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 COMMENT='Mutation Data for patient view';

drop table IF EXISTS copy_number_seg;
CREATE TABLE `copy_number_seg` (
  `SEG_ID` int(255) NOT NULL auto_increment,
  `CASE_ID` varchar(255) NOT NULL,
  `CHR` varchar(5) NOT NULL,
  `START` int(11) NOT NULL,
  `END` int(11) NOT NULL,
  `NUM_PROBES` int(11) NOT NULL,
  `SEGMENT_MEAN` double NOT NULL,
  PRIMARY KEY (`SEG_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

drop table IF EXISTS cosmic_mutation;
CREATE TABLE `cosmic_mutation` (
  `COSMIC_MUTATION_ID` int(255) NOT NULL auto_increment COMMENT 'this is not a real COSMIC ID but an internal one', 
  `ENTREZ_GENE_ID` int(255) NOT NULL,
  `AMINO_ACID_CHANGE` varchar(255) NOT NULL,
  `COUNT` int(11) NOT NULL,
  PRIMARY KEY (`COSMIC_MUTATION_ID`),
  UNIQUE (`ENTREZ_GENE_ID`,`AMINO_ACID_CHANGE`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

drop table IF EXISTS mutation_event_cosmic_mapping;
CREATE TABLE `mutation_event_cosmic_mapping` (
  `MUTATION_EVENT_ID` int(255) NOT NULL,
  `COSMIC_MUTATION_ID` int(255) NOT NULL,
  PRIMARY KEY (`MUTATION_EVENT_ID`,`COSMIC_MUTATION_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
