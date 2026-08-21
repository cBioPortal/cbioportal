import { expect } from 'chai';
import _ from 'lodash';
import axios from 'axios';
import { ClinicalDataDTO, ProjectionType } from '../../src/types';
import { config } from '../../src/config';
import { TestUtils } from '../../src/utils';

/**
 * E2E coverage for the streaming POST /api/clinical-data/fetch endpoint.
 *
 * This endpoint returns a StreamingResponseBody, which bypasses the UniqueKeyInterceptor,
 * so the controller's stream consumer must populate uniqueSampleKey / uniquePatientKey itself.
 * These tests pin both the data values and the derived keys so a regression that drops the
 * keys (as happens if the consumer just writes the raw datum) is caught.
 */
describe('ClinicalDataController E2E Tests (streaming /fetch)', () => {
  // Two acc_tcga samples, two sample-level attributes.
  const STUDY_ID = 'acc_tcga';
  const SAMPLE_1 = 'TCGA-OR-A5J1-01';
  const SAMPLE_2 = 'TCGA-OR-A5J2-01';
  // base64("TCGA-OR-A5J1-01:acc_tcga") and base64("TCGA-OR-A5J2-01:acc_tcga")
  const SAMPLE_1_KEY = 'VENHQS1PUi1BNUoxLTAxOmFjY190Y2dh';
  const SAMPLE_2_KEY = 'VENHQS1PUi1BNUoyLTAxOmFjY190Y2dh';

  async function callFetchClinicalData(
    testData: any,
    projection: ProjectionType
  ): Promise<ClinicalDataDTO[]> {
    const url = `${config.serverUrl}/api/clinical-data/fetch?clinicalDataType=SAMPLE&projection=${projection}`;

    const response = await axios.post<ClinicalDataDTO[]>(url, testData, {
      headers: { 'Content-Type': 'application/json' }
    });

    // Verify the response status is 200 OK
    expect(response.status).to.equal(200, 'Response status should be 200 OK');
    expect(response.data).to.not.be.null;
    expect(response.data).to.not.be.undefined;

    return response.data;
  }

  describe('POST /clinical-data/fetch - SAMPLE clinical data', () => {
    it('streams the requested clinical data with correct values and derived keys', async () => {
      // Two samples x two attributes -> four clinical data records
      const testData = await TestUtils.loadTestData('clinical_data_filter.json');

      const clinicalData = await callFetchClinicalData(testData, ProjectionType.SUMMARY);

      // Exactly four (sample, attribute) records are returned
      expect(clinicalData).to.be.an('array');
      expect(clinicalData.length).to.equal(
        4,
        'Two samples x two attributes should yield four clinical data records'
      );

      // Every record must carry the derived keys (regression guard: streaming bypasses the
      // UniqueKeyInterceptor, so the stream consumer must populate these itself).
      const allHaveKeys = _.every(
        clinicalData,
        d => !_.isEmpty(d.uniqueSampleKey) && !_.isEmpty(d.uniquePatientKey)
      );
      expect(allHaveKeys, 'Every record must have uniqueSampleKey/uniquePatientKey').to.be.true;

      // CANCER_TYPE for sample 1 is "Adrenocortical Carcinoma" with the expected sample key
      const s1CancerType = _.find(clinicalData, {
        sampleId: SAMPLE_1,
        clinicalAttributeId: 'CANCER_TYPE'
      });
      expect(s1CancerType, 'sample 1 CANCER_TYPE should be present').to.not.be.undefined;
      expect(s1CancerType!.value).to.equal('Adrenocortical Carcinoma');
      expect(s1CancerType!.uniqueSampleKey).to.equal(
        SAMPLE_1_KEY,
        'uniqueSampleKey must be base64 of sampleId:studyId'
      );

      // FRACTION_GENOME_ALTERED differs between the two samples
      const s1Fga = _.find(clinicalData, {
        sampleId: SAMPLE_1,
        clinicalAttributeId: 'FRACTION_GENOME_ALTERED'
      });
      const s2Fga = _.find(clinicalData, {
        sampleId: SAMPLE_2,
        clinicalAttributeId: 'FRACTION_GENOME_ALTERED'
      });
      expect(s1Fga!.value).to.equal('0.0585', 'sample 1 FRACTION_GENOME_ALTERED');
      expect(s2Fga!.value).to.equal('0.4033', 'sample 2 FRACTION_GENOME_ALTERED');
      expect(s2Fga!.uniqueSampleKey).to.equal(SAMPLE_2_KEY);

      // Every record belongs to the requested study
      const allInStudy = _.every(clinicalData, { studyId: STUDY_ID });
      expect(allInStudy, `All records should belong to ${STUDY_ID}`).to.be.true;
    });

    it('returns the total count in a header and an empty body under META projection', async () => {
      const testData = await TestUtils.loadTestData('clinical_data_filter.json');
      const url = `${config.serverUrl}/api/clinical-data/fetch?clinicalDataType=SAMPLE&projection=META`;

      const response = await axios.post(url, testData, {
        headers: { 'Content-Type': 'application/json' }
      });

      // META short-circuits before streaming: 200 with a total-count header and no body
      expect(response.status).to.equal(200, 'META projection should return 200 OK');
      expect(response.headers['total-count']).to.equal(
        '4',
        'total-count header should match the number of clinical data records'
      );
      expect(_.isEmpty(response.data), 'META projection should have an empty body').to.be.true;
    });
  });
});
