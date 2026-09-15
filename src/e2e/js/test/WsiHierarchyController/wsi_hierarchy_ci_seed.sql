-- Portal catalog identities used to validate the normalized WSI references.
-- Keep the small catalog dimensions here because an isolated ClickHouse stack
-- loads the WSI fixture on top of the schema without the production seed.
INSERT INTO reference_genome
  (reference_genome_id, species, name, build_name, genome_size, url, release_date)
SELECT *
FROM (
  SELECT toInt64(1) AS reference_genome_id, 'human' AS species, 'hg19' AS name,
         'GRCh37' AS build_name, toNullable(toInt64(2897310462)) AS genome_size,
         'http://hgdownload.cse.ucsc.edu/goldenPath/hg19/bigZips',
         toDateTime64('2009-02-01 00:00:00', 6) AS release_date
  UNION ALL
  SELECT toInt64(2), 'human', 'hg38', 'GRCh38', toNullable(toInt64(3049315783)),
         'http://hgdownload.cse.ucsc.edu/goldenPath/hg38/bigZips',
         toDateTime64('2013-12-01 00:00:00', 6)
) AS seed
WHERE reference_genome_id NOT IN (SELECT reference_genome_id FROM reference_genome);

INSERT INTO type_of_cancer
  (type_of_cancer_id, name, dedicated_color, short_name, parent)
SELECT *
FROM (
  -- `tissue` is the frontend's synthetic root category; do not insert a
  -- database row with the same ID or it replaces that root in the tree.
  SELECT 'mixed' AS type_of_cancer_id, 'Mixed' AS name,
         '' AS dedicated_color, 'Mixed' AS short_name, 'tissue' AS parent
) AS seed
WHERE type_of_cancer_id NOT IN (SELECT type_of_cancer_id FROM type_of_cancer);

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
INSERT INTO sample_list
  (list_id, stable_id, category, cancer_study_id, name, description)
VALUES (990002, 'wsi_ci_study_b_all', 'all', 990002,
        'All WSI CI control samples', 'Authenticated WSI CI control sample list');
INSERT INTO sample_list_list (list_id, sample_id) VALUES (990002, 990002);

-- Keep WSI slide availability in the normal study-view clinical data path as
-- sample-level attributes.  The frontend derives the corresponding patient
-- totals from these rows, while the hierarchy and timeline retain the full
-- specimen/slide detail.
INSERT INTO clinical_attribute_meta
  (attr_id, display_name, description, datatype, patient_attribute, priority,
   cancer_study_id)
VALUES
  ('WSI_SAMPLE_SLIDE_COUNT', 'WSI Slides per Sample',
   'Viewable pathology slide count for the sample', 'NUMBER', 0, '1', 990001),
  ('WSI_SAMPLE_PART_MATCHED_SLIDE_COUNT', 'WSI Part-matched Slides per Sample',
   'Viewable pathology slides matched to a specimen part', 'NUMBER', 0, '1', 990001),
  ('WSI_SAMPLE_BLOCK_MATCHED_SLIDE_COUNT', 'WSI Block-matched Slides per Sample',
   'Viewable pathology slides matched to a specimen block', 'NUMBER', 0, '1', 990001),
  ('WSI_SAMPLE_SLIDE_COUNT', 'WSI Slides per Sample',
   'Viewable pathology slide count for the sample', 'NUMBER', 0, '1', 990002),
  ('WSI_SAMPLE_PART_MATCHED_SLIDE_COUNT', 'WSI Part-matched Slides per Sample',
   'Viewable pathology slides matched to a specimen part', 'NUMBER', 0, '1', 990002),
  ('WSI_SAMPLE_BLOCK_MATCHED_SLIDE_COUNT', 'WSI Block-matched Slides per Sample',
   'Viewable pathology slides matched to a specimen block', 'NUMBER', 0, '1', 990002);

INSERT INTO clinical_sample (internal_id, attr_id, attr_value)
VALUES
  (990001, 'WSI_SAMPLE_SLIDE_COUNT', '2'),
  (990001, 'WSI_SAMPLE_PART_MATCHED_SLIDE_COUNT', '2'),
  (990001, 'WSI_SAMPLE_BLOCK_MATCHED_SLIDE_COUNT', '1'),
  (990002, 'WSI_SAMPLE_SLIDE_COUNT', '1'),
  (990002, 'WSI_SAMPLE_PART_MATCHED_SLIDE_COUNT', '1'),
  (990002, 'WSI_SAMPLE_BLOCK_MATCHED_SLIDE_COUNT', '1');

-- Pathology procedure timing belongs to the standard clinical timeline, not
-- the WSI hierarchy tables.  Keep one event per slide association so the
-- timeline retains the block/part/unmatched distinctions while all events
-- share the de-identified procedure offset.
INSERT INTO clinical_event
  (clinical_event_id, patient_id, start_date, stop_date, event_type)
VALUES
  (990001, 990001, -17, -17, 'PATHOLOGY SLIDES'),
  (990002, 990001, -17, -17, 'PATHOLOGY SLIDES'),
  (990003, 990001, -17, -17, 'PATHOLOGY SLIDES'),
  (990004, 990002, -17, -17, 'PATHOLOGY SLIDES');
INSERT INTO clinical_event_data (clinical_event_id, key, value)
VALUES
  (990001, 'IMAGE_COUNT', '1'),
  (990001, 'NON_SERVABLE_IMAGE_COUNT', '0'),
  (990001, 'TOTAL_IMAGE_COUNT', '1'),
  (990001, 'SAMPLE_ID', 'P-0055908-T01-IM6'),
  (990001, 'MATCH_LEVEL', 'Block'),
  (990001, 'SPECIMEN', 'Part 27 / Block 4RO'),
  (990001, 'SUBTYPE', 'H&E'),
  (990001, 'TIMEPOINT_SOURCE', 'Procedure date relative to tumor sequencing'),
  (990001, 'LINKOUT', '/patient/wsiHESlides?studyId=msk_spectrum_tme_2022&caseId=P-0055908&sampleId=P-0055908-T01-IM6&stainFilter=hne&matchLevel=BLOCK&specimenKey=block%3A%3A27%3A%3A4'),
  (990002, 'IMAGE_COUNT', '1'),
  (990002, 'NON_SERVABLE_IMAGE_COUNT', '0'),
  (990002, 'TOTAL_IMAGE_COUNT', '1'),
  (990002, 'SAMPLE_ID', 'P-0055908-T01-IM6'),
  (990002, 'MATCH_LEVEL', 'Part'),
  (990002, 'SPECIMEN', 'Part 27 / Block 1 RFIM'),
  (990002, 'SUBTYPE', 'H&E'),
  (990002, 'TIMEPOINT_SOURCE', 'Procedure date relative to tumor sequencing'),
  (990002, 'LINKOUT', '/patient/wsiHESlides?studyId=msk_spectrum_tme_2022&caseId=P-0055908&sampleId=P-0055908-T01-IM6&stainFilter=hne&matchLevel=PART&specimenKey=part%3A%3A27'),
  (990003, 'IMAGE_COUNT', '0'),
  (990003, 'NON_SERVABLE_IMAGE_COUNT', '1'),
  (990003, 'TOTAL_IMAGE_COUNT', '1'),
  (990003, 'SAMPLE_ID', ''),
  (990003, 'MATCH_LEVEL', 'Unmatched'),
  (990003, 'SPECIMEN', 'Part 34 / Block 4RS'),
  (990003, 'SUBTYPE', 'H&E'),
  (990003, 'TIMEPOINT_SOURCE', 'Procedure date relative to tumor sequencing'),
  (990003, 'LINKOUT', '/patient/wsiHESlides?studyId=msk_spectrum_tme_2022&caseId=P-0055908&stainFilter=hne&matchLevel=UNMATCHED&specimenKey=unmatched%3A%3A34%3A%3A4'),
  (990004, 'IMAGE_COUNT', '1'),
  (990004, 'NON_SERVABLE_IMAGE_COUNT', '0'),
  (990004, 'TOTAL_IMAGE_COUNT', '1'),
  (990004, 'SAMPLE_ID', 'WSI-CI-B-SAMPLE'),
  (990004, 'MATCH_LEVEL', 'Block'),
  (990004, 'SPECIMEN', 'Part 1 / Block 1'),
  (990004, 'SUBTYPE', 'H&E'),
  (990004, 'TIMEPOINT_SOURCE', 'Procedure date relative to tumor sequencing'),
  (990004, 'LINKOUT', '/patient/wsiHESlides?studyId=wsi_ci_study_b&caseId=WSI-CI-B-PATIENT&sampleId=WSI-CI-B-SAMPLE&stainFilter=hne&matchLevel=BLOCK&specimenKey=block%3A%3A1%3A%3A1');

-- Single snapshot for the MSK fixture, including one explicitly unmatched slide.
INSERT INTO wsi_patient
  (cancer_study_id, patient_id, reference_sample_id)
VALUES (990001, 990001, 990001);
INSERT INTO wsi_part
  (cancer_study_id, patient_id, part_key,
   part_number, part_designator, part_type, part_description, subspecialty,
   path_dx_title)
VALUES
  (990001, 990001, '27', '27', '27',
   'FALLOPIAN TUBE AND OVARY', 'Right fallopian tube and ovary', '',
   'Right fallopian tube and ovary'),
  (990001, 990001, '34', '34', '34',
   'SMALL BOWEL', 'Portion of small bowel with tumor', '',
   'Portion of small bowel with tumor');
INSERT INTO wsi_block
  (cancer_study_id, patient_id, part_key,
   block_key, block_number, block_label)
VALUES
  (990001, 990001, '27', '4', '4', '4RO'),
  (990001, 990001, '27', '62', '62', '1 RFIM'),
  (990001, 990001, '34', '4', '4', '4RS');
INSERT INTO wsi_slide
  (cancer_study_id, patient_id, image_id,
   stain_name, stain_group, is_hne, is_ihc, magnification, file_size_bytes,
   can_serve_tiles, barcode, slide_type, source_url, tile_metadata_json,
   thumbnail_url, thumbnail_width, thumbnail_height, thumbnail_content_type)
VALUES
  (990001, 990001, '3020726', 'H&E, Initial',
   'H&E (Initial)', true, false, '20x', 716956681, true, '', 'H&E',
   'file:///app/testdata/CMU-1-Small-Region.svs',
   '{"dimensions":{"width":2220,"height":2967},"levels":1,"level_dimensions":[{"width":2220,"height":2967}],"level_downsamples":[1.0],"max_zoom":4,"tile_size":256,"mpp":{"x":0.499,"y":0.499},"objective_power":20,"vendor":"aperio"}',
   'file:///app/testdata/3020691.jpg', 256, 232, 'image/jpeg'),
  (990001, 990001, '3020691', 'H&E, Initial',
   'H&E (Initial)', true, false, '20x', 538183815, true, '', 'H&E',
   'file:///app/testdata/CMU-1-Small-Region.svs',
   '{"dimensions":{"width":2220,"height":2967},"levels":1,"level_dimensions":[{"width":2220,"height":2967}],"level_downsamples":[1.0],"max_zoom":4,"tile_size":256,"mpp":{"x":0.499,"y":0.499},"objective_power":20,"vendor":"aperio"}',
   'file:///app/testdata/3020691.jpg', 256, 232, 'image/jpeg'),
  (990001, 990001, '3020648', 'H&E, Initial',
   'H&E (Initial)', true, false, '20x', 1014457317, false, '', 'H&E',
   NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO wsi_slide_placement
  (cancer_study_id, patient_id, image_id,
   part_key, block_key, sample_id, match_level, specimen_key)
VALUES
  (990001, 990001, '3020726', '27', '4',
   990001, 'BLOCK', 'block::27::4'),
  (990001, 990001, '3020691', '27', '62',
   990001, 'PART', 'part::27'),
  (990001, 990001, '3020648', '34', '4',
   NULL, 'UNMATCHED', 'unmatched::34::4');

-- A second study verifies study-scoped tile/index authorization in E2E tests.
INSERT INTO wsi_patient
  (cancer_study_id, patient_id, reference_sample_id)
VALUES (990002, 990002, 990002);
INSERT INTO wsi_part
  (cancer_study_id, patient_id, part_key,
   part_number, part_designator, part_type, part_description, subspecialty,
   path_dx_title)
VALUES (990002, 990002, '1', '1', '1',
        'SPECIMEN', 'Fixture specimen', '', 'Fixture specimen');
INSERT INTO wsi_block
  (cancer_study_id, patient_id, part_key,
   block_key, block_number, block_label)
VALUES (990002, 990002, '1', '1', '1', '1');
INSERT INTO wsi_slide
  (cancer_study_id, patient_id, image_id,
   stain_name, stain_group, is_hne, is_ihc, magnification, file_size_bytes,
   can_serve_tiles, barcode, slide_type, source_url, tile_metadata_json,
   thumbnail_url, thumbnail_width, thumbnail_height, thumbnail_content_type)
VALUES (990002, 990002, '4020726', 'H&E, Initial', 'H&E (Initial)',
        true, false, '20x', 716956681, true, '', 'H&E',
        'file:///app/testdata/CMU-1-Small-Region.svs',
        '{"dimensions":{"width":2220,"height":2967},"levels":1,"level_dimensions":[{"width":2220,"height":2967}],"level_downsamples":[1.0],"max_zoom":4,"tile_size":256,"mpp":{"x":0.499,"y":0.499},"objective_power":20,"vendor":"aperio"}',
        'file:///app/testdata/3020691.jpg', 256, 232, 'image/jpeg');
INSERT INTO wsi_slide_placement
  (cancer_study_id, patient_id, image_id,
   part_key, block_key, sample_id, match_level, specimen_key)
VALUES (990002, 990002, '4020726', '1', '1',
        990002, 'BLOCK', 'block::1::1');
