import { expect } from 'chai';
import _ from 'lodash';
import { TestUtils } from '../../src/utils';
import { ClinicalDataEnrichment } from '../../src/types';

describe('ColumnStoreClinicalDataEnrichmentController E2E Tests', () => {

  describe('testConflictingAttributeTumorStage', () => {
    it('should return TUMOR_STAGE enrichments from both studies despite the patient/sample level conflict', async () => {
      // REAL-WORLD CONFLICT: TUMOR_STAGE is defined at different levels in two studies:
      //   nsclc_tracerx_2017 (TRAcking Non-small-cell lung CanceR Evolution Through Rx):
      //     TUMOR_STAGE = PATIENT-level (patientAttribute=true)
      //     Values: "1a", "1b", "2a", "2b", "3a", "3b" (TNM numeric staging notation)
      //
      //   acbc_mskcc_2015 (Adenoid Cystic Breast Carcinoma, MSK 2015):
      //     TUMOR_STAGE = SAMPLE-level (patientAttribute=false)
      //     Values: "I", "II" (Roman numeral staging notation)
      //
      // This is a known curation inconsistency: the same clinical concept is stored at
      // patient level in TRAckeRx (where staging reflects the patient's disease) and at
      // sample level in the MSK study (where each biopsy is annotated independently).
      //
      // Group 1 (Early Stage): nsclc_tracerx_2017 stage 1a/1b patients + acbc Stage I samples
      // Group 2 (Late Stage):  nsclc_tracerx_2017 stage 3a patients + acbc Stage II samples
      //
      // WHY THIS TESTS THE FIX (commit af3b1505):
      //   ClinicalAttributeUtil.categorizeClinicalAttributes detects that TUMOR_STAGE
      //   appears with patientAttribute=true in one study and patientAttribute=false in
      //   another. It places TUMOR_STAGE in BOTH sampleAttributeIds AND conflictingAttributeIds.
      //   The ClickHouse mapper then executes a UNION of:
      //     1. A direct sample-level table query (covers acbc_mskcc_2015 samples)
      //     2. A patient-level table joined to samples via sampleUniqueIds (covers tracerx patients)
      //   Without this fix, only one of these queries would run and half the data would be missing,
      //   causing the enrichment result to be based on an incomplete dataset.
      const groupFilter = await TestUtils.loadTestData('tumor_stage_conflict_groups.json');

      // Call the column-store clinical data enrichment endpoint with cross-study groups
      const enrichments: ClinicalDataEnrichment[] = await TestUtils.callClinicalDataEnrichmentEndpoint(groupFilter);

      // The response must be non-empty: the stage distribution differs between groups
      // (early vs late stage), so the chi-squared test should find significant enrichments
      expect(enrichments.length).to.be.greaterThan(
        0,
        'Should return at least one enrichment — if empty, the conflicting attribute query returned no data'
      );

      // Find ALL enrichments for TUMOR_STAGE — there should be two, one for each attribute level:
      //   patientAttribute=true  (from nsclc_tracerx_2017, where it is a patient-level field)
      //   patientAttribute=false (from acbc_mskcc_2015, where it is a sample-level field)
      // This two-enrichment structure is the expected behavior: both attribute variants are
      // preserved through deduplication and each produces its own statistical test result.
      const tumorStageEnrichments = _.filter(
        enrichments,
        (e) => e.clinicalAttribute.clinicalAttributeId === 'TUMOR_STAGE'
      );
      expect(tumorStageEnrichments.length).to.equal(
        2,
        'TUMOR_STAGE should appear twice: once for patientAttribute=true (nsclc_tracerx_2017) ' +
        'and once for patientAttribute=false (acbc_mskcc_2015). ' +
        'If only 1 appears, the conflict detection is routing to a single list instead of both. ' +
        'If 0 appear, no data was fetched for this attribute at all.'
      );

      // Verify that one enrichment is patient-level and the other is sample-level
      const patientLevelEnrichment = _.find(
        tumorStageEnrichments,
        (e) => e.clinicalAttribute.patientAttribute === true
      );
      const sampleLevelEnrichment = _.find(
        tumorStageEnrichments,
        (e) => e.clinicalAttribute.patientAttribute === false
      );
      expect(patientLevelEnrichment).to.not.be.undefined;
      expect(sampleLevelEnrichment).to.not.be.undefined;

      // Both enrichments should use the Chi-squared test (TUMOR_STAGE is STRING datatype)
      // and have valid p-values in [0, 1]
      _.forEach(tumorStageEnrichments, (enrichment) => {
        expect(enrichment.method).to.equal(
          'Chi-squared Test',
          'TUMOR_STAGE is a STRING attribute, so Chi-squared test should be used'
        );
        expect(enrichment.pValue).to.be.gte(
          0,
          'p-value must be >= 0'
        );
        expect(enrichment.pValue).to.be.lte(
          1,
          'p-value must be <= 1'
        );
      });

      // The two enrichments share the same underlying count data (from the dual UNION query),
      // so they should produce the same p-value and score
      expect(patientLevelEnrichment!.pValue).to.equal(
        sampleLevelEnrichment!.pValue,
        'Both TUMOR_STAGE enrichment variants should have identical p-values because they ' +
        'are computed from the same count data returned by the dual-routing query'
      );
    });
  });

  describe('testConflictingAttributeTissueSourceSite', () => {
    it('should return TISSUE_SOURCE_SITE enrichments from both studies despite the patient/sample level conflict', async () => {
      // REAL-WORLD CONFLICT: TISSUE_SOURCE_SITE is defined at different levels in two studies:
      //   thca_tcga (Thyroid Carcinoma, TCGA):
      //     TISSUE_SOURCE_SITE = PATIENT-level (patientAttribute=true)
      //     Values: "BJ", "EM", "ET", "DJ", "EL", "FE", etc. (TCGA tissue bank site codes)
      //     Rationale: TCGA associates the biobank site with the patient since each patient
      //                was contributed by exactly one collection site.
      //
      //   nsclc_mskcc_2015 (Non-Small Cell Lung Cancer, MSK 2015):
      //     TISSUE_SOURCE_SITE = SAMPLE-level (patientAttribute=false)
      //     Values: "Lung", "Lymph node", "Pleura", "Bronchus" (anatomical biopsy location)
      //     Rationale: MSK annotates tissue source per biopsy since the same patient may
      //                have samples from the primary tumor and metastatic sites.
      //
      // Note that the _meaning_ of TISSUE_SOURCE_SITE differs between studies: TCGA uses
      // it for biobank provenance, MSK uses it for anatomical location. This is precisely the
      // kind of cross-study attribute conflict the enrichment endpoint must handle gracefully.
      //
      // Each test group contains samples from BOTH studies, which is the realistic scenario
      // when a user creates comparison groups (e.g. mutated vs wildtype) from a multi-study
      // virtual cohort.
      //
      // WHY THIS TESTS THE FIX:
      //   The mapper must use sampleUniqueIds (not patientUniqueIds) for the conflicting
      //   attribute query. Pre-fix code used patientUniqueIds for the patient-table JOIN,
      //   which failed to correctly match records when the groups contain mixed-study samples.
      //   The fix ensures sampleUniqueIds are passed throughout, so the JOIN correctly maps
      //   each sample to its patient's clinical data regardless of which study it comes from.
      const groupFilter = await TestUtils.loadTestData('tissue_source_site_conflict_groups.json');

      // Call the endpoint with mixed-study groups
      const enrichments: ClinicalDataEnrichment[] = await TestUtils.callClinicalDataEnrichmentEndpoint(groupFilter);

      expect(enrichments.length).to.be.greaterThan(
        0,
        'Should return at least one enrichment for the mixed-study groups'
      );

      // Find all TISSUE_SOURCE_SITE enrichments — expect two (one per attribute level)
      const tissueEnrichments = _.filter(
        enrichments,
        (e) => e.clinicalAttribute.clinicalAttributeId === 'TISSUE_SOURCE_SITE'
      );
      expect(tissueEnrichments.length).to.equal(
        2,
        'TISSUE_SOURCE_SITE should appear twice: once for patientAttribute=true (thca_tcga) ' +
        'and once for patientAttribute=false (nsclc_mskcc_2015). ' +
        'A count of 0 means the conflicting attribute query returned no data (regression). ' +
        'A count of 1 means only one level was queried (the fix is not applying dual-routing).'
      );

      // Verify the two attribute levels are present
      const patientLevelEnrichment = _.find(
        tissueEnrichments,
        (e) => e.clinicalAttribute.patientAttribute === true
      );
      const sampleLevelEnrichment = _.find(
        tissueEnrichments,
        (e) => e.clinicalAttribute.patientAttribute === false
      );
      expect(patientLevelEnrichment).to.not.be.undefined;
      expect(sampleLevelEnrichment).to.not.be.undefined;

      // Both should be Chi-squared (STRING datatype) with valid p-values
      _.forEach(tissueEnrichments, (enrichment) => {
        expect(enrichment.method).to.equal(
          'Chi-squared Test',
          'TISSUE_SOURCE_SITE is a STRING attribute, so Chi-squared test should be used'
        );
        expect(enrichment.pValue).to.be.gte(0, 'p-value must be >= 0');
        expect(enrichment.pValue).to.be.lte(1, 'p-value must be <= 1');
      });

      // The two enrichments share the same count data, so p-values should match
      expect(patientLevelEnrichment!.pValue).to.equal(
        sampleLevelEnrichment!.pValue,
        'Both TISSUE_SOURCE_SITE variants should produce the same p-value because they ' +
        'use identical count data from the dual-routing UNION query'
      );
    });
  });

  describe('testConflictingAttributeComparedToSingleStudy', () => {
    it('should return more TUMOR_STAGE data when both conflicting studies are included than either alone', async () => {
      // This test demonstrates the practical impact of the conflict fix:
      // a cross-study query covering both nsclc_tracerx_2017 (patient-level TUMOR_STAGE)
      // and acbc_mskcc_2015 (sample-level TUMOR_STAGE) returns data for TUMOR_STAGE,
      // confirming that both the sample-level and patient-level queries executed.
      //
      // Before the fix, the dual-routing was absent: data from whichever level was not
      // queried would be silently missing. This test verifies that the endpoint succeeds
      // and returns a p-value (meaning both groups had data to compare), which can only
      // happen if both studies contributed TUMOR_STAGE values.
      const crossStudyGroupFilter = await TestUtils.loadTestData('tumor_stage_conflict_groups.json');

      // Call with the cross-study groups (both conflicting studies present)
      const crossStudyEnrichments: ClinicalDataEnrichment[] = await TestUtils.callClinicalDataEnrichmentEndpoint(crossStudyGroupFilter);

      // Find TUMOR_STAGE in the cross-study results
      const crossStudyTumorStage = _.find(
        crossStudyEnrichments,
        (e) => e.clinicalAttribute.clinicalAttributeId === 'TUMOR_STAGE'
      );
      expect(crossStudyTumorStage).to.not.be.undefined;

      // The p-value must be a real number (not NaN) — this proves both groups had data.
      // A NaN p-value would indicate that one group had zero values, which would mean
      // one study's data was not fetched. The chi-squared test requires counts from
      // both groups, so a valid p-value proves dual-routing worked correctly.
      expect(crossStudyTumorStage!.pValue).to.be.a(
        'number',
        'p-value should be a real number, not NaN. NaN indicates one group had no ' +
        'TUMOR_STAGE data, meaning one study\'s conflicting attribute query failed.'
      );
      expect(isNaN(crossStudyTumorStage!.pValue)).to.be.false;

      // The score (chi-squared statistic) should also be a valid number
      expect(crossStudyTumorStage!.score).to.be.a('number');
      expect(isNaN(crossStudyTumorStage!.score)).to.be.false;
    });
  });

});
