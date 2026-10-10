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
13. [Recommended Clickhouse Privileges](#13-recommended-clickhouse-privileges)
14. [Further Reading](#14-further-reading)

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

## WSI hierarchy materialization and authenticated rollout

WSI is served from the generic `resource_data` table. Each slide is one row
with `type = 'WHOLE_SLIDE_IMAGE'` in the `WSI_SAMPLE` (sample-matched) or
`WSI_PATIENT` (unmatched) resource. Its `metadata` JSON carries the public
hierarchy and stain fields, including the opaque `slide_key`, and, for a
servable slide, a private `wsi_serving` object with the opaque `sealed_source`,
intrinsic tile metadata and the thumbnail width, height and content type. The
pathology image ID and the slide and thumbnail object URIs are not stored in
cBioPortal: they exist only inside `sealed_source`, which is sealed upstream
with a key cBioPortal does not hold. See
[Pathology Slide Data](../../File-Formats.md#pathology-slide-data) for the
format and for the offline converter from the legacy `meta_wsi.txt` pair.

Clients use two endpoints:

- `GET /api/wsi/v2/hierarchy/{studyId}/{patientId}` builds the hierarchy from
  the patient's `WSI_SAMPLE`/`WSI_PATIENT` rows. Each slide is identified only
  by its opaque `slideKey`; the response never carries a barcode,
  `resourceId` or `resourceDataId`. Slides without a valid `slide_key` are
  dropped (counted in the log; values are never logged). Removing
  institution-specific identifiers such as specimen accession numbers is the
  data provider's responsibility; the backend does not match any accession
  format. Parts and blocks are
  ordered by their Specimen/Block ranks, and slides within a block by
  `slide_key`. The response is pathology-only; portal
  clinical labels and pathology timeline events continue to come from the
  normal cBioPortal APIs. A patient with no WSI rows gets `200` with an empty
  hierarchy (`{"referenceSampleId":null,"sampleGroups":[]}`); an unknown study
  or patient gets `404`.
- `GET /api/wsi/v2/resources/{studyId}/{patientId}/access?slideKey=`
  returns the pixel access bundle and capability for one slide. `slideKey`
  must be 32 lowercase hex characters (`400` otherwise). It reads
  `wsi_serving` from the row whose `metadata.slide_key` matches, and only when
  the row belongs to the study and patient, its resource is `WSI_SAMPLE` or
  `WSI_PATIENT`, and its `type` is `WHOLE_SLIDE_IMAGE`. The slide key is
  unique within a study and, unlike the resource-data row ID, survives a
  reimport. A slide is servable only when `can_serve_tiles` is true, the slide
  key is valid, `sealed_source` is well formed (unpadded base64url, at most
  4096 characters, decoding to at least 29 bytes), the tile metadata has
  `tile_metadata_schema_version` 2 and passes the key allowlist and
  identifier/date checks, and the thumbnail is
  `image/jpeg` or `image/png` with dimensions between 1 and 8192; otherwise
  the endpoint returns `404`. The response carries `slideKey`, the tile
  metadata, the thumbnail width/height/content type and the capability;
  `sealed_source` appears only inside the capability.

The generic resource APIs (`/api/resource-table/*` and the legacy
`/api/studies/.../resource-data*` endpoints) never return `WSI_SAMPLE` or
`WSI_PATIENT` rows: not as rows, tabs, search or filter matches, sort keys,
facets, discovered metadata keys or column info. Slides are reached only
through the two endpoints above. Study-level resource counts still include
them.

`wsi_serving` is also private for every other `resource_data` row. The generic
resource table API strips it from row metadata and ignores it in search,
filters, sorting, facets and metadata-column discovery.

There are no native WSI tables: the schema does not create `wsi_patient`,
`wsi_part`, `wsi_block`, `wsi_slide`, `wsi_slide_placement` or
`wsi_slide_timing`, and migration `3.7.0` drops them from older databases.

The cBioPortal properties for an authenticated deployment are:

```properties
msk.wsi.tile_server.url=https://cbioportal.example.org/wsi
wsi.access-token-secret=<at-least-32-byte-secret>
wsi.access-token-audience=cbioportal-wsi
wsi.access-token-ttl-seconds=300
```

The tile server must use the matching values:

```text
WSI_AUTH_SECRET=<same-secret>
WSI_AUTH_AUDIENCE=cbioportal-wsi
WSI_AUTH_MAX_TTL=300
WSI_SOURCE_SEAL_KEY=<base64 32-byte key used to seal sealed_source upstream>
WSI_ALLOWED_SOURCE_SCHEMES=s3
WSI_ALLOWED_SOURCE_PREFIXES=<comma-separated slide object prefixes>
WSI_ALLOWED_THUMBNAIL_PREFIXES=<comma-separated thumbnail object prefixes>
```

The secret bytes and audience must match exactly, and the cBioPortal TTL must
not exceed `WSI_AUTH_MAX_TTL`. `WSI_SOURCE_SEAL_KEY` is held only by the
pipeline that seals the sources and by the tile server; cBioPortal never needs
it. The object-prefix allowlists and the source/thumbnail URI checks are
enforced by the tile server on the opened sources; cBioPortal cannot see the
URIs and does not read `WSI_ALLOWED_*` settings. The tile server receives only
v4 capabilities; it does not load a hierarchy, metadata backend, or resource
index. Setting only `msk.wsi.tile_server.url` configures a frontend URL; the
WSI resource rows must also contain `sealed_source`, the tile metadata and the
thumbnail fields in `wsi_serving`.

### Upstream serving data

The serving fields are produced by the data provider before export, not by
cBioPortal: a thumbnail for each servable slide, its tile metadata,
dimensions and content type, and the `sealed_source` that seals the slide and
thumbnail locations. The `data_wsi.txt` file carries only `SEALED_SOURCE`,
`TILE_METADATA_JSON`, the thumbnail dimensions and content type (never an
image ID or object URI), and the offline converter turns it into resource rows
for the standard cBioPortal core importer. Neither the frontend nor the tile
server publishes thumbnails; a slide missing any of these fields must be
fixed upstream before importing a new WSI snapshot.

WSI is login-only, including for public studies. Anonymous users receive
`401`, authenticated users without study access receive `403`, authenticated
blank study IDs receive `400`, and a nonexistent study deliberately returns
`403` to avoid an existence oracle. The capability is an HS256 JWT signed with
`wsi.access-token-secret`, with `wsi_auth_version` 4 (serving contract
`wsi-serving-v6`), and contains `sub`, `aud`, `scope=wsi:read`, `study_id`,
`slide_key`, bounded thumbnail dimensions, `iat`, `exp` and `enc`. `enc` is the
row's stored `sealed_source`, copied verbatim: `base64url(nonce[12] ||
AES-256-GCM ciphertext || tag[16])` over the JSON `{"image_id",
"tile_source", "thumbnail_source"}`, sealed upstream with
`WSI_SOURCE_SEAL_KEY` and the `slide_key` as additional authenticated data.
cBioPortal performs no encryption and cannot open it; the tile server does.

Before enabling the Pathology Slides feature for a private study:

1. Convert the study's complete `meta_wsi.txt`/`data_wsi.txt` pair with
   `scripts/importer/convertWsiToResources.py` from cbioportal-core
   (`--meta-wsi`, `--output-dir`, `--portal-base-url`, `--study-dir`), remove
   the legacy pair, and validate and import the study with the standard
   importer. The resource rows carry `sealed_source`, `tile_metadata_json`,
   thumbnail dimensions, and content type in `wsi_serving` for each
   servable slide. The pathology timeline pair is imported unchanged.
   MRNs and absolute dates must not occur in the study files:

   ```bash
   metaImport.py -s /path/to/study
   ```
2. Ensure the backend access endpoint returns no pixel bundle when any of
   those fields is missing. The browser must obtain a fresh bundle for each
   slide and send only its capability (`Authorization: Bearer`) to the tile
   server; it never sees the image ID or an object URI.
3. Configure protected pixel responses as private/cacheable and hierarchy or
   access responses as private/no-store. They must not be publicly cached.

Blue/green promotion is the WSI visibility boundary. Build the inactive
database, import each study's WSI resources once, validate the result, and
promote only after the build succeeds. If an import fails or must be retried,
discard and rebuild the inactive database. The core importer does not create
or migrate production tables.

### WSI serving query plan

The hierarchy and slide-access repositories first resolve the internal study
(and, for the hierarchy, patient) identifiers, then read `resource_data` with
those constants in `PREWHERE`. `resource_data` is ordered by
`(cancer_study_id, resource_id, patient_id, sample_id, resource_data_id)`, so both the
per-patient hierarchy read and the single-row access lookup are pruned by the
primary key. The hierarchy query extracts only public metadata fields; it does
not parse `wsi_serving`. Validate with `EXPLAIN indexes=1` that both queries
use the primary key.

---

## 12. Version Migration

Starting with `DB_SCHEMA_VERSION` `3.0.0`, supported in-place schema upgrades are handled by
`db-scripts/clickhouse/migrate/migrate_schema.sql` (a forward-only, version-tagged set of SQL
sections) applied by `db-scripts/clickhouse/migrate/migrate_db.py`. The runner reads the current
`db_schema_version` from the `info` table, skips sections already applied, and applies the rest in
order, advancing `db_schema_version` itself after each section succeeds. Rebuild-only changes are
still versioned here so an incompatible older database cannot start with a newer backend.

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

**`3.5.0` (resource data).** `3.5.0` creates the unified `resource_data` table
ordered by `(cancer_study_id, resource_id, patient_id, sample_id, resource_data_id)`,
which the WSI and resource-table queries rely on, backfills it from the legacy
`resource_sample`, `resource_patient` and `resource_study` tables, and drops them.

**`3.6.0` (WSI slide keys).** `3.6.0` removes WSI data that names slides by
their real image ID (serving contract `wsi-serving-v5`). It deletes the
pathology timeline's `IMAGE_IDS` events and image-keyed `LINKOUT` events from
`clinical_event_data`, and deletes every `WSI_SAMPLE`/`WSI_PATIENT` row in
`resource_data` whose `metadata` has no `slide_key`. Those slides disappear
from the portal until the study's WSI resources and pathology timeline are
re-imported (converted with `convertWsiToResources.py`, which requires
`SLIDE_KEY`). Both deletes are mutations; `migrate_db.py` waits for them to
finish before recording the version.

**`3.7.0` (WSI sealed source).** `3.7.0` removes the pathology image ID and
object URIs from the database (serving contract `wsi-serving-v6`). It drops
the native WSI tables (`wsi_patient`, `wsi_part`, `wsi_block`, `wsi_slide`,
`wsi_slide_placement`, `wsi_slide_timing` and the older `wsi_release` /
`wsi_release_patient`), which nothing reads, and deletes every
`WSI_SAMPLE`/`WSI_PATIENT` row in `resource_data` whose `metadata` still holds
an `image_id` (top level or in `wsi_serving`) or a `wsi_serving.source_url` or
`thumbnail_url`. Those slides disappear from the portal until the study's WSI
resources are re-imported from a `data_wsi.txt` that carries `SEALED_SOURCE`.
The delete is a mutation; `migrate_db.py` waits for it to finish before
recording the version.

Versions `3.1.0` to `3.4.0` are reserved and change nothing; earlier builds of
them created the native WSI tables that `3.7.0` drops. Slide procedure dates
are not served yet; they arrive with slides on the patient Summary timeline.

Import WSI resources into the inactive blue/green database and promote it only
after validation.

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

## 13. Recommended Clickhouse Privileges

Using the standard Docker Compose deployment approach, Clickhouse is deployed in a docker container
which is initialized with a user named 'cbio_user'. That user is automatically granted broad
database privileges, including the ability to create/alter/destroy databases and tables, to read
and write data from all databases on the Clickhouse service, and to create other users and manage
their privileges. This may be appropriate for a single purpose database service running locally.

For remote or multi-purpose Clickhouse services (such as a Clickhouse Cloud service), we recommend
creating and configuring two Clickhouse users within the service for cBioPortal operations:
1. a user with database read privileges for use with the cBioPortal web application
2. a user with database read/write privileges for use with data import and migration operations

Below are recommended privileges to be granted to these two users using the Clickhouse 'GRANT'
command. This can be done using the `clickhouse client` command line interface tool using a user
(such as the default user) with privileges to create and manage other users.

In the example statements below, a database has already been created for use with a cBioPortal
deployment with a command such as:

`CREATE DATABASE my_cbioportal_db`

Users have also been created with commands such as:

`CREATE USER my_cbioportal_user IDENTIFIED WITH sha256_password BY 'my_password_for_the_web_application'`

`CREATE USER my_cbioportal_admin IDENTIFIED WITH sha256_password BY 'my_password_for_importing_and_migrating'`

### web application user privileges

The web application generally needs only read privileges on the database tables:

`GRANT SELECT ON my_cbioportal_db.* TO my_cbioportal_user`

Users who configure their portal to use UUID based data access tokens would need to grant write
privileges for that table as well:

`GRANT INSERT, ALTER, TRUNCATE ON my_cbioportal_db.data_access_tokens TO my_cbioportal_user`

### import and migration user privileges

Importing data into the cBioPortal database, or migrating the database schema requires additional
write privileges within the created cBioPortal database:

`GRANT SHOW TABLES, SHOW COLUMNS, SELECT, INSERT, ALTER, CREATE TABLE, CREATE VIEW, DROP TABLE, DROP VIEW,
TRUNCATE, OPTIMIZE ON my_cbioportal_db.* TO my_cbioportal_admin`

Additional system level monitoring privileges are required as well:

`GRANT SHOW COLUMNS, SELECT ON system.tables TO my_cbioportal_admin`

`GRANT SHOW COLUMNS, SELECT ON system.parts TO my_cbioportal_admin`

`GRANT SHOW COLUMNS, SELECT ON system.mutations TO my_cbioportal_admin`

`GRANT SHOW COLUMNS, SELECT ON system.one TO my_cbioportal_admin`

For clickhouse server versions between 24.10 and 25.6:
`GRANT REMOTE ON *.* TO my_cbioportal_admin`

For clickhouse server versions beginning with 25.7 and onward:
`GRANT READ ON REMOTE TO my_cbioportal_admin`

By using restricted-privilege Clickhouse users, a compromised user account would be limited to improper
data access or disruption related to a particular cBioPortal deployment. Other databases operating on
the Clickhouse service would be insulated.

---

## 14. Further Reading

- [cBioPortal deploys on ClickHouse Cloud — case study](https://clickhouse.com/blog/how-memorial-sloan-kettering-cancer-center-is-using-clickhouse-to-accelerate-cancer-research) — how MSK uses ClickHouse to power cbioportal.org
- [ClickHouse Documentation](https://clickhouse.com/docs) — official ClickHouse docs
- [ClickHouse Cloud](https://clickhouse.com/cloud) — managed ClickHouse service
- [cBioPortal Docker Compose](https://github.com/cBioPortal/cbioportal-docker-compose) — reference Docker Compose deployment
- [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) — protocol spec for AI integrations

---

ClickHouse, the ClickHouse logo, and related marks are trademarks or registered trademarks of ClickHouse, Inc.
