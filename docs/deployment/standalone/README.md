# Deploy Standalone (Air-Gapped) Isolated Version

This guide covers deploying cBioPortal with a self-hosted ClickHouse database in isolated or air-gapped environments where cloud services (including ClickHouse Cloud) are not accessible. This is a common requirement for hospital and pharmaceutical deployments.

## When to Use Standalone Deployment

- **Air-gapped networks** — No internet access to external services like ClickHouse Cloud.
- **On-premises requirements** — Data must remain within a private network for regulatory or security reasons.
- **Offline evaluation** — Evaluating cBioPortal in a fully self-contained environment.

## Architecture Overview

In a standalone deployment, all components run on your local infrastructure within a private network:

```
┌─────────────────────────────────────────────┐
│              Private Network                 │
│                                              │
│  ┌──────────────┐    ┌──────────────────┐    │
│  │ cBioPortal    │    │  ClickHouse DB   │    │
│  │ Web App       │◄──►│  (self-hosted)   │    │
│  │ (Java/Spring) │    │                  │    │
│  └──────────────┘    └──────────────────┘    │
│         ▲                                     │
│         │                                     │
│  ┌──────┴───────┐                            │
│  │ Session      │                            │
│  │ Service      │                            │
│  │ (+ MongoDB)  │                            │
│  └──────────────┘                            │
│                                              │
│  External data sources (OncoKB, Genome       │
│  Nexus, CIVIC) may be unavailable or         │
│  require local mirrors.                      │
└─────────────────────────────────────────────┘
```

## Deploying with Docker Compose in an Isolated Network

The recommended approach for standalone deployment is Docker Compose with all services on the same internal network. If you are not currently using Docker Compose, see [Deploy with Docker](/deployment/docker/README.md) first.

### Using the cbioportal-docker-compose Repository

The [cbioportal-docker-compose](https://github.com/cBioPortal/cbioportal-docker-compose) repository works out of the box in isolated environments, with one important change: **you must use a self-hosted ClickHouse container instead of ClickHouse Cloud**.

The default `docker-compose.yml` already includes a `cbioportal-database` service running ClickHouse, so no additional configuration is needed for the basic case:

```yaml
services:
  cbioportal-database:
    image: ${DOCKER_IMAGE_CLICKHOUSE}   # defaults to clickhouse/clickhouse-server:24.10 in .env
    # ...configured for isolated use by default
```

### Adding ClickHouse to an Existing Isolated Topology

If you already have a custom Docker Compose setup managing cBioPortal without ClickHouse, add a self-hosted ClickHouse service:

```yaml
services:
  cbioportal-database:
    image: ${DOCKER_IMAGE_CLICKHOUSE}   # e.g. clickhouse/clickhouse-server:24.10
    container_name: cbioportal-database-container
    restart: unless-stopped
    env_file:
      - .env                            # supplies CLICKHOUSE_DB / USER / PASSWORD
    ports:
      - "8123:8123"   # HTTP interface (JDBC, used by the web app)
      - "9000:9000"   # Native TCP (importer, clickhouse client)
    volumes:
      # Schema, seed data, and derived tables are loaded on first start.
      # Copy these files in from an internet-connected machine (see below).
      - ./data/schema.sql:/data/schema.sql:ro
      - ./data/load_schema.sh:/docker-entrypoint-initdb.d/001_load_schema.sh:ro
      - ./data/seed.sql.gz:/data/seed.sql.gz:ro
      - ./data/load_seed.sh:/docker-entrypoint-initdb.d/002_load_seed.sh:ro
      - ./data/clickhouse.sql:/data/clickhouse.sql:ro
      - ./data/load_derived_tables.sh:/docker-entrypoint-initdb.d/003_load_derived_tables.sh:ro
      - cbioportal_clickhouse_data:/var/lib/clickhouse
    networks:
      - cbio-net
    deploy:
      resources:
        limits:
          memory: 8G
        reservations:
          memory: 4G

volumes:
  cbioportal_clickhouse_data:

networks:
  cbio-net:
    driver: bridge
```

> **Note:** Adjust the memory limits based on your cohort size. See the [Sizing Guidance](/deployment/clickhouse/README.md#4-sizing-guidance) for recommendations.

### Configuring the Connection

In your `.env` file, point cBioPortal to the self-hosted ClickHouse instance. These are the variable names used by [cbioportal-docker-compose](https://github.com/cBioPortal/cbioportal-docker-compose):

```properties
# .env
CLICKHOUSE_HOST=cbioportal-database
CLICKHOUSE_HTTP_PORT=8123
CLICKHOUSE_NATIVE_PORT=9000
CLICKHOUSE_DB=cbioportal
CLICKHOUSE_USER=cbio_user
CLICKHOUSE_PASSWORD=your-strong-password
CLICKHOUSE_URL=jdbc:ch://cbioportal-database:8123/cbioportal
```

> **Note:** `CLICKHOUSE_HOST` and the host in `CLICKHOUSE_URL` must both match the ClickHouse service name in your Compose file. `CLICKHOUSE_URL` is what the web app uses over JDBC; `CLICKHOUSE_NATIVE_PORT` is what the importer and CLI use.

## Important Considerations for Air-Gapped Deployments

### Initial Setup Without Internet

The `init.sh` script from `cbioportal-docker-compose` downloads seed data and SQL scripts from GitHub. In an air-gapped environment:

1. On a machine with internet access, run:
   ```bash
   git clone https://github.com/cBioPortal/cbioportal-docker-compose.git
   cd cbioportal-docker-compose
   ./init.sh
   ```
2. Copy the entire `cbioportal-docker-compose` directory (including downloaded data in `data/` and `study/`) to the air-gapped machine.
3. The `data/` directory will contain `schema.sql`, `seed.sql.gz`, `clickhouse.sql`, `clickhouse_user_settings.xml`, and the `load_*.sh` initialization scripts — all needed for an offline initialization.

> **Note:** The Docker images themselves also have to cross the air gap. On the connected machine, `docker pull` each image referenced in `.env` (cBioPortal, ClickHouse, session service, MongoDB), then `docker save` them to a tarball and `docker load` it on the isolated host.

### External Data Sources

OncoKB, Genome Nexus, CIVIC, and the various annotation overlays are external services that may not be available in air-gapped environments. Disable the ones you cannot reach in `application.properties`:

```properties
show.oncokb=false
show.civic=false
show.genomenexus=false
show.hotspot=false
show.signal=false
show.ndex=false
```

See the [application.properties reference](/deployment/customization/application.properties-Reference.md) for the full list of toggles.

### Backup and Recovery

In isolated environments, database backups are especially important since you cannot rely on ClickHouse Cloud backups. See [Data Safety Warnings](/deployment/clickhouse/README.md#10-data-safety-warnings) for best practices.

## Further Reading

- [ClickHouse Setup Guide](/deployment/clickhouse/README.md) — detailed ClickHouse configuration
- [Deploy with Docker](/deployment/docker/README.md) — Docker Compose setup instructions
- [Data Loading](/data-loading/README.md) — importing studies
- [Customization](/deployment/customization/Customizing-your-instance-of-cBioPortal.md) — application.properties reference
