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

});