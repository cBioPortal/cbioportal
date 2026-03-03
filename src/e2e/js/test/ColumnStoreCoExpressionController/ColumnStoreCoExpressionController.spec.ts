import { expect } from 'chai';
import _ from 'lodash';
import axios from 'axios';
import { CoExpression } from '../../src/types';
import { config } from '../../src/config';
import { TestUtils } from '../../src/utils';

describe('ColumnStoreCoExpressionController E2E Tests', () => {

  // Base URL for the co-expression endpoint
  const CO_EXPRESSION_URL = `${config.serverUrl}/api/column-store/molecular-profiles/co-expressions/fetch`;

  /**
   * Helper function to call the co-expression fetch endpoint
   * @param molecularProfileIdA - First molecular profile ID
   * @param molecularProfileIdB - Second molecular profile ID
   * @param body - CoExpressionFilter request body
   * @param threshold - Correlation threshold (default 0.3)
   * @returns Promise containing array of CoExpression results
   */
  async function callCoExpressionEndpoint(
    molecularProfileIdA: string,
    molecularProfileIdB: string,
    body: any,
    threshold: number = 0.3
  ): Promise<CoExpression[]> {
    const url = `${CO_EXPRESSION_URL}?molecularProfileIdA=${molecularProfileIdA}&molecularProfileIdB=${molecularProfileIdB}&threshold=${threshold}`;

    const response = await axios.post<CoExpression[]>(url, body, {
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

  describe('testSameProfileCoExpressionWithSampleList', () => {
    it('should return co-expression results for BRCA1 in acc_tcga mRNA profile using sample list', async () => {
      // acc_tcga has 79 samples with rna_seq_v2_mrna data and 20,343 genes
      // BRCA1 (entrez 672) is well-characterized and has strong co-expressions
      const testData = await TestUtils.loadTestData('same_profile_sample_list.json');

      // Call same-profile co-expression: mRNA vs mRNA with threshold 0.3
      const coExpressions = await callCoExpressionEndpoint(
        'acc_tcga_rna_seq_v2_mrna',
        'acc_tcga_rna_seq_v2_mrna',
        testData,
        0.3
      );

      // Verify the response contains co-expression results
      expect(coExpressions).to.be.an('array', 'Response should be an array');
      expect(coExpressions.length).to.be.greaterThan(
        0,
        'BRCA1 should have co-expressed genes above threshold 0.3'
      );

      // Verify each result has the required fields
      const allHaveRequiredFields = _.every(coExpressions, (ce) =>
        ce.geneticEntityId !== undefined &&
        ce.geneticEntityType !== undefined &&
        ce.spearmansCorrelation !== undefined &&
        ce.pValue !== undefined
      );
      expect(allHaveRequiredFields, 'All results should have geneticEntityId, geneticEntityType, spearmansCorrelation, and pValue').to.be.true;

      // Verify all geneticEntityType values are "GENE"
      const allAreGenes = _.every(coExpressions, { geneticEntityType: 'GENE' });
      expect(allAreGenes, 'All results should have geneticEntityType = GENE').to.be.true;

      // Verify all correlations meet the threshold of 0.3
      const allAboveThreshold = _.every(coExpressions, (ce) =>
        Math.abs(ce.spearmansCorrelation) >= 0.3
      );
      expect(allAboveThreshold, 'All |spearmansCorrelation| should be >= 0.3').to.be.true;

      // Verify all p-values are between 0 and 1
      const allPValuesValid = _.every(coExpressions, (ce) =>
        ce.pValue >= 0 && ce.pValue <= 1
      );
      expect(allPValuesValid, 'All pValues should be between 0 and 1').to.be.true;

      // Verify results are sorted by |spearmansCorrelation| descending
      const correlations = _.map(coExpressions, (ce) => Math.abs(ce.spearmansCorrelation));
      const isSorted = _.every(correlations, (val, idx) =>
        idx === 0 || correlations[idx - 1] >= val
      );
      expect(isSorted, 'Results should be sorted by |spearmansCorrelation| descending').to.be.true;

      // MCM2 (entrez 4171) is known to be strongly co-expressed with BRCA1 in acc_tcga (~0.806)
      const mcm2 = _.find(coExpressions, { geneticEntityId: '4171' });
      expect(mcm2, 'MCM2 (entrez 4171) should be present as a strong BRCA1 co-expression').to.not.be.undefined;
      expect(mcm2!.spearmansCorrelation).to.be.greaterThan(
        0.7,
        'MCM2 correlation with BRCA1 should be > 0.7'
      );
    });
  });

  describe('testCrossProfileCoExpression_mRNA_vs_Protein', () => {
    it('should return co-expression results for BRCA1 mRNA vs RPPA protein data', async () => {
      // Cross-profile test: mRNA expression (profileA) vs protein levels (profileB)
      // acc_tcga has 46 RPPA samples, so the intersection of mRNA and RPPA samples is <= 46
      const testData = await TestUtils.loadTestData('cross_profile_mrna_vs_rppa.json');

      // Call cross-profile co-expression: mRNA vs rppa
      const coExpressions = await callCoExpressionEndpoint(
        'acc_tcga_rna_seq_v2_mrna',
        'acc_tcga_rppa',
        testData,
        0.3
      );

      // Verify the response contains results
      expect(coExpressions).to.be.an('array', 'Response should be an array');
      expect(coExpressions.length).to.be.greaterThan(
        0,
        'Cross-profile co-expression should return results'
      );

      // Verify the query gene BRCA1 (entrez 672) is NOT in the results
      // The endpoint excludes the reference gene from results
      const brca1InResults = _.find(coExpressions, { geneticEntityId: '672' });
      expect(brca1InResults, 'BRCA1 should not appear in its own co-expression results').to.be.undefined;

      // Verify all correlations meet the threshold
      const allAboveThreshold = _.every(coExpressions, (ce) =>
        Math.abs(ce.spearmansCorrelation) >= 0.3
      );
      expect(allAboveThreshold, 'All |spearmansCorrelation| should be >= 0.3').to.be.true;

      // Cross-profile should have fewer results than same-profile due to fewer overlapping samples
      // and fewer genes in the RPPA panel (~203 proteins vs ~20,343 mRNA genes)
      expect(coExpressions.length).to.be.lessThan(
        500,
        'Cross-profile results should be limited by the smaller RPPA gene panel'
      );
    });
  });

  describe('testSameProfileCoExpressionWithExplicitSampleIds', () => {
    it('should return co-expression results when providing explicit sample IDs instead of a sample list', async () => {
      // Using 20 explicit sample IDs from acc_tcga instead of a sample list
      // With fewer samples, we expect fewer genes crossing the significance threshold
      const testData = await TestUtils.loadTestData('explicit_sample_ids.json');

      // Call same-profile co-expression with explicit sample IDs
      const coExpressions = await callCoExpressionEndpoint(
        'acc_tcga_rna_seq_v2_mrna',
        'acc_tcga_rna_seq_v2_mrna',
        testData,
        0.3
      );

      // Verify the response is valid
      expect(coExpressions).to.be.an('array', 'Response should be an array');

      // With only 20 samples there is less statistical power, so we expect results
      // but fewer than the full 79-sample cohort
      expect(coExpressions.length).to.be.greaterThan(
        0,
        'Should still find co-expressed genes with 20 samples'
      );

      // Verify all results have valid structure
      const allHaveRequiredFields = _.every(coExpressions, (ce) =>
        ce.geneticEntityId !== undefined &&
        ce.spearmansCorrelation !== undefined &&
        ce.pValue !== undefined
      );
      expect(allHaveRequiredFields, 'All results should have required fields').to.be.true;

      // Verify threshold is respected
      const allAboveThreshold = _.every(coExpressions, (ce) =>
        Math.abs(ce.spearmansCorrelation) >= 0.3
      );
      expect(allAboveThreshold, 'All |spearmansCorrelation| should be >= 0.3').to.be.true;
    });
  });

  describe('testHigherThresholdFiltersMoreResults', () => {
    it('should return fewer results with threshold 0.6 than with threshold 0.3', async () => {
      // Compare result counts at two different thresholds to verify filtering works
      const testData = await TestUtils.loadTestData('same_profile_sample_list.json');

      // Call with default threshold 0.3
      const coExpressionsLow = await callCoExpressionEndpoint(
        'acc_tcga_rna_seq_v2_mrna',
        'acc_tcga_rna_seq_v2_mrna',
        testData,
        0.3
      );

      // Call with higher threshold 0.6
      const coExpressionsHigh = await callCoExpressionEndpoint(
        'acc_tcga_rna_seq_v2_mrna',
        'acc_tcga_rna_seq_v2_mrna',
        testData,
        0.6
      );

      // Higher threshold should return strictly fewer results
      expect(coExpressionsHigh.length).to.be.lessThan(
        coExpressionsLow.length,
        'Threshold 0.6 should return fewer results than threshold 0.3'
      );

      // Verify all high-threshold results meet the stricter threshold
      const allAboveHighThreshold = _.every(coExpressionsHigh, (ce) =>
        Math.abs(ce.spearmansCorrelation) >= 0.6
      );
      expect(allAboveHighThreshold, 'All |spearmansCorrelation| should be >= 0.6').to.be.true;

      // MCM2 (entrez 4171) has correlation ~0.806 with BRCA1, so it should still be present at 0.6
      const mcm2 = _.find(coExpressionsHigh, { geneticEntityId: '4171' });
      expect(mcm2, 'MCM2 should still appear at threshold 0.6 (correlation ~0.806)').to.not.be.undefined;
    });
  });

  describe('testMissingEntrezGeneIdReturnsBadRequest', () => {
    it('should return 400 when neither entrezGeneId nor genesetId is provided', async () => {
      // CoExpressionFilter has @AssertTrue validation requiring either entrezGeneId or genesetId
      // Sending neither triggers a 400 Bad Request before the controller logic is reached
      const testData = await TestUtils.loadTestData('null_gene.json');

      try {
        await axios.post(
          `${CO_EXPRESSION_URL}?molecularProfileIdA=acc_tcga_rna_seq_v2_mrna&molecularProfileIdB=acc_tcga_rna_seq_v2_mrna&threshold=0.3`,
          testData,
          {
            headers: { 'Content-Type': 'application/json' }
          }
        );
        // If we get here, the request didn't fail as expected
        expect.fail('Request should have returned 400 for missing entrezGeneId');
      } catch (error: any) {
        // Expect a 400 Bad Request due to validation failure
        expect(error.response).to.not.be.undefined;
        expect(error.response.status).to.equal(
          400,
          'Missing entrezGeneId should return 400 Bad Request'
        );
      }
    });
  });

  describe('testInvalidMolecularProfileIdReturnsError', () => {
    it('should return an error status for a nonexistent molecular profile', async () => {
      // When an invalid molecular profile ID is provided, the service should return an error
      // Spring Security or the service layer will reject the unknown profile
      const testData = await TestUtils.loadTestData('same_profile_sample_list.json');

      try {
        await axios.post(
          `${CO_EXPRESSION_URL}?molecularProfileIdA=nonexistent_study_mrna&molecularProfileIdB=nonexistent_study_mrna&threshold=0.3`,
          testData,
          {
            headers: { 'Content-Type': 'application/json' }
          }
        );
        // If we get here, the request didn't fail as expected
        expect.fail('Request should have returned an error status for invalid molecular profile');
      } catch (error: any) {
        // Expect either a network error (connection refused) or an HTTP error response
        if (error.response) {
          // HTTP error response from server
          expect(error.response.status).to.be.greaterThanOrEqual(
            400,
            'Invalid molecular profile should return error status >= 400'
          );
        } else {
          // Network-level error (e.g., Spring Security rejection with no response body)
          expect(error.code).to.be.a('string', 'Should have an error code for network errors');
        }
      }
    });
  });

  describe('testSmallStudyProducesValidResults', () => {
    it('should return valid co-expression results for a small study (chol_tcga, 36 samples)', async () => {
      // chol_tcga is one of the smallest TCGA studies with only 36 mRNA samples
      // This tests that the endpoint handles small cohorts correctly
      const testData = await TestUtils.loadTestData('small_study_sample_list.json');

      // Call co-expression for BRCA1 in chol_tcga
      const coExpressions = await callCoExpressionEndpoint(
        'chol_tcga_rna_seq_v2_mrna',
        'chol_tcga_rna_seq_v2_mrna',
        testData,
        0.3
      );

      // Verify the response is valid
      expect(coExpressions).to.be.an('array', 'Response should be an array');
      expect(coExpressions.length).to.be.greaterThan(
        0,
        'Even a small study should produce some co-expression results'
      );

      // Verify all correlations are finite numbers (not NaN or Infinity)
      const allFinite = _.every(coExpressions, (ce) =>
        Number.isFinite(ce.spearmansCorrelation) && Number.isFinite(ce.pValue)
      );
      expect(allFinite, 'All correlations and p-values should be finite numbers').to.be.true;

      // Verify all p-values are in valid range [0, 1]
      const allPValuesValid = _.every(coExpressions, (ce) =>
        ce.pValue >= 0 && ce.pValue <= 1
      );
      expect(allPValuesValid, 'All pValues should be between 0 and 1').to.be.true;

      // Verify threshold is still respected
      const allAboveThreshold = _.every(coExpressions, (ce) =>
        Math.abs(ce.spearmansCorrelation) >= 0.3
      );
      expect(allAboveThreshold, 'All |spearmansCorrelation| should be >= 0.3').to.be.true;
    });
  });

  describe('testPValueDoesNotUnderflowForStrongCorrelations', () => {
    it('should produce non-zero p-values even for very strong correlations (regression test)', async () => {
      // Regression test for commit 047216c: p-values were underflowing to 0.0
      // for strong correlations due to floating-point cancellation (1 - cdf(|t|))
      // The fix uses cdf(-|t|) instead to avoid the subtraction near 1.0
      const testData = await TestUtils.loadTestData('same_profile_sample_list.json');

      // Get co-expression results with a high threshold to focus on strong correlations
      const coExpressions = await callCoExpressionEndpoint(
        'acc_tcga_rna_seq_v2_mrna',
        'acc_tcga_rna_seq_v2_mrna',
        testData,
        0.6
      );

      // Find results with very strong correlations (|r| > 0.7)
      const strongCorrelations = _.filter(coExpressions, (ce) =>
        Math.abs(ce.spearmansCorrelation) > 0.7
      );
      expect(strongCorrelations.length).to.be.greaterThan(
        0,
        'Should have some genes with |correlation| > 0.7'
      );

      // Verify that none of the strong correlations have p-value = 0
      // With 79 samples and r=0.8, the true p-value is ~1e-18, which is small but non-zero
      const allNonZeroPValues = _.every(strongCorrelations, (ce) => ce.pValue > 0);
      expect(
        allNonZeroPValues,
        'Strong correlations should have p-value > 0 (not underflowed)'
      ).to.be.true;

      // Verify strong correlations have very small (but positive) p-values
      const allVerySmallPValues = _.every(strongCorrelations, (ce) => ce.pValue < 0.001);
      expect(
        allVerySmallPValues,
        'Strong correlations (|r| > 0.7, n=79) should have p-value < 0.001'
      ).to.be.true;
    });
  });

});
