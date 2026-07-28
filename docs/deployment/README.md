# Deployment

Private instances of cBioPortal are maintained by institutions and companies [around the world](https://www.cbioportal.org/installations).

As of v7, the only officially supported method to deploy cBioPortal is through Docker. The source code of cBioPortal is [available](https://github.com/cBioPortal/cbioportal) on GitHub under the terms of [Affero GPL V3](https://www.gnu.org/licenses/agpl-3.0.en.html).

Please note that installing a local version requires system administration skills; for example, installing and configuring Docker and ClickHouse. With limited resources, we cannot provide technical support on system administration.

As of v7, [ClickHouse](https://clickhouse.com/) is the sole database backend for cBioPortal, replacing MySQL. It is an analytical (columnar) database designed for queries over billions of rows, which is what makes Study View responsive on large cohorts. You can run it self-hosted (the default in Docker Compose) or on [ClickHouse Cloud](https://clickhouse.com/cloud), the managed service that backs the public portals at cbioportal.org and genie.cbioportal.org.

## Deployment Overview

- **[Deploy with Docker](/deployment/docker/README.md)** — Quick start with Docker Compose
- **[ClickHouse Setup](/deployment/clickhouse/README.md)** — Detailed ClickHouse configuration guide: hosting options, architecture, sizing, and troubleshooting
- **[Standalone / Air-Gapped Deployment](/deployment/standalone/README.md)** — Self-hosted ClickHouse inside an isolated network
- **[Migrating from v6 (MySQL) to v7 (ClickHouse)](/Migration-v6-to-v7.md)** — Upgrade path for existing installations

> **Note on Kubernetes:** Kubernetes is not an officially supported deployment path for v7. The [v6 Kubernetes guide](/legacy/deployment/kubernetes/README.md) is retained for reference only and predates the move to ClickHouse.
