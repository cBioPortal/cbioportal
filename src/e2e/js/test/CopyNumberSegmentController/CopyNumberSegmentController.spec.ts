import { expect } from 'chai';
import _ from 'lodash';
import axios from 'axios';
import { CopyNumberSeg, ProjectionType } from '../../src/types';
import { config } from '../../src/config';
import { TestUtils } from '../../src/utils';

/**
 * E2E coverage for the streaming POST /api/copy-number-segments/fetch endpoint.
 *
 * CopyNumberSeg extends UniqueKeyBase, so the non-streaming response had its
 * uniqueSampleKey / uniquePatientKey filled by the UniqueKeyInterceptor. The interceptor does
 * not run for a StreamingResponseBody, so the stream consumer must populate them; these tests
 * pin that behavior (and the basic segment shape) so a key-dropping regression is caught.
 */
describe('CopyNumberSegmentController E2E Tests (streaming /fetch)', () => {
  const STUDY_ID = 'acc_tcga';
  const SAMPLE_1 = 'TCGA-OR-A5J1-01';
  // base64("TCGA-OR-A5J1-01:acc_tcga")
  const SAMPLE_1_KEY = 'VENHQS1PUi1BNUoxLTAxOmFjY190Y2dh';

  async function callFetchSegments(
    testData: any,
    projection: ProjectionType
  ): Promise<CopyNumberSeg[]> {
    const url = `${config.serverUrl}/api/copy-number-segments/fetch?projection=${projection}`;

    const response = await axios.post<CopyNumberSeg[]>(url, testData, {
      headers: { 'Content-Type': 'application/json' }
    });

    expect(response.status).to.equal(200, 'Response status should be 200 OK');
    expect(response.data).to.not.be.null;
    expect(response.data).to.not.be.undefined;

    return response.data;
  }

  describe('POST /copy-number-segments/fetch', () => {
    it('streams every segment for the sample with derived keys populated', async () => {
      // One acc_tcga sample, which has many copy-number segments
      const testData = await TestUtils.loadTestData('copy_number_segment_filter.json');

      const segments = await callFetchSegments(testData, ProjectionType.SUMMARY);

      // The sample has a non-trivial number of segments
      expect(segments).to.be.an('array');
      expect(segments.length).to.be.greaterThan(
        100,
        'acc_tcga sample TCGA-OR-A5J1-01 should have many copy-number segments'
      );

      // Every segment belongs to the requested sample/study
      const allForSample = _.every(segments, { sampleId: SAMPLE_1, studyId: STUDY_ID });
      expect(allForSample, 'All segments should belong to the requested sample/study').to.be.true;

      // Regression guard: streaming bypasses the UniqueKeyInterceptor, so the consumer must
      // populate the derived keys. Every segment should carry the expected sample/patient keys.
      const allHaveKeys = _.every(
        segments,
        s => s.uniqueSampleKey === SAMPLE_1_KEY && !_.isEmpty(s.uniquePatientKey)
      );
      expect(allHaveKeys, 'Every segment must have the derived unique keys populated').to.be.true;

      // Each segment carries the expected positional/value fields
      const firstSegment = segments[0];
      expect(firstSegment).to.include.keys([
        'chromosome',
        'start',
        'end',
        'numberOfProbes',
        'segmentMean'
      ]);
    });

    it('META projection total-count matches the streamed segment count', async () => {
      const testData = await TestUtils.loadTestData('copy_number_segment_filter.json');

      // Get the streamed count
      const segments = await callFetchSegments(testData, ProjectionType.SUMMARY);

      // META short-circuits before streaming and returns the count in a header with no body
      const url = `${config.serverUrl}/api/copy-number-segments/fetch?projection=META`;
      const response = await axios.post(url, testData, {
        headers: { 'Content-Type': 'application/json' }
      });

      expect(response.status).to.equal(200, 'META projection should return 200 OK');
      expect(response.headers['total-count']).to.equal(
        String(segments.length),
        'META total-count should equal the number of streamed segments'
      );
      expect(_.isEmpty(response.data), 'META projection should have an empty body').to.be.true;
    });
  });
});
