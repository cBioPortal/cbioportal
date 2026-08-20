-- Portal catalog identities used to validate the normalized WSI references.
INSERT INTO cancer_study (
  cancer_study_id, cancer_study_identifier, type_of_cancer_id, name,
  description, public, groups, status, import_date, reference_genome_id
) VALUES
  (990001, 'msk_spectrum_tme_2022', 'mixed', 'SPECTRUM TME public WSI fixture',
   'Public SPECTRUM WSI fixture', 1, 'PUBLIC', 1, now(), 2),
  (990002, 'wsi_ci_study_b', 'mixed', 'WSI CI study B',
   'Authenticated WSI CI control', 1, 'PUBLIC', 1, now(), 2);

INSERT INTO patient (internal_id, stable_id, cancer_study_id) VALUES
  (990001, 'P-0055908', 990001),
  (990002, 'WSI-CI-B-PATIENT', 990002);

INSERT INTO sample (internal_id, stable_id, sample_type, patient_id) VALUES
  (990001, 'P-0055908-T01-IM6', 'Primary', 990001),
  (990002, 'WSI-CI-B-SAMPLE', 'Primary', 990002);

-- Minimal public sample-list membership makes the fixture discoverable through
-- the normal study/patient API in addition to the WSI hierarchy endpoint.
INSERT INTO sample_list
  (list_id, stable_id, category, cancer_study_id, name, description)
VALUES (990001, 'msk_spectrum_tme_2022_all', 'all', 990001,
        'All SPECTRUM fixture samples', 'Public WSI fixture sample list');
INSERT INTO sample_list_list (list_id, sample_id) VALUES (990001, 990001);

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
  (990001, 990001, 'wsi-ci-release-a-1', '27', '62', '62', '1 RFIM'),
  (990001, 990001, 'wsi-ci-release-a-1', '34', '4', '4', '4RS');
INSERT INTO wsi_slide
  (cancer_study_id, patient_id, release_id, image_id,
   stain_name, stain_group, is_hne, is_ihc, magnification, file_size_bytes,
   can_serve_tiles, barcode, slide_type, source_url, tile_metadata_json,
   thumbnail_url, thumbnail_width, thumbnail_height, thumbnail_content_type)
VALUES
  (990001, 990001, 'wsi-ci-release-a-1', '3020726', 'H&E, Initial',
   'H&E (Initial)', true, false, '20x', 716956681, true, '', 'H&E',
   'file:///app/testdata/CMU-1-Small-Region.svs',
   '{"dimensions":{"width":2220,"height":2967},"levels":1,"level_dimensions":[{"width":2220,"height":2967}],"level_downsamples":[1.0],"max_zoom":4,"tile_size":256,"mpp":{"x":0.499,"y":0.499},"objective_power":20,"vendor":"aperio"}',
   'file:///app/testdata/3020691.jpg', 256, 232, 'image/jpeg'),
  (990001, 990001, 'wsi-ci-release-a-1', '3020691', 'H&E, Initial',
   'H&E (Initial)', true, false, '20x', 538183815, true, '', 'H&E',
   'file:///app/testdata/CMU-1-Small-Region.svs',
   '{"dimensions":{"width":2220,"height":2967},"levels":1,"level_dimensions":[{"width":2220,"height":2967}],"level_downsamples":[1.0],"max_zoom":4,"tile_size":256,"mpp":{"x":0.499,"y":0.499},"objective_power":20,"vendor":"aperio"}',
   'file:///app/testdata/3020691.jpg', 256, 232, 'image/jpeg'),
  (990001, 990001, 'wsi-ci-release-a-1', '3020648', 'H&E, Initial',
   'H&E (Initial)', true, false, '20x', 1014457317, false, '', 'H&E',
   NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO wsi_slide_placement
  (cancer_study_id, patient_id, release_id, image_id,
   part_key, block_key, sample_id, match_level, specimen_key,
   procedure_date_days, timepoint_source)
VALUES
  (990001, 990001, 'wsi-ci-release-a-1', '3020726', '27', '4',
   990001, 'BLOCK', 'block::27::4', -17,
   'Procedure date relative to tumor sequencing'),
  (990001, 990001, 'wsi-ci-release-a-1', '3020691', '27', '62',
   990001, 'PART', 'part::27', -17,
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
