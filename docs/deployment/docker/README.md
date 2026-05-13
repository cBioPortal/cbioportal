# Deploy with Docker

## Overview

This guide covers deploying cBioPortal using [Docker Compose](https://docs.docker.com/compose/), which orchestrates all required services together. The deployment includes:

- **ClickHouse** — the primary database for cancer study and reference data
- **cBioPortal web app** — serves the React frontend and REST API
- **Session service** — stores session data (active queries, saved cohorts) via a REST API
- **MongoDB** — persists session service data

Some data (e.g. OncoKB annotations) is fetched from external services at runtime.

## Prerequisites

- [Docker](https://www.docker.com/products/overview#/install_the_platform) (latest version)

> **Windows (WSL):** Use [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop/) and run commands from the WSL2 terminal — not PowerShell or Git Bash. Installing Docker inside Ubuntu will not work.

For additional notes on macOS and Windows, see [Notes for non-Linux systems](notes-for-non-linux.md).

## Quick Start

Clone the Docker Compose repository:

```
git clone https://github.com/cBioPortal/cbioportal-docker-compose.git
cd cbioportal-docker-compose
```

Edit the `.env` file to configure your environment — set passwords, choose the cBioPortal image version, and configure ClickHouse connection settings.

Then run the init script to download seed data, example config, and example studies:

```
./init.sh
```

> **Note:** If `.env` does not exist yet, `./init.sh` will prompt you to create one or generate it automatically. After the first run, edit `.env` to customize passwords, image versions, and database connection settings.

Review the generated files in `config/`, `data/`, and `study/` to confirm everything looks correct for your setup. Then start all services:

```
docker compose up
```

The first run will take a few minutes to import the seed database and run any necessary migrations. Each container writes logs to the terminal, prefixed with its container name (e.g. `cbioportal_container`). Some warnings are expected and can be ignored. Once everything is running, visit [http://localhost:8080](http://localhost:8080) — cBioPortal will be preloaded with the recommended gene panels.

<img width="1414" alt="Screen Shot 2022-01-24 at 2 10 10 PM" src="https://github.com/user-attachments/assets/296e1224-d390-45de-b1d1-6c8ec859e0e1">

## Managing the Deployment

To run in detached mode (background, no terminal output):

```
docker compose up -d
```

In detached mode, check logs per container:

```
docker logs -f cbioportal_container
```

List all containers on your system:

```
docker ps -a
```

Shut down all services:

```
docker compose down
# or docker compose down -v to remove volumes too
```

> **Tip:** If you are using [Docker Desktop](https://www.docker.com/products/docker-desktop/), detached mode is preferred — the app provides a UI for managing containers and viewing logs.

## Importing Studies

To import a study, run:

```
docker compose exec cbioportal metaImport.py -s study/msk_impact_2017/ -o  # Replace with your study directory name
```

This will import the [msk_impact_2017 study](https://www.cbioportal.org/study/summary?id=msk_impact_2017) into your local database. It will take a few minutes.

All public studies can be downloaded from [cbioportal.org/datasets](https://www.cbioportal.org/datasets) or [github.com/cBioPortal/datahub](https://github.com/cBioPortal/datahub). Add any study to the `./study` folder and import it. The `./study/init.sh` script can download multiple studies at once — set `DATAHUB_STUDIES` to any public study ID (e.g. `lgg_ucsf_2014`) and run `./init.sh`.

### Clearing the cache after import

After importing, the cBioPortal web app cache must be cleared for the new study to appear. You can either restart the container:

```
docker compose restart cbioportal
```

Or call the cache eviction API endpoint directly (no restart required):

```
curl -X DELETE -H "X-API-KEY: my-secret-api-key-value" http://localhost:8080/api/cache
```

The API key value is configured in `application.properties`. See [evicting caches with the /api/cache endpoint](/deployment/customization/application.properties-Reference.md#evict-caches-with-the-apicache-endpoint) for more details.

## Configuration

The main configuration file is `config/application.properties`, generated when you ran `./init.sh`.

For a full overview of available properties — custom logos, external databases, authentication, and more — see the [customization documentation](/deployment/customization/Customizing-your-instance-of-cBioPortal.md).

**Memory:** On systems with 4 GiB or more to spare, set `-Xms` and `-Xmx` to the same value to improve performance of memory-intensive features (e.g. the co-expression tab). If you are on macOS or Windows, see [Notes for non-Linux systems](notes-for-non-linux.md) for how to increase memory allocated to the Docker VM.

**OncoKB:** To enable OncoKB annotations, obtain a data access token via [OncoKB Data Access](/deployment/integration-with-other-webservices/OncoKB-Data-Access.md) and add it to `application.properties`.

## Further Reading

- [Importing data](import_data.md) — detailed import tutorial
- [Example commands](example_commands.md) — additional uses of the cBioPortal image
- [Keycloak authentication](using-keycloak.md) — Dockerizing Keycloak alongside cBioPortal

## Building the Image

If you need to build the cBioPortal image locally (e.g. for development), run the following from the root of the cbioportal repository:

```
docker build -t cbioportal/cbioportal:my-dev-cbioportal-image -f docker/web-and-data/Dockerfile .
```

Then update the `DOCKER_IMAGE_CBIOPORTAL` variable in the [cbioportal-docker-compose `.env` file](https://github.com/cBioPortal/cbioportal-docker-compose/blob/master/.env):

```
DOCKER_IMAGE_CBIOPORTAL=cbioportal/cbioportal:my-dev-cbioportal-image
```

The above builds the app as loose files (`web-and-data` variant). To build a single executable `app.jar` instead, use the `web` Dockerfile:

```
docker build -t cbioportal/cbioportal:my-dev-cbioportal-image -f docker/web/Dockerfile .
```

This variant may require a different Compose file. If you see an error about a missing `PortalApplication`, configure the launch command to use `app.jar`:

```
java -Xms2g -Xmx4g -jar /cbioportal-webapp/app.jar -spring...
```

A pre-configured [`docker-compose.web.yml`](https://github.com/cBioPortal/cbioportal-docker-compose/blob/master/docker-compose.web.yml) may be available for this variant:

```
docker compose -f docker-compose.web.yml up -d
```
