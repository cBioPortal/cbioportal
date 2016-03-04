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
