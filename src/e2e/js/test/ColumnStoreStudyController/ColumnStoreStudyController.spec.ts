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

    const KNOWN_STUDY_ID = 'acc_tcga';

    it('should return a study with required fields', async () => {
      const url = `${config.serverUrl}/api/column-store/studies/${KNOWN_STUDY_ID}`;
      const response = await axios.get<CancerStudyMetadataDTO>(url);
      const study = response.data;

      expect(response.status).to.equal(200);
      expect(study.studyId).to.equal(KNOWN_STUDY_ID);
      expect(study.name).to.be.a('string').and.not.be.empty;
      expect(study.cancerTypeId).to.be.a('string');
      expect(study.allSampleCount).to.be.a('number');
      expect(study.cancerType).to.have.property('id');
      expect(study.cancerType).to.have.property('name');
    });

    it('should return 404 for a non-existent study', async () => {
      try {
        await axios.get(`${config.serverUrl}/api/column-store/studies/nonexistent_study_xyz`);
        expect.fail('Expected 404');
      } catch (error: any) {
        // Distinguish HTTP error responses from network/transport failures
        expect(error.response, 'Expected an HTTP error response, not a network error').to.not.be.undefined;
        expect(error.response.status).to.equal(404);
      }
    });

    // INTENTIONAL FAILURE — demonstrates how a failing test appears in CircleCI Tests tab.
    // Remove this test once the failure screenshot has been captured.
    it('DEMO FAILURE: this test is intentionally broken', async () => {
      expect(1, 'Intentional failure to demonstrate CircleCI test reporting').to.equal(2);
    });

    it('should return data consistent with the list endpoint', async () => {
      // Fetch the same study from both the single and list endpoints
      const singleResponse = await axios.get<CancerStudyMetadataDTO>(
        `${config.serverUrl}/api/column-store/studies/${KNOWN_STUDY_ID}`
      );
      const listResponse = await axios.get<CancerStudyMetadataDTO[]>(
        `${config.serverUrl}/api/column-store/studies?projection=DETAILED`
      );

      const listStudy = _.find(listResponse.data, { studyId: KNOWN_STUDY_ID });
      expect(listStudy, `${KNOWN_STUDY_ID} should exist in list`).to.not.be.undefined;

      // Deep-equal the entire response objects
      expect(singleResponse.data).to.deep.equal(listStudy);
    });
  });

});