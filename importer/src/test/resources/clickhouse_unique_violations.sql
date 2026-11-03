-- Unique-key violation fixtures for ClickHouseConstraintChecker

-- type_of_cancer
INSERT INTO type_of_cancer (type_of_cancer_id, name) VALUES
('UK_TC_1', 'Type A'),
('UK_TC_1', 'Type B');

-- reference_genome (reference_genome_id)
INSERT INTO reference_genome (reference_genome_id, build_name, name) VALUES
(700001, 'BUILD_A1', 'RG_A1'),
(700001, 'BUILD_A2', 'RG_A2');

-- reference_genome (build_name)
INSERT INTO reference_genome (reference_genome_id, build_name, name) VALUES
(700002, 'BUILD_DUP', 'RG_B1'),
(700003, 'BUILD_DUP', 'RG_B2');

-- cancer_study (cancer_study_id)
INSERT INTO cancer_study (cancer_study_id, cancer_study_identifier, name) VALUES
(700010, 'STUDY_A', 'Study A'),
(700010, 'STUDY_B', 'Study B');

-- cancer_study (cancer_study_identifier)
INSERT INTO cancer_study (cancer_study_id, cancer_study_identifier, name) VALUES
(700011, 'STUDY_DUP', 'Study C'),
(700012, 'STUDY_DUP', 'Study D');

-- cancer_study_tags
INSERT INTO cancer_study_tags (cancer_study_id, tags) VALUES
(700020, 'tag1'),
(700020, 'tag2');

-- users
INSERT INTO users (email, name, enabled) VALUES
('uk_user@example.org', 'User A', 1),
('uk_user@example.org', 'User B', 1);

-- patient
INSERT INTO patient (internal_id, stable_id, cancer_study_id) VALUES
(700030, 'PAT_A', 700010),
(700030, 'PAT_B', 700011);

-- sample
INSERT INTO sample (internal_id, stable_id, patient_id) VALUES
(700040, 'SAMP_A', 700030),
(700040, 'SAMP_B', 700031);

-- sample_list (list_id)
INSERT INTO sample_list (list_id, stable_id, cancer_study_id) VALUES
(700050, 'SL_A', 700010),
(700050, 'SL_B', 700011);

-- sample_list (stable_id)
INSERT INTO sample_list (list_id, stable_id, cancer_study_id) VALUES
(700051, 'SL_DUP', 700010),
(700052, 'SL_DUP', 700010);

-- sample_list_list
INSERT INTO sample_list_list (list_id, sample_id) VALUES
(700060, 700040),
(700060, 700040);

-- genetic_entity
INSERT INTO genetic_entity (id, entity_type, stable_id) VALUES
(700070, 'GENE', 'GE_A'),
(700070, 'GENE', 'GE_B');

-- gene (entrez_gene_id)
INSERT INTO gene (entrez_gene_id, hugo_gene_symbol, genetic_entity_id) VALUES
(700080, 'GENE_A', 700070),
(700080, 'GENE_B', 700071);

-- gene (genetic_entity_id)
INSERT INTO gene (entrez_gene_id, hugo_gene_symbol, genetic_entity_id) VALUES
(700081, 'GENE_C', 700072),
(700082, 'GENE_D', 700072);

-- gene_alias
INSERT INTO gene_alias (entrez_gene_id, gene_alias) VALUES
(700080, 'ALIAS_DUP'),
(700080, 'ALIAS_DUP');

-- geneset (id)
INSERT INTO geneset (id, genetic_entity_id, name, external_id) VALUES
(700090, 700073, 'GS_ID_A', 'GS_ID_A_EXT'),
(700090, 700074, 'GS_ID_B', 'GS_ID_B_EXT');

-- geneset (name)
INSERT INTO geneset (id, genetic_entity_id, name, external_id) VALUES
(700091, 700075, 'GS_DUP_NAME', 'GS_NAME_A_EXT'),
(700092, 700076, 'GS_DUP_NAME', 'GS_NAME_B_EXT');

-- geneset (external_id)
INSERT INTO geneset (id, genetic_entity_id, name, external_id) VALUES
(700093, 700077, 'GS_EXT_A', 'GS_DUP_EXT'),
(700094, 700078, 'GS_EXT_B', 'GS_DUP_EXT');

-- geneset (genetic_entity_id)
INSERT INTO geneset (id, genetic_entity_id, name, external_id) VALUES
(700095, 700079, 'GS_GE_A', 'GS_GE_A_EXT'),
(700096, 700079, 'GS_GE_B', 'GS_GE_B_EXT');

-- geneset_gene
INSERT INTO geneset_gene (geneset_id, entrez_gene_id) VALUES
(700090, 700080),
(700090, 700080);

-- geneset_hierarchy_node (node_id)
INSERT INTO geneset_hierarchy_node (node_id, node_name, parent_id) VALUES
(700100, 'NODE_A', 1),
(700100, 'NODE_B', 2);

-- geneset_hierarchy_node (node_name, parent_id)
INSERT INTO geneset_hierarchy_node (node_id, node_name, parent_id) VALUES
(700101, 'NODE_DUP', 700200),
(700102, 'NODE_DUP', 700200);

-- geneset_hierarchy_leaf
INSERT INTO geneset_hierarchy_leaf (node_id, geneset_id) VALUES
(700100, 700090),
(700100, 700090);

-- generic_entity_properties (id)
INSERT INTO generic_entity_properties (id, genetic_entity_id, name, value) VALUES
(700110, 700070, 'PROP_A', 'VAL_A'),
(700110, 700071, 'PROP_B', 'VAL_B');

-- generic_entity_properties (genetic_entity_id, name)
INSERT INTO generic_entity_properties (id, genetic_entity_id, name, value) VALUES
(700111, 700072, 'PROP_DUP', 'VAL_C'),
(700112, 700072, 'PROP_DUP', 'VAL_D');

-- genetic_profile (genetic_profile_id)
INSERT INTO genetic_profile (genetic_profile_id, stable_id, cancer_study_id, name) VALUES
(700120, 'GP_A', 700010, 'GP A'),
(700120, 'GP_B', 700011, 'GP B');

-- genetic_profile (stable_id)
INSERT INTO genetic_profile (genetic_profile_id, stable_id, cancer_study_id, name) VALUES
(700121, 'GP_DUP', 700010, 'GP C'),
(700122, 'GP_DUP', 700011, 'GP D');

-- genetic_profile_link
INSERT INTO genetic_profile_link (referring_genetic_profile_id, referred_genetic_profile_id) VALUES
(700120, 700121),
(700120, 700121);

-- genetic_alteration
INSERT INTO genetic_alteration (genetic_profile_id, genetic_entity_id, `values`) VALUES
(700120, 700070, 'A');
INSERT INTO genetic_alteration (genetic_profile_id, genetic_entity_id, `values`) VALUES
(700120, 700070, 'B');

-- genetic_profile_samples
INSERT INTO genetic_profile_samples (genetic_profile_id, ordered_sample_list) VALUES
(700120, 'S1');
INSERT INTO genetic_profile_samples (genetic_profile_id, ordered_sample_list) VALUES
(700120, 'S2');

-- gene_panel (internal_id)
INSERT INTO gene_panel (internal_id, stable_id, description) VALUES
(700130, 'PANEL_A', 'Panel A'),
(700130, 'PANEL_B', 'Panel B');

-- gene_panel (stable_id)
INSERT INTO gene_panel (internal_id, stable_id, description) VALUES
(700131, 'PANEL_DUP', 'Panel C'),
(700132, 'PANEL_DUP', 'Panel D');

-- gene_panel_list
INSERT INTO gene_panel_list (internal_id, gene_id) VALUES
(700130, 700080),
(700130, 700080);

-- sample_profile
INSERT INTO sample_profile (sample_id, genetic_profile_id, panel_id) VALUES
(700040, 700120, 700130);
INSERT INTO sample_profile (sample_id, genetic_profile_id, panel_id) VALUES
(700040, 700120, 700131);

-- structural_variant
INSERT INTO structural_variant (internal_id, genetic_profile_id, sample_id) VALUES
(700140, 700120, 700040),
(700140, 700121, 700041);

-- alteration_driver_annotation
INSERT INTO alteration_driver_annotation (alteration_event_id, genetic_profile_id, sample_id) VALUES
(700150, 700120, 700040),
(700150, 700120, 700040);

-- mutation_event
INSERT INTO mutation_event (mutation_event_id, entrez_gene_id) VALUES
(700160, 700080),
(700160, 700081);

-- mutation
INSERT INTO mutation (mutation_event_id, genetic_profile_id, sample_id, entrez_gene_id) VALUES
(700160, 700120, 700040, 700080),
(700160, 700120, 700040, 700081);

-- clinical_patient
INSERT INTO clinical_patient (internal_id, attr_id, attr_value) VALUES
(700170, 'ATTR_DUP', 'V1');
INSERT INTO clinical_patient (internal_id, attr_id, attr_value) VALUES
(700170, 'ATTR_DUP', 'V2');

-- clinical_sample
INSERT INTO clinical_sample (internal_id, attr_id, attr_value) VALUES
(700180, 'ATTR_DUP', 'V1');
INSERT INTO clinical_sample (internal_id, attr_id, attr_value) VALUES
(700180, 'ATTR_DUP', 'V2');

-- clinical_attribute_meta
INSERT INTO clinical_attribute_meta (attr_id, cancer_study_id, display_name) VALUES
('ATTR_META_DUP', 700010, 'Attr A'),
('ATTR_META_DUP', 700010, 'Attr B');

-- mut_sig
INSERT INTO mut_sig (cancer_study_id, entrez_gene_id, rank) VALUES
(700010, 700080, 1),
(700010, 700080, 2);

-- gistic
INSERT INTO gistic (gistic_roi_id, cancer_study_id) VALUES
(700190, 700010),
(700190, 700011);

-- gistic_to_gene
INSERT INTO gistic_to_gene (gistic_roi_id, entrez_gene_id) VALUES
(700190, 700080),
(700190, 700080);

-- cna_event (cna_event_id)
INSERT INTO cna_event (cna_event_id, entrez_gene_id, alteration) VALUES
(700200, 700080, 1),
(700200, 700081, 2);

-- cna_event (entrez_gene_id, alteration)
INSERT INTO cna_event (cna_event_id, entrez_gene_id, alteration) VALUES
(700201, 700082, 3),
(700202, 700082, 3);

-- sample_cna_event
INSERT INTO sample_cna_event (cna_event_id, sample_id, genetic_profile_id) VALUES
(700200, 700040, 700120),
(700200, 700040, 700120);

-- copy_number_seg
INSERT INTO copy_number_seg (seg_id, cancer_study_id, sample_id) VALUES
(700210, 700010, 700040),
(700210, 700011, 700041);

-- copy_number_seg_file
INSERT INTO copy_number_seg_file (seg_file_id, cancer_study_id) VALUES
(700220, 700010),
(700220, 700011);

-- clinical_event
INSERT INTO clinical_event (clinical_event_id, patient_id, event_type) VALUES
(700230, 700170, 'EVT'),
(700230, 700171, 'EVT');

-- reference_genome_gene
INSERT INTO reference_genome_gene (entrez_gene_id, reference_genome_id) VALUES
(700080, 700001),
(700080, 700001);

-- data_access_tokens
INSERT INTO data_access_tokens (token, username) VALUES
('TOKEN_DUP', 'user1'),
('TOKEN_DUP', 'user2');

-- resource_definition
INSERT INTO resource_definition (resource_id, cancer_study_id) VALUES
('RES_DEF_DUP', 700010),
('RES_DEF_DUP', 700010);

-- resource: two rows sharing one internal_id, which deletions select by.
INSERT INTO resource (internal_id, resource_id, cancer_study_id, resource_scope, patient_id, sample_id, url, display_name, metadata) VALUES
(700500, 'RES_DATA_DUP', 700010, 'STUDY', NULL, NULL, 'http://study', NULL, NULL),
(700500, 'RES_DATA_DUP', 700010, 'STUDY', NULL, NULL, 'http://study', NULL, NULL);

-- allele_specific_copy_number
INSERT INTO allele_specific_copy_number (mutation_event_id, genetic_profile_id, sample_id) VALUES
(700160, 700120, 700040),
(700160, 700120, 700040);
