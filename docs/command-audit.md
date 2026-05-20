# Command Audit

This document tracks the status of runnable command examples across the docs, specifically for:

- **Type 1** — Script called directly (`./metaImport.py`, `python validateData.py`, etc.) without `docker compose exec cbioportal`
- **Type 2** — `docker compose exec` missing the `cbioportal` service name
- **Type 3** — Host filesystem paths (e.g. `../../../test/scripts/test_data/`) in runnable bash blocks that should use container-internal paths (`/study/...`)

---

## Fixed

| File | Lines | Type | Notes |
|------|-------|------|-------|
| `data-loading/Using-the-dataset-validator.md` | 780 | 2 | `docker compose exec validateStudies.py` → added `cbioportal` service name |
| `data-loading/Using-the-dataset-validator.md` | 817–827 | 3 | Sample output paths updated from `../../../test/scripts/test_data/` to `/study/` |
| `data-loading/Using-the-dataset-validator.md` | 832, 838, 844, 850 | 1+3 | `./validateStudies.py` with host paths → `docker compose exec cbioportal validateStudies.py` with `/study/` paths |

---

## Out of scope (deprecated docs)

These files have an `⚠️ Outdated Documentation` banner at the top. The commands are wrong but intentionally not updated as the pages themselves need a full rewrite for v7.

| File | Lines | Type | Notes |
|------|-------|------|-------|
| `deployment/deploy-without-docker/Load-Sample-Cancer-Study.md` | 44–45 | 1 | `./importGenePanel.pl` called directly |
| `deployment/deploy-without-docker/Load-Sample-Cancer-Study.md` | 67 | 1 | `./validateData.py` called directly |
| `deployment/deploy-without-docker/Load-Sample-Cancer-Study.md` | 87 | 1 | `./metaImport.py` called directly |
| `Updating-gene-and-gene_alias-tables.md` | 104 | 1 | `./importGenesetData.pl` called directly |

---

## Verified clean

These files were audited and all commands are correct.

- `data-loading/Using-the-metaImport-script.md`
- `data-loading/Data-Loading-For-Developers.md`
- `data-loading/Data-Loading-Maintaining-Studies.md`
- `data-loading/Import-Gene-Panels.md`
- `data-loading/Import-Gene-Sets.md`
- `deployment/docker/example_commands.md`
- `deployment/docker/import_test_data.md`
- `deployment/docker/README.md`
