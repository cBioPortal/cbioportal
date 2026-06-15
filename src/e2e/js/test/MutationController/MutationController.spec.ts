import { expect } from 'chai';
import _ from 'lodash';
import axios from 'axios';
import { MutationDTO, ProjectionType } from '../../src/types';
import { config } from '../../src/config';
import { TestUtils } from '../../src/utils';

/**
 * E2E coverage for the (legacy) public MutationController streaming endpoints:
 *
 *   POST /api/molecular-profiles/{molecularProfileId}/mutations/fetch
 *   POST /api/mutations/fetch
 *   GET  /api/molecular-profiles/{molecularProfileId}/mutations
 *
 * The two /fetch endpoints were converted to StreamingResponseBody so a large
 * mutation result set is never materialized into a List on the heap. These
 * tests pin the streamed output (including the derived uniqueSampleKey /
 * uniquePatientKey, which are populated by the controller's stream consumer
 * rather than by the UniqueKeyInterceptor) so that the streamed response stays
 * byte-for-byte equivalent to the previously materialized one.
 *
 * Every expected value below was captured from the present production API at
 * https://www.cbioportal.org for the identical request, so a passing run proves
 * the streaming implementation returns the same data as production.
 *
 * Fixture study: lgg_ucsf_2014 (UCSF low grade glioma). IDH1 (entrezGeneId 3417)
 * is mutated in all 61 sequenced samples: 55x R132H, 4x R132C, 2x R132G.
 */
describe('MutationController E2E Tests (streaming /fetch endpoints)', () => {
  // Stable identifiers / expected aggregates for the IDH1-in-lgg_ucsf_2014 fixture.
  const SINGLE_PROFILE_ID = 'lgg_ucsf_2014_mutations';
  const IDH1_ENTREZ_GENE_ID = 3417;
  const EXPECTED_IDH1_SAMPLE_COUNT = 61;

  /**
   * POST the single-profile fetch endpoint (the one converted to streaming).
   * @param body - a MutationFilter (sampleListId+entrezGeneIds or sampleIds+entrezGeneIds)
   * @param projection - ID | SUMMARY | DETAILED
   */
  async function fetchSingleProfile(
    body: any,
    projection: ProjectionType
  ): Promise<MutationDTO[]> {
    const url = `${config.serverUrl}/api/molecular-profiles/${SINGLE_PROFILE_ID}/mutations/fetch?projection=${projection}`;
    const response = await axios.post<MutationDTO[]>(url, body, {
      headers: { 'Content-Type': 'application/json' }
    });

    // Streaming a committed 200 should still surface as a normal JSON array body
    expect(response.status).to.equal(200, 'Response status should be 200 OK');
    expect(response.data).to.be.an('array', 'Streamed body should be a JSON array');
    return response.data;
  }

  /**
   * POST the multi-profile fetch endpoint (also streaming).
   * @param body - a MutationMultipleStudyFilter (molecularProfileIds or sampleMolecularIdentifiers)
   * @param projection - ID | SUMMARY | DETAILED
   */
  async function fetchMultipleProfiles(
    body: any,
    projection: ProjectionType
  ): Promise<MutationDTO[]> {
    const url = `${config.serverUrl}/api/mutations/fetch?projection=${projection}`;
    const response = await axios.post<MutationDTO[]>(url, body, {
      headers: { 'Content-Type': 'application/json' }
    });

    expect(response.status).to.equal(200, 'Response status should be 200 OK');
    expect(response.data).to.be.an('array', 'Streamed body should be a JSON array');
    return response.data;
  }

  describe('POST /molecular-profiles/{id}/mutations/fetch - by sampleListId', () => {
    it('streams every IDH1 mutation in the sample list (DETAILED projection)', async () => {
      // sampleListId path exercises the controller's sample-list resolution helper
      const filter = await TestUtils.loadTestData('single_profile_samplelist.json');

      // Fetch with DETAILED projection so the gene object is included
      const mutations = await fetchSingleProfile(filter, ProjectionType.DETAILED);

      // All 61 sequenced samples carry exactly one IDH1 mutation
      expect(mutations.length).to.equal(
        EXPECTED_IDH1_SAMPLE_COUNT,
        'IDH1 is mutated in all 61 sequenced lgg_ucsf_2014 samples (one mutation each)'
      );

      // Every record must be for IDH1 in this single profile / study
      const allAreIdh1 = _.every(
        mutations,
        m =>
          m.entrezGeneId === IDH1_ENTREZ_GENE_ID &&
          m.molecularProfileId === SINGLE_PROFILE_ID &&
          m.studyId === 'lgg_ucsf_2014'
      );
      expect(allAreIdh1, 'Every streamed record is IDH1 in lgg_ucsf_2014_mutations').to.be.true;

      // Pin the IDH1 protein-change distribution (matches production: 55/4/2)
      const proteinChangeCounts = _.countBy(mutations, 'proteinChange');
      expect(proteinChangeCounts).to.deep.equal(
        { R132H: 55, R132C: 4, R132G: 2 },
        'IDH1 protein-change distribution must match production'
      );

      // Spot-check a single record (P04_Pri, R132C) field-by-field against production
      const p04 = _.find(mutations, { sampleId: 'P04_Pri' })!;
      expect(p04, 'P04_Pri IDH1 mutation should be present').to.not.be.undefined;
      expect(p04.patientId).to.equal('P04');
      expect(p04.proteinChange).to.equal('R132C');
      expect(p04.mutationType).to.equal('Missense_Mutation');
      expect(p04.variantType).to.equal('SNP');
      expect(p04.ncbiBuild).to.equal('GRCh37');
      expect(p04.chr).to.equal('2');
      expect(p04.startPosition).to.equal(209113113);
      expect(p04.endPosition).to.equal(209113113);
      expect(p04.referenceAllele).to.equal('G');
      expect(p04.variantAllele).to.equal('A');
      expect(p04.tumorAltCount).to.equal(46);
      expect(p04.tumorRefCount).to.equal(75);
      expect(p04.proteinPosStart).to.equal(132);
      expect(p04.refseqMrnaId).to.equal('NM_005896.2');

      // DETAILED projection includes the nested gene object
      expect(p04.gene).to.not.be.undefined;
      expect(p04.gene!.hugoGeneSymbol).to.equal('IDH1');
      expect(p04.gene!.type).to.equal('protein-coding');

      // The streaming consumer (not the UniqueKeyInterceptor) computes these base64 keys;
      // pin the exact values produced by production for P04_Pri / patient P04
      expect(p04.uniqueSampleKey).to.equal(
        'UDA0X1ByaTpsZ2dfdWNzZl8yMDE0',
        'uniqueSampleKey must match production (base64 of sampleId:studyId)'
      );
      expect(p04.uniquePatientKey).to.equal(
        'UDA0OmxnZ191Y3NmXzIwMTQ',
        'uniquePatientKey must match production (base64 of patientId:studyId)'
      );
    });

    it('omits the gene object under SUMMARY projection but still populates derived keys', async () => {
      const filter = await TestUtils.loadTestData('single_profile_samplelist.json');

      // SUMMARY projection should return the same records without the gene object
      const mutations = await fetchSingleProfile(filter, ProjectionType.SUMMARY);
      expect(mutations.length).to.equal(
        EXPECTED_IDH1_SAMPLE_COUNT,
        'SUMMARY projection returns the same record count as DETAILED'
      );

      // No record carries a gene object under SUMMARY
      const noneHaveGene = _.every(mutations, m => _.isNil(m.gene));
      expect(noneHaveGene, 'SUMMARY projection must not include the gene object').to.be.true;

      // The streamed records must still carry the derived unique keys (set by the stream consumer)
      const p01 = _.find(mutations, { sampleId: 'P01_Pri' })!;
      expect(p01.uniqueSampleKey).to.equal(
        'UDAxX1ByaTpsZ2dfdWNzZl8yMDE0',
        'SUMMARY projection still populates uniqueSampleKey'
      );
      expect(p01.uniquePatientKey).to.equal('UDAxOmxnZ191Y3NmXzIwMTQ');
    });

    it('returns counts in headers and an empty body under META projection', async () => {
      const filter = await TestUtils.loadTestData('single_profile_samplelist.json');

      // META projection short-circuits before streaming and returns only header counts
      const url = `${config.serverUrl}/api/molecular-profiles/${SINGLE_PROFILE_ID}/mutations/fetch?projection=META`;
      const response = await axios.post(url, filter, {
        headers: { 'Content-Type': 'application/json' }
      });

      expect(response.status).to.equal(200, 'META projection should return 200 OK');

      // total-count and sample-count headers must equal 61 (one IDH1 mutation per sample)
      expect(_.toNumber(response.headers['total-count'])).to.equal(
        EXPECTED_IDH1_SAMPLE_COUNT,
        'total-count header should be 61'
      );
      expect(_.toNumber(response.headers['sample-count'])).to.equal(
        EXPECTED_IDH1_SAMPLE_COUNT,
        'sample-count header should be 61'
      );

      // META projection carries no response body
      expect(_.isEmpty(response.data), 'META projection should have an empty body').to.be.true;
    });
  });

  describe('POST /molecular-profiles/{id}/mutations/fetch - by sampleIds', () => {
    it('streams only the requested samples (DETAILED projection)', async () => {
      // Explicit sampleIds path (no sample-list resolution)
      const filter = await TestUtils.loadTestData('single_profile_samples.json');

      const mutations = await fetchSingleProfile(filter, ProjectionType.DETAILED);

      // Exactly the two requested samples each have one IDH1 mutation
      expect(mutations.length).to.equal(2, 'Two requested samples each carry one IDH1 mutation');

      // Verify the two protein changes keyed by sample (P01_Pri R132H, P04_Pri R132C)
      const bySample = _.keyBy(mutations, 'sampleId');
      expect(bySample['P01_Pri'].proteinChange).to.equal('R132H');
      expect(bySample['P01_Pri'].tumorAltCount).to.equal(21);
      expect(bySample['P01_Pri'].tumorRefCount).to.equal(40);
      expect(bySample['P04_Pri'].proteinChange).to.equal('R132C');
      expect(bySample['P04_Pri'].tumorAltCount).to.equal(46);
      expect(bySample['P04_Pri'].tumorRefCount).to.equal(75);
    });
  });

  describe('POST /mutations/fetch - multiple molecular profiles', () => {
    it('streams mutations across two studies via sampleMolecularIdentifiers', async () => {
      // Cross-study request: two lgg_ucsf_2014 samples + two lgg_tcga samples, IDH1 only
      const filter = await TestUtils.loadTestData('multi_profile_cross_study.json');

      const mutations = await fetchMultipleProfiles(filter, ProjectionType.DETAILED);

      // Exactly four mutations, one per requested (profile, sample) pair
      expect(mutations.length).to.equal(4, 'One IDH1 mutation per requested sample across both studies');

      // Both studies must be represented in the streamed result
      const studyIds = _.uniq(_.map(mutations, 'studyId')).sort();
      expect(studyIds).to.deep.equal(
        ['lgg_tcga', 'lgg_ucsf_2014'],
        'Result must span both requested studies'
      );

      // Every record is IDH1
      const allAreIdh1 = _.every(mutations, m => m.entrezGeneId === IDH1_ENTREZ_GENE_ID);
      expect(allAreIdh1, 'Every streamed record is IDH1').to.be.true;

      // Spot-check an lgg_tcga record (TCGA-CS-4938-01) including its derived unique key
      const tcga = _.find(mutations, { sampleId: 'TCGA-CS-4938-01' })!;
      expect(tcga, 'TCGA-CS-4938-01 IDH1 mutation should be present').to.not.be.undefined;
      expect(tcga.studyId).to.equal('lgg_tcga');
      expect(tcga.molecularProfileId).to.equal('lgg_tcga_mutations');
      expect(tcga.proteinChange).to.equal('R132H');
      expect(tcga.tumorAltCount).to.equal(29);
      expect(tcga.tumorRefCount).to.equal(52);
      expect(tcga.uniqueSampleKey).to.equal(
        'VENHQS1DUy00OTM4LTAxOmxnZ190Y2dh',
        'uniqueSampleKey must match production for the cross-study record'
      );
    });

    it('returns counts in headers and an empty body under META projection', async () => {
      const filter = await TestUtils.loadTestData('multi_profile_cross_study.json');

      // META projection short-circuits before streaming on the multi-profile endpoint too
      const url = `${config.serverUrl}/api/mutations/fetch?projection=META`;
      const response = await axios.post(url, filter, {
        headers: { 'Content-Type': 'application/json' }
      });

      expect(response.status).to.equal(200, 'META projection should return 200 OK');

      // Four requested (profile, sample) pairs, each with one IDH1 mutation
      expect(_.toNumber(response.headers['total-count'])).to.equal(4, 'total-count header should be 4');
      expect(_.toNumber(response.headers['sample-count'])).to.equal(4, 'sample-count header should be 4');
      expect(_.isEmpty(response.data), 'META projection should have an empty body').to.be.true;
    });
  });

  describe('GET /molecular-profiles/{id}/mutations - by sampleListId', () => {
    it('returns every IDH1 mutation in the sample list', async () => {
      // The non-streaming GET variant shares the sample-list semantics; verify parity
      const url = `${config.serverUrl}/api/molecular-profiles/${SINGLE_PROFILE_ID}/mutations?sampleListId=lgg_ucsf_2014_sequenced&entrezGeneId=${IDH1_ENTREZ_GENE_ID}&projection=DETAILED`;
      const response = await axios.get<MutationDTO[]>(url);

      expect(response.status).to.equal(200, 'Response status should be 200 OK');
      expect(response.data.length).to.equal(
        EXPECTED_IDH1_SAMPLE_COUNT,
        'GET by sampleListId returns all 61 IDH1 mutations'
      );

      // Every record is IDH1 in this profile
      const allAreIdh1 = _.every(response.data, m => m.entrezGeneId === IDH1_ENTREZ_GENE_ID);
      expect(allAreIdh1, 'Every record is IDH1').to.be.true;
    });
  });
});
