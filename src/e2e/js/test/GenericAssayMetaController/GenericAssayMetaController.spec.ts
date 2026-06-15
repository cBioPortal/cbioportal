import { expect } from 'chai';
import _ from 'lodash';
import axios from 'axios';
import { GenericAssayMeta, ProjectionType } from '../../src/types';
import { config } from '../../src/config';
import { TestUtils } from '../../src/utils';

describe('GenericAssayMetaController E2E Tests', () => {
  /**
   * Calls POST /api/legacy/generic_assay_meta/fetch for a given projection.
   * For non-DETAILED projections the additional-properties query is now restricted to the
   * property names the frontend consumes (NAME/DESCRIPTION/URL); DETAILED still returns the
   * full property map. These tests guard both behaviors.
   * @param testData - request body containing molecularProfileIds and/or genericAssayStableIds
   * @param projection - ID | SUMMARY | DETAILED
   * @returns array of GenericAssayMeta entities
   */
  async function callFetchGenericAssayMeta(
    testData: any,
    projection: ProjectionType
  ): Promise<GenericAssayMeta[]> {
    const url = `${config.serverUrl}/api/legacy/generic_assay_meta/fetch?projection=${projection}`;

    const response = await axios.post<GenericAssayMeta[]>(url, testData, {
      headers: { 'Content-Type': 'application/json' }
    });

    // Verify the response status is 200 OK
    expect(response.status).to.equal(200, 'Response status should be 200 OK');

    // Verify the response body is present
    expect(response.data).to.not.be.null;
    expect(response.data).to.not.be.undefined;

    return response.data;
  }

  // This profile's entities carry two property names: NAME and GENE_SYMBOL. The frontend only
  // consumes NAME/DESCRIPTION/URL, so SUMMARY should drop GENE_SYMBOL while DETAILED keeps it.
  const PROFILE_ID = 'lusc_cptac_2021_circular_rna';
  const EXPECTED_ENTITY_COUNT = 306;

  describe('testFetchGenericAssayMeta_summaryRestrictsProperties', () => {
    it('SUMMARY should return only the frontend-consumed property names', async () => {
      // Load the filter targeting the circular-RNA generic-assay profile
      const testData = await TestUtils.loadTestData('generic_assay_meta_filter.json');

      // Fetch with SUMMARY projection (the default the frontend uses)
      const summaryMeta = await callFetchGenericAssayMeta(testData, ProjectionType.SUMMARY);

      // The set of entities is unchanged by the property restriction
      expect(summaryMeta.length).to.equal(
        EXPECTED_ENTITY_COUNT,
        `SUMMARY should return all ${EXPECTED_ENTITY_COUNT} entities of the profile`
      );

      // Collect every distinct property key present across all entities
      const summaryPropertyKeys = _.uniq(
        _.flatMap(summaryMeta, entity => _.keys(entity.genericEntityMetaProperties))
      ).sort();

      // SUMMARY must expose only NAME here (GENE_SYMBOL is dropped; DESCRIPTION/URL are absent
      // from this profile). It must never expose the non-consumed GENE_SYMBOL property.
      expect(summaryPropertyKeys).to.deep.equal(
        ['NAME'],
        'SUMMARY should expose only NAME for this profile'
      );
      expect(summaryPropertyKeys).to.not.include(
        'GENE_SYMBOL',
        'SUMMARY must not include the unused GENE_SYMBOL property'
      );

      // Every entity should still carry its NAME value (the restriction drops keys, not entities)
      const allHaveName = _.every(
        summaryMeta,
        entity => !_.isEmpty(_.get(entity, ['genericEntityMetaProperties', 'NAME']))
      );
      expect(allHaveName, 'Every entity should still have a NAME property').to.be.true;
    });
  });

  describe('testFetchGenericAssayMeta_detailedReturnsFullMap', () => {
    it('DETAILED should still return the full property map (including GENE_SYMBOL)', async () => {
      const testData = await TestUtils.loadTestData('generic_assay_meta_filter.json');

      // Fetch SUMMARY and DETAILED for the same profile to compare property coverage
      const summaryMeta = await callFetchGenericAssayMeta(testData, ProjectionType.SUMMARY);
      const detailedMeta = await callFetchGenericAssayMeta(testData, ProjectionType.DETAILED);

      // Same entity set for both projections
      expect(detailedMeta.length).to.equal(
        summaryMeta.length,
        'DETAILED and SUMMARY should return the same number of entities'
      );

      // DETAILED must expose the full property set for this profile: NAME and GENE_SYMBOL
      const detailedPropertyKeys = _.uniq(
        _.flatMap(detailedMeta, entity => _.keys(entity.genericEntityMetaProperties))
      ).sort();
      expect(detailedPropertyKeys).to.deep.equal(
        ['GENE_SYMBOL', 'NAME'],
        'DETAILED should expose the full property map (NAME and GENE_SYMBOL)'
      );

      // Concretely: DETAILED carries GENE_SYMBOL where SUMMARY does not, for the same entity
      const sampleStableId = detailedMeta[0].stableId;
      const detailedEntity = _.find(detailedMeta, { stableId: sampleStableId });
      const summaryEntity = _.find(summaryMeta, { stableId: sampleStableId });
      expect(
        _.has(detailedEntity!.genericEntityMetaProperties, 'GENE_SYMBOL'),
        'DETAILED entity should include GENE_SYMBOL'
      ).to.be.true;
      expect(
        _.has(summaryEntity!.genericEntityMetaProperties, 'GENE_SYMBOL'),
        'SUMMARY entity should not include GENE_SYMBOL'
      ).to.be.false;

      // The NAME value must be identical across projections (only the key set differs)
      expect(summaryEntity!.genericEntityMetaProperties.NAME).to.equal(
        detailedEntity!.genericEntityMetaProperties.NAME,
        'NAME should be identical between SUMMARY and DETAILED'
      );
    });
  });
});
