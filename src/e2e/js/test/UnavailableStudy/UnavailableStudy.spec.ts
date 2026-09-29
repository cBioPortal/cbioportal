import { expect } from 'chai';
import _ from 'lodash';
import axios from 'axios';
import { CancerStudyMetadataDTO } from '../../src/types';
import { config } from '../../src/config';

describe('Unavailable Study E2E Tests', () => {

  // brca_jup_msk_2020 has status = 0 in the test database, which means it is mid-(re)import
  // and should be blocked from direct reads while still appearing in study listings.
  const UNAVAILABLE_STUDY_ID = 'brca_jup_msk_2020';

  // The only molecular profile of the unavailable study in the test database
  const UNAVAILABLE_MUTATION_PROFILE_ID = 'brca_jup_msk_2020_mutations';

  describe('GET /api/studies/{studyId}', () => {

    it('should return 423 naming the study that is being updated', async () => {
      try {
        // Attempt to read a study whose status marks it as mid-import
        await axios.get(`${config.serverUrl}/api/studies/${UNAVAILABLE_STUDY_ID}`);
        expect.fail('Expected HTTP 423 Locked, but the request succeeded');
      } catch (error: any) {
        // Verify the server returned an HTTP error, not a network failure
        expect(error.response, 'Expected an HTTP error response, not a network error').to.not.be.undefined;

        // 423 Locked lets the frontend tell "temporarily unavailable" apart from "no access" (403)
        expect(error.response.status).to.equal(423, 'An unavailable study should return 423 Locked');

        // The message must name the study so the frontend can show which one is unavailable
        const message: string = error.response.data?.message ?? '';
        expect(message).to.include(
          UNAVAILABLE_STUDY_ID,
          'The error message should name the study that is unavailable'
        );
      }
    });
  });

  describe('GET /api/studies', () => {

    it('should still list the unavailable study so the frontend can show it greyed out', async () => {
      // The listing names no study, so nothing in the request matches an unavailable study
      // and every study is returned, letting the homepage display it greyed out
      const response = await axios.get<CancerStudyMetadataDTO[]>(
        `${config.serverUrl}/api/studies?projection=SUMMARY`
      );

      expect(response.status).to.equal(200);

      // Find the unavailable study in the listing
      const unavailableStudy = _.find(response.data, { studyId: UNAVAILABLE_STUDY_ID });
      expect(
        unavailableStudy,
        `${UNAVAILABLE_STUDY_ID} should appear in the study list even though it is unavailable`
      ).to.not.be.undefined;

      // The homepage leaves unreadable studies out of its batch requests, which would otherwise 423
      expect(unavailableStudy!.readPermission).to.equal(
        false,
        `${UNAVAILABLE_STUDY_ID} should be reported as unreadable while it is unavailable`
      );
    });
  });

  describe('endpoints identified by a molecular profile', () => {

    it('should return 423 naming the owning study when requesting its molecular profile', async () => {
      try {
        // Only the profile id is in the path; it must be traced back to its unavailable study
        await axios.get(
          `${config.serverUrl}/api/molecular-profiles/${UNAVAILABLE_MUTATION_PROFILE_ID}`
        );
        expect.fail('Expected HTTP 423 Locked, but the request succeeded');
      } catch (error: any) {
        expect(error.response, 'Expected an HTTP error response, not a network error').to.not.be.undefined;
        expect(error.response.status).to.equal(
          423,
          'A molecular profile of an unavailable study should return 423 Locked'
        );

        // The message names the owning study, not the profile
        const message: string = error.response.data?.message ?? '';
        expect(message).to.include(`Study ${UNAVAILABLE_STUDY_ID} `);
      }
    });

    it('should return 423 when fetching mutations of an unavailable study by profile', async () => {
      try {
        // The profile id is nested in the POST filter body, which the legacy fetch endpoints
        // receive as an intercepted request attribute; BRCA1 (672) is arbitrary because the
        // request is rejected before any data is queried
        await axios.post(
          `${config.serverUrl}/api/mutations/fetch`,
          { molecularProfileIds: [UNAVAILABLE_MUTATION_PROFILE_ID], entrezGeneIds: [672] },
          { headers: { 'Content-Type': 'application/json' } }
        );
        expect.fail('Expected HTTP 423 Locked, but the request succeeded');
      } catch (error: any) {
        expect(error.response, 'Expected an HTTP error response, not a network error').to.not.be.undefined;
        expect(error.response.status).to.equal(
          423,
          'Mutations of an unavailable study should return 423 Locked'
        );
      }
    });
  });
});
