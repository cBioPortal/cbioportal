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
    image: clickhouse/clickhouse-server:latest
    # ...configured for isolated use by default
```

### Adding ClickHouse to an Existing Isolated Topology

If you already have a custom Docker Compose setup managing cBioPortal without ClickHouse, add a self-hosted ClickHouse service:

```yaml
services:
  clickhouse:
    image: clickhouse/clickhouse-server:24.8
    container_name: cbioportal-clickhouse
    restart: unless-stopped
    ports:
      - "8123:8123"   # HTTP interface (JDBC)
      - "9000:9000"   # Native TCP (importer, CLI)
    volumes:
      - clickhouse-data:/var/lib/clickhouse
      - ./config/clickhouse_users.xml:/etc/clickhouse-server/users.d/cbioportal.xml:ro
    networks:
      - cbio-internal
    environment:
      CLICKHOUSE_DB: cbioportal
      CLICKHOUSE_USER: cbio_user
      CLICKHOUSE_PASSWORD: ${CLICKHOUSE_PASSWORD}
    deploy:
      resources:
        limits:
          memory: 8G
        reservations:
          memory: 4G

volumes:
  clickhouse-data:

networks:
  cbio-internal:
    driver: bridge
```

> **Note:** Adjust the memory limits based on your cohort size. See the [Sizing Guidance](/deployment/clickhouse/README.md#4-sizing-guidance) for recommendations.

### Configuring the Connection

In your `.env` file (or `application.properties`), point cBioPortal to the self-hosted ClickHouse instance:

```properties
# .env
CLICKHOUSE_HOST=clickhouse
CLICKHOUSE_PORT=8123
CLICKHOUSE_DB=cbioportal
CLICKHOUSE_USER=cbio_user
CLICKHOUSE_PASSWORD=your-strong-password
```

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
3. The `data/` directory will contain `schema.sql`, `seed.sql.gz`, `clickhouse.sql`, and `clickhouse_user_settings.xml` — all needed for an offline initialization.

### External Data Sources

OncoKB, Genome Nexus, CIVIC, and G2S are external services that may not be available in air-gapped environments. Disable them in `application.properties`:

```properties
show.oncokb=false
show.civic=false
show.genomenexus=false
show.g2s=false
```

### Backup and Recovery

In isolated environments, database backups are especially important since you cannot rely on ClickHouse Cloud backups. See [Data Safety Warnings](/deployment/clickhouse/README.md#10-data-safety-warnings) for best practices.

## Further Reading

- [ClickHouse Setup Guide](/deployment/clickhouse/README.md) — detailed ClickHouse configuration
- [Deploy with Docker](/deployment/docker/README.md) — Docker Compose setup instructions
- [Data Loading](/data-loading/README.md) — importing studies
- [Customization](/deployment/customization/Customizing-your-instance-of-cBioPortal.md) — application.properties reference
