import { expect } from 'chai';
import _ from 'lodash';
import { TestUtils } from '../../src/utils';
import { EnrichmentType } from '../../src/types';

describe('AlterationEnrichmentController E2E Tests', () => {

  describe('testFetchAlterationEnrichmentsWithDataJson', () => {
    it('should handle combination comparison session with two studies (WES and IMPACT)', async () => {
      // This combination comparison session has two studies, one WES and the other IMPACT
      // 104 samples total, 92 of which belong to WES study. 14 samples should be profiled for only IMPACT genes
      // NOTE that of 92, only 91 are profiled
      const testDataJson = await TestUtils.loadTestData('all_alterations.json');
      const enrichments = await TestUtils.callEnrichmentEndpoint(testDataJson);

      // Find the SPSB1 gene enrichment from the results using lodash
      const spsb1Enrichment = _.find(enrichments, { hugoGeneSymbol: 'SPSB1' });
      expect(spsb1Enrichment, 'SPSB1 enrichment should be present in response').to.not.be.undefined;

      // Calculate total profiled samples across all groups by summing profiledCount from counts array
      const spsb1TotalProfiled = _.sumBy(spsb1Enrichment!.counts, 'profiledCount');
      expect(spsb1TotalProfiled).to.equal(
        91,
        'SPSB1 should have 91 total profiled samples across all groups'
      );

      // Find the TP53 gene enrichment from the results
      const tp53Enrichment = _.find(enrichments, { hugoGeneSymbol: 'TP53' });
      expect(tp53Enrichment, 'TP53 enrichment should be present in response').to.not.be.undefined;

      // Calculate total profiled samples for TP53
      const tp53TotalProfiled = _.sumBy(tp53Enrichment!.counts, 'profiledCount');
      expect(tp53TotalProfiled).to.equal(
        103,
        'TP53 should have 103 total profiled samples across all groups because it is in IMPACT'
      );
    });
  });

  describe('testFetchAlterationEnrichmentsWithDataJsonCNAOnly', () => {
    it('should filter out mutation and structural variant profiles', async () => {
      // This combination comparison session has two studies, one WES and the other IMPACT
      // 104 samples total, 92 of which belong to WES study. 14 samples should be profiled for only IMPACT genes
      // NOTE that of 92, only 91 are profiled
      const testData = await TestUtils.loadTestData('all_alterations.json');

      // Filter out mutation and structural variant profiles from molecularProfileCaseIdentifiers
      const groupsArray = testData.molecularProfileCasesGroupFilter;

      // Filter each group's molecularProfileCaseIdentifiers to exclude mutation and structural variant profiles
      _.forEach(groupsArray, (group) => {
        // Keep only CNA profiles (exclude profiles ending with _mutations or _structural_variants)
        group.molecularProfileCaseIdentifiers = _.filter(
          group.molecularProfileCaseIdentifiers,
          (identifier: any) => {
            const profileId = identifier.molecularProfileId;
            return !_.endsWith(profileId, '_mutations') && !_.endsWith(profileId, '_structural_variants');
          }
        );
      });

      const enrichments = await TestUtils.callEnrichmentEndpoint(testData);

      // Find the SPSB1 gene enrichment from the filtered results
      const spsb1Enrichment = _.find(enrichments, { hugoGeneSymbol: 'SPSB1' });
      expect(spsb1Enrichment, 'SPSB1 enrichment should be present in response').to.not.be.undefined;

      // Calculate total profiled samples by summing profiledCount across all counts
      const spsb1TotalProfiled = _.sumBy(spsb1Enrichment!.counts, 'profiledCount');
      expect(spsb1TotalProfiled).to.equal(
        89,
        'SPSB1 should have 89 total profiled samples across all groups'
      );

      // Find the TP53 gene enrichment
      const tp53Enrichment = _.find(enrichments, { hugoGeneSymbol: 'TP53' });
      expect(tp53Enrichment, 'TP53 enrichment should be present in response').to.not.be.undefined;

      // Calculate total profiled samples for TP53
      const tp53TotalProfiled = _.sumBy(tp53Enrichment!.counts, 'profiledCount');
      expect(tp53TotalProfiled).to.equal(
        89,
        'TP53 should have 89 total profiled samples across all groups'
      );
    });
  });

  describe('testFetchAlterationEnrichmentsWithDataJsonNoMissense', () => {
    it('should correctly filter out missense mutations when disabled', async () => {
      // This combination comparison session has two studies, one WES and the other IMPACT
      // 104 samples total, 92 of which belong to WES study. 14 samples should be profiled for only IMPACT genes
      // NOTE that of 92, only 91 are profiled
      const testData = await TestUtils.loadTestData('all_alterations.json');

      // Call with unmodified testData first to establish baseline
      const rawEnrichments = await TestUtils.callEnrichmentEndpoint(testData);

      // Find ANP32E gene enrichment in baseline results
      const rawAnp32eEnrichment = _.find(rawEnrichments, { hugoGeneSymbol: 'ANP32E' });
      expect(rawAnp32eEnrichment, 'ANP32E enrichment should be present in raw response').to.not.be.undefined;

      // Calculate total altered samples by summing alteredCount across all groups
      const rawAnp32eTotalAltered = _.sumBy(rawAnp32eEnrichment!.counts, 'alteredCount');
      expect(rawAnp32eTotalAltered).to.equal(
        4,
        'ANP32E should have 4 altered samples with all mutations enabled'
      );

      // Deep clone the test data to avoid mutating the original
      const modifiedData = _.cloneDeep(testData);

      // Disable missense_mutation in the cloned data
      modifiedData.alterationEventTypes.mutationEventTypes.missense_mutation = false;

      // Call enrichment endpoint with missense mutations disabled
      const enrichments = await TestUtils.callEnrichmentEndpoint(modifiedData);

      // Find ANP32E gene enrichment in filtered results
      const anp32eEnrichment = _.find(enrichments, { hugoGeneSymbol: 'ANP32E' });
      expect(anp32eEnrichment, 'ANP32E enrichment should be present in response').to.not.be.undefined;

      // Calculate total altered samples with missense disabled
      const anp32eTotalAltered = _.sumBy(anp32eEnrichment!.counts, 'alteredCount');
      expect(anp32eTotalAltered).to.equal(
        3,
        'ANP32E should have 3 altered samples with missense_mutation disabled'
      );
    });
  });

  describe('testFetchAlterationEnrichmentsWithMultiPanel', () => {
    it('should handle samples covered by 2 different panels', async () => {
      // This comparison session is of 33 samples (from a single study) which are covered by 2 different panels
      const testDataJson = await TestUtils.loadTestData('multi_panel.json');
      const enrichments = await TestUtils.callEnrichmentEndpoint(testDataJson);

      // Verify all genes have exactly 4 groups using lodash every
      const allHaveFourGroups = _.every(enrichments, (enrichment) => enrichment.counts.length === 4);
      expect(allHaveFourGroups, 'All genes should have exactly 4 groups').to.be.true;

      // Verify each gene has at least one group with an alteration
      const allHaveAlterations = _.every(enrichments, (enrichment) =>
        // Use lodash some to check if any count has alteredCount > 0
        _.some(enrichment.counts, (count) => count.alteredCount > 0)
      );
      expect(allHaveAlterations, 'Each gene should have at least one group with an alteration').to.be.true;

      // Find TP53I13 gene enrichment
      const tp53i13Enrichment = _.find(enrichments, { hugoGeneSymbol: 'TP53I13' });
      expect(tp53i13Enrichment, 'TP53I13 enrichment should be present in response').to.not.be.undefined;

      // Of 33 samples, 26 are covered by WES panel for mutation, so only those will be profiled for
      // genes which are not covered by panel
      const tp53i13TotalProfiled = _.sumBy(tp53i13Enrichment!.counts, 'profiledCount');
      expect(tp53i13TotalProfiled).to.equal(
        26,
        'TP53I13 should have 26 total profiled samples across all groups'
      );

      // Find TP53 gene enrichment
      const tp53Enrichment = _.find(enrichments, { hugoGeneSymbol: 'TP53' });
      expect(tp53Enrichment, 'TP53 enrichment should be present in response').to.not.be.undefined;

      // TP53 is in IMPACT panel so all 33 samples should be profiled
      const tp53TotalProfiled = _.sumBy(tp53Enrichment!.counts, 'profiledCount');
      expect(tp53TotalProfiled).to.equal(
        33,
        'TP53 should have 33 total profiled samples across all groups because it is in IMPACT'
      );
    });
  });

  describe('testFetchAlterationFilteringByAlterationType', () => {
    it('should correctly filter alterations by type', async () => {
      const testDataJson = await TestUtils.loadTestData('multi_panel.json');
      const enrichments = await TestUtils.callEnrichmentEndpoint(testDataJson);

      // Find TP53 gene enrichment
      const tp53Enrichment = _.find(enrichments, { hugoGeneSymbol: 'TP53' });
      expect(tp53Enrichment, 'TP53 enrichment should be present in response').to.not.be.undefined;

      // Calculate total altered samples by summing alteredCount across all groups
      const tp53TotalAltered = _.sumBy(tp53Enrichment!.counts, 'alteredCount');
      expect(tp53TotalAltered).to.equal(
        24,
        'TP53 should have 24 total altered samples across all groups'
      );
    });
  });

  describe('testFetchAlterationEnrichmentsExcludingMissenseMutations', () => {
    it('should have fewer altered samples when missense mutations are excluded', async () => {
      const testData = await TestUtils.loadTestData('multi_panel.json');

      // Get baseline results without filter
      const originalEnrichments = await TestUtils.callEnrichmentEndpoint(testData);

      // Find TP53 in original results
      const originalTp53Enrichment = _.find(originalEnrichments, { hugoGeneSymbol: 'TP53' });
      expect(originalTp53Enrichment, 'TP53 enrichment should be present in original response').to.not.be.undefined;

      // Calculate original total altered samples using lodash sumBy
      const originalTotalAlteredSamples = _.sumBy(originalTp53Enrichment!.counts, 'alteredCount');

      // Deep clone test data to avoid mutating original
      const modifiedData = _.cloneDeep(testData);

      // Disable all missense mutation variants
      const mutationEventTypes = modifiedData.alterationEventTypes.mutationEventTypes;
      mutationEventTypes.missense = false;
      mutationEventTypes.missense_mutation = false;
      mutationEventTypes.missense_variant = false;

      // Get filtered results with missense mutations excluded
      const filteredEnrichments = await TestUtils.callEnrichmentEndpoint(modifiedData);

      // Find TP53 in filtered results
      const filteredTp53Enrichment = _.find(filteredEnrichments, { hugoGeneSymbol: 'TP53' });
      expect(filteredTp53Enrichment, 'TP53 enrichment should be present in filtered response').to.not.be.undefined;

      // Calculate filtered total altered samples
      const filteredTotalAlteredSamples = _.sumBy(filteredTp53Enrichment!.counts, 'alteredCount');

      // Verify filtering worked - filtered count should be less than original
      expect(filteredTotalAlteredSamples).to.be.lessThan(
        originalTotalAlteredSamples,
        `TP53 should have fewer altered samples when missense mutations are excluded. Original: ${originalTotalAlteredSamples}, Filtered: ${filteredTotalAlteredSamples}`
      );

      // Verify exact expected count after filtering
      expect(filteredTotalAlteredSamples).to.equal(
        12,
        'TP53 should have 12 altered samples when missense mutations are excluded'
      );

      // Verify that filtered response has fewer genes (genes with only missense mutations should be excluded)
      expect(filteredEnrichments.length).to.be.lessThan(
        originalEnrichments.length,
        `Filtered response should have fewer genes than original. Original: ${originalEnrichments.length}, Filtered: ${filteredEnrichments.length} (genes with only missense mutations should be excluded)`
      );
    });
  });

  describe('testFetchAlterationEnrichmentsPatientVSample', () => {
    it('should produce different results for SAMPLE vs PATIENT enrichment types', async () => {
      // Test data from https://www.cbioportal.org/comparison/alterations?comparisonId=6184fd03f8f71021ce56e3ff
      const testDataJsonSample = await TestUtils.loadTestData('sample.json');
      const testDataJsonPatient = await TestUtils.loadTestData('patient.json');

      // Get F8 enrichment with SAMPLE enrichment type
      const sampleEnrichments = await TestUtils.callEnrichmentEndpoint(
        testDataJsonSample,
        EnrichmentType.SAMPLE
      );

      // Find F8 gene in sample-level enrichments
      const f8SampleEnrichment = _.find(sampleEnrichments, { hugoGeneSymbol: 'F8' });
      expect(f8SampleEnrichment, 'F8 enrichment should be present in SAMPLE response').to.not.be.undefined;

      // Get F8 enrichment with PATIENT enrichment type
      const patientEnrichments = await TestUtils.callEnrichmentEndpoint(
        testDataJsonPatient,
        EnrichmentType.PATIENT
      );

      // Find F8 gene in patient-level enrichments
      const f8PatientEnrichment = _.find(patientEnrichments, { hugoGeneSymbol: 'F8' });
      expect(f8PatientEnrichment, 'F8 enrichment should be present in PATIENT response').to.not.be.undefined;

      // Calculate counts for sample-level using lodash sumBy
      const sampleProfiledCount = _.sumBy(f8SampleEnrichment!.counts, 'profiledCount');
      const sampleAlteredCount = _.sumBy(f8SampleEnrichment!.counts, 'alteredCount');

      // Calculate counts for patient-level using lodash sumBy
      const patientProfiledCount = _.sumBy(f8PatientEnrichment!.counts, 'profiledCount');
      const patientAlteredCount = _.sumBy(f8PatientEnrichment!.counts, 'alteredCount');

      // Verify that SAMPLE and PATIENT enrichment types produce different results
      expect(sampleAlteredCount).to.equal(31, 'F8 should have 31 altered samples in SAMPLE mode');
      expect(patientAlteredCount).to.equal(14, 'F8 should have 14 altered patients in PATIENT mode');
      expect(sampleProfiledCount).to.equal(447, 'F8 should have 447 profiled samples in SAMPLE mode');
      expect(patientProfiledCount).to.equal(100, 'F8 should have 100 profiled patients in PATIENT mode');
    });
  });

});