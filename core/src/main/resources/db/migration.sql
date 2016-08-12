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

##version: 1.2.2
CREATE TABLE `sv`(
        `SAMPLE_ID` varchar(25) NOT NULL,
        `INTERNAL_ID` int(255) NOT NULL auto_increment,
        `SV_VARIANT_ID` int(255) NOT NULL,
        `BREAKPOINT_TYPE` varchar(25),
        `ANNOTATION` varchar(255),
        `COMMENTS` varchar(2048),
        `CONFIDENCE_CLASS` varchar(25),
        `CONN_TYPE` varchar(25),
        `CONNECTION_TYPE` varchar(25),
        `EVENT_INFO` varchar(255),
        `MAPQ` int(11),
        `NORMAL_READ_COUNT` int(255),
        `NORMAL_VARIANT_COUNT` int(255),
        `PAIRED_END_READ_SUPPORT` varchar(255),
        `SITE1_CHROM` varchar(25),
        `SITE1_DESC` varchar(255),
        `SITE1_GENE` varchar(25),
        `SITE1_POS` int(255),
        `SITE2_CHROM` varchar(25),
        `SITE2_DESC` varchar(255),
        `SITE2_GENE` varchar(25),
        `SITE2_POS` int(255),
        `SPLIT_READ_SUPPORT` varchar(255),
        `SV_CLASS_NAME` varchar(25),
        `SV_DESC` varchar(255),
        `SV_LENGTH` int(255),
        `TUMOR_READ_COUNT` int(255),
        `TUMOR_VARIANT_COUNT` int(255),
        `VARIANT_STATUS_NAME` varchar(255),
        `GENETIC_PROFILE_ID` varchar(255) NOT NULL,
        PRIMARY KEY (`SV_VARIANT_ID`),
        FOREIGN KEY (`SAMPLE_ID`) REFERENCES `sample` (`STABLE_ID`) ON DELETE CASCADE,
        FOREIGN KEY (`SITE1_GENE`) REFERENCES `gene` (`HUGO_GENE_SYMBOL`) ON DELETE CASCADE,
        FOREIGN KEY (`SITE2_GENE`) REFERENCES `gene` (`HUGO_GENE_SYMBOL`) ON DELETE CASCADE,
        FOREIGN KEY (`GENETIC_PROFILE_ID`) REFERENCES `genetic_profile` (`STABLE_ID`) ON DELETE CASCADE,
        UNIQUE (`INTERNAL_ID`)
);
UPDATE info SET DB_SCEHMA_VERSION="1.2.2";
