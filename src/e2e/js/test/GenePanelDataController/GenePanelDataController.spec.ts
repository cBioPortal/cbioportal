import { expect } from 'chai';
import _ from 'lodash';
import axios from 'axios';
import { GenePanelData } from '../../src/types';
import { config } from '../../src/config';
import { TestUtils } from '../../src/utils';

describe('GenePanelDataController E2E Tests', () => {
  /**
   * Calls POST /api/gene-panel-data/fetch with a multi-study filter.
   * This endpoint (the sampleMolecularIdentifiers path) was changed to push the
   * (profile, sample) filter into SQL instead of fetching every sample of the profile and
   * filtering in Java; these tests guard that it still returns the correct rows.
   * @param testData - request body containing sampleMolecularIdentifiers
   * @returns array of GenePanelData rows
   */
  async function callFetchGenePanelData(testData: any): Promise<GenePanelData[]> {
    const url = `${config.serverUrl}/api/gene-panel-data/fetch`;

    const response = await axios.post<GenePanelData[]>(url, testData, {
      headers: { 'Content-Type': 'application/json' }
    });

    // Verify the response status is 200 OK
    expect(response.status).to.equal(200, 'Response status should be 200 OK');

    // Verify the response body is present
    expect(response.data).to.not.be.null;
    expect(response.data).to.not.be.undefined;

    return response.data;
  }

  describe('testFetchGenePanelData_sampleMolecularIdentifiers', () => {
    it('should return exactly one profiled row per requested (profile, sample) pair', async () => {
      // The JSON requests three specific samples of the acc_tcga mutation profile.
      // All three are sequenced/profiled in acc_tcga, so each yields a profiled row.
      const testData = await TestUtils.loadTestData('gene_panel_data_filter.json');

      // Call the gene-panel-data fetch endpoint
      const genePanelData = await callFetchGenePanelData(testData);

      // The SQL-filtered query must return exactly the three requested rows (no cross-product)
      expect(genePanelData).to.be.an('array', 'Response should be an array');
      expect(genePanelData.length).to.equal(
        3,
        'Should return exactly one row per requested (profile, sample) pair'
      );

      // Every row should belong to the requested profile and study
      const allForRequestedProfile = _.every(genePanelData, {
        molecularProfileId: 'acc_tcga_mutations',
        studyId: 'acc_tcga'
      });
      expect(
        allForRequestedProfile,
        'All rows should be for acc_tcga_mutations / acc_tcga'
      ).to.be.true;

      // The returned sample ids should be exactly the three requested ones
      const returnedSampleIds = _.sortBy(_.map(genePanelData, 'sampleId'));
      expect(returnedSampleIds).to.deep.equal(
        ['TCGA-OR-A5J1-01', 'TCGA-OR-A5J2-01', 'TCGA-OR-A5J3-01'],
        'Returned sample ids should match exactly the requested sample ids'
      );

      // All three samples are sequenced in acc_tcga, so all should be profiled
      const allProfiled = _.every(genePanelData, { profiled: true });
      expect(allProfiled, 'All three acc_tcga samples should be profiled').to.be.true;

      // The derived unique keys (added in the response) should be populated for every row
      const allHaveKeys = _.every(
        genePanelData,
        row => !_.isEmpty(row.uniqueSampleKey) && !_.isEmpty(row.uniquePatientKey)
      );
      expect(allHaveKeys, 'Every row should have uniqueSampleKey/uniquePatientKey').to.be.true;
    });

    it('should not return rows for samples that were not requested', async () => {
      // Request a single sample of the acc_tcga mutation profile
      const testData = {
        sampleMolecularIdentifiers: [
          { molecularProfileId: 'acc_tcga_mutations', sampleId: 'TCGA-OR-A5J1-01' }
        ]
      };

      // Call the endpoint
      const genePanelData = await callFetchGenePanelData(testData);

      // Because filtering happens in SQL, only the single requested sample comes back,
      // even though acc_tcga has ~90 samples in this profile.
      expect(genePanelData.length).to.equal(
        1,
        'Only the single requested (profile, sample) pair should be returned'
      );
      expect(genePanelData[0].sampleId).to.equal(
        'TCGA-OR-A5J1-01',
        'The returned row should be the requested sample'
      );
    });
  });
});
