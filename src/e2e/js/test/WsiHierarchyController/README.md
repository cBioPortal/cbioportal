# WSI hierarchy E2E fixture notes

This E2E fixture is designed to be portable, minimal, and compatible with the
existing cBioPortal API E2E style.

The dedicated CircleCI job runs this fixture in authenticated WSI mode. The
local tile contract fixture validates the same source-bound version-2 JWT as
the companion tile server; it is not a public-mode production substitute. WSI
is login-only, including for public studies.

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

The CI seed retains a second non-public study only as an authorization-negative
control; no private slide source or patient data is used by the public fixture.

## Contract shape

The fixture intentionally asserts the backend JSON contract consumed by the WSI
v2 hierarchy endpoint:

- `sampleGroups[].parts[].blocks[].slides[]`
- slide placement fields (`sampleId`, `matchLevel`, `specimenKey`, and tile
  availability) are carried on each nested slide

The v2 access endpoint returns the exact source URL, intrinsic tile metadata,
thumbnail artifact, and a short-lived capability. The tile server receives
only that bundle and serves `/tiles/zxy` and `/thumbnails`; it does not expose
hierarchy, search, patient, or slide metadata routes.
