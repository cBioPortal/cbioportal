# Migrating from v6 (MySQL) to v7 (ClickHouse)

## Overview

cBioPortal version 7 replaces MySQL with ClickHouse as the sole database for the portal. MySQL is no longer supported going forward. This guide walks you through migrating an existing v6 (MySQL) cBioPortal deployment to v7 (ClickHouse).

## Prerequisites

You will need:

- The **study files** (meta files, data files, case lists) for every study currently in your MySQL database. The import process re-reads study files from disk.
- A running ClickHouse database instance (see the [ClickHouse Setup Guide](deployment/clickhouse/README.md)) -- either a local instance running on Docker Compose, or one on ClickHouse Cloud.

## If You're Missing Study Files

If you no longer have the original study files for some studies, you can use the cBioPortal study export tool to extract study data from your existing MySQL instance.

> **Brief overview:** The study export tool reads study metadata and data from the database and writes out study files in the standard cBioPortal format. See the [cbioportal-core tools documentation](https://github.com/cBioPortal/cbioportal-core) for details.

## Step-by-Step Migration

### Step 1: Initialize a New ClickHouse Database

Follow the [ClickHouse Setup Guide](deployment/clickhouse/README.md#3-docker-compose-setup) to:

1. Create a ClickHouse database and user.
2. Load the cBioPortal schema (`clickhouse.sql`).
3. Load the seed database (reference data like genes, cancer types).
4. Create derived tables.

### Step 2: Re-Import All Studies

For each study, run a full import using `metaImport.py`:

```bash
docker compose exec cbioportal metaImport.py -s /study/your_study -o
```

Repeat for every study that was in your MySQL database. Study order does not matter.

### Step 3: Verify Study Data

After all imports complete, verify your data:

1. Start cBioPortal pointing at your new ClickHouse database.
2. Check that all expected studies appear on the homepage.
3. Spot-check a few studies — verify patient counts, sample counts, and that mutation/copy-number data loads correctly in the UI.

You can also use the ClickHouse CLI to inspect the `cancer_study` table and confirm that all expected studies are present with recent import dates. See [the ClickHouse page](deployment/clickhouse/README.md) for more information.

## Existing MySQL Deployments

If you have an existing MySQL-backed cBioPortal deployment and are not ready to migrate:

- **v7 cBioPortal will not connect to MySQL.** You must use ClickHouse.
- v6 deployments using MySQL may continue to function but will receive **limited support** (security fixes only on the `maintenance-v6` branch). No new features or bug fixes will be developed for v6.
- All cBioPortal releases from v7 onward use ClickHouse as the primary database.
