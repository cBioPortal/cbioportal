# ClickHouse Setup Guide

Starting with version 7, cBioPortal uses [ClickHouse](https://clickhouse.com/) as its sole database. This guide will help you set up and configure a ClickHouse-backed cBioPortal instance.

## Table of Contents

1. [Installing the ClickHouse CLI](#1-installing-the-clickhouse-cli)
2. [Hosting Options](#2-hosting-options)
3. [Architecture](#3-architecture)
4. [Sizing Guidance](#4-sizing-guidance)
5. [Docker Compose Setup](#5-docker-compose-setup)
6. [Relevant Data Files](#6-relevant-data-files)
7. [Data Loading](#7-data-loading)
8. [Notes on Derived Tables](#8-notes-on-derived-tables)
9. [Notes for Users with High-Volume Data](#9-notes-for-users-with-high-volume-data)
10. [Data Safety Warnings](#10-data-safety-warnings)
11. [Verifying Database Integrity](#11-verifying-database-integrity)
12. [Migrating from MySQL to ClickHouse](#12-migrating-from-mysql-to-clickhouse)
13. [Troubleshooting](#13-troubleshooting)
14. [Version Migration](#14-version-migration)
15. [Further Reading](#15-further-reading)

---

## 1. Installing the ClickHouse CLI

The ClickHouse command-line client is useful for inspecting data, running ad-hoc queries, and debugging.

> **Note:** It is not strictly necessary to install the ClickHouse CLI on your local machine, as it comes pre-installed inside the Docker container. However, it can be more convenient for database access. See [Deploy with Docker](/deployment/docker/README.md) for more information.

**Linux / macOS:**

```bash
curl https://clickhouse.com/ | sh && ./clickhouse install

# If running inside of an automated script, do: 
curl https://clickhouse.com/ | sh && ./clickhouse install --noninteractive
```

**Verify installation:**

```bash
clickhouse client --version
```

> **Note:** The ClickHouse CLI is the `clickhouse` binary invoked as `clickhouse client`. The `clickhouse-client` command is a legacy wrapper that is no longer installed by default on newer ClickHouse versions. Use `clickhouse client` for all command-line operations.

Once you have your ClickHouse database set up, you can connect to it with the ClickHouse CLI. ClickHouse exposes two ports: HTTP (default 8123) and native TCP (default 9000). The `clickhouse client` command connects via the native TCP protocol.

**Connecting to a local instance** (default native port 9000):

```bash
clickhouse client --host localhost --port 9000 --user cbio_user --password 'your-password'
```

**Connecting to a remote instance (e.g., ClickHouse Cloud):**

```bash
# Note: the default native port for ClickHouse Cloud is 9440
clickhouse client --host <hostname> --port <port> --user <user> --password '<password>' --database cbioportal
```

For HTTP access (port 8123), use `curl` or the ClickHouse HTTP interface directly.

If you are having trouble installing the ClickHouse CLI on your host machine, it is also possible to connect to the ClickHouse database through Docker. See [Docker Compose Setup](#5-docker-compose-setup).

---

## 2. Hosting Options

### Local Docker Compose

The simplest way to get started. The [cBioPortal Docker Compose](https://github.com/cBioPortal/cbioportal-docker-compose) repository provides a pre-configured `docker-compose.yml` that spins up cBioPortal with a ClickHouse database, session service, and importer in one command.

- **Pros:** Zero configuration, easy to tear down, great for evaluation and development.
- **Cons:** Limited by your machine's resources. Not suitable for large production datasets.

See [Deploy with Docker](/deployment/docker/README.md) for more information.

### ClickHouse Cloud

[ClickHouse Cloud](https://clickhouse.com/cloud) offers managed ClickHouse instances with adjustable RAM and compute.

- **Pros:** No server maintenance, elastic scaling, built-in backups.
- **Cons:** Can be expensive for large databases. Network latency if not in the same region as your cBioPortal instance.

#### How MSK hosts ClickHouse

MSK uses ClickHouse Cloud for backing its own cBioPortal instances at cbioportal.org and genie.cbioportal.org. We benefit from being able to adjust the amount of RAM/compute each instance is using, since importing large studies can cause very high memory usage. We also have our own blue-green deployment architecture that enables us to swap between new copies of the data seamlessly.

If you want to get ClickHouse Cloud working with your own setup, you can try removing the `cbioportal-database` container from the Docker Compose file and adjusting the ClickHouse settings in `.env` to point to your ClickHouse Cloud instance. However, this method is not documented extensively yet because we are prioritizing Docker Compose as the official, community-supported method of deployment. If you need help getting ClickHouse Cloud set up and it is mission-critical for your deployment, please reach out to the cBioPortal team.

---

## 3. Architecture

cBioPortal v7 uses ClickHouse as its sole database backend. This section describes how ClickHouse fits into the overall application architecture.

### Database Layers

ClickHouse stores two categories of tables:

- **Base tables** — Store the raw study data as imported: cancer studies, samples, patients, genetic profiles, mutations, copy-number alterations, clinical data, etc. These are populated by `metaImport.py` during study import.
- **Derived tables** — Precomputed, denormalized tables built from the base tables by running `clickhouse.sql`. These accelerate Study View queries by collapsing joins across multiple base tables into a single table scan. See [section 8](#8-notes-on-derived-tables) for details.

### How Components Connect

```
┌──────────────────────┐     HTTP (8123)      ┌──────────────────┐
│   cBioPortal Web App │ ◄──────────────────► │  ClickHouse DB   │
│   (Java Spring Boot) │     JDBC (native)     │                  │
└──────────────────────┘                       └──────────────────┘
         ▲                                              ▲
         │                                              │
         │ HTTP REST API                                │ native TCP (9000)
         ▼                                              ▼
┌──────────────────────┐                       ┌──────────────────┐
│  Frontend (React)    │                       │ metaImport.py    │
│  / Session Service   │                       │ (importer)       │
└──────────────────────┘                       └──────────────────┘
```

1. **Web App** — The cBioPortal Java backend connects to ClickHouse via JDBC (using the ClickHouse JDBC driver) on port 8123 (HTTP) or the native protocol. It queries both base tables and derived tables depending on the endpoint.
2. **Importer** — `metaImport.py` and the Java importer JAR connect to ClickHouse using the ClickHouse native protocol (port 9000). They write to base tables and optionally rebuild derived tables.
3. **CLI / Admin** — The `clickhouse client` command and any administrative scripts connect via native TCP.

### Connection Configuration

The web app connects to ClickHouse using properties in `application.properties`:

```properties
spring.datasource.url=jdbc:clickhouse://<host>:8123/<database>
spring.datasource.username=<user>
spring.datasource.password=<password>
spring.datasource.driver-class-name=com.clickhouse.jdbc.ClickHouseDriver
```

When using Docker Compose, these are set automatically from the `.env` file.



---

## 4. Sizing Guidance

Choosing the right ClickHouse resources depends on your cohort size and query workload. The table below provides rough guidelines for self-hosted ClickHouse deployments. For ClickHouse Cloud (used by the public cBioPortal at cbioportal.org and genie.cbioportal.org), you can adjust compute and RAM on demand.

| Cohort Size | Minimum RAM | Recommended RAM | Storage (estimated) | CPU | Notes |
|---|---|---|---|---|---|
| <10K samples | 4 GB | 8 GB | 10–50 GB | 2 vCPU | Suitable for evaluation, development, or small institutional deployments. The default Docker Compose setup works well here. |
| 10K–100K samples | 8 GB | 16 GB | 50–500 GB | 4 vCPU | Typical mid-size institutional deployment. Derived table rebuilds may require 16 GB+ during peak. |
| 100K–500K samples | 16 GB | 32 GB | 500 GB – 2 TB | 8 vCPU | Large research consortia. Strongly consider ClickHouse Cloud for elastic scaling. Batch imports with `--no-derive-tables`. |
| >500K samples | 32 GB | 64 GB+ | 2 TB+ | 16 vCPU | Public portals (e.g., cbioportal.org). ClickHouse Cloud recommended; blue-green deployment advised. |

> **Note:** Storage estimates depend heavily on the number of genetic profiles, clinical attributes, and mutation density per sample. The figures above assume typical whole-exome sequencing studies.

### Memory Pressure During Imports

Importing large studies and rebuilding derived tables are the most memory-intensive operations. If you see `Memory limit exceeded` errors:

- **Reduce concurrency** — Import studies one at a time with `--no-derive-tables`, then run `derive-tables` once at the end.
- **Add back-off between optimize operations** — Set `CLICKHOUSE_OPTIMIZE_BACKOFF_SECS` in your `.env` file (see [section 9](#9-notes-for-users-with-high-volume-data)).
- **Upgrade your ClickHouse instance** — Add RAM or switch to ClickHouse Cloud.

### ClickHouse Cloud Tiers

For ClickHouse Cloud, the service tier determines resource allocation:

- **Development tier** — Suitable for evaluation and small cohorts (<10K samples)
- **Production tier** — Suitable for institutional deployments (10K–500K samples)
- **Enterprise tier** — Suitable for public portals and large consortia (>500K samples)

See the [ClickHouse Cloud documentation](https://clickhouse.com/docs/cloud/manage) for the latest pricing and service tiers.

---

## 5. Docker Compose Setup

For instructions on running cBioPortal with ClickHouse via Docker Compose, see the [Docker deployment guide](/deployment/docker/README.md).

### Connecting to ClickHouse from Docker

Once you have followed the steps in the Docker Compose guide, it is also possible to connect to the ClickHouse database without having the ClickHouse CLI installed on your host machine.

First, ensure that the cBioPortal containers are running (if not, run `docker compose up -d`). Then, run this command from the root of the `cbioportal-docker-compose` repo:

```shell
# Set the appropriate variables first
CLICKHOUSE_USER=<your_clickhouse_user>
CLICKHOUSE_PASSWORD=<your_clickhouse_password>
CLICKHOUSE_DB=<your_clickhouse_db_name>

docker compose exec cbioportal-database \
    sh -c 'clickhouse client -u"$CLICKHOUSE_USER" --password="$CLICKHOUSE_PASSWORD" --database="$CLICKHOUSE_DB"'
```

This will use the ClickHouse CLI that is embedded in the `cbioportal-database` container in order to connect.

---

## 6. Relevant Data Files

After running the `init.sh` script from the Docker Compose steps above, you will notice several new files present in the `data/` directory. These include:

- **schema.sql** -- This is the base schema for the cBioPortal database.
- **seed.sql.gz** -- This contains the latest "seed data" for this version of the schema, including reference data like gene symbols.
- **clickhouse.sql** -- This script is responsible for creating "derived tables" that the cBioPortal web application uses to load pages faster. Refer below for more info on derived tables.
- **clickhouse_user_settings.xml** -- This file contains the default settings that are assigned to the ClickHouse user in the newly created database.

---

## 7. Data Loading

See [Data Loading](/data-loading/README.md).

Note that cBioPortal study files themselves are backwards-compatible -- there is no change in their file format required when transitioning from a legacy MySQL cBioPortal installation to a ClickHouse-based one.

---

## 8. Notes on Derived Tables

### What Are Derived Tables?

Derived tables are **standalone tables** that function analogously to materialized views — they pre-join and denormalize data from the base cBioPortal tables. They exist purely for query performance — when a user opens the Study View, cBioPortal queries derived tables instead of joining many base tables at runtime.

Without derived tables, every Study View page load would need to join across genetic_profiles, genetic_alterations, samples, patients, and clinical data on the fly. Derived tables collapse these joins into precomputed structures, making queries 10–100× faster. Unlike database-level materialized views, derived tables have no built-in automatic refresh mechanism — they must be rebuilt explicitly when data changes.

### When Derived Tables Are Built

By default, `metaImport.py` **automatically rebuilds derived tables** after every import. This ensures query performance stays fast after loading new studies.

### Skipping Derived Table Rebuild (`--no-derive-tables` and `derive-tables`)

The `derive-tables` command recreates all derived table structures based on all study data in the database. Normally, it's not necessary to run since `metaImport.py` will automatically do so every time a study is imported. However, if you are importing many studies in a batch, you can skip the derived table rebuild after each import to save time, only doing it once at the end:

```bash
docker compose exec cbioportal metaImport.py -s /study/study1 -o --no-derive-tables
docker compose exec cbioportal metaImport.py -s /study/study2 -o --no-derive-tables
docker compose exec cbioportal metaImport.py -s /study/study3 -o --no-derive-tables
# ...
# Rebuild derived tables only once at the end
docker compose exec cbioportal metaImport.py derive-tables
```

This imports the study data without rebuilding derived tables unnecessarily.

### Important Notes

- **Always rebuild derived tables as the last step before viewing a cBioPortal instance connected to the database** in production. Without them, the website may fail to load or display inaccurate data.
- The derived table scripts may require significant memory for large databases. See [Notes for Users with High-Volume Data](#9-notes-for-users-with-high-volume-data) if you encounter issues.
- Derived tables **cannot be incrementally updated** — they are fully rebuilt from scratch each time, even for incremental imports.


---

## 9. Notes for Users with High-Volume Data

When working with large studies (>100K samples or >10GB of clinical/genomic data), you may encounter resource limitations with the local Docker Compose ClickHouse database. Here are some recommendations:

### Out-of-Memory Issues During Derived Table Rebuild

The derived table scripts perform large joins and aggregations that can consume significant memory. If you see errors like `Memory limit exceeded` or the ClickHouse container crashes during `derive-tables`, consider these options:

1. **Deploy ClickHouse Cloud** instead of a local ClickHouse container. [ClickHouse Cloud](https://clickhouse.com/cloud) offers managed instances with adjustable RAM and elastic scaling. This is the recommended approach for production deployments with high-volume data.

2. **Set `CLICKHOUSE_OPTIMIZE_BACKOFF_SECS`** in your `.env` file in order to add a pause in between multiple `OPTIMIZE TABLE .. FINAL` statements, which can lead to OOM errors for large databases. The importer container reads this environment variable:

   ```properties
   CLICKHOUSE_OPTIMIZE_BACKOFF_SECS=90
   ```

This adds a delay between `OPTIMIZE TABLE .. FINAL` operations, reducing peak memory usage during imports. Increase this value if you continue to see memory pressure.

### General Recommendations for Large Datasets

- **Use ClickHouse Cloud** -- has a configurable amount of RAM/compute
- **Batch your imports** — import studies one at a time with `--no-derive-tables`, then run `derive-tables` once at the end.
- **Consider a blue/green deployment** — maintain two databases (one staging, one production) and switch after successful import.

---

## 10. Data Safety Warnings

> ⚠️ **Critical:** Interrupting an import (e.g., killing the process, network failure, power loss) can leave your ClickHouse database in a **corrupt or inconsistent state**. Data may be partially imported, derived tables may be stale, and the database may become unusable.

**Recommended Practices for Deployment Stability:**

- Maintain backup copies of all study files.
- Consider using a blue/green deployment strategy for production databases — import into the inactive database, then switch.
- Consider taking a ClickHouse snapshot or backup before large import operations.

> ⚠️ **Note:** ClickHouse backup commands require special privileges that are **not enabled by default on ClickHouse Cloud**. You must request these privileges from your ClickHouse Cloud administrator before using backup features.

---

## 11. Verifying Database Integrity

After importing studies and rebuilding derived tables, you can verify that your ClickHouse database has no structural integrity problems by following the instructions provided [here](https://github.com/cBioPortal/cbioportal-core/tree/rfc100-rc#check-clickhouse-constraint-violations).

---

## 12. Migrating from MySQL to ClickHouse

> **Note:** ClickHouse is the sole database from v7 onward. A v7 web app will not connect to MySQL, so this is a one-way migration rather than an "add ClickHouse alongside MySQL" step.

The full step-by-step procedure lives in the [v6 to v7 Migration Guide](/Migration-v6-to-v7.md). In outline:

1. **Keep your original study files.** There is no export command that reconstructs study files from a MySQL database — migration re-imports the study directories you originally loaded. If you no longer have them, retrieve them from your source of truth (e.g. Datahub, your curation pipeline, or backups) before starting.
2. **Stand up a ClickHouse database** — self-hosted or ClickHouse Cloud, see [section 2](#2-hosting-options). Load the schema and seed data.
3. **Re-import every study** with `metaImport.py -s /study/<study_dir> -o`. Study order does not matter. Study file formats are unchanged between v6 and v7, so no file conversion is needed (see [Data Loading](#7-data-loading)).
4. **Rebuild derived tables** once at the end with `metaImport.py derive-tables`.
5. **Point the v7 web app at ClickHouse** (see [Connection Configuration](#3-architecture)) and verify study, patient, and sample counts against the old instance. REST API endpoints are unchanged.

### Cutting Over Without Downtime

Because a single web app connects to a single database, run the old and new stacks in parallel during the transition:

1. Leave the v6 (MySQL) deployment serving users untouched.
2. Bring up a separate v7 (ClickHouse) deployment and import into it.
3. After validating the v7 deployment, switch DNS/traffic to it and retire the v6 stack.

> **Tip:** This is the same blue-green approach MSK uses for ClickHouse (see [How MSK hosts ClickHouse](#how-msk-hosts-clickhouse)).

---

## 13. Troubleshooting

### Connection Errors

**`Connection refused` / `Code: 210. DB::NetException`**

The port is wrong or ClickHouse is not up yet. ClickHouse exposes HTTP on 8123 and native TCP on 9000 (ClickHouse Cloud uses 8443 and 9440). The web app connects over HTTP via JDBC (`CLICKHOUSE_URL`), while `metaImport.py` and `clickhouse client` use the native port. Pointing JDBC at 9000 or the CLI at 8123 produces this error. On Docker Compose, check the container is healthy first:

```bash
docker compose ps cbioportal-database
docker compose logs cbioportal-database | tail -50
```

**`Authentication failed` / `Code: 516`**

The user, password, or database in `.env` does not match what the database was initialized with. Note that the ClickHouse container only runs its initialization scripts on an **empty** data volume — changing `CLICKHOUSE_USER`/`CLICKHOUSE_PASSWORD` after the first start has no effect until you recreate the volume (`docker compose down -v`, which deletes all data).

**Web app starts but every page 500s**

Usually the schema loaded but derived tables did not. See below.

### Import and Derived Table Failures

**`Memory limit (total) exceeded` during import or `derive-tables`**

The most common failure on large cohorts. See [section 9](#9-notes-for-users-with-high-volume-data) — batch imports with `--no-derive-tables`, set `CLICKHOUSE_OPTIMIZE_BACKOFF_SECS`, or increase RAM per [section 4](#4-sizing-guidance).

**`Table cbioportal.sample_derived doesn't exist` (or another `*_derived` table)**

Derived tables were never built, or were built before the study was imported. Rebuild them:

```bash
docker compose exec cbioportal metaImport.py derive-tables
```

**Study View is empty or shows stale counts after an import**

Derived tables are not refreshed automatically outside of `metaImport.py`, and they cannot be updated incrementally. Any direct writes to base tables require a full `derive-tables` run afterwards.

**Import interrupted partway through**

The database can be left inconsistent — see [section 10](#10-data-safety-warnings). Verify integrity with the constraint checker in [section 11](#11-verifying-database-integrity) before serving traffic.

### Getting Help

Include the ClickHouse server version (`SELECT version()`), the cBioPortal version, whether you are self-hosted or on ClickHouse Cloud, and the full error from `docker compose logs` when reporting an issue on the [cBioPortal GitHub repository](https://github.com/cBioPortal/cbioportal/issues) or in the [public Slack](https://slack.cbioportal.org/).

---

## 14. Version Migration

> ⚠️ **There is currently no automated mechanism for migrating data between ClickHouse versions.**

A migration tool for in-place schema upgrades is under development and will be available when the first update to the base table schema (`DB_SCHEMA_VERSION`) is released. There will be no updates to the base table schema before this tool is ready. Derived table schema updates (tracked by `DERIVED_TABLE_SCHEMA_VERSION`) can be applied by simply rebuilding your derived tables. Stay tuned to the [cBioPortal release notes](https://docs.cbioportal.org/news/) for updates.

If you upgrade to a newer version of cBioPortal that includes schema changes, you will need to:

1. Export your study data (study files).
2. Initialize a fresh ClickHouse database with the new schema.
3. Re-import all studies using `metaImport.py -s ...`.

This manual process will only be necessary for the initial v6→v7 migration and during the development period before the schema migration tool is released.



---

## 15. Further Reading

- [cBioPortal deploys on ClickHouse Cloud — case study](https://clickhouse.com/blog/how-memorial-sloan-kettering-cancer-center-is-using-clickhouse-to-accelerate-cancer-research) — how MSK uses ClickHouse to power cbioportal.org
- [ClickHouse Documentation](https://clickhouse.com/docs) — official ClickHouse docs
- [ClickHouse Cloud](https://clickhouse.com/cloud) — managed ClickHouse service
- [cBioPortal Docker Compose](https://github.com/cBioPortal/cbioportal-docker-compose) — reference Docker Compose deployment
- [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) — protocol spec for AI integrations
