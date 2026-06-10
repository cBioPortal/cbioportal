# ClickHouse Setup Guide

Starting with version 7, cBioPortal uses [ClickHouse](https://clickhouse.com/) as its sole database. This guide will help you set up and configure a ClickHouse-backed cBioPortal instance.

## Table of Contents

1. [Installing the ClickHouse CLI](#1-installing-the-clickhouse-cli)
2. [Hosting Options](#2-hosting-options)
3. [Architecture](#3-architecture)
4. [Docker Compose Setup](#4-docker-compose-setup)
5. [Relevant Data Files](#5-relevant-data-files)
6. [Data Loading](#6-data-loading)
7. [Notes on Derived Tables](#7-notes-on-derived-tables)
8. [Notes for Users with High-Volume Data](#8-notes-for-users-with-high-volume-data)
9. [Data Safety Warnings](#9-data-safety-warnings)
10. [Verifying Structural Integrity](#10-verifying-structural-integrity)
11. [Version Migration](#11-version-migration)
12. [Further Reading](#12-further-reading)

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

If you are having trouble installing the ClickHouse CLI on your host machine, it is also possible to connect to the ClickHouse database through Docker. See [Docker Compose Setup](#4-docker-compose-setup).

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
- **Derived tables** — Precomputed, denormalized tables built from the base tables by running `clickhouse.sql`. These accelerate Study View queries by collapsing joins across multiple base tables into a single table scan. See [section 7](#7-notes-on-derived-tables) for details.

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

## 4. Docker Compose Setup

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

## 5. Relevant Data Files

After running the `init.sh` script from the Docker Compose steps above, you will notice several new files present in the `data/` directory. These include:

- **schema.sql** -- This is the base schema for the cBioPortal database.
- **seed.sql.gz** -- This contains the latest "seed data" for this version of the schema, including reference data like gene symbols.
- **clickhouse.sql** -- This script is responsible for creating "derived tables" that the cBioPortal web application uses to load pages faster. Refer below for more info on derived tables.
- **clickhouse_user_settings.xml** -- This file contains the default settings that are assigned to the ClickHouse user in the newly created database.

---

## 6. Data Loading

See [Data Loading](/data-loading/README.md).

Note that cBioPortal study files themselves are backwards-compatible -- there is no change in their file format required when transitioning from a legacy MySQL cBioPortal installation to a ClickHouse-based one.

---

## 7. Notes on Derived Tables

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
- The derived table scripts may require significant memory for large databases. See [Notes for Users with High-Volume Data](#8-notes-for-users-with-high-volume-data) if you encounter issues.
- Derived tables **cannot be incrementally updated** — they are fully rebuilt from scratch each time, even for incremental imports.

---

## 8. Notes for Users with High-Volume Data

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

## 9. Data Safety Warnings

> ⚠️ **Critical:** Interrupting an import (e.g., killing the process, network failure, power loss) can leave your ClickHouse database in a **corrupt or inconsistent state**. Data may be partially imported, derived tables may be stale, and the database may become unusable.

**Recommended Practices for Deployment Stability:**

- Maintain backup copies of all study files.
- Consider using a blue/green deployment strategy for production databases — import into the inactive database, then switch.
- Consider taking a ClickHouse snapshot or backup before large import operations.

> ⚠️ **Note:** ClickHouse backup commands require special privileges that are **not enabled by default on ClickHouse Cloud**. You must request these privileges from your ClickHouse Cloud administrator before using backup features.

---

## 10. Verifying Database Integrity

After importing studies and rebuilding derived tables, you can verify that your ClickHouse database has no structural integrity problems by following the instructions provided [here](https://github.com/cBioPortal/cbioportal-core/tree/rfc100-rc#check-clickhouse-constraint-violations).

---

## 11. Version Migration

> ⚠️ **There is currently no automated mechanism for migrating data between ClickHouse versions.**

A migration tool for in-place schema upgrades is under development and will be available when the first update to the base table schema (`DB_SCHEMA_VERSION`) is released. There will be no updates to the base table schema before this tool is ready. Derived table schema updates (tracked by `DERIVED_TABLE_SCHEMA_VERSION`) can be applied by simply rebuilding your derived tables. Stay tuned to the [cBioPortal release notes](https://docs.cbioportal.org/news/) for updates.

If you upgrade to a newer version of cBioPortal that includes schema changes, you will need to:

1. Export your study data (study files).
2. Initialize a fresh ClickHouse database with the new schema.
3. Re-import all studies using `metaImport.py -s ...`.

This manual process will only be necessary for the initial v6→v7 migration and during the development period before the schema migration tool is released.

---

## 12. Further Reading

- [cBioPortal deploys on ClickHouse Cloud — case study](https://clickhouse.com/blog/how-memorial-sloan-kettering-cancer-center-is-using-clickhouse-to-accelerate-cancer-research) — how MSK uses ClickHouse to power cbioportal.org
- [ClickHouse Documentation](https://clickhouse.com/docs) — official ClickHouse docs
- [ClickHouse Cloud](https://clickhouse.com/cloud) — managed ClickHouse service
- [cBioPortal Docker Compose](https://github.com/cBioPortal/cbioportal-docker-compose) — reference Docker Compose deployment
- [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) — protocol spec for AI integrations
