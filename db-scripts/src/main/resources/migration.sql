--
-- Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
--
-- This library is distributed in the hope that it will be useful, but WITHOUT
-- ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
-- FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
-- is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
-- obligations to provide maintenance, support, updates, enhancements or
-- modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
-- liable to any party for direct, indirect, special, incidental or
-- consequential damages, including lost profits, arising out of the use of this
-- software and its documentation, even if Memorial Sloan-Kettering Cancer
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
##version: 1.0.0
CREATE TABLE info (DB_SCHEMA_VERSION VARCHAR(8));
INSERT INTO info VALUES ("1.0.0");

##version: 1.1.0
CREATE TABLE sample_list LIKE patient_list;
INSERT sample_list SELECT * FROM patient_list;
CREATE TABLE sample_list_list LIKE patient_list_list;
INSERT sample_list_list SELECT * FROM patient_list_list;
ALTER TABLE sample_list_list CHANGE PATIENT_ID SAMPLE_ID INT(11);
UPDATE info SET DB_SCHEMA_VERSION="1.1.0";

##version: 1.2.0
ALTER TABLE cna_event AUTO_INCREMENT=1;
ALTER TABLE mutation add UNIQUE KEY `UQ_MUTATION_EVENT_ID_GENETIC_PROFILE_ID_SAMPLE_ID` (`MUTATION_EVENT_ID`,`GENETIC_PROFILE_ID`,`SAMPLE_ID`);
ALTER TABLE sample_profile add UNIQUE KEY `UQ_SAMPLE_ID_GENETIC_PROFILE_ID` (`SAMPLE_ID`,`GENETIC_PROFILE_ID`);
UPDATE info SET DB_SCHEMA_VERSION="1.2.0";

##version: 1.2.1
SET @s = (SELECT IF(    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'cancer_study' AND table_schema = DATABASE() AND column_name = 'STATUS') > 0,  "SELECT 1", " ALTER TABLE cancer_study ADD STATUS int(1) DEFAULT NULL"));
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
UPDATE info SET DB_SCHEMA_VERSION="1.2.1";

##version: 1.3.0
DROP TABLE IF EXISTS `clinical_trial_keywords`;
DROP TABLE IF EXISTS `clinical_trials`;
ALTER TABLE `users` CONVERT TO CHARACTER SET utf8;
ALTER TABLE `info` CONVERT TO CHARACTER SET utf8;
ALTER TABLE `gene` MODIFY COLUMN `ENTREZ_GENE_ID` int(11) NOT NULL;
ALTER TABLE `gene_alias` MODIFY COLUMN `ENTREZ_GENE_ID` int(11) NOT NULL;
ALTER TABLE `uniprot_id_mapping` MODIFY COLUMN `ENTREZ_GENE_ID` int(11) NOT NULL;
ALTER TABLE `genetic_alteration` MODIFY COLUMN `ENTREZ_GENE_ID` int(11) NOT NULL;
ALTER TABLE `mutation_event` MODIFY COLUMN `ENTREZ_GENE_ID` int(11) NOT NULL;
ALTER TABLE `mut_sig` MODIFY COLUMN `ENTREZ_GENE_ID` int(11) NOT NULL;
ALTER TABLE `protein_array_target` MODIFY COLUMN `ENTREZ_GENE_ID` int(11) NOT NULL;
ALTER TABLE `sanger_cancer_census` MODIFY COLUMN `ENTREZ_GENE_ID` int(11) NOT NULL;
ALTER TABLE `gistic_to_gene` MODIFY COLUMN `ENTREZ_GENE_ID` int(11) NOT NULL;
ALTER TABLE `cna_event` MODIFY COLUMN `ENTREZ_GENE_ID` int(11) NOT NULL;
ALTER TABLE `cosmic_mutation` MODIFY COLUMN `ENTREZ_GENE_ID` int(11) NOT NULL;
ALTER TABLE `mutation` MODIFY COLUMN `ENTREZ_GENE_ID` int(11) NOT NULL;
ALTER TABLE `mutation` ADD KEY (`MUTATION_EVENT_ID`);
ALTER TABLE `sample_profile` ADD KEY (`SAMPLE_ID`);
CREATE TABLE gene_panel (
  INTERNAL_ID int(11) NOT NULL auto_increment,
  STABLE_ID varchar(255) NOT NULL,
  DESCRIPTION mediumtext,
  PRIMARY KEY (INTERNAL_ID),
  UNIQUE (STABLE_ID)
);
CREATE TABLE gene_panel_list (
  INTERNAL_ID int(11) NOT NULL,
  GENE_ID int(255) NOT NULL,
  PRIMARY KEY (INTERNAL_ID, GENE_ID),
  FOREIGN KEY (INTERNAL_ID) REFERENCES gene_panel (INTERNAL_ID) ON DELETE CASCADE,
  FOREIGN KEY (GENE_ID) REFERENCES gene (ENTREZ_GENE_ID) ON DELETE CASCADE
);
ALTER TABLE `sample_profile` ADD COLUMN PANEL_ID int(11) DEFAULT NULL, ADD FOREIGN KEY (PANEL_ID) REFERENCES `gene_panel` (PANEL_ID) ON DELETE RESTRICT;
CREATE TABLE `clinical_attribute_meta` (
  `ATTR_ID` varchar(255) NOT NULL,
  `DISPLAY_NAME` varchar(255) NOT NULL,
  `DESCRIPTION` varchar(2048) NOT NULL,
  `DATATYPE` varchar(255) NOT NULL,
  `PATIENT_ATTRIBUTE` BOOLEAN NOT NULL,
  `PRIORITY` varchar(255) NOT NULL,
  `CANCER_STUDY_ID` int(11) NOT NULL,
  PRIMARY KEY (`ATTR_ID`, `CANCER_STUDY_ID`),
  FOREIGN KEY (`CANCER_STUDY_ID`) REFERENCES `cancer_study` (`CANCER_STUDY_ID`) ON DELETE CASCADE
);
INSERT INTO clinical_attribute_meta 
  SELECT DISTINCT clinical_sample.attr_id, clinical_attribute.display_name, clinical_attribute.description, clinical_attribute.datatype, clinical_attribute.patient_attribute, clinical_attribute.priority, cancer_study.cancer_study_id 
  FROM clinical_attribute 
  INNER JOIN clinical_sample ON clinical_attribute.ATTR_ID = clinical_sample.ATTR_ID 
  INNER JOIN sample ON clinical_sample.internal_id = sample.internal_id 
  INNER JOIN patient ON sample.patient_id = patient.internal_id 
  INNER  JOIN cancer_study ON patient.cancer_study_id = cancer_study.cancer_study_id;
INSERT INTO clinical_attribute_meta 
  SELECT DISTINCT clinical_patient.attr_id, clinical_attribute.display_name, clinical_attribute.description, clinical_attribute.datatype, clinical_attribute.patient_attribute, clinical_attribute.priority, cancer_study.cancer_study_id 
  FROM clinical_attribute 
  INNER JOIN clinical_patient ON clinical_attribute.ATTR_ID = clinical_patient.ATTR_ID 
  INNER JOIN patient ON clinical_patient.internal_id = patient.internal_id 
  INNER JOIN cancer_study ON patient.cancer_study_id = cancer_study.cancer_study_id;
DROP TABLE IF EXISTS clinical_attribute;
CREATE TABLE `structural_variant` (
  `SAMPLE_ID` int(11) NOT NULL,
  `INTERNAL_ID` int(11) NOT NULL auto_increment,
  `BREAKPOINT_TYPE` varchar(25),
  `ANNOTATION` varchar(255),
  `COMMENTS` varchar(2048),
  `CONFIDENCE_CLASS` varchar(25),
  `CONNECTION_TYPE` varchar(25),
  `EVENT_INFO` varchar(255),
  `MAPQ` int(11),
  `NORMAL_READ_COUNT` int(11),
  `NORMAL_VARIANT_COUNT` int(11),
  `PAIRED_END_READ_SUPPORT` varchar(255),
  `SITE1_CHROM` varchar(25),
  `SITE1_DESC` varchar(255),
  `SITE1_GENE` varchar(255),
  `SITE1_POS` int(11),
  `SITE2_CHROM` varchar(25),
  `SITE2_DESC` varchar(255),
  `SITE2_GENE` varchar(255),
  `SITE2_POS` int(11),
  `SPLIT_READ_SUPPORT` varchar(255),
  `SV_CLASS_NAME` varchar(25),
  `SV_DESC` varchar(255),
  `SV_LENGTH` int(11),
  `TUMOR_READ_COUNT` int(11),
  `TUMOR_VARIANT_COUNT` int(11),
  `VARIANT_STATUS_NAME` varchar(255),
  `GENETIC_PROFILE_ID` int(11) NOT NULL,
  PRIMARY KEY (`INTERNAL_ID`),
  FOREIGN KEY (`SAMPLE_ID`) REFERENCES `sample` (`INTERNAL_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`SITE1_GENE`) REFERENCES `gene` (`HUGO_GENE_SYMBOL`) ON DELETE CASCADE,
  FOREIGN KEY (`SITE2_GENE`) REFERENCES `gene` (`HUGO_GENE_SYMBOL`) ON DELETE CASCADE,
  FOREIGN KEY (`GENETIC_PROFILE_ID`) REFERENCES `genetic_profile` (`GENETIC_PROFILE_ID`) ON DELETE CASCADE
);
UPDATE info SET DB_SCHEMA_VERSION="1.3.0";

##version: 1.3.1
DROP TABLE IF EXISTS entity_attribute;
DROP TABLE IF EXISTS attribute_metadata;
DROP TABLE IF EXISTS entity_link;
DROP TABLE IF EXISTS entity;
-- cannot drop / adjust foreign keys without knowing unspecified constraint identifier : drop and recreate table instead
DROP TABLE IF EXISTS structural_variant;
CREATE TABLE `structural_variant` (
  `SAMPLE_ID` int(11) NOT NULL,
  `INTERNAL_ID` int(11) NOT NULL auto_increment,
  `BREAKPOINT_TYPE` varchar(25),
  `ANNOTATION` varchar(255),
  `COMMENTS` varchar(2048),
  `CONFIDENCE_CLASS` varchar(25),
  `CONNECTION_TYPE` varchar(25),
  `EVENT_INFO` varchar(255),
  `MAPQ` int(11),
  `NORMAL_READ_COUNT` int(11),
  `NORMAL_VARIANT_COUNT` int(11),
  `PAIRED_END_READ_SUPPORT` varchar(255),
  `SITE1_CHROM` varchar(25),
  `SITE1_DESC` varchar(255),
  `SITE1_ENTREZ_GENE_ID` int(11),
  `SITE1_POS` int(11),
  `SITE2_CHROM` varchar(25),
  `SITE2_DESC` varchar(255),
  `SITE2_ENTREZ_GENE_ID` int(11),
  `SITE2_POS` int(11),
  `SPLIT_READ_SUPPORT` varchar(255),
  `SV_CLASS_NAME` varchar(25),
  `SV_DESC` varchar(255),
  `SV_LENGTH` int(11),
  `TUMOR_READ_COUNT` int(11),
  `TUMOR_VARIANT_COUNT` int(11),
  `VARIANT_STATUS_NAME` varchar(255),
  `GENETIC_PROFILE_ID` int(11) NOT NULL,
  PRIMARY KEY (`INTERNAL_ID`),
  FOREIGN KEY (`SAMPLE_ID`) REFERENCES `sample` (`INTERNAL_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`SITE1_ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`SITE2_ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`GENETIC_PROFILE_ID`) REFERENCES `genetic_profile` (`GENETIC_PROFILE_ID`) ON DELETE CASCADE
);
UPDATE info SET DB_SCHEMA_VERSION="1.3.1";

##version: 1.3.2
-- increase varchar size to accomodate reference or tumor seq alleles larger than 255 chars
ALTER TABLE `mutation_event` MODIFY COLUMN `REFERENCE_ALLELE` varchar(400);
ALTER TABLE `mutation_event` MODIFY COLUMN `TUMOR_SEQ_ALLELE` varchar(400);
UPDATE info SET DB_SCHEMA_VERSION="1.3.2";

##version: 1.4.0
-- alter version number to distinguish from cbioportal web application version numbering
ALTER TABLE info MODIFY COLUMN DB_SCHEMA_VERSION VARCHAR(24);
UPDATE info SET DB_SCHEMA_VERSION="1.4.0";

##version: 2.0.0
-- ========================== start of genetic_entity related migration =============================================
-- add genetic_entity table 
CREATE TABLE `genetic_entity` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `ENTITY_TYPE` varchar(45) NOT NULL,
  PRIMARY KEY (`ID`)
);
-- update gene table to use genetic_element:
ALTER TABLE `gene` 
ADD COLUMN `GENETIC_ENTITY_ID` INT NULL;

-- add temporary column to support migration:
ALTER TABLE `genetic_entity` 
ADD COLUMN `TMP_GENE_ID` INT NOT NULL AFTER `ENTITY_TYPE`,
ADD UNIQUE INDEX `TMP_GENE_ID_UNIQUE` (`TMP_GENE_ID` ASC);

-- populate genetic_entity
insert into genetic_entity (entity_type, tmp_gene_id)
(Select 'GENE', ENTREZ_GENE_ID from gene);

-- update gene table to have GENETIC_ENTITY_ID point to the correct one:
UPDATE gene
INNER JOIN genetic_entity ON gene.ENTREZ_GENE_ID = genetic_entity.TMP_GENE_ID
SET GENETIC_ENTITY_ID = genetic_entity.ID;

-- add UQ and FK constraint for GENETIC_ENTITY_ID in gene table:
ALTER TABLE `gene` 
CHANGE COLUMN `GENETIC_ENTITY_ID` `GENETIC_ENTITY_ID` INT NOT NULL,
ADD UNIQUE INDEX `GENETIC_ENTITY_ID_UNIQUE` (`GENETIC_ENTITY_ID` ASC);

ALTER TABLE `gene` 
ADD CONSTRAINT `fk_gene_1`
  FOREIGN KEY (`GENETIC_ENTITY_ID`)
  REFERENCES `genetic_entity` (`ID`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- migrate genetic_alteration table in a similar way, pointing to GENETIC_ENTITY_ID 
-- instead of ENTREZ_GENE_ID (note: the INSERT part can take some time [~20 min], 
-- depending on how many studies you have in your DB):
CREATE TABLE `genetic_alteration_new` (
  `GENETIC_PROFILE_ID` int(11) NOT NULL,
  `GENETIC_ENTITY_ID` int(11) NOT NULL,
  `VALUES` longtext NOT NULL,
  PRIMARY KEY (`GENETIC_PROFILE_ID`, `GENETIC_ENTITY_ID`),
  CONSTRAINT `genetic_alteration_fk_1` FOREIGN KEY (`GENETIC_PROFILE_ID`) REFERENCES `genetic_profile` (`GENETIC_PROFILE_ID`) ON DELETE CASCADE,
  CONSTRAINT `genetic_alteration_fk_2` FOREIGN KEY (`GENETIC_ENTITY_ID`) REFERENCES `genetic_entity` (`ID`)
);

INSERT INTO genetic_alteration_new
(GENETIC_PROFILE_ID, GENETIC_ENTITY_ID, `VALUES`)
SELECT genetic_alteration.GENETIC_PROFILE_ID, genetic_entity.ID, genetic_alteration.`VALUES`
FROM genetic_alteration 
INNER JOIN genetic_entity ON genetic_alteration.ENTREZ_GENE_ID = genetic_entity.TMP_GENE_ID;

-- drop old genetic_alteration
DROP TABLE genetic_alteration;
-- rename new one to genetic_alteration:
RENAME TABLE `genetic_alteration_new` TO `genetic_alteration`;
-- drop temporary column:
ALTER TABLE `genetic_entity` DROP COLUMN `TMP_GENE_ID`;


-- ========================== end of genetic_entity related migration =============================================

UPDATE info SET DB_SCHEMA_VERSION="2.0.0";

##version: 2.0.1
ALTER TABLE `genetic_profile` MODIFY COLUMN `SHOW_PROFILE_IN_ANALYSIS_TAB` BOOLEAN NOT NULL;
UPDATE info SET DB_SCHEMA_VERSION="2.0.1";

##version: 2.1.0
-- ========================== new geneset related tables =============================================

CREATE TABLE `geneset` (
  `ID` INT(11) NOT NULL auto_increment,
  `GENETIC_ENTITY_ID` INT NOT NULL,
  `EXTERNAL_ID` VARCHAR(100) NOT NULL,
  `NAME` VARCHAR(100) NOT NULL,
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
CREATE TABLE `geneset_hierarchy` (
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
  FOREIGN KEY (`NODE_ID`) REFERENCES `geneset_hierarchy` (`NODE_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`GENESET_ID`) REFERENCES `geneset` (`ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
CREATE TABLE `geneset_info` (
  `GENESET_VERSION` varchar(24)
);

-- --------------------------------------------------------
CREATE TABLE `genetic_profile_link` (
  `REFERRING_GENETIC_PROFILE_ID` INT NOT NULL,
  `REFERRED_GENETIC_PROFILE_ID` INT NOT NULL,
  `REFERENCE_TYPE` VARCHAR(45) NULL,
  PRIMARY KEY (`REFERRING_GENETIC_PROFILE_ID`, `REFERRED_GENETIC_PROFILE_ID`),
  FOREIGN KEY (`REFERRING_GENETIC_PROFILE_ID` ) REFERENCES `genetic_profile` (`GENETIC_PROFILE_ID`)  ON DELETE CASCADE,
  FOREIGN KEY (`REFERRED_GENETIC_PROFILE_ID` ) REFERENCES `genetic_profile` (`GENETIC_PROFILE_ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
);

UPDATE info SET DB_SCHEMA_VERSION="2.1.0";

-- ========================== end of geneset related tables =============================================

