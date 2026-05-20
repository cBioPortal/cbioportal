## Importing Data into cBioPortal
The metaImport script should be used to automate the process of validating and loading datasets. It also has some nice features like an extra option to only load datasets that completely pass validation (i.e. with no errors, while warnings can be explicitly allowed by the user). 

#### Running the metaImport Script
```bash
docker compose exec cbioportal metaImport.py -h
```
This will tell you the parameters you can use:
```
$ docker compose exec cbioportal metaImport.py -h
usage: metaImport.py [-h] [-s STUDY_DIRECTORY | -d DATA_DIRECTORY] [-u URL_SERVER | -p PORTAL_INFO_DIR | -n] [-jvo JAVA_OPTS] [-jar JAR_PATH]
                     [-html HTML_TABLE] [-v] [-o] [-r] [-m] [-update UPDATE_GENERIC_ASSAY_ENTITY] [-oncokb] [-skipimport] [--no-derive-tables |
                     --derived-table-sql PATH]

cBioPortal meta Importer

options:
  -h, --help            show this help message and exit
  -s, --study_directory STUDY_DIRECTORY
                        path to study directory.
  -d, --data_directory DATA_DIRECTORY
                        path to data directory for incremental upload.
  -u, --url_server URL_SERVER
                        URL to cBioPortal server. You can set this if your URL is not http://localhost:8080
  -p, --portal_info_dir PORTAL_INFO_DIR
                        Path to a directory of cBioPortal info files to be used instead of contacting the web API
  -n, --no_portal_checks
                        Skip tests requiring information from the cBioPortal installation
  -jvo, --java_opts JAVA_OPTS
                        Path to specify JAVA_OPTS for the importer. (default: gets the JAVA_OPTS from the environment)
  -jar, --jar_path JAR_PATH
                        Path to scripts JAR file (default: locate it relative to the import script)
  -html, --html_table HTML_TABLE
                        path to html report
  -v, --verbose         report status info messages while validating
  -o, --override_warning
                        override warnings and continue importing
  -r, --relaxed_clinical_definitions
                        Option to enable relaxed mode for validator when validating clinical data without header definitions
  -m, --strict_maf_checks
                        Option to enable strict mode for validator when validating mutation data
  -update, --update_generic_assay_entity UPDATE_GENERIC_ASSAY_ENTITY
                        Set as True to update the existing generic assay entities, set as False to keep the existing generic assay entities for
                        generic assay
  -oncokb, --import_oncokb
                        Set as True to download OncoKB annotations for Mutations and CNA and load as custom driver annotations
  -skipimport, --skip_db_import
                        Perform validation and OncoKB download but do not import study into database.
  --no-derive-tables    Skip derived table construction after import.
  --derived-table-sql PATH
                        Path to SQL file used for derived table construction.
```

#### Example of Importing a study

```bash
docker compose exec cbioportal metaImport.py -s /study/lgg_ucsf_2014 -o
```

> **Note:** `-o` overrides validation warnings and proceeds with the import. If you are confident your data will pass all validation checks without warnings, you can drop `-o`.

Adding `-v` shows status messages.

#### Generating an HTML Validation Report

```bash
# From the cbioportal-docker-compose repo
mkdir -p study/reports
docker compose exec cbioportal metaImport.py -s /study/my_study -o -html /study/reports/report.html
```

The HTML report is written to the mounted `cbioportal-docker-compose/study/reports` directory on your host and can be opened directly.

#### Incremental Upload

You have to specify `--data_directory` (or `-d`) instead of `--study_directory` (or `-s`) option to load data incrementally.
Incremental upload enables data entries of certain data types to be updated without the need of re-uploading the whole study.
The data directory follows the same structure and data format as the study directory.
It should contain complete information about entries you want to add or update.
Please note that some data types like study are not supported and must not be present in the data directory.
[Here](./Incremental-Data-Loading.md) you can find more details.

#### Derived Tables

After each import (incremental or otherwise), `metaImport.py` automatically rebuilds **derived tables** — ClickHouse tables that pre-join and denormalize data for fast Study View queries. See the [ClickHouse Setup Guide](../deployment/clickhouse/README.md#5-derived-tables) for details on what derived tables are and why they matter.

#### Rebuilding Derived Tables Only

You can rebuild derived tables without importing any studies by running:

```bash
docker compose exec cbioportal metaImport.py derive-tables
```

This command executes the derived table SQL scripts against your ClickHouse database without performing any study validation or import. It is useful after batch imports, study deletions, or whenever you need to refresh precomputed query structures.

##### Skipping Derived Table Rebuild

When batch-importing multiple studies, you can skip the derived table rebuild after each import with `--no-derive-tables`:

```bash
docker compose exec cbioportal metaImport.py -s /study/your_study -o --no-derive-tables
```

Then rebuild derived tables once after all studies have been imported:

```bash
docker compose exec cbioportal metaImport.py derive-tables
```

This can save a lot of time when many different studies are being imported in sequence.

> **Important:** Always rebuild derived tables before using the portal in production. Without them, the cBioPortal web app will not function properly.

## Development / debugging mode

For developers and specific testing purposes, an extra script, cbioportalImporter.py, is available which imports data regardless of validation results. Check [this](Data-Loading-For-Developers.md) page for more information on how to use it.
