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
8. [Migrating from MySQL to ClickHouse](#8-migrating-from-mysql-to-clickhouse)
9. [Notes for Users with High-Volume Data](#9-notes-for-users-with-high-volume-data)
10. [Data Safety Warnings](#10-data-safety-warnings)
11. [Verifying Database Integrity](#11-verifying-database-integrity)
12. [Version Migration](#12-version-migration)
13. [Further Reading](#13-further-reading)

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

<a href="https://clickhouse.com/cloud"><img src="../../images/clickhouse-logo.svg" alt="ClickHouse Cloud" height="88" /></a>

ClickHouse Cloud offers managed ClickHouse instances with adjustable RAM and compute.

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
- **Derived tables** — Precomputed, denormalized tables built from the base tables. Their structure is defined in `schema.sql` alongside every other table; their data is populated by running `populate_derived_tables.sql`. These accelerate Study View queries by collapsing joins across multiple base tables into a single table scan. See [section 7](#7-notes-on-derived-tables) for details.

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

- **schema.sql** -- This is the base schema for the cBioPortal database, including the (empty) derived table definitions.
- **seed.sql.gz** -- This contains the latest "seed data" for this version of the schema, including reference data like gene symbols.
- **populate_derived_tables.sql** -- This script populates the "derived tables" that the cBioPortal web application uses to load pages faster. It doesn't define table structure (that's in `schema.sql`) — it's just data population, safe to run repeatedly. Refer below for more info on derived tables.
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

| Scenario | Derived tables rebuilt? | Why |
|---|---|---|
| First-ever `docker compose up` (empty ClickHouse volume) | Yes | The fresh-install init scripts load `schema.sql` (creates derived tables, empty) then run `populate_derived_tables.sql` as part of first-time database setup. |
| `docker compose up` on an existing, already-initialized database, no pending migration | No | Docker's init scripts only run once against an empty data volume; nothing else rebuilds derived tables on a plain restart. |
| After importing a study (`metaImport.py`) | Yes, automatically | `metaImport.py` repopulates derived tables after every successful import, unless you pass `--no-derive-tables` (see below). |
| After `docker compose up` applies a pending schema migration | Yes, automatically | `migrate_db.py` is invoked with `--populate-derived-tables` in `cbioportal-docker-compose`, so it repopulates derived tables whenever a migration run actually applied one or more `migrate_schema.sql` sections. See [§12 Version Migration](#12-version-migration). |
| Manual deployments running `migrate_db.py` directly (no docker-compose) | No, unless you opt in | `migrate_db.py` does **not** repopulate derived tables by default — pass `--populate-derived-tables`, or rebuild them yourself as a separate step. See [§12 Version Migration](#12-version-migration). |

By default, `metaImport.py` **automatically rebuilds derived tables** after every import. This ensures query performance stays fast after loading new studies.

### Skipping Derived Table Rebuild (`--no-derive-tables` and `derive-tables`)

The `derive-tables` command repopulates all derived tables based on all study data currently in the database (table structure is unaffected — that's defined in `schema.sql`). Normally, it's not necessary to run since `metaImport.py` will automatically do so every time a study is imported. However, if you are importing many studies in a batch, you can skip the derived table rebuild after each import to save time, only doing it once at the end:

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

## 8. Migrating from MySQL to ClickHouse

v7 will not connect to MySQL, so this is a one-way migration. There is no command that turns a populated MySQL database back into study files: you re-import the study directories you originally loaded. Set up ClickHouse per [Docker Compose Setup](#4-docker-compose-setup), re-import each study per [Data Loading](#6-data-loading), then rebuild derived tables once at the end.

For the full procedure, see the [v6 to v7 Migration Guide](/Migration-v6-to-v7.md).

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

## 12. Version Migration

Starting with `DB_SCHEMA_VERSION` `3.0.0`, in-place schema upgrades are handled by
`db-scripts/clickhouse/migrate/migrate_schema.sql` (a forward-only, version-tagged set of SQL
sections) applied by `db-scripts/clickhouse/migrate/migrate_db.py`. The runner reads the current
`db_schema_version` from the `info` table, skips sections already applied, and applies the rest in
order, advancing `db_schema_version` itself after each section succeeds.

There is a single `db_schema_version` covering both base and derived tables — derived table
*structure* is defined in `schema.sql` alongside every other table, so a derived-table structure
change ships as an ordinary `migrate_schema.sql` section like any other schema change. Derived
table *data* is repopulated separately by `db-scripts/clickhouse/populate_derived_tables.sql`,
which doesn't have its own version — it only clears and rebuilds data, never structure, and can
be run any time (after an import, after a migration, or manually) **as long as no backend web
service is connected to the database in production**. It `TRUNCATE`s derived tables before
repopulating them, so a live instance querying the database mid-run will see empty or
partially-rebuilt derived tables and surface errors — take the web service offline first.

**Docker Compose deployments:** `git pull` the latest `cbioportal-docker-compose` master, then
`docker compose up`. The migration step runs automatically before the `cbioportal` service starts;
on a fresh install it's a safe no-op since `schema.sql` already seeds `info` at the current
version. It also repopulates derived tables automatically whenever a migration run actually
applies one or more sections — you don't need a separate manual step.

**Manual deployments (e.g. ClickHouse Cloud, Kubernetes, or any setup that doesn't go through
`cbioportal-docker-compose`):** run `migrate_db.py` directly against your database before deploying
the new cBioPortal backend image. By default `migrate_db.py` only touches base tables — pass
`--populate-derived-tables` if you want it to also repopulate derived tables in the same run when
migrations were applied; otherwise, rebuild derived tables yourself as a separate step (e.g. if you
run derivation through your own tooling against ClickHouse Cloud). The backend refuses to start
against a `db_schema_version` that doesn't match its build's `db.version` unless
`db.suppress_schema_version_mismatch_errors=true` is set.

For **ClickHouse Cloud** specifically, set `CLICKHOUSE_SECURE=true` (in addition to the usual
`CLICKHOUSE_HOST`/`CLICKHOUSE_NATIVE_PORT`/`CLICKHOUSE_USER`/`CLICKHOUSE_PASSWORD`/`CLICKHOUSE_DB`)
so `migrate_db.py` connects over TLS — Cloud's native port (typically `9440`) is TLS-only and will
reject a plain connection.

**Required permissions:** `migrate_db.py` polls `system.mutations` to know when an
`ALTER TABLE ... UPDATE`/`DELETE`/`DROP COLUMN` has finished applying, in addition to whatever
privileges it needs to actually run the migration's own statements. On ClickHouse Cloud (and any
self-hosted instance with RBAC locked down beyond the default user), the ClickHouse user running
`migrate_db.py` needs an explicit grant to read that system table, or the run fails partway
through with an `ACCESS_DENIED` error even though the migration's own `ALTER`/`DROP COLUMN`
statements already succeeded:

```sql
GRANT SHOW COLUMNS, SELECT ON system.mutations TO <your_clickhouse_user>;
```

Upgrades from **before** `3.0.0` (i.e. the original v6→v7 migration, or any pre-migration-tooling
ClickHouse deployment) still require the manual re-import process, since no migration path exists
for versions prior to `3.0.0`:

1. Export your study data (study files).
2. Initialize a fresh ClickHouse database with the new schema.
3. Re-import all studies using `metaImport.py -s ...`.

---

## 13. Further Reading

- [cBioPortal deploys on ClickHouse Cloud — case study](https://clickhouse.com/blog/how-memorial-sloan-kettering-cancer-center-is-using-clickhouse-to-accelerate-cancer-research) — how MSK uses ClickHouse to power cbioportal.org
- [ClickHouse Documentation](https://clickhouse.com/docs) — official ClickHouse docs
- [ClickHouse Cloud](https://clickhouse.com/cloud) — managed ClickHouse service
- [cBioPortal Docker Compose](https://github.com/cBioPortal/cbioportal-docker-compose) — reference Docker Compose deployment
- [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) — protocol spec for AI integrations

---

ClickHouse, the ClickHouse logo, and related marks are trademarks or registered trademarks of ClickHouse, Inc.
