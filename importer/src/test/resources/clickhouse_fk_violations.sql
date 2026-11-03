-- Foreign-key violation fixtures for ClickHouseConstraintChecker

INSERT INTO cancer_study (cancer_study_id, type_of_cancer_id, reference_genome_id) VALUES
(910002, 'MISSING_TC', 900001);

INSERT INTO cancer_study_tags (cancer_study_id, tags) VALUES
(900002, 'missing_tag');

INSERT INTO patient (internal_id, cancer_study_id) VALUES
(910003, 900002);

INSERT INTO sample (internal_id, patient_id) VALUES
(910004, 900003);

INSERT INTO sample_list (list_id, cancer_study_id) VALUES
(910500, 900002);

INSERT INTO sample_list_list (list_id, sample_id) VALUES
(910500, 900004);

INSERT INTO gene (entrez_gene_id, genetic_entity_id) VALUES
(910005, 900006);

INSERT INTO gene_alias (entrez_gene_id, gene_alias) VALUES
(900005, 'missing_alias');

INSERT INTO geneset (id, genetic_entity_id, name) VALUES
(910007, 900006, 'missing_geneset');

INSERT INTO geneset_gene (geneset_id, entrez_gene_id) VALUES
(900007, 900005);

INSERT INTO geneset_hierarchy_leaf (node_id, geneset_id) VALUES
(900008, 900007);

INSERT INTO generic_entity_properties (id, genetic_entity_id, name) VALUES
(910600, 900006, 'missing_prop');

INSERT INTO genetic_profile (genetic_profile_id, cancer_study_id) VALUES
(910009, 900002);

INSERT INTO genetic_profile_link (referring_genetic_profile_id, referred_genetic_profile_id) VALUES
(900009, 900010);

INSERT INTO genetic_alteration (genetic_profile_id, genetic_entity_id, `values`) VALUES
(900009, 900006, 'missing');

INSERT INTO genetic_profile_samples (genetic_profile_id, ordered_sample_list) VALUES
(900009, 'missing_samples');

INSERT INTO gene_panel_list (internal_id, gene_id) VALUES
(900010, 900005);

INSERT INTO sample_profile (sample_id, genetic_profile_id, panel_id) VALUES
(900004, 900009, 900010);

INSERT INTO structural_variant (internal_id, genetic_profile_id, sample_id, site1_entrez_gene_id, site2_entrez_gene_id) VALUES
(910700, 900009, 900004, 900005, 900005);

INSERT INTO alteration_driver_annotation (alteration_event_id, genetic_profile_id, sample_id) VALUES
(910800, 900009, 900004);

INSERT INTO mutation_event (mutation_event_id, entrez_gene_id) VALUES
(910011, 900005);

INSERT INTO mutation (mutation_event_id, genetic_profile_id, sample_id, entrez_gene_id) VALUES
(900011, 900009, 900004, 900005);

INSERT INTO mutation_count_by_keyword (genetic_profile_id, `keyword`, entrez_gene_id) VALUES
(900009, 'missing_keyword', 900005);

INSERT INTO clinical_patient (internal_id, attr_id, attr_value) VALUES
(900003, 'ATTR1', 'VAL1');

INSERT INTO clinical_sample (internal_id, attr_id, attr_value) VALUES
(900004, 'ATTR1', 'VAL1');

INSERT INTO clinical_attribute_meta (attr_id, cancer_study_id) VALUES
('ATTRX', 900002);

INSERT INTO mut_sig (cancer_study_id, entrez_gene_id) VALUES
(900002, 900005);

INSERT INTO gistic (gistic_roi_id, cancer_study_id) VALUES
(910012, 900002);

INSERT INTO gistic_to_gene (gistic_roi_id, entrez_gene_id) VALUES
(900012, 900005);

INSERT INTO cna_event (cna_event_id, entrez_gene_id) VALUES
(910013, 900005);

INSERT INTO sample_cna_event (cna_event_id, sample_id, genetic_profile_id) VALUES
(900013, 900004, 900009);

INSERT INTO copy_number_seg (seg_id, cancer_study_id, sample_id) VALUES
(910900, 900002, 900004);

INSERT INTO copy_number_seg_file (seg_file_id, cancer_study_id) VALUES
(910901, 900002);

INSERT INTO clinical_event (clinical_event_id, patient_id, event_type) VALUES
(910014, 900003, 'MISSING');

INSERT INTO clinical_event_data (clinical_event_id, `key`, `value`) VALUES
(900014, 'missing_key', 'missing_value');

INSERT INTO reference_genome_gene (reference_genome_id, entrez_gene_id) VALUES
(900001, 900005);

INSERT INTO data_access_tokens (token, username) VALUES
('missing_token', 'missing_user@example.org');

INSERT INTO allele_specific_copy_number (mutation_event_id, genetic_profile_id, sample_id) VALUES
(900011, 900009, 900004);

INSERT INTO resource_definition (resource_id, cancer_study_id) VALUES
('res_missing', 900002);

-- Rows are checked only where every key column is non-NULL, so each row below breaks the keys
-- that apply to its scope:
--   STUDY  - cancer_study 900002 does not exist; patient_id and sample_id are NULL.
--   PATIENT - no patient has this stable id in that study.
--   SAMPLE - no sample has this stable id in that study.
INSERT INTO resource (internal_id, resource_id, cancer_study_id, resource_scope, patient_id, sample_id, url, display_name, metadata) VALUES
(900020, 'res_missing', 900002, 'STUDY', NULL, NULL, 'http://missing', NULL, NULL),
(900021, 'res_missing', 900002, 'PATIENT', 'patient_missing', NULL, 'http://missing', NULL, NULL),
(900022, 'res_missing', 900002, 'SAMPLE', 'patient_missing', 'sample_missing', 'http://missing', NULL, NULL);
