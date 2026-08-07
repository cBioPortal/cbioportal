-- Portal catalog identities used to validate the normalized WSI references.
INSERT INTO cancer_study (
  cancer_study_id, cancer_study_identifier, type_of_cancer_id, name,
  description, public, groups
) VALUES
  (990001, 'msk_spectrum_tme_2022', 'dummy', 'WSI CI study A',
   'Authenticated WSI CI fixture', 1, 'WSI_CI_A'),
  (990002, 'wsi_ci_study_b', 'dummy', 'WSI CI study B',
   'Authenticated WSI CI fixture', 0, 'WSI_CI_B');

INSERT INTO patient (internal_id, stable_id, cancer_study_id) VALUES
  (990001, 'P-0055908', 990001),
  (990002, 'WSI-CI-B-PATIENT', 990002);

INSERT INTO sample (internal_id, stable_id, sample_type, patient_id) VALUES
  (990001, 'P-0055908-T01-IM6', 'Primary', 990001),
  (990002, 'WSI-CI-B-SAMPLE', 'Primary', 990002);

-- Release 1 for the MSK fixture, including one explicitly unmatched slide.
INSERT INTO wsi_release
  (cancer_study_id, release_id, release_version, released_at)
VALUES (990001, 'wsi-ci-release-a-1', 1, now());
INSERT INTO wsi_release_patient
  (cancer_study_id, patient_id, release_id, reference_sample_id)
VALUES (990001, 990001, 'wsi-ci-release-a-1', 990001);
INSERT INTO wsi_part
  (cancer_study_id, patient_id, release_id, part_key,
   part_number, part_designator, part_type, part_description, subspecialty,
   path_dx_title)
VALUES
  (990001, 990001, 'wsi-ci-release-a-1', '27', '27', '27',
   'FALLOPIAN TUBE AND OVARY', 'Right fallopian tube and ovary', '',
   'Right fallopian tube and ovary'),
  (990001, 990001, 'wsi-ci-release-a-1', '34', '34', '34',
   'SMALL BOWEL', 'Portion of small bowel with tumor', '',
   'Portion of small bowel with tumor');
INSERT INTO wsi_block
  (cancer_study_id, patient_id, release_id, part_key,
   block_key, block_number, block_label)
VALUES
  (990001, 990001, 'wsi-ci-release-a-1', '27', '4', '4', '4RO'),
  (990001, 990001, 'wsi-ci-release-a-1', '34', '4', '4', '4RS');
INSERT INTO wsi_slide
  (cancer_study_id, patient_id, release_id, image_id,
   stain_name, stain_group, is_hne, is_ihc, magnification, file_size_bytes,
   can_serve_tiles, barcode, slide_type)
VALUES
  (990001, 990001, 'wsi-ci-release-a-1', '3020726', 'H&E, Initial',
   'H&E (Initial)', true, false, '20x', 716956681, true, '', 'H&E'),
  (990001, 990001, 'wsi-ci-release-a-1', '3020648', 'H&E, Initial',
   'H&E (Initial)', true, false, '20x', 1014457317, false, '', 'H&E');
INSERT INTO wsi_slide_placement
  (cancer_study_id, patient_id, release_id, image_id,
   part_key, block_key, sample_id, match_level, specimen_key,
   procedure_date_days, timepoint_source)
VALUES
  (990001, 990001, 'wsi-ci-release-a-1', '3020726', '27', '4',
   990001, 'BLOCK', 'block::27::4', -17,
   'Procedure date relative to tumor sequencing'),
  (990001, 990001, 'wsi-ci-release-a-1', '3020648', '34', '4',
   NULL, 'UNMATCHED', 'unmatched::34::4', -17,
   'Procedure date relative to tumor sequencing');

-- A second study verifies study-scoped tile/index authorization in E2E tests.
INSERT INTO wsi_release
  (cancer_study_id, release_id, release_version, released_at)
VALUES (990002, 'wsi-ci-release-b-1', 1, now());
INSERT INTO wsi_release_patient
  (cancer_study_id, patient_id, release_id, reference_sample_id)
VALUES (990002, 990002, 'wsi-ci-release-b-1', 990002);
INSERT INTO wsi_part
  (cancer_study_id, patient_id, release_id, part_key,
   part_number, part_designator, part_type, part_description, subspecialty,
   path_dx_title)
VALUES (990002, 990002, 'wsi-ci-release-b-1', '1', '1', '1',
        'SPECIMEN', 'Fixture specimen', '', 'Fixture specimen');
INSERT INTO wsi_block
  (cancer_study_id, patient_id, release_id, part_key,
   block_key, block_number, block_label)
VALUES (990002, 990002, 'wsi-ci-release-b-1', '1', '1', '1', '1');
INSERT INTO wsi_slide
  (cancer_study_id, patient_id, release_id, image_id,
   stain_name, stain_group, is_hne, is_ihc, magnification, file_size_bytes,
   can_serve_tiles, barcode, slide_type)
VALUES (990002, 990002, 'wsi-ci-release-b-1', '4020726', NULL, NULL,
        true, false, NULL, NULL, true, NULL, 'H&E');
INSERT INTO wsi_slide_placement
  (cancer_study_id, patient_id, release_id, image_id,
   part_key, block_key, sample_id, match_level, specimen_key,
   procedure_date_days, timepoint_source)
VALUES (990002, 990002, 'wsi-ci-release-b-1', '4020726', '1', '1',
        990002, 'BLOCK', 'block::1::1', NULL, NULL);
