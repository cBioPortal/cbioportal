-- phpMyAdmin SQL Dump
-- version 2.11.9.2
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: Mar 25, 2010 at 11:45 AM
-- Server version: 5.0.67
-- PHP Version: 5.2.11

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

USE CGDS;

drop table IF EXISTS encrypted_keys;
drop table IF EXISTS cancer_study;
drop table IF EXISTS access_rights;
drop table IF EXISTS users;
drop table IF EXISTS authorities;
drop table IF EXISTS type_of_cancer;
drop table IF EXISTS mut_sig;
drop table IF EXISTS _case;
drop table IF EXISTS cancer_type;
drop table IF EXISTS case_list;
drop table IF EXISTS case_list_list;
drop table IF EXISTS gene;
drop table IF EXISTS gene_in_profile;
drop table IF EXISTS genetic_alteration;
drop table IF EXISTS genetic_profile_cases;
drop table IF EXISTS genetic_profile;
drop table IF EXISTS micro_rna;
drop table IF EXISTS mutation;
drop table IF EXISTS mutation_frequency;
drop table IF EXISTS micro_rna_alteration;
drop table IF EXISTS clinical;
drop table IF EXISTS interaction;

drop table IF EXISTS protein_array_info;
drop table IF EXISTS protein_array_target;
drop table IF EXISTS protein_array_data;

--
-- Database: `cgds`
--

-- --------------------------------------------------------

--
-- Table structure for table `encrypted_keys`
--

CREATE TABLE IF NOT EXISTS `encrypted_keys` (
  `ENCRYPTED_KEY` varchar(100) NOT NULL,
  PRIMARY KEY  (`ENCRYPTED_KEY`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `cancer_study`
--

CREATE TABLE IF NOT EXISTS `cancer_study` (
  `CANCER_STUDY_ID` int(11) NOT NULL auto_increment,
  `CANCER_STUDY_IDENTIFIER` varchar(25),
  `TYPE_OF_CANCER_ID` varchar(25) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  `DESCRIPTION` varchar(1024) NOT NULL,
  `PUBLIC` BOOLEAN NOT NULL,
  PRIMARY KEY  (`CANCER_STUDY_ID`),
  UNIQUE (`CANCER_STUDY_IDENTIFIER`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE IF NOT EXISTS `users` (
  `EMAIL` varchar(128) NOT NULL,
  `NAME` varchar(255),
  `ENABLED` BOOLEAN NOT NULL,
  `CONSUMER_SECRET` varchar(100) NOT NULL,
  PRIMARY KEY  (`EMAIL`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `access_rights`
--

CREATE TABLE IF NOT EXISTS `authorities` (
  `EMAIL` varchar(128) NOT NULL,
  `AUTHORITY` varchar(50) NOT NULL
  --FOREIGN KEY (EMAIL) REFERENCES users (EMAIL),
  --UNIQUE index authorities_idx_1 (EMAIL, AUTHORITY)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `access_rights`
--

CREATE TABLE IF NOT EXISTS `access_rights` (
  `EMAIL` varchar(128) NOT NULL,
  `CANCER_STUDY_ID` int(11) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `type_of_cancer`
--

CREATE TABLE IF NOT EXISTS `type_of_cancer` (
  `TYPE_OF_CANCER_ID` varchar(25) NOT NULL,
  `NAME` varchar(255) NOT NULL,
  PRIMARY KEY  (`TYPE_OF_CANCER_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `case_list`
--

CREATE TABLE IF NOT EXISTS `case_list` (
  `LIST_ID` int(11) NOT NULL auto_increment,
  `STABLE_ID` varchar(50) NOT NULL,
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

CREATE TABLE IF NOT EXISTS `case_list_list` (
  `LIST_ID` int(11) NOT NULL,
  `CASE_ID` varchar(255) NOT NULL,
  PRIMARY KEY  (`LIST_ID`,`CASE_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `gene`
--

CREATE TABLE IF NOT EXISTS `gene` (
  `ENTREZ_GENE_ID` int(255) NOT NULL,
  `HUGO_GENE_SYMBOL` varchar(255) NOT NULL,
  PRIMARY KEY  (`ENTREZ_GENE_ID`),
  KEY `HUGO_GENE_SYMBOL` (`HUGO_GENE_SYMBOL`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `genetic_alteration`
--

CREATE TABLE IF NOT EXISTS `genetic_alteration` (
  `GENETIC_PROFILE_ID` int(11) NOT NULL,
  `ENTREZ_GENE_ID` int(255) NOT NULL,
  `VALUES` longtext NOT NULL,
  KEY `QUICK_LOOK_UP` (`ENTREZ_GENE_ID`),
  KEY `QUICK_LOOK_UP2` (`ENTREZ_GENE_ID`,`GENETIC_PROFILE_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `micro_rna_alteration` (
  `GENETIC_PROFILE_ID` int(11) NOT NULL,
  `MICRO_RNA_ID` varchar(50) NOT NULL,
  `VALUES` longtext NOT NULL,
  UNIQUE KEY `QUICK_LOOK_UP1` (`GENETIC_PROFILE_ID`,`MICRO_RNA_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `genetic_profile`
--

CREATE TABLE IF NOT EXISTS `genetic_profile` (
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

CREATE TABLE IF NOT EXISTS `genetic_profile_cases` (
  `GENETIC_PROFILE_ID` int(11) NOT NULL,
  `ORDERED_CASE_LIST` longtext NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

-- --------------------------------------------------------

--
-- Table structure for table `micro_rna`
--

CREATE TABLE IF NOT EXISTS `micro_rna` (
  `ID` varchar(50) NOT NULL,
  `VARIANT_ID` varchar(50) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `mutation`
--

CREATE TABLE IF NOT EXISTS `mutation` (
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
  `AMINO_ACID_CHANGE` varchar(255) NOT NULL,
  `MUTATION_TYPE` varchar(255) NOT NULL COMMENT 'e.g. Missense, Nonsence, etc.',
  `FUNCTIONAL_IMPACT_SCORE` varchar(5) NOT NULL COMMENT 'Result from OMA/XVAR.',
  `LINK_XVAR` varchar(500) NOT NULL COMMENT 'Link to OMA/XVAR Landing Page for the specific mutation.',
  `LINK_PDB` varchar(500) NOT NULL,
  `LINK_MSA` varchar(500) NOT NULL,
  KEY `QUICK_LOOK_UP2` (`GENETIC_PROFILE_ID`,`ENTREZ_GENE_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='Mutation Data Details';

-- --------------------------------------------------------

--
-- Table structure for table `mutation_frequency`
--

CREATE TABLE IF NOT EXISTS `mutation_frequency` (
  `ENTREZ_GENE_ID` int(11) NOT NULL,
  `SOMATIC_MUTATION_RATE` double NOT NULL,
  `CANCER_STUDY_ID` int(11) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;


-- --------------------------------------------------------

--
-- Table structure for table `_case`
--

CREATE TABLE IF NOT EXISTS `_case` (
  `CASE_ID` varchar(255) NOT NULL,
  `GENETIC_PROFILE_ID` int(11) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `clinical` (
  `CASE_ID` varchar(255) NOT NULL,
  `OVERALL_SURVIVAL_MONTHS` double default NULL,
  `OVERALL_SURVIVAL_STATUS` varchar(50) default NULL,
  `DISEASE_FREE_SURVIVAL_MONTHS` double default NULL,
  `DISEASE_FREE_SURVIVAL_STATUS` varchar(50) default NULL,
  `AGE_AT_DIAGNOSIS` double default NULL,
  PRIMARY KEY (`CASE_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `interaction`
--

CREATE TABLE `interaction` (
  `GENE_A` bigint(20) NOT NULL,
  `GENE_B` bigint(20) NOT NULL,
  `INTERACTION_TYPE` varchar(256) NOT NULL,
  `DATA_SOURCE` varchar(256) NOT NULL,
  `EXPERIMENT_TYPES` varchar(1024) NOT NULL,
  `PMIDS` varchar(1024) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- Table Structure for `mut_sig`

CREATE TABLE IF NOT EXISTS `mut_sig` (
  `CANCER_STUDY_ID` int(11) NOT NULL,
  `ENTREZ_GENE_ID` bigint(20) NOT NULL,
  `RANK` int(11) NOT NULL,
  `BIG_N` int(11) NOT NULL,
  `SMALL_N` int(11) NOT NULL,
  `N_VAL` int(11) NOT NULL,
  `N_VER` int(11) NOT NULL,
  `CPG` int(11) NOT NULL,
  `C+G` int(11) NOT NULL,
  `A+T` int(11) NOT NULL,
  `INDEL` int(11) NOT NULL,
  `P_VALUE` varchar(30) NOT NULL,
  `Q_VALUE` varchar(30) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `protein_array_info` (
  `PROTEIN_ARRAY_ID` varchar(50) NOT NULL,
  `TYPE` varchar(50) NOT NULL,
  `SOURCE_ORGANISM` varchar(50),
  `VALIDATED` boolean,
  PRIMARY KEY (`PROTEIN_ARRAY_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `protein_array_target` (
  `PROTEIN_ARRAY_ID` varchar(50) NOT NULL,
  `ENTREZ_GENE_ID` int(255) NOT NULL,
  `TARGET_RESIDUE` varchar(10) default NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `protein_array_data` (
  `PROTEIN_ARRAY_ID` varchar(50) NOT NULL,
  `CASE_ID` varchar(255) NOT NULL,
  `CANCER_STUDY_ID` int(11) NOT NULL,
  `ABUNDANCE` double NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;