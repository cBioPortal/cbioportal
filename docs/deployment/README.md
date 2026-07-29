# Deployment

Private instances of cBioPortal are maintained by institutions and companies [around the world](https://www.cbioportal.org/installations).

As of v7, the only officially supported method to deploy cBioPortal is through Docker. The source code of cBioPortal is [available](https://github.com/cBioPortal/cbioportal) on GitHub under the terms of [Affero GPL V3](https://www.gnu.org/licenses/agpl-3.0.en.html).

Please note that installing a local version requires system administration skills; for example, installing and configuring Docker and ClickHouse. With limited resources, we cannot provide technical support on system administration.

v7 replaced MySQL with [ClickHouse](https://clickhouse.com/), which is now the only supported database. Run it self-hosted, as the Docker Compose setup does by default, or on [ClickHouse Cloud](https://clickhouse.com/cloud), which is what backs cbioportal.org and genie.cbioportal.org.

## Deployment Overview

- **[Deploy with Docker](/deployment/docker/README.md)** — Quick start with Docker Compose
- **[ClickHouse Setup](/deployment/clickhouse/README.md)** — Hosting options and architecture
- **[Standalone / Air-Gapped Deployment](/deployment/standalone/README.md)** — Self-hosted ClickHouse inside an isolated network
- **[Migrating from v6 (MySQL) to v7 (ClickHouse)](/Migration-v6-to-v7.md)** — Upgrade path for existing installations

Kubernetes is not supported for v7. The [v6 Kubernetes guide](/legacy/deployment/kubernetes/README.md) predates ClickHouse and is kept for reference only.
