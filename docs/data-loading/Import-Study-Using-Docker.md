# Import Study Using Docker

> :warning: Every time you add/remove/overwrite a study, please restart the Docker container with `docker compose restart cbioportal`.

## Adding a Study

First, make sure that the study has been placed in the `study/` directory under the `cbioportal-docker-compose` repo. Then open a terminal in the repo root and run:

```bash
docker compose exec cbioportal metaImport.py -s /study/<your_study> -o
```

Where:
- **`<your_study>`**: Replace with the subdirectory name in `cbioportal-docker-compose/study/` containing the study's meta and data files.

For example, to import a study placed in `cbioportal-docker-compose/study/my_study/`:

```bash
docker compose exec cbioportal metaImport.py -s /study/my_study/ -o
```

### Importing Multiple Studies

If importing multiple studies in a batch, skip the derived table rebuild after each import to save time:

```bash
docker compose exec cbioportal metaImport.py --no-derive-tables -s /study/my_study1/ -o
docker compose exec cbioportal metaImport.py --no-derive-tables -s /study/my_study2/ -o
docker compose exec cbioportal metaImport.py --no-derive-tables -s /study/my_study3/ -o
```

After all imports are complete, rebuild derived tables just once:

```bash
docker compose exec cbioportal metaImport.py derive-tables
```

Refer to [the ClickHouse guide](/deployment/clickhouse/README.md) for more information on derived tables.

## Restarting the Portal

After importing, restart the cBioPortal web container to clear the cache and see the new study:

```bash
docker compose restart cbioportal
```
