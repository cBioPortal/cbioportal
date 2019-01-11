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
ALTER TABLE `sample_profile` ADD COLUMN PANEL_ID int(11) DEFAULT NULL, ADD FOREIGN KEY (PANEL_ID) REFERENCES `gene_panel` (INTERNAL_ID) ON DELETE RESTRICT;
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
ALTER TABLE `gene` ADD COLUMN `GENETIC_ENTITY_ID` INT NULL AFTER `HUGO_GENE_SYMBOL`;

-- add temporary column to support migration:
ALTER TABLE `genetic_entity` 
ADD COLUMN `TMP_GENE_ID` INT NOT NULL AFTER `ENTITY_TYPE`,
ADD UNIQUE INDEX `TMP_GENE_ID_UNIQUE` (`TMP_GENE_ID` ASC);

-- populate genetic_entity
INSERT INTO genetic_entity (entity_type, tmp_gene_id)
(SELECT 'GENE', ENTREZ_GENE_ID FROM gene);

-- update gene table to have GENETIC_ENTITY_ID point to the correct one:
UPDATE gene INNER JOIN genetic_entity ON gene.ENTREZ_GENE_ID = genetic_entity.TMP_GENE_ID SET GENETIC_ENTITY_ID = genetic_entity.ID;

-- add UQ and FK constraint for GENETIC_ENTITY_ID in gene table:
ALTER TABLE `gene` 
CHANGE COLUMN `GENETIC_ENTITY_ID` `GENETIC_ENTITY_ID` INT NOT NULL,
ADD UNIQUE INDEX `GENETIC_ENTITY_ID_UNIQUE` (`GENETIC_ENTITY_ID` ASC);

ALTER TABLE `gene` 
ADD FOREIGN KEY (`GENETIC_ENTITY_ID`) REFERENCES `genetic_entity` (`ID`) ON DELETE CASCADE;

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
FROM genetic_alteration INNER JOIN genetic_entity ON genetic_alteration.ENTREZ_GENE_ID = genetic_entity.TMP_GENE_ID;

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
ALTER TABLE `cancer_study` MODIFY COLUMN `TYPE_OF_CANCER_ID` varchar(63) NOT NULL;
ALTER TABLE `sample` MODIFY COLUMN `TYPE_OF_CANCER_ID` varchar(63) NOT NULL;
UPDATE info SET DB_SCHEMA_VERSION="2.1.0";

##version: 2.2.0
CREATE TABLE `mutation_count_by_keyword` (
    `GENETIC_PROFILE_ID` int(11) NOT NULL,
    `KEYWORD` varchar(50) DEFAULT NULL,
    `ENTREZ_GENE_ID` int(11) NOT NULL,
    `KEYWORD_COUNT` int NOT NULL,
    `GENE_COUNT` int NOT NULL,
    KEY (`GENETIC_PROFILE_ID`,`KEYWORD`),
    FOREIGN KEY (`GENETIC_PROFILE_ID`) REFERENCES `genetic_profile` (`GENETIC_PROFILE_ID`) ON DELETE CASCADE,
    FOREIGN KEY (`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`) ON DELETE CASCADE
);

INSERT INTO mutation_count_by_keyword
    SELECT g2.`GENETIC_PROFILE_ID`, mutation_event.`KEYWORD`, m2.`ENTREZ_GENE_ID`,
        IF(mutation_event.`KEYWORD` IS NULL, 0, COUNT(*)) AS KEYWORD_COUNT, 
        (SELECT COUNT(*) FROM `mutation` AS m1 , `genetic_profile` AS g1
        WHERE m1.`GENETIC_PROFILE_ID` = g1.`GENETIC_PROFILE_ID`
        AND g1.`GENETIC_PROFILE_ID`= g2.`GENETIC_PROFILE_ID` AND m1.`ENTREZ_GENE_ID` = m2.`ENTREZ_GENE_ID`
        GROUP BY g1.`GENETIC_PROFILE_ID` , m1.`ENTREZ_GENE_ID`) AS GENE_COUNT
    FROM `mutation` AS m2 , `genetic_profile` AS g2 , `mutation_event`
    WHERE m2.`GENETIC_PROFILE_ID` = g2.`GENETIC_PROFILE_ID`
          AND m2.`MUTATION_EVENT_ID` = mutation_event.`MUTATION_EVENT_ID`
          AND g2.`GENETIC_ALTERATION_TYPE` = 'MUTATION_EXTENDED'
    GROUP BY g2.`GENETIC_PROFILE_ID` , mutation_event.`KEYWORD` , m2.`ENTREZ_GENE_ID`;
UPDATE info SET DB_SCHEMA_VERSION="2.2.0";


##version: 2.3.0
-- ========================== new geneset related tables =============================================

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

-- --------------------------------------------------------
ALTER TABLE `info` ADD COLUMN `GENESET_VERSION` VARCHAR(24) NULL AFTER `DB_SCHEMA_VERSION`;

-- --------------------------------------------------------
CREATE TABLE `genetic_profile_link` (
  `REFERRING_GENETIC_PROFILE_ID` INT NOT NULL,
  `REFERRED_GENETIC_PROFILE_ID` INT NOT NULL,
  `REFERENCE_TYPE` VARCHAR(45) NULL,
  PRIMARY KEY (`REFERRING_GENETIC_PROFILE_ID`, `REFERRED_GENETIC_PROFILE_ID`),
  FOREIGN KEY (`REFERRING_GENETIC_PROFILE_ID` ) REFERENCES `genetic_profile` (`GENETIC_PROFILE_ID`)  ON DELETE CASCADE,
  FOREIGN KEY (`REFERRED_GENETIC_PROFILE_ID` ) REFERENCES `genetic_profile` (`GENETIC_PROFILE_ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
);

UPDATE info SET DB_SCHEMA_VERSION="2.3.0";

-- ========================== end of geneset related tables =============================================

##version: 2.3.1
TRUNCATE TABLE mutation_count_by_keyword;

INSERT INTO mutation_count_by_keyword
    SELECT g2.`GENETIC_PROFILE_ID`, mutation_event.`KEYWORD`, m2.`ENTREZ_GENE_ID`,
        IF(mutation_event.`KEYWORD` IS NULL, 0, COUNT(DISTINCT(m2.SAMPLE_ID))) AS KEYWORD_COUNT,
        (SELECT COUNT(DISTINCT(m1.SAMPLE_ID)) FROM `mutation` AS m1 , `genetic_profile` AS g1
        WHERE m1.`GENETIC_PROFILE_ID` = g1.`GENETIC_PROFILE_ID`
              AND g1.`GENETIC_PROFILE_ID`= g2.`GENETIC_PROFILE_ID` AND m1.`ENTREZ_GENE_ID` = m2.`ENTREZ_GENE_ID`
        GROUP BY g1.`GENETIC_PROFILE_ID` , m1.`ENTREZ_GENE_ID`) AS GENE_COUNT
    FROM `mutation` AS m2 , `genetic_profile` AS g2 , `mutation_event`
    WHERE m2.`GENETIC_PROFILE_ID` = g2.`GENETIC_PROFILE_ID`
          AND m2.`MUTATION_EVENT_ID` = mutation_event.`MUTATION_EVENT_ID`
          AND g2.`GENETIC_ALTERATION_TYPE` = 'MUTATION_EXTENDED'
    GROUP BY g2.`GENETIC_PROFILE_ID` , mutation_event.`KEYWORD` , m2.`ENTREZ_GENE_ID`;

UPDATE info SET DB_SCHEMA_VERSION="2.3.1";

##version: 2.4.0
ALTER TABLE `mutation` ADD COLUMN `DRIVER_FILTER` VARCHAR(20) NULL;
ALTER TABLE `mutation` ADD COLUMN `DRIVER_FILTER_ANNOTATION` VARCHAR(80) NULL;
ALTER TABLE `mutation` ADD COLUMN `DRIVER_TIERS_FILTER` VARCHAR(50) NULL;
ALTER TABLE `mutation` ADD COLUMN `DRIVER_TIERS_FILTER_ANNOTATION` VARCHAR(80) NULL;

UPDATE info SET DB_SCHEMA_VERSION="2.4.0";

##version: 2.4.1
-- ========================== new reference genome genes related tables =============================================
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

INSERT INTO `reference_genome` 
VALUES (1, 'human', 'hg19', 'GRCh37', NULL, 'http://hgdownload.cse.ucsc.edu/goldenPath/hg19/bigZips', '2009-02-01');
INSERT INTO `reference_genome` 
VALUES (2, 'human', 'hg38', 'GRCh38', NULL, 'http://hgdownload.cse.ucsc.edu/goldenPath/hg38/bigZips', '2013-12-01');
INSERT INTO `reference_genome` 
VALUES (3, 'mouse', 'mm10', 'GRCm38', NULL, 'http://hgdownload.cse.ucsc.edu//goldenPath/mm10/bigZips', '2012-01-01');

CREATE TABLE `reference_genome_gene` (
    `ENTREZ_GENE_ID` int(11) NOT NULL,
    `REFERENCE_GENOME_ID` int(4) NOT NULL,
    `CHR` varchar(4) DEFAULT NULL,
    `CYTOBAND` varchar(64) DEFAULT NULL,
    `EXONIC_LENGTH` int(11) DEFAULT NULL,
    `START` bigint(20) DEFAULT NULL,
    `END` bigint(20) DEFAULT NULL,
    `ENSEMBL_GENE_ID` varchar(64) DEFAULT NULL,
    PRIMARY KEY (`ENTREZ_GENE_ID`,`REFERENCE_GENOME_ID`),
    FOREIGN KEY (`REFERENCE_GENOME_ID`) REFERENCES `reference_genome` (`REFERENCE_GENOME_ID`) ON DELETE CASCADE,
    FOREIGN KEY (`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`) ON DELETE CASCADE
);

INSERT INTO reference_genome_gene (ENTREZ_GENE_ID, CYTOBAND, EXONIC_LENGTH, CHR, REFERENCE_GENOME_ID)
(SELECT 
	ENTREZ_GENE_ID, 
	CYTOBAND, 
	LENGTH,
    SUBSTRING_INDEX(SUBSTRING_INDEX(SUBSTRING_INDEX(gene.CYTOBAND,IF(LOCATE('p', gene.CYTOBAND), 'p', 'q'), 1),'q',1),'cen',1),
	1 
FROM `gene`);

UPDATE info SET DB_SCHEMA_VERSION="2.4.1";
-- ========================= end of reference genes related tables ========================================================================

##version: 2.5.0

CREATE TABLE `fraction_genome_altered` (
  `CANCER_STUDY_ID` int(11) NOT NULL,
  `SAMPLE_ID` int(11) NOT NULL,
  `VALUE` double NOT NULL,
  PRIMARY KEY (`CANCER_STUDY_ID`,`SAMPLE_ID`),
  FOREIGN KEY (`CANCER_STUDY_ID`) REFERENCES `cancer_study` (`CANCER_STUDY_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`SAMPLE_ID`) REFERENCES `sample` (`INTERNAL_ID`) ON DELETE CASCADE
);

INSERT INTO `fraction_genome_altered` SELECT cancer_study.`CANCER_STUDY_ID`, `SAMPLE_ID`, IF((SELECT SUM(`END`-`START`) FROM copy_number_seg AS c2 
WHERE c2.`CANCER_STUDY_ID` = c1.`CANCER_STUDY_ID` AND c2.`SAMPLE_ID` = c1.`SAMPLE_ID` AND ABS(c2.`SEGMENT_MEAN`) >= 0.2) IS NULL, 0, 
(SELECT SUM(`END`-`START`) FROM copy_number_seg AS c2 WHERE c2.`CANCER_STUDY_ID` = c1.`CANCER_STUDY_ID` AND c2.`SAMPLE_ID` = c1.`SAMPLE_ID` AND 
ABS(c2.`SEGMENT_MEAN`) >= 0.2) / SUM(`END`-`START`)) AS `VALUE` FROM `copy_number_seg` AS c1, cancer_study WHERE 
c1.`CANCER_STUDY_ID` = cancer_study.`CANCER_STUDY_ID` GROUP BY cancer_study.`CANCER_STUDY_ID`, `SAMPLE_ID` HAVING SUM(`END`-`START`) > 0;

UPDATE info SET DB_SCHEMA_VERSION="2.5.0";

##version: 2.6.0
-- modify fkc for gistic_to_gene
ALTER TABLE gistic_to_gene DROP FOREIGN KEY gistic_to_gene_ibfk_2;
ALTER TABLE gistic_to_gene ADD CONSTRAINT `gistic_to_gene_ibfk_2` FOREIGN KEY (`GISTIC_ROI_ID`) REFERENCES `gistic` (`GISTIC_ROI_ID`) ON DELETE CASCADE;
UPDATE info SET DB_SCHEMA_VERSION="2.6.0";

##version: 2.6.1
ALTER TABLE `mutation_event` MODIFY COLUMN `KEYWORD` VARCHAR(255);
ALTER TABLE `mutation_count_by_keyword` MODIFY COLUMN `KEYWORD` VARCHAR(255);
UPDATE `info` SET `DB_SCHEMA_VERSION`="2.6.1";

##version: 2.6.2
-- add mutational signature table
CREATE TABLE `mutational_signature` (
  `MUTATIONAL_SIGNATURE_ID` VARCHAR (30) NOT NULL,
  `GENETIC_ENTITY_ID` INT NULL,
  `DESCRIPTION` VARCHAR(300) NOT NULL,
  PRIMARY KEY (`MUTATIONAL_SIGNATURE_ID`)
);

-- insert mutational signature id and description
INSERT INTO `mutational_signature` 
VALUES ('mutational_signature_1', NULL, 'Signature 1 has been found in all cancer types and in most cancer samples.'),
('mutational_signature_2', NULL, 'Signature 2 has been found in 22 cancer types, but most commonly in cervical and bladder cancers. In most of these 22 cancer types, Signature 2 is present in at least 10% of samples.'),
('mutational_signature_3', NULL, 'Signature 3 has been found in breast, ovarian, and pancreatic cancers.'),
('mutational_signature_4', NULL, 'Signature 4 has been found in head and neck cancer, liver cancer, lung adenocarcinoma, lung squamous carcinoma, small cell lung carcinoma, and oesophageal cancer.'),
('mutational_signature_5', NULL, 'Signature 5 has been found in all cancer types and most cancer samples.'),
('mutational_signature_6', NULL, 'Signature 6 has been found in 17 cancer types and is most common in colorectal and uterine cancers. In most other cancer types, Signature 6 is found in less than 3% of examined samples.'),
('mutational_signature_7', NULL, 'Signature 7 has been found predominantly in skin cancers and in cancers of the lip categorized as head and neck or oral squamous cancers.'),
('mutational_signature_8', NULL, 'Signature 8 has been found in breast cancer and medulloblastoma.'),
('mutational_signature_9', NULL, 'Signature 9 has been found in chronic lymphocytic leukaemias and malignant B-cell lymphomas.'),
('mutational_signature_10', NULL, 'Signature 10 has been found in six cancer types, notably colorectal and uterine cancer, usually generating huge numbers of mutations in small subsets of samples.'),
('mutational_signature_11', NULL, 'Signature 11 has been found in melanoma and glioblastoma.'),
('mutational_signature_12', NULL, 'Signature 12 has been found in liver cancer.'),
('mutational_signature_13', NULL, 'Signature 13 has been found in 22 cancer types and seems to be commonest in cervical and bladder cancers. In most of these 22 cancer types, Signature 13 is present in at least 10% of samples.'),
('mutational_signature_14', NULL, 'Signature 14 has been observed in four uterine cancers and a single adult low-grade glioma sample.'),
('mutational_signature_15', NULL, 'Signature 15 has been found in several stomach cancers and a single small cell lung carcinoma.'),
('mutational_signature_16', NULL, 'Signature 16 has been found in liver cancer.'),
('mutational_signature_17', NULL, 'Signature 17 has been found in oesophagus cancer, breast cancer, liver cancer, lung adenocarcinoma, B-cell lymphoma, stomach cancer and melanoma.'),
('mutational_signature_18', NULL, 'Signature 18 has been found commonly in neuroblastoma. Additionally, Signature 18 has been also observed in breast and stomach carcinomas.'),
('mutational_signature_19', NULL, 'Signature 19 has been found only in pilocytic astrocytoma.'),
('mutational_signature_20', NULL, 'Signature 20 has been found in stomach and breast cancers.'),
('mutational_signature_21', NULL, 'Signature 21 has been found only in stomach cancer.'),
('mutational_signature_22', NULL, 'Signature 22 has been found in urothelial (renal pelvis) carcinoma and liver cancers.'),
('mutational_signature_23', NULL, 'Signature 23 has been found only in a single liver cancer sample.'),
('mutational_signature_24', NULL, 'Signature 24 has been observed in a subset of liver cancers.'),
('mutational_signature_25', NULL, 'Signature 25 has been observed in Hodgkin lymphomas.'),
('mutational_signature_26', NULL, 'Signature 26 has been found in breast cancer, cervical cancer, stomach cancer and uterine carcinoma.'),
('mutational_signature_27', NULL, 'Signature 27 has been observed in a subset of kidney clear cell carcinomas.'),
('mutational_signature_28', NULL, 'Signature 28 has been observed in a subset of stomach cancers.'),
('mutational_signature_29', NULL, 'Signature 29 has been observed only in gingivo-buccal oral squamous cell carcinoma.'),
('mutational_signature_30', NULL, 'Signature 30 has been observed in a small subset of breast cancers.');

--add temporary column in genetic_entity to support migration
ALTER TABLE `genetic_entity` 
ADD COLUMN `TMP_MUTATIONAL_SIGNATURE_ID` VARCHAR (30) NULL AFTER `ENTITY_TYPE`;

--autoincrement genetic_entity table and add mutational signatures as new genetic entities
INSERT INTO genetic_entity (ENTITY_TYPE, TMP_MUTATIONAL_SIGNATURE_ID) 
(SELECT 'MUTATIONAL_SIGNATURE', MUTATIONAL_SIGNATURE_ID FROM mutational_signature);

--update mutational signature table to have GENETIC_ENTITY_ID match the correct one in the genetic_entity table
UPDATE mutational_signature INNER JOIN genetic_entity on mutational_signature.mutational_signature_id = 
genetic_entity.TMP_MUTATIONAL_SIGNATURE_ID SET GENETIC_ENTITY_ID = genetic_entity.ID;

--make GENETIC_ENTITY_ID a foreign key
ALTER TABLE mutational_signature CHANGE COLUMN `GENETIC_ENTITY_ID` `GENETIC_ENTITY_ID` INT NOT NULL,
ADD UNIQUE INDEX `MUTATIONAL_SIGNATURE_GENETIC_ENTITY_ID_UNIQUE` (`GENETIC_ENTITY_ID` ASC),
ADD FOREIGN KEY (`GENETIC_ENTITY_ID`) REFERENCES `genetic_entity` (`ID`) ON DELETE CASCADE;

-- drop temporary column:
ALTER TABLE `genetic_entity` DROP COLUMN `TMP_MUTATIONAL_SIGNATURE_ID`;

UPDATE `info` SET `DB_SCHEMA_VERSION`="2.6.2";
##version: 2.7.0
DELETE FROM `clinical_attribute_meta` WHERE clinical_attribute_meta.`ATTR_ID` = 'MUTATION_COUNT';
INSERT INTO `clinical_attribute_meta` SELECT 'MUTATION_COUNT', 'Mutation Count', 'Mutation Count', 'NUMBER', 0, '30', 
genetic_profile.`CANCER_STUDY_ID` FROM mutation_count INNER JOIN genetic_profile ON 
mutation_count.`GENETIC_PROFILE_ID` = genetic_profile.`GENETIC_PROFILE_ID` GROUP BY genetic_profile.`CANCER_STUDY_ID`;

-- MSK internal change to update all uncalled profiles to have the same
-- genetic_alteration_type. This avoids including those profiles when
-- recalculating the mutation counts.
update genetic_profile set GENETIC_ALTERATION_TYPE = 'MUTATION_UNCALLED' where stable_id like '%_uncalled';

-- recalculate mutation counts
-- exclude germline and fusions (msk internal)
DELETE FROM `clinical_sample` WHERE clinical_sample.`ATTR_ID` = 'MUTATION_COUNT';
INSERT INTO `clinical_sample` SELECT `SAMPLE_ID`, 'MUTATION_COUNT', COUNT(DISTINCT mutation_event.`CHR`, mutation_event.`START_POSITION`, 
mutation_event.`END_POSITION`, mutation_event.`REFERENCE_ALLELE`, mutation_event.`TUMOR_SEQ_ALLELE`) AS MUTATION_COUNT 
FROM `mutation` , `genetic_profile`, `mutation_event` WHERE genetic_profile.`GENETIC_ALTERATION_TYPE` = 'MUTATION_EXTENDED'
AND mutation.`GENETIC_PROFILE_ID` = genetic_profile.`GENETIC_PROFILE_ID`
AND mutation.`MUTATION_EVENT_ID` = mutation_event.`MUTATION_EVENT_ID` AND mutation.`MUTATION_STATUS` <> 'GERMLINE' 
AND mutation_event.`MUTATION_TYPE` <> 'Fusion'
GROUP BY genetic_profile.`GENETIC_PROFILE_ID` , `SAMPLE_ID`;

-- recalculate fraction genome altered
DELETE FROM `clinical_attribute_meta` WHERE clinical_attribute_meta.`ATTR_ID` = 'FRACTION_GENOME_ALTERED';
INSERT INTO `clinical_attribute_meta` SELECT 'FRACTION_GENOME_ALTERED', 'Fraction Genome Altered', 
'Fraction Genome Altered', 'NUMBER', 0, '20', fraction_genome_altered.`CANCER_STUDY_ID` FROM fraction_genome_altered 
GROUP BY fraction_genome_altered.`CANCER_STUDY_ID`;

DELETE FROM `clinical_sample` WHERE clinical_sample.`ATTR_ID` = 'FRACTION_GENOME_ALTERED';
INSERT INTO `clinical_sample` SELECT `SAMPLE_ID`, 'FRACTION_GENOME_ALTERED', IF((SELECT SUM(`END`-`START`) 
FROM copy_number_seg AS c2 WHERE c2.`CANCER_STUDY_ID` = c1.`CANCER_STUDY_ID` AND c2.`SAMPLE_ID` = c1.`SAMPLE_ID` 
AND ABS(c2.`SEGMENT_MEAN`) >= 0.2) IS NULL, 0, (SELECT SUM(`END`-`START`) FROM copy_number_seg AS c2 
WHERE c2.`CANCER_STUDY_ID` = c1.`CANCER_STUDY_ID` AND c2.`SAMPLE_ID` = c1.`SAMPLE_ID` 
AND ABS(c2.`SEGMENT_MEAN`) >= 0.2) / SUM(`END`-`START`)) AS `VALUE` FROM `copy_number_seg` AS c1 , `cancer_study` 
WHERE c1.`CANCER_STUDY_ID` = cancer_study.`CANCER_STUDY_ID` GROUP BY cancer_study.`CANCER_STUDY_ID` , `SAMPLE_ID` 
HAVING SUM(`END`-`START`) > 0;

DROP TABLE IF EXISTS mutation_count;
DROP TABLE IF EXISTS fraction_genome_altered;

UPDATE `info` SET `DB_SCHEMA_VERSION`="2.7.0";

##version: 2.7.1
DELETE FROM `clinical_sample` WHERE clinical_sample.`ATTR_ID` = 'MUTATION_COUNT';
INSERT INTO `clinical_sample` SELECT sample_profile.`SAMPLE_ID`, 'MUTATION_COUNT', COUNT(DISTINCT mutation_event.`CHR`, mutation_event.`START_POSITION`, 
mutation_event.`END_POSITION`, mutation_event.`REFERENCE_ALLELE`, mutation_event.`TUMOR_SEQ_ALLELE`) AS MUTATION_COUNT 
FROM `sample_profile` 
LEFT JOIN mutation ON mutation.`SAMPLE_ID` = sample_profile.`SAMPLE_ID`
LEFT JOIN mutation_event ON mutation.`MUTATION_EVENT_ID` = mutation_event.`MUTATION_EVENT_ID`
INNER JOIN genetic_profile ON genetic_profile.`GENETIC_PROFILE_ID` = sample_profile.`GENETIC_PROFILE_ID`
WHERE genetic_profile.`GENETIC_ALTERATION_TYPE` = 'MUTATION_EXTENDED'
AND ( mutation.`MUTATION_STATUS` <> 'GERMLINE' OR mutation.`MUTATION_STATUS` IS NULL )
AND ( mutation_event.`MUTATION_TYPE` <> 'Fusion' OR mutation_event.`MUTATION_TYPE` IS NULL )
GROUP BY sample_profile.`GENETIC_PROFILE_ID` , sample_profile.`SAMPLE_ID`;

UPDATE `info` SET `DB_SCHEMA_VERSION`="2.7.1";

##version: 2.7.2
DELETE FROM `clinical_sample` WHERE clinical_sample.`ATTR_ID` = 'MUTATION_COUNT';
INSERT INTO `clinical_sample` SELECT sample_profile.`SAMPLE_ID`, 'MUTATION_COUNT', COUNT(DISTINCT mutation_event.`CHR`, mutation_event.`START_POSITION`,
mutation_event.`END_POSITION`, mutation_event.`REFERENCE_ALLELE`, mutation_event.`TUMOR_SEQ_ALLELE`) AS MUTATION_COUNT
FROM `sample_profile`
LEFT JOIN mutation ON mutation.`SAMPLE_ID` = sample_profile.`SAMPLE_ID`
AND ( mutation.`MUTATION_STATUS` <> 'GERMLINE' OR mutation.`MUTATION_STATUS` IS NULL )
LEFT JOIN mutation_event ON mutation.`MUTATION_EVENT_ID` = mutation_event.`MUTATION_EVENT_ID`
AND ( mutation_event.`MUTATION_TYPE` <> 'Fusion' OR mutation_event.`MUTATION_TYPE` IS NULL )
INNER JOIN genetic_profile ON genetic_profile.`GENETIC_PROFILE_ID` = sample_profile.`GENETIC_PROFILE_ID`
WHERE genetic_profile.`GENETIC_ALTERATION_TYPE` = 'MUTATION_EXTENDED'
GROUP BY sample_profile.`GENETIC_PROFILE_ID` , sample_profile.`SAMPLE_ID`;

DELETE FROM `clinical_sample` WHERE clinical_sample.`ATTR_ID` = 'FRACTION_GENOME_ALTERED';
INSERT INTO `clinical_sample` SELECT `SAMPLE_ID`, 'FRACTION_GENOME_ALTERED', IF((SELECT SUM(`END`-`START`) 
FROM copy_number_seg AS c2 WHERE c2.`CANCER_STUDY_ID` = c1.`CANCER_STUDY_ID` AND c2.`SAMPLE_ID` = c1.`SAMPLE_ID`
AND ABS(c2.`SEGMENT_MEAN`) >= 0.2) IS NULL, 0, (SELECT SUM(`END`-`START`) FROM copy_number_seg AS c2
WHERE c2.`CANCER_STUDY_ID` = c1.`CANCER_STUDY_ID` AND c2.`SAMPLE_ID` = c1.`SAMPLE_ID`
AND ABS(c2.`SEGMENT_MEAN`) >= 0.2) / SUM(`END`-`START`)) AS `VALUE` FROM `copy_number_seg` AS c1 , `cancer_study`
WHERE c1.`CANCER_STUDY_ID` = cancer_study.`CANCER_STUDY_ID` GROUP BY cancer_study.`CANCER_STUDY_ID` , `SAMPLE_ID`
HAVING SUM(`END`-`START`) > 0;

UPDATE `info` SET `DB_SCHEMA_VERSION`="2.7.2";

##version: 2.7.3
DELETE FROM `clinical_attribute_meta` WHERE clinical_attribute_meta.`ATTR_ID` = 'SAMPLE_COUNT';
INSERT INTO `clinical_attribute_meta` SELECT 'SAMPLE_COUNT', 'Number of Samples Per Patient', 
'Number of Samples Per Patient', 'STRING', 1, '1', patient.`CANCER_STUDY_ID` FROM patient
GROUP BY patient.`CANCER_STUDY_ID`;

DELETE FROM `clinical_patient` WHERE clinical_patient.`ATTR_ID` = 'SAMPLE_COUNT';
INSERT INTO `clinical_patient` SELECT patient.`INTERNAL_ID`, 'SAMPLE_COUNT', COUNT(*) FROM sample 
INNER JOIN patient ON sample.`PATIENT_ID` = patient.`INTERNAL_ID` GROUP BY patient.`INTERNAL_ID`;
UPDATE `info` SET `DB_SCHEMA_VERSION`="2.7.3";
