# WSI hierarchy E2E fixture notes

This E2E fixture is designed to be portable, minimal, and compatible with the
existing cBioPortal API E2E style.

## Public source

The fixture is anchored to the public `msk_spectrum_tme_2022` study and uses
publicly available patient/sample/slide identifiers from that study:

- patient `P-0055908`
- sample `P-0055908-T01-IM6`
- sample `UNMATCHED`
- block-matched slide `3020726`
- part-matched slide `3020691`
- unmatched slide `3020648`

The fixture trims the live hierarchy down to one example per required match
level:

- `PART`
- `BLOCK`
- `UNMATCHED`

## Contract shape

The fixture intentionally asserts the backend JSON contract consumed by the WSI
hierarchy endpoints:

- `samples[].parts[].blocks[].slides[]`
- `slide_associations[]`
- `/bootstrap` response envelope with `initial: null`
