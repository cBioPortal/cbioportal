import { expect } from 'chai';
import _ from 'lodash';
import { TestUtils } from '../../src/utils';
import { ClinicalDataCountItem } from '../../src/types';

describe('ColumnStoreClinicalDataCountsController E2E Tests', () => {

  describe('testConflictingAttributeTissueSourceSiteCounts', () => {
    it('should return TISSUE_SOURCE_SITE counts from both patient-level and sample-level studies', async () => {
      // REAL-WORLD CONFLICT: TISSUE_SOURCE_SITE is defined at different levels in two studies:
      //   thca_tcga (Thyroid Carcinoma, TCGA):
      //     TISSUE_SOURCE_SITE = PATIENT-level (patientAttribute=true)
      //     Values: "BJ", "EM", "DO", etc. (TCGA tissue bank site codes)
      //
      //   nsclc_mskcc_2015 (Non-Small Cell Lung Cancer, MSK 2015):
      //     TISSUE_SOURCE_SITE = SAMPLE-level (patientAttribute=false)
      //     Values: "Lung", "Lymph node", "Pleura", etc. (anatomical biopsy location)
      //
      // WHY THIS TESTS THE FIX (GetClinicalDataCountsUseCase / getClinicalDataCountsByStudyViewFilter):
      //   ClinicalAttributeUtil.categorizeClinicalAttributes detects TISSUE_SOURCE_SITE appears
      //   as patient-level in thca_tcga and sample-level in nsclc_mskcc_2015 — so it is placed
      //   in BOTH sampleAttributeIds AND conflictingAttributeIds. The mapper then issues a
      //   three-branch UNION:
      //     1. sample-level table query       (covers nsclc_mskcc_2015 samples)
      //     2. patient-level table query      (not triggered for pure patients here)
      //     3. conflicting-attribute query:   patient-level table JOINed to sample_derived
      //                                       (covers thca_tcga patients via their samples)
      //   Without the fix, the conflicting branch was absent: data from thca_tcga would be
      //   silently missing, causing only Lung/Lymph node values to appear and BJ/EM/DO to
      //   disappear. With the fix, all five distinct values must appear.
      //
      // Test dataset (19 samples, all having TISSUE_SOURCE_SITE values — NA count = 0):
      //   thca_tcga patient-level:  BJ=5, EM=3, DO=2  (10 samples)
      //   nsclc_mskcc_2015 sample-level: Lung=5, Lymph node=4  (9 samples)
      const filter = await TestUtils.loadTestData('tissue_source_site_counts_filter.json');

      // Call the column-store clinical-data-counts endpoint with the mixed-study filter
      const items: ClinicalDataCountItem[] = await TestUtils.callClinicalDataCountsEndpoint(filter);

      // The response must include at least TISSUE_SOURCE_SITE
      expect(items.length).to.be.greaterThan(
        0,
        'Should return at least one ClinicalDataCountItem — empty result means the query returned no data'
      );

      // Find the TISSUE_SOURCE_SITE count item in the response
      const tissueItem = _.find(items, (item) => item.attributeId === 'TISSUE_SOURCE_SITE');
      expect(tissueItem).to.not.be.undefined;

      // --- Verify patient-level values from thca_tcga are present ---
      // These values ("BJ", "EM", "DO") live in the patient clinical data table.
      // They can only appear if the conflicting-attribute branch of the query ran and
      // JOINed the patient table to sample_derived to map patient data to the sample filter.

      const bjCount = _.find(tissueItem!.counts, (c) => c.value === 'BJ');
      expect(bjCount).to.not.be.undefined;
      expect(bjCount!.count).to.equal(
        5,
        'BJ is a PATIENT-level TISSUE_SOURCE_SITE value from thca_tcga. ' +
        'Count=5 because 5 BJ-site thca_tcga samples are in the filter. ' +
        'If undefined or 0, the patient-table conflicting-attribute branch did not run.'
      );

      const emCount = _.find(tissueItem!.counts, (c) => c.value === 'EM');
      expect(emCount).to.not.be.undefined;
      expect(emCount!.count).to.equal(
        3,
        'EM is a PATIENT-level TISSUE_SOURCE_SITE value from thca_tcga — 3 samples expected.'
      );

      const doCount = _.find(tissueItem!.counts, (c) => c.value === 'DO');
      expect(doCount).to.not.be.undefined;
      expect(doCount!.count).to.equal(
        2,
        'DO is a PATIENT-level TISSUE_SOURCE_SITE value from thca_tcga — 2 samples expected.'
      );

      // --- Verify sample-level values from nsclc_mskcc_2015 are present ---
      // These values ("Lung", "Lymph node") live in the sample clinical data table.
      // They can only appear if the sample-level branch of the query ran against the
      // nsclc_mskcc_2015 samples.

      const lungCount = _.find(tissueItem!.counts, (c) => c.value === 'Lung');
      expect(lungCount).to.not.be.undefined;
      expect(lungCount!.count).to.equal(
        5,
        'Lung is a SAMPLE-level TISSUE_SOURCE_SITE value from nsclc_mskcc_2015 — 5 samples expected.'
      );

      const lymphNodeCount = _.find(tissueItem!.counts, (c) => c.value === 'Lymph node');
      expect(lymphNodeCount).to.not.be.undefined;
      expect(lymphNodeCount!.count).to.equal(
        4,
        'Lymph node is a SAMPLE-level TISSUE_SOURCE_SITE value from nsclc_mskcc_2015 — 4 samples expected.'
      );

      // --- Verify total count (no NA expected since all 19 samples have TISSUE_SOURCE_SITE) ---
      // Sum of real values: BJ(5) + EM(3) + DO(2) + Lung(5) + Lymph node(4) = 19
      // All 19 samples in the filter have TISSUE_SOURCE_SITE data, so the NA bucket should be 0.
      const naEntry = _.find(tissueItem!.counts, (c) => c.value === 'NA');
      const naCount = naEntry ? naEntry.count : 0;
      expect(naCount).to.equal(
        0,
        'All 19 samples in the filter have TISSUE_SOURCE_SITE data; NA count should be 0'
      );

      // Total non-NA count should equal 19 (all samples accounted for)
      const nonNaCounts = _.filter(tissueItem!.counts, (c) => c.value !== 'NA');
      const totalCount = _.sumBy(nonNaCounts, 'count');
      expect(totalCount).to.equal(
        19,
        'Total non-NA count should be 19: BJ(5)+EM(3)+DO(2)+Lung(5)+Lymph node(4). ' +
        'A lower total means some samples had their data silently dropped by a broken query branch.'
      );
    });
  });

  describe('testConflictingAttributeGradeCounts', () => {
    it('should return GRADE counts from both patient-level and sample-level studies', async () => {
      // REAL-WORLD CONFLICT: GRADE is defined at different levels in two bladder cancer studies:
      //   blca_tcga (Bladder Urothelial Carcinoma, TCGA):
      //     GRADE = PATIENT-level (patientAttribute=true)
      //     Values: "High Grade", "Low Grade"
      //
      //   blca_bgi (Bladder Urothelial Carcinoma, BGI):
      //     GRADE = SAMPLE-level (patientAttribute=false)
      //     Values: "1", "2", "3"  (numeric grading system)
      //
      // WHY THIS TESTS THE FIX:
      //   This is a second independent conflict that exercises the same code path:
      //   GRADE is categorized as both sampleAttributeId and conflictingAttributeId.
      //   The patient-level branch (blca_tcga) and the sample-level branch (blca_bgi)
      //   must both run for all five distinct values to appear in the result.
      //   Failing the fix causes either the text grades (High/Low Grade from blca_tcga)
      //   or the numeric grades (1/2 from blca_bgi) to be missing.
      //
      // Test dataset (13 samples, all with GRADE values — NA count = 0):
      //   blca_tcga patient-level:   High Grade=5, Low Grade=3  (8 samples)
      //   blca_bgi  sample-level:    1=4, 2=1                   (5 samples)
      const filter = await TestUtils.loadTestData('grade_counts_filter.json');

      // Call the column-store clinical-data-counts endpoint
      const items: ClinicalDataCountItem[] = await TestUtils.callClinicalDataCountsEndpoint(filter);

      expect(items.length).to.be.greaterThan(0, 'Should return at least one count item');

      // Find the GRADE count item
      const gradeItem = _.find(items, (item) => item.attributeId === 'GRADE');
      expect(gradeItem).to.not.be.undefined;

      // --- Verify patient-level values from blca_tcga ---
      // "High Grade" and "Low Grade" are stored in the patient table of blca_tcga.
      // These only appear if the conflicting-attribute branch JOINed patient data to samples.

      const highGradeCount = _.find(gradeItem!.counts, (c) => c.value === 'High Grade');
      expect(highGradeCount).to.not.be.undefined;
      expect(highGradeCount!.count).to.equal(
        5,
        '"High Grade" is a PATIENT-level GRADE value from blca_tcga — 5 samples expected. ' +
        'Missing means the patient-table branch of the conflicting-attribute query did not run.'
      );

      const lowGradeCount = _.find(gradeItem!.counts, (c) => c.value === 'Low Grade');
      expect(lowGradeCount).to.not.be.undefined;
      expect(lowGradeCount!.count).to.equal(
        3,
        '"Low Grade" is a PATIENT-level GRADE value from blca_tcga — 3 samples expected.'
      );

      // --- Verify sample-level values from blca_bgi ---
      // "1" and "2" are stored in the sample table of blca_bgi.
      // These only appear if the sample-level branch ran against blca_bgi samples.

      const grade1Count = _.find(gradeItem!.counts, (c) => c.value === '1');
      expect(grade1Count).to.not.be.undefined;
      expect(grade1Count!.count).to.equal(
        4,
        '"1" is a SAMPLE-level GRADE value from blca_bgi — 4 samples expected. ' +
        'Missing means the sample-table branch of the conflicting-attribute query did not run.'
      );

      const grade2Count = _.find(gradeItem!.counts, (c) => c.value === '2');
      expect(grade2Count).to.not.be.undefined;
      expect(grade2Count!.count).to.equal(
        1,
        '"2" is a SAMPLE-level GRADE value from blca_bgi — 1 sample expected.'
      );

      // --- Verify total count (no NA expected since all 13 samples have GRADE) ---
      // Non-NA sum: High Grade(5) + Low Grade(3) + 1(4) + 2(1) = 13
      const naEntry = _.find(gradeItem!.counts, (c) => c.value === 'NA');
      const naCount = naEntry ? naEntry.count : 0;
      expect(naCount).to.equal(
        0,
        'All 13 samples in the filter have GRADE data; NA count should be 0'
      );

      const nonNaCounts = _.filter(gradeItem!.counts, (c) => c.value !== 'NA');
      const totalCount = _.sumBy(nonNaCounts, 'count');
      expect(totalCount).to.equal(
        13,
        'Total non-NA count should be 13: High Grade(5)+Low Grade(3)+1(4)+2(1). ' +
        'A total below 13 indicates one branch of the conflicting-attribute query was skipped.'
      );
    });
  });

  describe('testConflictingAttributeCountsCompleteness', () => {
    it('should return non-zero counts for all five distinct TISSUE_SOURCE_SITE values proving both query branches ran', async () => {
      // This test focuses on the completeness guarantee: all five expected distinct values
      // must have non-zero counts. A single missing value is enough to prove a regression.
      //
      // The five values come from two separate query branches:
      //   Patient-level branch (thca_tcga):  BJ, EM, DO
      //   Sample-level branch (nsclc_mskcc_2015): Lung, Lymph node
      //
      // If the fix regresses, one of the two branches will not run, and the corresponding
      // three or two values will be absent. We assert ALL five are present and non-zero.
      const filter = await TestUtils.loadTestData('tissue_source_site_counts_filter.json');

      // Call the endpoint
      const items: ClinicalDataCountItem[] = await TestUtils.callClinicalDataCountsEndpoint(filter);

      // Find the TISSUE_SOURCE_SITE item
      const tissueItem = _.find(items, (item) => item.attributeId === 'TISSUE_SOURCE_SITE');
      expect(tissueItem).to.not.be.undefined;

      // The expected values and their source — split by query branch for diagnostic clarity
      const expectedPatientLevelValues = ['BJ', 'EM', 'DO'];
      const expectedSampleLevelValues = ['Lung', 'Lymph node'];

      // Assert every patient-level value is present and has count > 0
      // Failure here indicates the conflicting-attribute patient JOIN branch did not run
      _.forEach(expectedPatientLevelValues, (expectedValue) => {
        const countEntry = _.find(tissueItem!.counts, (c) => c.value === expectedValue);
        expect(countEntry).to.not.be.undefined;
        expect(countEntry!.count).to.be.greaterThan(
          0,
          `Patient-level TISSUE_SOURCE_SITE value "${expectedValue}" (from thca_tcga) ` +
          'should have count > 0. A missing or zero count means the patient-JOIN branch ' +
          'of getClinicalDataCountsByStudyViewFilter did not run for conflicting attributes.'
        );
      });

      // Assert every sample-level value is present and has count > 0
      // Failure here indicates the sample-level branch did not run for nsclc_mskcc_2015
      _.forEach(expectedSampleLevelValues, (expectedValue) => {
        const countEntry = _.find(tissueItem!.counts, (c) => c.value === expectedValue);
        expect(countEntry).to.not.be.undefined;
        expect(countEntry!.count).to.be.greaterThan(
          0,
          `Sample-level TISSUE_SOURCE_SITE value "${expectedValue}" (from nsclc_mskcc_2015) ` +
          'should have count > 0. A missing or zero count means the sample-level branch ' +
          'of getClinicalDataCountsByStudyViewFilter did not run.'
        );
      });

      // Total distinct values present (excluding NA) should be exactly 5
      const nonNaCounts = _.filter(tissueItem!.counts, (c) => c.value !== 'NA');
      expect(nonNaCounts.length).to.equal(
        5,
        'Exactly 5 distinct non-NA TISSUE_SOURCE_SITE values should appear: ' +
        'BJ, EM, DO (patient-level) + Lung, Lymph node (sample-level). ' +
        'Fewer than 5 means at least one query branch produced no output.'
      );
    });
  });

});
