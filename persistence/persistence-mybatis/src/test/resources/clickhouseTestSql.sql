INSERT INTO patient_clinical_attribute_numeric (patient_unique_id, attribute_name, attribute_value, cancer_study_identifier) VALUES ('msk_ch_2020_P-0000004', 'AGE', 39.739902, 'msk_ch_2020');
INSERT INTO patient_clinical_attribute_numeric (patient_unique_id, attribute_name, attribute_value, cancer_study_identifier) VALUES ('msk_ch_2020_P-0000004', 'TIME_FROM_DX_TO_SEQ', 991, 'msk_ch_2020');
INSERT INTO patient_clinical_attribute_numeric (patient_unique_id, attribute_name, attribute_value, cancer_study_identifier) VALUES ('msk_ch_2020_P-0000004', 'TIME_TO_BLOOD_DRAW_FROM_TX', 609, 'msk_ch_2020');
INSERT INTO patient_clinical_attribute_numeric (patient_unique_id, attribute_name, attribute_value, cancer_study_identifier) VALUES ('msk_ch_2020_P-0000015', 'AGE', 44.440792, 'msk_ch_2020');
INSERT INTO patient_clinical_attribute_numeric (patient_unique_id, attribute_name, attribute_value, cancer_study_identifier) VALUES ('msk_ch_2020_P-0000015', 'TIME_FROM_DX_TO_SEQ', 2558, 'msk_ch_2020');
INSERT INTO patient_clinical_attribute_numeric (patient_unique_id, attribute_name, attribute_value, cancer_study_identifier) VALUES ('msk_ch_2020_P-0000015', 'TIME_TO_BLOOD_DRAW_FROM_TX', 5, 'msk_ch_2020');
INSERT INTO patient_clinical_attribute_numeric (patient_unique_id, attribute_name, attribute_value, cancer_study_identifier) VALUES ('msk_ch_2020_P-0000023', 'AGE', 61.319645, 'msk_ch_2020');
INSERT INTO patient_clinical_attribute_numeric (patient_unique_id, attribute_name, attribute_value, cancer_study_identifier) VALUES ('msk_ch_2020_P-0000023', 'TIME_FROM_DX_TO_SEQ', 245, 'msk_ch_2020');
INSERT INTO patient_clinical_attribute_numeric (patient_unique_id, attribute_name, attribute_value, cancer_study_identifier) VALUES ('msk_ch_2020_P-0000023', 'TIME_TO_BLOOD_DRAW_FROM_TX', 166, 'msk_ch_2020');

INSERT INTO sample (sample_unique_id, sample_unique_id_base64, sample_stable_id, patient_unique_id, patient_unique_id_base64, patient_stable_id, cancer_study_identifier) VALUES ('msk_ch_2020_P-0000004-N01', '', 'P-0000004-N01', 'msk_ch_2020_P-0000004', '', 'P-0000004', 'msk_ch_2020');
INSERT INTO sample (sample_unique_id, sample_unique_id_base64, sample_stable_id, patient_unique_id, patient_unique_id_base64, patient_stable_id, cancer_study_identifier) VALUES ('msk_ch_2020_P-0000015-N01', '', 'P-0000015-N01', 'msk_ch_2020_P-0000015', '', 'P-0000015', 'msk_ch_2020');
INSERT INTO sample (sample_unique_id, sample_unique_id_base64, sample_stable_id, patient_unique_id, patient_unique_id_base64, patient_stable_id, cancer_study_identifier) VALUES ('msk_ch_2020_P-0000023-N01', '', 'P-0000023-N01', 'msk_ch_2020_P-0000023', '', 'P-0000023', 'msk_ch_2020');
-- 
INSERT INTO genomic_event (sample_unique_id, variant, hugo_gene_symbol, gene_panel_stable_id, cancer_study_identifier, genetic_profile_stable_id) VALUES ('msk_ch_2020_P-0000004-N01', 'p.R1051Q', 'KDR', '', 'msk_ch_2020', 'msk_ch_2020_mutations');
INSERT INTO genomic_event (sample_unique_id, variant, hugo_gene_symbol, gene_panel_stable_id, cancer_study_identifier, genetic_profile_stable_id) VALUES ('msk_ch_2020_P-0000004-N01', 'p.T1884I', 'TET2', '', 'msk_ch_2020', 'msk_ch_2020_mutations');

INSERT INTO mutation (sample_unique_id, variant, hugo_gene_symbol, gene_panel_stable_id, cancer_study_identifier, genetic_profile_stable_id) VALUES ('msk_ch_2020_P-0000004-N01', 'p.R1051Q', 'KDR', '', 'msk_ch_2020', 'msk_ch_2020_mutations');
INSERT INTO mutation (sample_unique_id, variant, hugo_gene_symbol, gene_panel_stable_id, cancer_study_identifier, genetic_profile_stable_id) VALUES ('msk_ch_2020_P-0000004-N01', 'p.T1884I', 'TET2', '', 'msk_ch_2020', 'msk_ch_2020_mutations');

