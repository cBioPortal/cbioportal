CREATE TABLE sample_list LIKE patient_list;
INSERT sample_list SELECT * FROM patient_list;
CREATE TABLE sample_list_list LIKE patient_list_list;
INSERT sample_list_list SELECT * FROM patient_list_list;
DROP TABLE micro_rna;
DROP TABLE micro_rna_alteration;
DROP TABLE mutation_frequency;
ALTER TABLE sample_list_list CHANGE PATIENT_ID SAMPLE_ID INT(11);
