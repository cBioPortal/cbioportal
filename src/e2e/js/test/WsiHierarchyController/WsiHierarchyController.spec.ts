import { expect } from 'chai';
import axios from 'axios';
import crypto from 'crypto';
import fs from 'fs';
import path from 'path';

const config = {
  serverUrl: process.env.CBIOPORTAL_URL || 'http://localhost:8080',
  tileServerUrl: process.env.WSI_TILE_SERVER_URL || 'http://localhost:8081',
  frontendUrl: process.env.CBIOPORTAL_FRONTEND_URL || 'http://localhost:3000',
  authSecret: process.env.WSI_AUTH_SECRET || 'local-development-wsi-secret-change-me-32chars',
  authAudience: process.env.WSI_AUTH_AUDIENCE || 'cbioportal-wsi',
  blockTileSlideId: process.env.WSI_TEST_BLOCK_SLIDE_ID || '',
  partTileSlideId: process.env.WSI_TEST_PART_SLIDE_ID || '',
  unmatchedTileSlideId: process.env.WSI_TEST_UNMATCHED_SLIDE_ID || '',
};

const hasAuthenticatedWsiSetup = Boolean(
  process.env.WSI_AUTH_SECRET && process.env.WSI_LOCAL_AUTH_BYPASS !== 'true'
);
const hasTileSetup = Boolean(process.env.WSI_AUTH_SECRET);
const localAuthBypass = process.env.WSI_LOCAL_AUTH_BYPASS === 'true';
const hasExplicitFrontend = Boolean(process.env.CBIOPORTAL_FRONTEND_URL);
const hasExplicitTileIds = Boolean(
  process.env.WSI_TEST_BLOCK_SLIDE_ID &&
    process.env.WSI_TEST_PART_SLIDE_ID &&
    process.env.WSI_TEST_UNMATCHED_SLIDE_ID
);

type Slide = {
  imageId: string;
  sampleId: string | null;
  matchLevel: 'PART' | 'BLOCK' | 'UNMATCHED';
  canServeTiles: boolean;
};
type Block = { slides: Slide[] };
type Part = { blocks: Block[] };
type Sample = { sampleId: string | null; parts: Part[] };
type PatientHierarchy = {
  referenceSampleId: string | null;
  sampleGroups: Sample[];
};
type SlideMetadata = {
  dimensions: { width: number; height: number };
  levels: number;
  level_dimensions: Array<{ width: number; height: number }>;
  tile_size?: number;
  objective_power?: number;
  vendor?: string;
};
type SlideAccess = { tileMetadata: SlideMetadata };
type HierarchyFixture = {
  study_id: string;
  patient_id: string;
  hierarchy: PatientHierarchy;
};

const currentDir = path.resolve(process.cwd(), 'test/WsiHierarchyController');
const fixturePath = path.join(
  currentDir,
  'msk_spectrum_tme_2022.wsi_hierarchy.jsonl'
);
const fixture = JSON.parse(
  fs.readFileSync(fixturePath, 'utf8').trim()
) as HierarchyFixture;

function collectSlideIds(hierarchy: PatientHierarchy): string[] {
  return hierarchy.sampleGroups.flatMap(sample =>
    sample.parts.flatMap(part =>
      part.blocks.flatMap(block => block.slides.map(slide => slide.imageId))
    )
  );
}

function findSlide(
  hierarchy: PatientHierarchy,
  matchLevel: Slide['matchLevel']
): Slide {
  const slide = hierarchy.sampleGroups
    .flatMap(sample => sample.parts)
    .flatMap(part => part.blocks)
    .flatMap(block => block.slides)
    .find(candidate => candidate.matchLevel === matchLevel);
  expect(slide, `missing ${matchLevel} slide`).to.not.equal(undefined);
  return slide!;
}

function base64url(value: object): string {
  return Buffer.from(JSON.stringify(value)).toString('base64url');
}

function makeToken(
  studyId: string,
  imageId: string,
  source = 'file:///app/testdata/CMU-1-Small-Region.svs',
  thumbnail = 'file:///app/testdata/3020691.jpg',
  secret = config.authSecret,
  overrides: Record<string, unknown> = {}
): string {
  const now = Math.floor(Date.now() / 1000);
  const header = base64url({ alg: 'HS256', typ: 'JWT' });
  const payload = base64url({
    sub: 'wsi-ci-user',
    aud: config.authAudience,
    scope: 'wsi:read',
    study_id: studyId,
    image_id: imageId,
    wsi_auth_version: 2,
    tile_source_sha256: crypto.createHash('sha256').update(source).digest('hex'),
    thumbnail_source_sha256: crypto.createHash('sha256').update(thumbnail).digest('hex'),
    thumbnail_width: 256,
    thumbnail_height: 232,
    iat: now,
    exp: now + 300,
    ...overrides,
  });
  const signature = crypto
    .createHmac('sha256', secret)
    .update(`${header}.${payload}`)
    .digest('base64url');
  return `${header}.${payload}.${signature}`;
}

function bearer(token: string) {
  return { headers: { Authorization: `Bearer ${token}` } };
}

function sourceRequest(token: string, source: string) {
  return {
    headers: { Authorization: `Bearer ${token}`, 'X-WSI-Source': source },
  };
}

function cookieHeader(response: any): string {
  return (response.headers['set-cookie'] || [])
    .map((cookie: string) => cookie.split(';', 1)[0])
    .join('; ');
}

async function login(): Promise<string> {
  const response = await axios.post(
    `${config.serverUrl}/j_spring_security_check`,
    'j_username=wsi-ci-user&j_password=wsi-ci-password&user_id=wsi-ci-user',
    {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      maxRedirects: 0,
      validateStatus: status => status === 302,
    }
  );
  expect(cookieHeader(response)).to.not.equal('');
  return cookieHeader(response);
}

function sessionRequest(cookie: string) {
  return { headers: { Cookie: cookie } };
}

async function authenticatedRequestOptions() {
  return localAuthBypass ? undefined : sessionRequest(await login());
}

async function statusOf(request: Promise<any>): Promise<number> {
  try {
    return (await request).status;
  } catch (error: any) {
    expect(error.response, 'request did not return an HTTP response').to.not.equal(undefined);
    return error.response.status;
  }
}

describe('Authenticated WsiHierarchyController and tile contract', () => {
  const hierarchyUrl = `${config.serverUrl}/api/wsi/v2/hierarchy/${fixture.study_id}/${fixture.patient_id}`;
  const frontendHierarchyUrl = `${config.frontendUrl}/api/wsi/v2/hierarchy/${fixture.study_id}/${fixture.patient_id}`;
  const blockSlide = findSlide(fixture.hierarchy, 'BLOCK');
  const partSlide = findSlide(fixture.hierarchy, 'PART');
  const unmatchedSlide = findSlide(fixture.hierarchy, 'UNMATCHED');
  const blockTileSlideId = config.blockTileSlideId || blockSlide.imageId;
  const partTileSlideId = config.partTileSlideId || partSlide.imageId;
  const unmatchedTileSlideId = config.unmatchedTileSlideId || unmatchedSlide.imageId;

  it('requires login before issuing a slide capability', async function () {
    if (!hasAuthenticatedWsiSetup) this.skip();
    expect(
      await statusOf(
        axios.get(
          `${config.serverUrl}/api/wsi/v2/slides/${fixture.study_id}/${blockSlide.imageId}/access`
        )
      )
    ).to.equal(401);
  });

  it('issues a source-bound slide capability only after login and permission checks', async function () {
    if (!hasTileSetup) this.skip();
    const requestOptions = await authenticatedRequestOptions();
    const authorized = await axios.get(
      `${config.serverUrl}/api/wsi/v2/slides/${fixture.study_id}/${blockSlide.imageId}/access`,
      requestOptions
    );
    expect(authorized.status).to.equal(200);
    expect(authorized.headers['cache-control']).to.contain('no-store');
    expect(authorized.data.imageId).to.equal(blockSlide.imageId);
    expect(authorized.data.accessToken).to.be.a('string').and.not.empty;
    expect(authorized.data.sourceUrl).to.equal(
      'file:///app/testdata/CMU-1-Small-Region.svs'
    );

    if (!localAuthBypass) {
      expect(
        await statusOf(
          axios.get(
            `${config.serverUrl}/api/wsi/v2/slides/wsi_ci_study_b/4020726/access`,
            requestOptions
          )
        )
      ).to.equal(403);
    }
    expect(
      await statusOf(
        axios.get(
          `${config.serverUrl}/api/wsi/v2/slides/${fixture.study_id}/missing-slide/access`,
          requestOptions
        )
      )
    ).to.equal(404);
  });

  it('returns the materialized hierarchy only for the authenticated study session', async function () {
    if (!hasTileSetup) this.skip();
    const requestOptions = await authenticatedRequestOptions();
    const response = await axios.get<PatientHierarchy>(hierarchyUrl, requestOptions);

    expect(response.status).to.equal(200);
    expect(response.headers['content-type']).to.contain('application/json');
    expect(response.headers['cache-control']).to.contain('private');
    expect(response.data).to.deep.equal(fixture.hierarchy);
    if (!localAuthBypass) {
      expect(
        await statusOf(
          axios.get(
            `${config.serverUrl}/api/wsi/v2/hierarchy/wsi_ci_study_b/WSI-CI-B-PATIENT`,
            requestOptions
          )
        )
      ).to.equal(403);
    }
  });

  const frontendIt = hasTileSetup && hasExplicitFrontend ? it : it.skip;
  frontendIt('matches the authenticated backend hierarchy through the frontend proxy', async function () {
    const requestOptions = await authenticatedRequestOptions();
    const [backendResponse, frontendResponse] = await Promise.all([
      axios.get<PatientHierarchy>(hierarchyUrl, requestOptions),
      axios.get<PatientHierarchy>(frontendHierarchyUrl, requestOptions),
    ]);

    expect(frontendResponse.status).to.equal(200);
    expect(frontendResponse.data).to.deep.equal(backendResponse.data);
  });

  it('covers block, part, and unmatched slide associations in the hierarchy', () => {
    const slideIds = collectSlideIds(fixture.hierarchy);
    const slides = fixture.hierarchy.sampleGroups
      .flatMap(sample => sample.parts)
      .flatMap(part => part.blocks)
      .flatMap(block => block.slides);
    expect(slideIds).to.have.members(['3020726', '3020691', '3020648']);
    expect(slides.map(slide => slide.matchLevel)).to.have.members([
      'PART',
      'BLOCK',
      'UNMATCHED',
    ]);
    slides.forEach(slide => {
      expect(slideIds).to.include(slide.imageId);
      if (slide.matchLevel === 'UNMATCHED') {
        expect(slide.sampleId).to.equal(null);
      } else {
        expect(slide.sampleId).to.be.a('string').and.not.empty;
      }
    });
  });

  it('returns 404 for an unknown patient after authentication', async function () {
    if (!hasTileSetup) this.skip();
    const requestOptions = await authenticatedRequestOptions();
    expect(
      await statusOf(
        axios.get(
          `${config.serverUrl}/api/wsi/v2/hierarchy/${fixture.study_id}/missing-patient`,
          requestOptions
        )
      )
    ).to.equal(404);
  });

  const tileIt = hasTileSetup ? it : it.skip;
  tileIt('binds tiles and thumbnails to the exact source in the capability', async function () {
    const source = 'file:///app/testdata/CMU-1-Small-Region.svs';
    const thumbnail = 'file:///app/testdata/3020691.jpg';
    const token = makeToken(fixture.study_id, blockTileSlideId, source, thumbnail);
    const tileUrl = `${config.tileServerUrl}/tiles/zxy/0/0/0`;
    const thumbnailUrl = `${config.tileServerUrl}/thumbnails?width=128&height=96`;
    expect((await axios.get(tileUrl, { ...sourceRequest(token, source), responseType: 'arraybuffer' })).status).to.equal(200);
    expect((await axios.get(thumbnailUrl, { ...sourceRequest(token, thumbnail), responseType: 'arraybuffer' })).status).to.equal(200);
    expect(
      await statusOf(
        axios.get(
          `${config.tileServerUrl}/tiles/zxy/0/0/0`,
          sourceRequest(token, 'file:///app/testdata/other.svs')
        )
      )
    ).to.equal(403);
  });

  tileIt('rejects missing, invalid, expired, over-maximum, and wrong-audience tokens', async function () {
    const source = 'file:///app/testdata/CMU-1-Small-Region.svs';
    const pathToTest = `${config.tileServerUrl}/tiles/zxy/0/0/0`;
    const sourceHeaders = { 'X-WSI-Source': source };
    expect(await statusOf(axios.get(pathToTest, { headers: sourceHeaders }))).to.equal(401);
    expect(await statusOf(axios.get(pathToTest, sourceRequest('not-a-jwt', source)))).to.equal(401);
    expect(await statusOf(axios.get(pathToTest, sourceRequest(makeToken(fixture.study_id, blockTileSlideId, source, 'file:///app/testdata/3020691.jpg', 'x'.repeat(32)), source)))).to.equal(401);
    expect(
      await statusOf(
        axios.get(pathToTest, sourceRequest(makeToken(fixture.study_id, blockTileSlideId, source, 'file:///app/testdata/3020691.jpg', config.authSecret, { aud: 'wrong-audience' }), source))
      )
    ).to.equal(401);
    const now = Math.floor(Date.now() / 1000);
    expect(
      await statusOf(
        axios.get(
          pathToTest,
          sourceRequest(makeToken(fixture.study_id, blockTileSlideId, source, 'file:///app/testdata/3020691.jpg', config.authSecret, { iat: now - 300, exp: now - 1 }), source)
        )
      )
    ).to.equal(401);
    expect(
      await statusOf(
        axios.get(
          pathToTest,
          sourceRequest(makeToken(fixture.study_id, blockTileSlideId, source, 'file:///app/testdata/3020691.jpg', config.authSecret, { exp: now + 901 }), source)
        )
      )
    ).to.equal(401);
  });

  tileIt('accepts a replacement token after refresh', async function () {
    const requestOptions = await authenticatedRequestOptions();
    const first = await axios.get(
      `${config.serverUrl}/api/wsi/v2/slides/${fixture.study_id}/${blockTileSlideId}/access`,
      requestOptions
    );
    const second = await axios.get(
      `${config.serverUrl}/api/wsi/v2/slides/${fixture.study_id}/${blockTileSlideId}/access`,
      requestOptions
    );
    // Issuance can occur within the same second, so a deterministic JWT may be
    // byte-identical.  The refresh contract is that the replacement is valid
    // and source-bound, not that its serialized value must differ.
    expect(second.data.accessToken).to.be.a('string').and.not.empty;
    const source = 'file:///app/testdata/CMU-1-Small-Region.svs';
    expect(
      (await axios.get(`${config.tileServerUrl}/tiles/zxy/0/0/0`, sourceRequest(second.data.accessToken, source))).status
    ).to.equal(200);
  });

  const hierarchyTileConsistencyIt =
    hasTileSetup && !hasExplicitTileIds ? it : it.skip;
  hierarchyTileConsistencyIt('reports tile-serving capability consistently with live behavior', async function () {
    const slides = fixture.hierarchy.sampleGroups
      .flatMap(sample => sample.parts)
      .flatMap(part => part.blocks)
      .flatMap(block => block.slides);
    const servableSlides = slides.filter(slide => slide.canServeTiles === true);
    const nonServableSlides = slides.filter(slide => slide.canServeTiles === false);
    expect(servableSlides.map(slide => slide.imageId)).to.have.members([
      blockSlide.imageId,
      partSlide.imageId,
    ]);
    expect(nonServableSlides.map(slide => slide.imageId)).to.deep.equal([
      unmatchedSlide.imageId,
    ]);
    const requestOptions = await authenticatedRequestOptions();
    const metadataResponses = await Promise.all(
      servableSlides.map(slide =>
        axios.get<SlideAccess>(
          `${config.serverUrl}/api/wsi/v2/slides/${fixture.study_id}/${slide.imageId}/access`,
          requestOptions
        )
      )
    );
    metadataResponses.forEach(response => {
      expect(response.status).to.equal(200);
      expect(response.data.tileMetadata.levels).to.be.greaterThan(0);
    });
    expect(
      await statusOf(
        axios.get(
          `${config.serverUrl}/api/wsi/v2/slides/${fixture.study_id}/${unmatchedTileSlideId}/access`,
          requestOptions
        )
      )
    ).to.equal(404);
  });
});
