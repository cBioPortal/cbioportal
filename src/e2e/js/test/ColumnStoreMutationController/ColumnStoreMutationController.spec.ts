import { expect } from 'chai';
import _ from 'lodash';
import axios from 'axios';
import { MutationDTO, ProjectionType } from '../../src/types';
import { config } from '../../src/config';
import { TestUtils } from '../../src/utils';

describe('ColumnStoreMutationController E2E Tests', () => {

  /**
   * Helper function to call the mutations fetch endpoint
   * @param testData - The request payload with sample identifiers and gene IDs
   * @param projectionType - The projection type (ID, SUMMARY, or DETAILED)
   * @returns Promise containing array of mutation DTOs
   */
  async function callFetchMutationEndpoint(
    testData: any,
    projectionType: ProjectionType
  ): Promise<MutationDTO[]> {
    const url = `${config.serverUrl}/api/column-store/mutations/fetch?projection=${projectionType}`;

    const response = await axios.post<MutationDTO[]>(url, testData, {
      headers: {
        'Content-Type': 'application/json'
      }
    });

    // Verify response status is 200 OK
    expect(response.status).to.equal(200, 'Response status should be 200 OK');

    // Verify response body is not empty
    expect(response.data).to.not.be.null;
    expect(response.data).to.not.be.undefined;

    return response.data;
  }

  describe('testFetchMutationEndPointWithDataJson_IdProjection', () => {
    it('should return mutations with ID projection', async () => {
      // The JSON has two molecularProfileIds with a sampleId and entrezGeneIds to restrict search
      // Two mutations meet this criteria
      const testData = await TestUtils.loadTestData('mutation_filter.json');

      // Call the mutations endpoint with ID projection
      const mutations = await callFetchMutationEndpoint(testData, ProjectionType.ID);

      // Verify the response contains mutation DTOs
      expect(mutations).to.be.an('array', 'Response should be an array of mutations');
      expect(mutations.length).to.equal(
        2,
        'Two mutations meet the search criteria of the json file'
      );

      // Extract molecularProfileId from both mutations using lodash map
      const molecularProfileIds = _.map(mutations, 'molecularProfileId');

      // Verify all molecularProfileIds are identical using lodash uniq
      const uniqueProfileIds = _.uniq(molecularProfileIds);
      expect(uniqueProfileIds.length).to.equal(
        1,
        'All mutations should have the same molecularProfileId'
      );

      // Extract studyId from both mutations using lodash map
      const studyIds = _.map(mutations, 'studyId');

      // Verify all studyIds are identical using lodash uniq
      const uniqueStudyIds = _.uniq(studyIds);
      expect(uniqueStudyIds.length).to.equal(
        1,
        'All mutations should have the same studyId'
      );
    });
  });

  describe('testFetchMutationEndPointWithDataJson_SummaryProjection', () => {
    it('should return mutations with SUMMARY projection without gene details', async () => {
      // The JSON has two molecularProfileIds with a sampleId and entrezGeneIds to restrict search
      // Two mutations meet this criteria
      const testData = await TestUtils.loadTestData('mutation_filter.json');

      // Call the mutations endpoint with SUMMARY projection
      const mutations = await callFetchMutationEndpoint(testData, ProjectionType.SUMMARY);

      // Verify the response contains mutation DTOs
      expect(mutations).to.be.an('array', 'Response should be an array of mutations');
      expect(mutations.length).to.equal(
        2,
        'SUMMARY projection should not add or remove records'
      );

      // Extract molecularProfileId from both mutations using lodash map
      const molecularProfileIds = _.map(mutations, 'molecularProfileId');

      // Verify all molecularProfileIds are identical
      const uniqueProfileIds = _.uniq(molecularProfileIds);
      expect(uniqueProfileIds.length).to.equal(
        1,
        'All mutations should have the same molecularProfileId'
      );

      // Extract studyId from both mutations
      const studyIds = _.map(mutations, 'studyId');

      // Verify all studyIds are identical
      const uniqueStudyIds = _.uniq(studyIds);
      expect(uniqueStudyIds.length).to.equal(
        1,
        'All mutations should have the same studyId'
      );

      // Testing that different projection types expose different fields
      // SUMMARY projection should NOT have gene present
      const allLackGene = _.every(mutations, (mutation) => !mutation.gene);
      expect(allLackGene, 'SUMMARY projection should not have gene field present').to.be.true;

      // AlleleSpecificCopyNumber should also not be present (or be null)
      const allLackAlleleSpecificCopyNumber = _.every(mutations, (mutation) =>
        !mutation.alleleSpecificCopyNumber
      );
      expect(
        allLackAlleleSpecificCopyNumber,
        'SUMMARY projection should not have alleleSpecificCopyNumber present'
      ).to.be.true;
    });
  });

  describe('testFetchMutationEndPointWithDataJson_DetailedProjection', () => {
    it('should return mutations with DETAILED projection including gene details', async () => {
      // The JSON has two molecularProfileIds with a sampleId and entrezGeneIds to restrict search
      // Two mutations meet this criteria
      const testData = await TestUtils.loadTestData('mutation_filter.json');

      // Call the mutations endpoint with DETAILED projection
      const mutations = await callFetchMutationEndpoint(testData, ProjectionType.DETAILED);

      // Verify the response contains mutation DTOs
      expect(mutations).to.be.an('array', 'Response should be an array of mutations');
      expect(mutations.length).to.equal(
        2,
        'DETAILED projection should not add or remove records'
      );

      // Extract molecularProfileId from both mutations using lodash map
      const molecularProfileIds = _.map(mutations, 'molecularProfileId');

      // Verify all molecularProfileIds are identical
      const uniqueProfileIds = _.uniq(molecularProfileIds);
      expect(uniqueProfileIds.length).to.equal(
        1,
        'All mutations should have the same molecularProfileId'
      );

      // Extract studyId from both mutations
      const studyIds = _.map(mutations, 'studyId');

      // Verify all studyIds are identical
      const uniqueStudyIds = _.uniq(studyIds);
      expect(uniqueStudyIds.length).to.equal(
        1,
        'All mutations should have the same studyId'
      );

      // Testing that different projection types expose different fields
      // DETAILED projection SHOULD have gene present
      const allHaveGene = _.every(mutations, (mutation) =>
        mutation.gene && mutation.gene.entrezGeneId && mutation.gene.hugoGeneSymbol
      );
      expect(allHaveGene, 'DETAILED projection should have gene field with required properties').to.be.true;

      // AlleleSpecificCopyNumber is null for this mutation profile
      // The field may not be present in the response if it's null
      // Verify that if the field exists, we can access it, or it's simply not present
      // This matches the Java test which just checks that it's null
      const allHaveNullOrMissingAlleleSpecificCopyNumber = _.every(mutations, (mutation) =>
        !mutation.alleleSpecificCopyNumber || mutation.alleleSpecificCopyNumber === null
      );
      expect(
        allHaveNullOrMissingAlleleSpecificCopyNumber,
        'DETAILED projection should have alleleSpecificCopyNumber as null or missing'
      ).to.be.true;
    });
  });

});