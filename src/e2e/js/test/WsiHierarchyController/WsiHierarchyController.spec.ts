import { expect } from 'chai';
import axios from 'axios';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const config = {
  serverUrl: process.env.CBIOPORTAL_URL || 'http://localhost:8080',
  tileServerUrl: process.env.WSI_TILE_SERVER_URL || 'http://localhost:8081',
  frontendUrl: process.env.CBIOPORTAL_FRONTEND_URL || 'http://localhost:3000',
  blockTileSlideId: process.env.WSI_TEST_BLOCK_SLIDE_ID || '',
  partTileSlideId: process.env.WSI_TEST_PART_SLIDE_ID || '',
  unmatchedTileSlideId: process.env.WSI_TEST_UNMATCHED_SLIDE_ID || '',
};

const hasExplicitTileServer = Boolean(process.env.WSI_TILE_SERVER_URL);
const hasExplicitFrontend = Boolean(process.env.CBIOPORTAL_FRONTEND_URL);
const hasExplicitTileIds = Boolean(
  process.env.WSI_TEST_BLOCK_SLIDE_ID &&
    process.env.WSI_TEST_PART_SLIDE_ID &&
    process.env.WSI_TEST_UNMATCHED_SLIDE_ID
);

type Slide = {
  image_id: string;
};

type Block = {
  slides: Slide[];
};

type Part = {
  blocks: Block[];
};

type Sample = {
  sample_id: string;
  parts: Part[];
};

type SlideAssociation = {
  image_id: string;
  sample_id: string | null;
  match_level: 'PART' | 'BLOCK' | 'UNMATCHED';
  can_serve_tiles?: boolean;
};

type PatientHierarchy = {
  patient_id: string;
  samples: Sample[];
  slide_associations: SlideAssociation[];
};

type SlideMetadata = {
  dimensions: {
    width: number;
    height: number;
  };
  levels: number;
  level_dimensions: Array<{ width: number; height: number }>;
  tile_size?: number;
  objective_power?: number;
  vendor?: string;
};

type HierarchyFixture = {
  study_id: string;
  patient_id: string;
  hierarchy: PatientHierarchy;
};

const currentFile = fileURLToPath(import.meta.url);
const currentDir = path.dirname(currentFile);

const fixturePath = path.join(
  currentDir,
  'msk_spectrum_tme_2022.wsi_hierarchy.jsonl'
);
const fixture = JSON.parse(
  fs.readFileSync(fixturePath, 'utf8').trim()
) as HierarchyFixture;

function collectSlideIds(hierarchy: PatientHierarchy): string[] {
  return hierarchy.samples.flatMap(sample =>
    sample.parts.flatMap(part =>
      part.blocks.flatMap(block => block.slides.map(slide => slide.image_id))
    )
  );
}

function findAssociation(
  hierarchy: PatientHierarchy,
  matchLevel: SlideAssociation['match_level']
): SlideAssociation {
  const association = hierarchy.slide_associations.find(
    candidate => candidate.match_level === matchLevel
  );
  expect(association, `missing ${matchLevel} slide association`).to.not.equal(undefined);
  return association!;
}

describe('WsiHierarchyController E2E Tests', () => {
  const hierarchyUrl = `${config.serverUrl}/api/wsi/hierarchy/${fixture.study_id}/${fixture.patient_id}`;
  const frontendHierarchyUrl = `${config.frontendUrl}/api/wsi/hierarchy/${fixture.study_id}/${fixture.patient_id}`;
  const blockAssociation = findAssociation(fixture.hierarchy, 'BLOCK');
  const partAssociation = findAssociation(fixture.hierarchy, 'PART');
  const unmatchedAssociation = findAssociation(fixture.hierarchy, 'UNMATCHED');
  const blockTileSlideId = config.blockTileSlideId || blockAssociation.image_id;
  const partTileSlideId = config.partTileSlideId || partAssociation.image_id;
  const unmatchedTileSlideId = config.unmatchedTileSlideId || unmatchedAssociation.image_id;

  it('returns the materialized public hierarchy fixture', async () => {
    const response = await axios.get<PatientHierarchy>(hierarchyUrl);

    expect(response.status).to.equal(200);
    expect(response.headers['content-type']).to.contain('application/json');
    expect(response.data).to.deep.equal(fixture.hierarchy);
  });

  const frontendIt = hasExplicitFrontend ? it : it.skip;
  const tileIt = hasExplicitTileServer ? it : it.skip;
  const hierarchyTileConsistencyIt =
    hasExplicitTileServer && !hasExplicitTileIds ? it : it.skip;

  frontendIt('matches the backend hierarchy payload through the frontend proxy', async () => {
    const [backendResponse, frontendResponse] = await Promise.all([
      axios.get<PatientHierarchy>(hierarchyUrl),
      axios.get<PatientHierarchy>(frontendHierarchyUrl),
    ]);

    expect(frontendResponse.status).to.equal(200);
    expect(frontendResponse.headers['content-type']).to.contain('application/json');
    expect(frontendResponse.data).to.deep.equal(backendResponse.data);
  });

  it('covers block, part, and unmatched slide associations in the hierarchy', () => {
    const hierarchy = fixture.hierarchy;
    const slideIds = collectSlideIds(hierarchy);
    const associations = hierarchy.slide_associations;

    expect(slideIds).to.have.members([
      '3020726',
      '3020691',
      '3020648',
    ]);
    expect(associations.map(association => association.match_level)).to.have.members([
      'PART',
      'BLOCK',
      'UNMATCHED',
    ]);

    associations.forEach(association => {
      expect(slideIds).to.include(association.image_id);
      if (association.match_level === 'UNMATCHED') {
        expect(association.sample_id).to.equal(null);
      } else {
        expect(association.sample_id).to.be.a('string').and.not.empty;
      }
    });
  });

  it('returns 404 for an unknown patient', async () => {
    try {
      await axios.get(
        `${config.serverUrl}/api/wsi/hierarchy/${fixture.study_id}/missing-patient`
      );
      expect.fail('Expected request to fail with 404');
    } catch (error: any) {
      expect(error.response).to.not.equal(undefined);
      expect(error.response.status).to.equal(404);
    }
  });

  tileIt('serves tile metadata for block- and part-matched public slides', async () => {
    const [blockResponse, partResponse] = await Promise.all([
      axios.get<SlideMetadata>(
        `${config.tileServerUrl}/tiles/${blockTileSlideId}/metadata`
      ),
      axios.get<SlideMetadata>(
        `${config.tileServerUrl}/tiles/${partTileSlideId}/metadata`
      ),
    ]);

    [blockResponse, partResponse].forEach(response => {
      expect(response.status).to.equal(200);
      expect(response.headers['content-type']).to.contain('application/json');
      expect(response.data.dimensions.width).to.be.greaterThan(0);
      expect(response.data.dimensions.height).to.be.greaterThan(0);
      expect(response.data.levels).to.be.greaterThan(0);
      expect(response.data.level_dimensions).to.not.be.empty;
      expect(response.data.tile_size).to.equal(256);
      expect(response.data.objective_power).to.equal(20);
      expect(response.data.vendor).to.equal('aperio');
    });
  });

  hierarchyTileConsistencyIt('reports tile-serving capability in the hierarchy consistently with live tile behavior', async () => {
    const servableAssociations = fixture.hierarchy.slide_associations.filter(
      association => association.can_serve_tiles === true
    );
    const nonServableAssociations = fixture.hierarchy.slide_associations.filter(
      association => association.can_serve_tiles === false
    );

    expect(servableAssociations.map(association => association.image_id)).to.have.members([
      blockAssociation.image_id,
      partAssociation.image_id,
    ]);
    expect(nonServableAssociations.map(association => association.image_id)).to.deep.equal([
      unmatchedAssociation.image_id,
    ]);

    const metadataResponses = await Promise.all(
      servableAssociations.map(association =>
        axios.get<SlideMetadata>(`${config.tileServerUrl}/tiles/${association.image_id}/metadata`)
      )
    );

    metadataResponses.forEach(response => {
      expect(response.status).to.equal(200);
      expect(response.data.levels).to.be.greaterThan(0);
    });
  });

  tileIt('serves thumbnail and tile bytes for a block-matched public slide', async () => {
    const [thumbnailResponse, tileResponse] = await Promise.all([
      axios.get<ArrayBuffer>(
        `${config.tileServerUrl}/tiles/${blockTileSlideId}/thumbnail?width=128&height=128`,
        { responseType: 'arraybuffer' }
      ),
      axios.get<ArrayBuffer>(
        `${config.tileServerUrl}/tiles/${blockTileSlideId}/zxy/0/0/0`,
        { responseType: 'arraybuffer' }
      ),
    ]);

    [thumbnailResponse, tileResponse].forEach(response => {
      expect(response.status).to.equal(200);
      expect(response.headers['content-type']).to.contain('image/jpeg');
      expect(response.data.byteLength).to.be.greaterThan(0);
    });
  });

  tileIt('does not serve tiles for the unmatched non-servable slide', async () => {
    try {
      await axios.get(
        `${config.tileServerUrl}/tiles/${unmatchedTileSlideId}/metadata`
      );
      expect.fail('Expected unmatched slide metadata request to fail with 404');
    } catch (error: any) {
      expect(error.response).to.not.equal(undefined);
      expect(error.response.status).to.equal(404);
    }
  });
});
