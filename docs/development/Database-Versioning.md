# cBioPortal Database Versioning

We follow the following logic when deciding how/when to increment the version of cBioPortal database. It's a complete modification of semantic versioning (MAJOR.MINOR.PATCH) more suitable for our purposes:

**MAJOR** : A non-backward compatible significant change in the database. Which requires the maintainer to reload and re-import all studies in the database entirely.

**MINOR** : Triggered by any of the following changes:

- Deleting a table
- Dropping a column
- Adding a constraint
- Dropping a constraint
- Renaming a table
- Renaming a column

**PATCH** : Changes that don't change existing database schemes but add new tables or columns, manipulating data.

- Creating a new table
- Adding a new column
- Removing all data from a table (truncate)
- Deleting rows from a table
- Updating data in a table
- Inserting rows into a table

## Making a schema change

A pull request that changes the ClickHouse schema has to bump the version in several places and usually needs a matching [cbioportal-core](https://github.com/cBioPortal/cbioportal-core) change, because the importer refuses to run against a database version it wasn't built for.

### 1. Update the backend (this repo)

- `pom.xml`: bump `db.version`.
- `src/main/resources/db-scripts/clickhouse/init/schema.sql`: apply the change to the table definitions and update the version in the `INSERT INTO info` statement, so fresh installs start at the new version.
- `src/main/resources/db-scripts/clickhouse/migrate/migrate_schema.sql`: add a `## db_schema_version: <new version>` section that upgrades an existing database (see the rules in the file header). Don't edit or renumber released sections.
- `src/main/resources/db-scripts/clickhouse/populate_derived_tables.sql`: update it if derived tables change.

The sanity check (`test/test_db_version.sh`) fails if `pom.xml`, `schema.sql` and `migrate_schema.sql` disagree.

### 2. Update cbioportal-core

Open a cbioportal-core PR that bumps `db.version` in its `pom.xml` to the same version, together with any importer changes the new schema needs.

### 3. Run the integration tests against both PRs

The [integration tests](https://github.com/cBioPortal/cbioportal/blob/master/.github/workflows/integration-test.yml) run on every backend PR, in two variants:

- **fresh**: the database is created from this PR's `schema.sql` and seed.
- **upgrade**: the database is created from the latest released schema (the cBioPortal image in [cbioportal-docker-compose](https://github.com/cBioPortal/cbioportal-docker-compose)'s `.env`) and upgraded by `migrate_db.py` with this PR's `migrate_schema.sql`, the same way docker-compose deployments upgrade. If the migration is missing or wrong, the backend refuses to start and the run fails.

By default the tests import data with the cbioportal-core bundled in the `cbioportal/cbioportal:master` image (core `main`), which won't accept the new version. To test against the matching core PR, add a line like this to the backend PR description:

```
core-ref: pull/123
```

The value can be a cbioportal-core PR (`pull/<number>`, which also works for PRs from forks), a branch name, or a commit SHA. The line is read when a run is triggered: after adding or changing it, push a commit. Re-running an existing run reuses the description it was started with. You can also start the workflow manually from the Actions tab with the `core_ref` input.

Note that changes to cbioportal-core's `requirements.txt` aren't installed in these runs; the image's Python dependencies are used.

### 4. Merge

Merge the cbioportal-core PR first and the backend PR right after it. The `cbioportal/cbioportal:master` image is rebuilt when the backend PR lands and bundles core `main` at that point, so both sides are on the new version from then on.
