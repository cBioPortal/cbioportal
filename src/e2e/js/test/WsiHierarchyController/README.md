# WSI hierarchy E2E fixture notes

This E2E fixture is designed to be portable, minimal, and compatible with the
existing cBioPortal API E2E style.

The dedicated CircleCI job runs this fixture in authenticated WSI mode. The
local tile contract fixture validates the same version-1 JWT and trusted
study-to-resource index contract as the companion tile server; it is not a
public-mode production substitute. WSI is login-only, including for public
studies.

## Public source

The fixture is anchored to the public `msk_spectrum_tme_2022` study and uses
publicly available patient/sample/slide identifiers from that study:

- patient `P-0055908`
- sample `P-0055908-T01-IM6`
- an unmatched pathology group with `sampleId: null`
- block-matched slide `3020726`
- part-matched slide `3020691`
- unmatched slide `3020648`

The fixture trims the live hierarchy down to one example per required match
level:

- `PART`
- `BLOCK`
- `UNMATCHED`

The CI seed also adds private `wsi_ci_study_b` with a separate patient and
slide. The test user is authorized for the public A study only, so a study-A
capability must not read the B patient or slide resources.

## Contract shape

The fixture intentionally asserts the backend JSON contract consumed by the WSI
v2 hierarchy endpoint:

- `sampleGroups[].parts[].blocks[].slides[]`
- slide placement fields (`sampleId`, `matchLevel`, `specimenKey`, and tile
  availability) are carried on each nested slide

Protected tile-server routes are expected to bind the token's `study_id` to
the trusted index for `/patient`, `/slides/{id}/dbmeta`, thumbnails, tiles,
warmup, metadata, and filtered search. A client-supplied `studyId` is only a
consistency check.
