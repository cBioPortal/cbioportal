--
-- Copyright (c) 2016 - 2019 Memorial Sloan-Kettering Cancer Center.
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
VALUES (1, 'human', 'hg19', 'GRCh37', 2897310462, 'http://hgdownload.cse.ucsc.edu/goldenPath/hg19/bigZips', '2009-02-01');
INSERT INTO `reference_genome` 
VALUES (2, 'human', 'hg38', 'GRCh38', 3049315783, 'http://hgdownload.cse.ucsc.edu/goldenPath/hg38/bigZips', '2013-12-01');
INSERT INTO `reference_genome` 
VALUES (3, 'mouse', 'mm10', 'GRCm38', 2652783500, 'http://hgdownload.cse.ucsc.edu//goldenPath/mm10/bigZips', '2012-01-01');

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
  SUBSTRING_INDEX(
    SUBSTRING_INDEX(
      SUBSTRING_INDEX(
        SUBSTRING_INDEX(gene.CYTOBAND, 'p', 1),
      'q', 1),
    'cen', 1),
  ' ', 1),
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

##version: 2.7.4
CREATE TABLE `cancer_study_tags` (
  `CANCER_STUDY_ID` int(11) NOT NULL,
  `TAGS` text NOT NULL,
  PRIMARY KEY (`CANCER_STUDY_ID`),
  FOREIGN KEY (`CANCER_STUDY_ID`) REFERENCES `cancer_study` (`CANCER_STUDY_ID`) ON DELETE CASCADE
);

-- Corresponding change in cgds.sql was missed, hence this duplicate migration statement from version 2.6.1 to ensure dbs installed after v 2.6.1 are in sync.
ALTER TABLE `mutation_count_by_keyword` MODIFY COLUMN `KEYWORD` VARCHAR(255);

UPDATE `info` SET `DB_SCHEMA_VERSION`="2.7.4";

##version: 2.8.0
ALTER TABLE `cancer_study` MODIFY COLUMN `PMID` varchar(1024) DEFAULT NULL;
UPDATE `info` SET `DB_SCHEMA_VERSION`="2.8.0";

##version: 2.8.1
ALTER TABLE `mutation` MODIFY COLUMN `TUMOR_SEQ_ALLELE1` TEXT;
ALTER TABLE `mutation` MODIFY COLUMN `TUMOR_SEQ_ALLELE2` TEXT;
ALTER TABLE `mutation` MODIFY COLUMN `MATCH_NORM_SEQ_ALLELE1` TEXT;
ALTER TABLE `mutation` MODIFY COLUMN `MATCH_NORM_SEQ_ALLELE2` TEXT;
ALTER TABLE `mutation` MODIFY COLUMN `TUMOR_VALIDATION_ALLELE1` TEXT;
ALTER TABLE `mutation` MODIFY COLUMN `TUMOR_VALIDATION_ALLELE2` TEXT;
ALTER TABLE `mutation` MODIFY COLUMN `MATCH_NORM_VALIDATION_ALLELE1` TEXT;
ALTER TABLE `mutation` MODIFY COLUMN `MATCH_NORM_VALIDATION_ALLELE2` TEXT;

ALTER TABLE `mutation_event` DROP INDEX `CHR`;
ALTER TABLE `mutation_event` MODIFY COLUMN `TUMOR_SEQ_ALLELE` TEXT;
ALTER TABLE `mutation_event` MODIFY COLUMN `REFERENCE_ALLELE` TEXT;
ALTER TABLE `mutation_event` ADD KEY `KEY_MUTATION_EVENT_DETAILS` (`CHR`, `START_POSITION`, `END_POSITION`, `TUMOR_SEQ_ALLELE`(255), `ENTREZ_GENE_ID`, `PROTEIN_CHANGE`, `MUTATION_TYPE`);
UPDATE `info` SET `DB_SCHEMA_VERSION`="2.8.1";

##version: 2.8.2
ALTER TABLE `mutation_event` DROP KEY `KEY_MUTATION_EVENT_DETAILS`;
ALTER TABLE `mutation_event` ADD KEY `KEY_MUTATION_EVENT_DETAILS` (`CHR`, `START_POSITION`, `END_POSITION`, `TUMOR_SEQ_ALLELE`(240), `ENTREZ_GENE_ID`, `PROTEIN_CHANGE`, `MUTATION_TYPE`);
UPDATE `info` SET `DB_SCHEMA_VERSION`="2.8.2";

##version: 2.9.0
CREATE TABLE `data_access_tokens` (
    `TOKEN` varchar(50) NOT NULL,
    `USERNAME` varchar(128) NOT NULL,
    `EXPIRATION` datetime NOT NULL,
    `CREATION` datetime,
    PRIMARY KEY (`TOKEN`),
    FOREIGN KEY (`USERNAME`) REFERENCES `users` (`EMAIL`) ON DELETE CASCADE
);
UPDATE `info` SET `DB_SCHEMA_VERSION`="2.9.0";

-- ========================== new treatment related tables =============================================
##version: 2.9.1
CREATE TABLE `treatment` (
  `ID` INT(11) NOT NULL auto_increment,
  `STABLE_ID` VARCHAR(45) NOT NULL UNIQUE,
  `NAME` VARCHAR(45) NOT NULL,
  `DESCRIPTION` VARCHAR(200) NOT NULL,
  `LINKOUT_URL` VARCHAR(400) NOT NULL,
  `GENETIC_ENTITY_ID` INT NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `TREATMENT_GENETIC_ENTITY_ID_UNIQUE` (`GENETIC_ENTITY_ID` ASC),
  FOREIGN KEY (`GENETIC_ENTITY_ID`) REFERENCES `genetic_entity` (`ID`) ON DELETE CASCADE
);

-- --------------------------------------------------------
ALTER TABLE `genetic_profile` ADD COLUMN `PIVOT_THRESHOLD` FLOAT DEFAULT NULL;
ALTER TABLE `genetic_profile` ADD COLUMN `SORT_ORDER` ENUM('ASC','DESC') DEFAULT NULL;

UPDATE `info` SET `DB_SCHEMA_VERSION`="2.9.1";

-- ========================== end of treatment related tables =============================================
##version: 2.9.2
-- Previous structural_variant was never used, so recreate it
DROP TABLE IF EXISTS structural_variant;
CREATE TABLE `structural_variant` (
  `INTERNAL_ID` int(11) NOT NULL auto_increment,
  `GENETIC_PROFILE_ID` int(11) NOT NULL,
  `SAMPLE_ID` int(11) NOT NULL,
  `SITE1_ENTREZ_GENE_ID` int(11) NOT NULL,
  `SITE1_ENSEMBL_TRANSCRIPT_ID` varchar(25),
  `SITE1_EXON` int(4),
  `SITE1_CHROMOSOME` varchar(5),
  `SITE1_POSITION` int(11),
  `SITE1_DESCRIPTION` varchar(255),
  `SITE2_ENTREZ_GENE_ID` int(11),
  `SITE2_ENSEMBL_TRANSCRIPT_ID` varchar(25),
  `SITE2_EXON` int(4),
  `SITE2_CHROMOSOME` varchar(5),
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
  `CENTER` varchar(25),
  `CONNECTION_TYPE` varchar(25),
  `EVENT_INFO` varchar(255),
  `CLASS` varchar(25),
  `LENGTH` int(11),
  `COMMENTS` varchar(255),
  `EXTERNAL_ANNOTATION` varchar(80),
  `DRIVER_FILTER` VARCHAR(20),
  `DRIVER_FILTER_ANNOTATION` VARCHAR(80),
  `DRIVER_TIERS_FILTER` VARCHAR(50),
  `DRIVER_TIERS_FILTER_ANNOTATION` VARCHAR(80),
  PRIMARY KEY (`INTERNAL_ID`),
  FOREIGN KEY (`SAMPLE_ID`) REFERENCES `sample` (`INTERNAL_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`SITE1_ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`SITE2_ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`GENETIC_PROFILE_ID`) REFERENCES `genetic_profile` (`GENETIC_PROFILE_ID`) ON DELETE CASCADE
);

UPDATE `info` SET `DB_SCHEMA_VERSION`="2.9.2";

##version: 2.10.0
-- remove gene length, this is stored in genome nexus
ALTER TABLE `gene` DROP COLUMN `length`;

UPDATE `info` SET `DB_SCHEMA_VERSION`="2.10.0";

##version: 2.10.1
ALTER TABLE `copy_number_seg` MODIFY COLUMN `SEG_ID` BIGINT(20);

UPDATE `info` SET `DB_SCHEMA_VERSION`="2.10.1";

##version: 2.11.0
UPDATE `reference_genome` SET `GENOME_SIZE` = 2897310462 WHERE `NAME`='hg19';
UPDATE `reference_genome` SET `GENOME_SIZE` = 3049315783 WHERE `NAME`='hg38';
UPDATE `reference_genome` SET `GENOME_SIZE` = 2652783500 WHERE `NAME`='mm10';
ALTER TABLE `reference_genome_gene` MODIFY COLUMN `CHR` varchar(5);
INSERT INTO reference_genome_gene (ENTREZ_GENE_ID, CYTOBAND, EXONIC_LENGTH, CHR, REFERENCE_GENOME_ID)
SELECT
    ENTREZ_GENE_ID,
    CYTOBAND,
    0,
    SUBSTRING_INDEX(SUBSTRING_INDEX(SUBSTRING_INDEX(gene.CYTOBAND,IF(LOCATE('p', gene.CYTOBAND), 'p', 'q'), 1),'q',1),'cen',1),
    1
FROM `gene`
WHERE NOT EXISTS (SELECT * FROM reference_genome_gene);
ALTER TABLE `gene` DROP COLUMN `CYTOBAND`;
ALTER TABLE `cancer_study` ADD COLUMN `REFERENCE_GENOME_ID` INT(4) DEFAULT 1,
                           ADD CONSTRAINT `FK_REFERENCE_GENOME` FOREIGN KEY (`REFERENCE_GENOME_ID`)
                               REFERENCES `reference_genome`(`REFERENCE_GENOME_ID`) ON DELETE RESTRICT;
UPDATE `cancer_study`
    INNER JOIN `genetic_profile` ON `cancer_study`.CANCER_STUDY_ID = `genetic_profile`.CANCER_STUDY_ID
    INNER JOIN `mutation` ON `mutation`.GENETIC_PROFILE_ID = `genetic_profile`.GENETIC_PROFILE_ID
    INNER JOIN `mutation_event` ON `mutation`.MUTATION_EVENT_ID = `mutation_event`.MUTATION_EVENT_ID
SET `cancer_study`.REFERENCE_GENOME_ID =
CASE
    WHEN `mutation_event`.NCBI_BUILD in ('mm10', 'GRCm38') THEN 3
    WHEN `mutation_event`.NCBI_BUILD in ('38', 'hg38', 'GRCh38') THEN 2
    ELSE 1
END;
UPDATE `info` SET `DB_SCHEMA_VERSION`="2.11.0";

##version: 2.12.0
ALTER TABLE `mutation` ADD COLUMN ANNOTATION_JSON JSON DEFAULT NULL;
-- ========================== new ascn table =============================================
CREATE TABLE `allele_specific_copy_number` (
    `MUTATION_EVENT_ID` int(255) NOT NULL,
    `GENETIC_PROFILE_ID` int(11) NOT NULL,
    `SAMPLE_ID` int(11) NOT NULL,
    `ASCN_INTEGER_COPY_NUMBER` int DEFAULT NULL,
    `ASCN_METHOD` varchar(24) NOT NULL,
    `CCF_M_COPIES_UPPER` float DEFAULT NULL,
    `CCF_M_COPIES` float DEFAULT NULL,
    `CLONAL` boolean DEFAULT NULL,
    `MINOR_COPY_NUMBER` int DEFAULT NULL,
    `MUTANT_COPIES` int DEFAULT NULL,
    `TOTAL_COPY_NUMBER` int DEFAULT NULL,
    UNIQUE KEY `UQ_ASCN_MUTATION_EVENT_ID_GENETIC_PROFILE_ID_SAMPLE_ID` (`MUTATION_EVENT_ID`,`GENETIC_PROFILE_ID`,`SAMPLE_ID`),
    FOREIGN KEY (`MUTATION_EVENT_ID`) REFERENCES `mutation_event` (`MUTATION_EVENT_ID`),
    FOREIGN KEY (`GENETIC_PROFILE_ID`) REFERENCES `genetic_profile` (`GENETIC_PROFILE_ID`) ON DELETE CASCADE,
    FOREIGN KEY (`SAMPLE_ID`) REFERENCES `sample` (`INTERNAL_ID`) ON DELETE CASCADE
);

UPDATE `info` SET `DB_SCHEMA_VERSION`="2.12.0";
-- ========================== end of ascn table =============================================
##version: 2.12.1
-- update genetic_entity table
ALTER TABLE `genetic_entity` ADD COLUMN `STABLE_ID` varchar(45) DEFAULT NULL;
ALTER TABLE `genetic_profile` ADD COLUMN `GENERIC_ASSAY_TYPE` varchar(255) DEFAULT NULL;
ALTER TABLE `genetic_alteration` DROP FOREIGN KEY genetic_alteration_ibfk_2;
ALTER TABLE `genetic_alteration` ADD CONSTRAINT `genetic_alteration_ibfk_2` FOREIGN KEY (`GENETIC_ENTITY_ID`) REFERENCES `genetic_entity` (`ID`) ON DELETE CASCADE;

CREATE TABLE `generic_entity_properties` (
  `ID` INT(11) NOT NULL auto_increment,
  `GENETIC_ENTITY_ID` INT NOT NULL,
  `NAME` varchar(255) NOT NULL,
  `VALUE` varchar(5000) NOT NULL,
  UNIQUE (`GENETIC_ENTITY_ID`, `NAME`),
  PRIMARY KEY (`ID`),
  FOREIGN KEY (`GENETIC_ENTITY_ID`) REFERENCES `genetic_entity` (`ID`) ON DELETE CASCADE
);

UPDATE `info` SET `DB_SCHEMA_VERSION`="2.12.1";

##version: 2.12.2
-- treatment to generic_assay migration
-- insert NAME into generic_entity_properties
INSERT INTO generic_entity_properties (GENETIC_ENTITY_ID, NAME, VALUE)
SELECT
    GENETIC_ENTITY_ID,
    "NAME",
    NAME
FROM `treatment`;
-- insert DESCRIPTION into generic_entity_properties
INSERT INTO generic_entity_properties (GENETIC_ENTITY_ID, NAME, VALUE)
SELECT
    GENETIC_ENTITY_ID,
    "DESCRIPTION",
    DESCRIPTION
FROM `treatment`;
-- insert URL into generic_entity_properties
INSERT INTO generic_entity_properties (GENETIC_ENTITY_ID, NAME, VALUE)
SELECT
    GENETIC_ENTITY_ID,
    "URL",
    LINKOUT_URL
FROM `treatment`;
-- update genetic_entity and genetic_profile
UPDATE genetic_entity INNER JOIN treatment ON genetic_entity.ID = treatment.GENETIC_ENTITY_ID SET genetic_entity.STABLE_ID = treatment.STABLE_ID, genetic_entity.ENTITY_TYPE = "GENERIC_ASSAY";
UPDATE genetic_profile SET GENERIC_ASSAY_TYPE = "TREATMENT_RESPONSE" WHERE genetic_profile.GENETIC_ALTERATION_TYPE = "GENERIC_ASSAY";
-- drop treatment table
DROP TABLE IF EXISTS `treatment`;

UPDATE `info` SET `DB_SCHEMA_VERSION`="2.12.2";

##version: 2.12.3
CREATE TEMPORARY TABLE IF NOT EXISTS
    fusion_studies ( INDEX(CANCER_STUDY_IDENTIFIER) )
AS (
    SELECT DISTINCT CANCER_STUDY_IDENTIFIER, cancer_study.CANCER_STUDY_ID
    FROM `mutation_event`
             JOIN `mutation` ON `mutation`.MUTATION_EVENT_ID = `mutation_event`.MUTATION_EVENT_ID
             JOIN `genetic_profile` ON `genetic_profile`.GENETIC_PROFILE_ID = `mutation`.GENETIC_PROFILE_ID
             JOIN `cancer_study` ON `cancer_study`.CANCER_STUDY_ID = `genetic_profile`.CANCER_STUDY_ID
    WHERE MUTATION_TYPE = 'fusion'
);
INSERT INTO genetic_profile(STABLE_ID, CANCER_STUDY_ID, GENETIC_ALTERATION_TYPE, DATATYPE, NAME, DESCRIPTION, SHOW_PROFILE_IN_ANALYSIS_TAB)
SELECT CONCAT(CANCER_STUDY_IDENTIFIER, '_fusion'), CANCER_STUDY_ID, 'STRUCTURAL_VARIANT','FUSION','Fusions','Fusions',0
FROM `fusion_studies`
WHERE NOT EXISTS (SELECT * FROM genetic_profile WHERE  STABLE_ID=CONCAT(`fusion_studies`.CANCER_STUDY_IDENTIFIER, '_fusion')
    AND CANCER_STUDY_ID = `fusion_studies`.CANCER_STUDY_ID);
DROP TEMPORARY TABLE fusion_studies;
UPDATE `info` SET `DB_SCHEMA_VERSION`="2.12.3";

##version: 2.12.4
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

CREATE TABLE `resource_sample` (
  `INTERNAL_ID` int(11) NOT NULL,
  `RESOURCE_ID` varchar(255) NOT NULL,
  `URL` varchar(255) NOT NULL,
  PRIMARY KEY (`INTERNAL_ID`, `RESOURCE_ID`, `URL`),
  FOREIGN KEY (`INTERNAL_ID`) REFERENCES `sample` (`INTERNAL_ID`) ON DELETE CASCADE
);

CREATE TABLE `resource_patient` (
  `INTERNAL_ID` int(11) NOT NULL,
  `RESOURCE_ID` varchar(255) NOT NULL,
  `URL` varchar(255) NOT NULL,
  PRIMARY KEY (`INTERNAL_ID`, `RESOURCE_ID`, `URL`),
  FOREIGN KEY (`INTERNAL_ID`) REFERENCES `patient` (`INTERNAL_ID`) ON DELETE CASCADE
);

CREATE TABLE `resource_study` (
  `INTERNAL_ID` int(11) NOT NULL,
  `RESOURCE_ID` varchar(255) NOT NULL,
  `URL` varchar(255) NOT NULL,
  PRIMARY KEY (`INTERNAL_ID`, `RESOURCE_ID`, `URL`),
  FOREIGN KEY (`INTERNAL_ID`) REFERENCES `cancer_study` (`CANCER_STUDY_ID`) ON DELETE CASCADE
);
UPDATE `info` SET `DB_SCHEMA_VERSION`="2.12.4";