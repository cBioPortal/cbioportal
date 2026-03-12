import { expect } from 'chai';
import _ from 'lodash';
import axios from 'axios';
import { CancerStudyMetadataDTO } from '../../src/types';
import { config } from '../../src/config';

describe('ColumnStoreStudyController E2E Tests', () => {

  // Total number of studies expected in the database
  const TOTAL_STUDIES = 492;

  describe('testGetAllStudies', () => {
    it('should return all studies with detailed projection', async () => {
      // Build the URL for fetching all studies with detailed projection
      const url = `${config.serverUrl}/api/column-store/studies?projection=DETAILED`;

      // Call the studies endpoint to get all study metadata
      const response = await axios.get<CancerStudyMetadataDTO[]>(url);

      // Verify the response status is 200 OK
      expect(response.status).to.equal(200, 'Response status should be 200 OK');

      // Extract the study data from the response
      const studies = response.data;

      // Verify the response is an array
      expect(studies).to.be.an('array', 'Response data should be an array');

      // Verify the total number of studies matches the expected count
      expect(studies.length).to.be.gte(
        TOTAL_STUDIES,
        `Should be more than ${TOTAL_STUDIES} studies`
      );

      // Verify all studies have required fields
      const allHaveRequiredFields = _.every(studies, (study) =>
        study.studyId &&
        study.name &&
        study.cancerTypeId &&
        typeof study.allSampleCount === 'number'
      );
      expect(allHaveRequiredFields, 'All studies should have required fields').to.be.true;

      // Verify all studies have typeOfCancer nested object
      const allHaveTypeOfCancer = _.every(studies, (study) =>
        study.cancerType &&
        study.cancerType.id &&
        study.cancerType.name
      );
      expect(allHaveTypeOfCancer, 'All studies should have typeOfCancer with id and name').to.be.true;
    });
  });

  describe('testGetStudyById', () => {

    // Study IDs used across multiple tests
    const KNOWN_STUDY_ID = 'mskimpact';
    const NONEXISTENT_STUDY_ID = 'nonexistent_study_xyz_000';

    it('should return a single study with all required fields', async () => {
      // Fetch a single study by its study ID
      const url = `${config.serverUrl}/api/column-store/studies/${KNOWN_STUDY_ID}`;
      const response = await axios.get<CancerStudyMetadataDTO>(url);

      // Verify the response status is 200 OK
      expect(response.status).to.equal(200, 'Response status should be 200 OK');

      // Extract the study data from the response
      const study = response.data;

      // Verify the studyId matches the requested ID
      expect(study.studyId).to.equal(
        KNOWN_STUDY_ID,
        `studyId should match the requested study ID '${KNOWN_STUDY_ID}'`
      );

      // Verify all top-level required fields are present and have correct types
      expect(study.name).to.be.a('string', 'name should be a string');
      expect(study.name.length).to.be.greaterThan(0, 'name should not be empty');
      expect(study.cancerTypeId).to.be.a('string', 'cancerTypeId should be a string');
      expect(study.description).to.be.a('string', 'description should be a string');
      expect(study.publicStudy).to.be.a('boolean', 'publicStudy should be a boolean');
      expect(study.status).to.be.a('number', 'status should be a number');
      expect(study.importDate).to.be.a('string', 'importDate should be a string');
      expect(study.referenceGenome).to.be.a('string', 'referenceGenome should be a string');
      expect(study.readPermission).to.be.a('boolean', 'readPermission should be a boolean');

      // Verify sample count fields are present and are numbers
      expect(study.allSampleCount).to.be.a('number', 'allSampleCount should be a number');
      expect(study.sequencedSampleCount).to.be.a('number', 'sequencedSampleCount should be a number');
      expect(study.cnaSampleCount).to.be.a('number', 'cnaSampleCount should be a number');
      expect(study.mrnaRnaSeqSampleCount).to.be.a('number', 'mrnaRnaSeqSampleCount should be a number');
      expect(study.mrnaRnaSeqV2SampleCount).to.be.a('number', 'mrnaRnaSeqV2SampleCount should be a number');
      expect(study.mrnaMicroarraySampleCount).to.be.a('number', 'mrnaMicroarraySampleCount should be a number');
      expect(study.miRnaSampleCount).to.be.a('number', 'miRnaSampleCount should be a number');
      expect(study.methylationHm27SampleCount).to.be.a('number', 'methylationHm27SampleCount should be a number');
      expect(study.rppaSampleCount).to.be.a('number', 'rppaSampleCount should be a number');
      expect(study.massSpectrometrySampleCount).to.be.a('number', 'massSpectrometrySampleCount should be a number');
      expect(study.completeSampleCount).to.be.a('number', 'completeSampleCount should be a number');
      expect(study.treatmentCount).to.be.a('number', 'treatmentCount should be a number');
      expect(study.structuralVariantCount).to.be.a('number', 'structuralVariantCount should be a number');

      // Verify the nested cancerType object has all required fields
      expect(study.cancerType).to.not.be.undefined;
      expect(study.cancerType.id).to.be.a('string', 'cancerType.id should be a string');
      expect(study.cancerType.name).to.be.a('string', 'cancerType.name should be a string');
      expect(study.cancerType.dedicatedColor).to.be.a('string', 'cancerType.dedicatedColor should be a string');
      expect(study.cancerType.shortName).to.be.a('string', 'cancerType.shortName should be a string');
      expect(study.cancerType.parent).to.be.a('string', 'cancerType.parent should be a string');
    });

    it('should return 404 for a non-existent study', async () => {
      // Attempt to fetch a study that does not exist
      const url = `${config.serverUrl}/api/column-store/studies/${NONEXISTENT_STUDY_ID}`;

      try {
        await axios.get<CancerStudyMetadataDTO>(url);
        // If we get here, the request did not fail as expected
        expect.fail('Expected a 404 error but request succeeded');
      } catch (error: any) {
        // Verify that the error is a 404 Not Found response
        expect(error.response).to.not.be.undefined;
        expect(error.response.status).to.equal(
          404,
          'Non-existent study should return 404 Not Found'
        );
      }
    });

    it('should return data consistent with the list endpoint', async () => {
      // Fetch a single study by ID
      const singleUrl = `${config.serverUrl}/api/column-store/studies/${KNOWN_STUDY_ID}`;
      const singleResponse = await axios.get<CancerStudyMetadataDTO>(singleUrl);
      const singleStudy = singleResponse.data;

      // Fetch all studies with DETAILED projection to find the same study
      const listUrl = `${config.serverUrl}/api/column-store/studies?projection=DETAILED`;
      const listResponse = await axios.get<CancerStudyMetadataDTO[]>(listUrl);

      // Find the matching study in the list response using lodash
      const listStudy = _.find(listResponse.data, { studyId: KNOWN_STUDY_ID });
      expect(listStudy, `${KNOWN_STUDY_ID} should be present in the list endpoint`).to.not.be.undefined;

      // Compare all scalar fields between the single and list endpoints
      expect(singleStudy.studyId).to.equal(listStudy!.studyId, 'studyId should match');
      expect(singleStudy.cancerTypeId).to.equal(listStudy!.cancerTypeId, 'cancerTypeId should match');
      expect(singleStudy.name).to.equal(listStudy!.name, 'name should match');
      expect(singleStudy.description).to.equal(listStudy!.description, 'description should match');
      expect(singleStudy.publicStudy).to.equal(listStudy!.publicStudy, 'publicStudy should match');
      expect(singleStudy.status).to.equal(listStudy!.status, 'status should match');
      expect(singleStudy.referenceGenome).to.equal(listStudy!.referenceGenome, 'referenceGenome should match');

      // Compare sample counts between single and list endpoints
      expect(singleStudy.allSampleCount).to.equal(listStudy!.allSampleCount, 'allSampleCount should match');
      expect(singleStudy.sequencedSampleCount).to.equal(listStudy!.sequencedSampleCount, 'sequencedSampleCount should match');
      expect(singleStudy.cnaSampleCount).to.equal(listStudy!.cnaSampleCount, 'cnaSampleCount should match');
      expect(singleStudy.treatmentCount).to.equal(listStudy!.treatmentCount, 'treatmentCount should match');
      expect(singleStudy.structuralVariantCount).to.equal(listStudy!.structuralVariantCount, 'structuralVariantCount should match');

      // Compare nested cancerType fields
      expect(singleStudy.cancerType.id).to.equal(listStudy!.cancerType.id, 'cancerType.id should match');
      expect(singleStudy.cancerType.name).to.equal(listStudy!.cancerType.name, 'cancerType.name should match');
      expect(singleStudy.cancerType.shortName).to.equal(listStudy!.cancerType.shortName, 'cancerType.shortName should match');
    });

    it('should return consistent data across multiple studies', async () => {
      // Fetch the list of all studies to pick several for validation
      const listUrl = `${config.serverUrl}/api/column-store/studies?projection=DETAILED`;
      const listResponse = await axios.get<CancerStudyMetadataDTO[]>(listUrl);
      const allStudies = listResponse.data;

      // Pick 5 studies from the list to verify individually
      const sampleStudies = _.take(allStudies, 5);

      // For each study, fetch it individually and compare key fields with the list
      for (const listStudy of sampleStudies) {
        const singleUrl = `${config.serverUrl}/api/column-store/studies/${listStudy.studyId}`;
        const singleResponse = await axios.get<CancerStudyMetadataDTO>(singleUrl);
        const singleStudy = singleResponse.data;

        // Verify the individual endpoint returns matching data for this study
        expect(singleStudy.studyId).to.equal(
          listStudy.studyId,
          `studyId should match for ${listStudy.studyId}`
        );
        expect(singleStudy.name).to.equal(
          listStudy.name,
          `name should match for ${listStudy.studyId}`
        );
        expect(singleStudy.allSampleCount).to.equal(
          listStudy.allSampleCount,
          `allSampleCount should match for ${listStudy.studyId}`
        );
        expect(singleStudy.cancerType.id).to.equal(
          listStudy.cancerType.id,
          `cancerType.id should match for ${listStudy.studyId}`
        );
      }
    });
  });

});