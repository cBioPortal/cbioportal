# Deploy with Docker

## Overview

This guide covers deploying cBioPortal using [Docker Compose](https://docs.docker.com/compose/), which orchestrates all required services together. The deployment includes:

- **ClickHouse** — the primary database for cancer study and reference data
- **cBioPortal web app** — serves the React frontend and REST API
- **Session service** — stores session data (active queries, saved cohorts) via a REST API
- **MongoDB** — persists session service data

Some data (e.g. OncoKB annotations) is fetched from external services at runtime.

> **See also:** For detailed ClickHouse setup instructions including ClickHouse Cloud and self-managed options, see the [ClickHouse Setup Guide](/deployment/clickhouse/README.md).

## Prerequisites

- [Docker](https://www.docker.com/products/overview#/install_the_platform) (latest version)

> **Windows (WSL):** Use [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop/) and run commands from the WSL2 terminal — not PowerShell or Git Bash. Installing Docker inside Ubuntu will not work.

For additional notes on macOS and Windows, see [Notes for non-Linux systems](notes-for-non-linux.md).

## Getting Started

> **Important:** Before starting, clear all existing Docker images, containers, and volumes from your system. This ensures that you aren't using an old Docker image by accident.

Clone the cBioPortal Docker Compose repository:

```
git clone https://github.com/cBioPortal/cbioportal-docker-compose.git
cd cbioportal-docker-compose
```

Edit the `.env` file to configure your environment — set passwords, choose the cBioPortal image version, and configure ClickHouse connection settings.

Then run the init script to download seed data, example configuration, and example studies:

```
./init.sh
```

Review the generated files in `config/`, `data/`, and `study/` to confirm everything looks correct for your setup.

Next, make sure that the Docker daemon is up and running:

- **macOS/Windows:** Start Docker Desktop.
- **Linux:** Run `sudo systemctl start docker`.

Then start all services:

```
# -d = detached mode, return control to the console
docker compose up -d

# Import an example study
docker compose exec cbioportal metaImport.py -s study/lgg_ucsf_2014 -o
```

> **IMPORTANT:** Please import at least one sample study before you attempt to view the portal. The website will not load if there are no studies present in the database. If you see an error like "studyIds should be a number between 1 and ...", that means the database is empty.

> **Note:** If the study references gene panels that are not loaded in the database, you will need to load them before the study will import properly. See [Importing Gene Panels](/data-loading/Import-Gene-Panels.md) for more information.

The first run will take a few minutes to import the seed database and run any necessary migrations. Each container writes logs to the terminal, prefixed with its container name (e.g. `cbioportal-container`). Some warnings are expected and can be ignored.

Once everything is running, visit [http://localhost:8080](http://localhost:8080) — cBioPortal will be preloaded with the recommended gene panels.

<img width="1414" alt="Screen Shot 2022-01-24 at 2 10 10 PM" src="https://github.com/user-attachments/assets/296e1224-d390-45de-b1d1-6c8ec859e0e1">

## Managing the Deployment

To start all services and view log output:

```
docker compose up
```

To run in detached mode (background, no terminal output):

```
docker compose up -d
```

> **Tip:** If you are using [Docker Desktop](https://www.docker.com/products/docker-desktop/), detached mode is preferred — the app provides a UI for managing containers and viewing logs.

In detached mode, check logs per container:

```
docker compose logs <container>
```

List all containers on your system:

```
docker ps -a
```

Shut down all services:

```
# -v removes volume data, such as that of the clickhouse container
docker compose down -v
```

## Importing Studies

First, copy your study data into the `study/` directory under `cbioportal-docker-compose`. The `study/` directory is mounted as a volume within the Docker container, which gives the cBioPortal importer access to it.

```
cp -r /path/to/your_study /path/to/cbioportal-docker-compose/study/
```

Next, run the following command to actually import your study. The `-s` path specifies the path to the study _as seen within the Docker container_.

```
docker compose exec cbioportal metaImport.py -s study/your_study -o
```

When this command executes -- it does so from the path `/` within the container. Then `study/your_study` resolves to the path `/study/your_study`, which is mounted to the `cbioportal-docker-compose/study` volume outside of the container.

> **Note:** If the validator detects any critical errors with the data, those must be fixed before the study can be imported.

> :warning: **Warning:** When importing large studies, you may run into a Java out-of-memory error on machines with limited RAM. You can try adjusting the Java heap size used by the importer in order to work around this, for example:
>
> ```
> docker compose exec cbioportal metaImport.py -s /study/your_study -o -jvo "-Xms16g -Xmx96g"
> ```

All public studies can be downloaded from [cbioportal.org/datasets](https://www.cbioportal.org/datasets) or [github.com/cBioPortal/datahub](https://github.com/cBioPortal/datahub). Add any study to the `./study` folder and import it. The `./study/init.sh` script can download multiple studies at once — set `DATAHUB_STUDIES` to any public study ID (e.g. `lgg_ucsf_2014`) and run `./init.sh`.

### Clearing the cache after import

After importing, the cBioPortal web app cache must be cleared for the new study to appear. You can do this by restarting the container:

```
docker compose restart cbioportal
```

## Configuration

The main configuration file for the cBioPortal web application is `config/application.properties`, generated when you run `./init.sh`.

For a full overview of available properties — custom logos, external databases, authentication, and more — see the [customization documentation](/deployment/customization/Customizing-your-instance-of-cBioPortal.md).

**Memory:** On systems with 4 GiB or more to spare, set `-Xms` and `-Xmx` to the same value in `docker-compose.yml` to improve performance of memory-intensive features (e.g. the co-expression tab). If you are on macOS or Windows, see [Notes for non-Linux systems](notes-for-non-linux.md) for how to increase memory allocated to the Docker VM.

**OncoKB:** To enable OncoKB annotations, obtain a data access token via [OncoKB Data Access](/deployment/integration-with-other-webservices/OncoKB-Data-Access.md) and add it to `application.properties`.

## Further Reading

- [Importing test data](import_test_data.md) — detailed import tutorial
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
