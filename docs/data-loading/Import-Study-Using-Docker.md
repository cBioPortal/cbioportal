# Import Study Using Docker

:warning: Every time you add/remove/overwrite a study please restart the Docker container, or 
call the `/api/cache` endpoint with a `DELETE` http-request (see [here](../deployment/customization/application.properties-Reference.md#evict-caches-with-the-apicache-endpoint) for more information).

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

Alternatively, call the cache eviction API (requires the API key configured in `application.properties`):

> ⚠️ During import operations, cBioPortal directly manipulates the Redis cache. The `/api/cache` DELETE endpoint may not be sufficient in all cases. If you encounter issues, restart the portal container instead: `docker compose restart cbioportal`.

```bash
curl -X DELETE -H "X-API-KEY: my-secret-api-key-value" http://localhost:8080/api/cache
```
