# Import Study Using Docker

:warning: Every time you add/remove/overwrite a study please restart the Docker container with `docker compose restart cbioportal`.

## Adding a Study

Open a terminal in the `cbioportal-docker-compose` directory and run:

```bash
docker compose exec cbioportal metaImport.py -s /study/${STUDY_DIRECTORY}
```

Where:
- **`${STUDY_DIRECTORY}`**: A subdirectory of `./study/` containing the study's meta and data files.

For example, to import a study placed in `./study/my_study/`:

```bash
docker compose exec cbioportal metaImport.py -s /study/my_study/
```

### Skipping Derived Table Rebuild

If importing multiple studies in a batch, skip the derived table rebuild after each import to save time:

```bash
docker compose exec cbioportal metaImport.py --no-derive-tables -s /study/my_study/
```

After all imports are complete, rebuild derived tables once:

```bash
docker compose exec cbioportal metaImport.py derive-tables
```

## Restarting the Portal

After importing, restart the cBioPortal web container to see the new study:

```bash
docker compose restart cbioportal
```
