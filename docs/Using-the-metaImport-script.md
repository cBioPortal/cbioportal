## Importing Data into cBioPortal
The metaImport script should be used to automate the process of validating and loading datasets. It also has some nice features like an extra option to only load datasets that completely pass validation (i.e. with no errors, while warnings can be explicitly allowed by the user).

> :information_source: The `metaImport.py` script and its companion data-loading tooling now live in the [`cBioPortal/cbioportal-core`](https://github.com/cBioPortal/cbioportal-core) repository. The script is still bundled into the cBioPortal Docker images, so the docker-based instructions below remain the recommended way to run it.

#### Running the metaImport Script

There are two common ways to invoke the importer:

##### Option 1: Run it inside the cBioPortal Docker container (recommended)

`metaImport.py` is on the `PATH` of the cBioPortal web-and-data image, so you can call it directly. For a `docker compose` deployment:
```bash
docker compose run cbioportal metaImport.py -h
```

Or, against a running container:
```bash
docker exec -it ${CBIOPORTAL_CONTAINER_NAME} metaImport.py -h
```

See [Import Study Using Docker](Import-Study-Using-Docker.md) and [Import data with Docker](deployment/docker/import_data.md) for end-to-end examples.

##### Option 2: Run it from a local checkout of `cbioportal-core`

```bash
git clone https://github.com/cBioPortal/cbioportal-core.git
cd cbioportal-core
python scripts/importer/metaImport.py -h
```

The [`cbioportal-core` README](https://github.com/cBioPortal/cbioportal-core/blob/main/README.md) describes the additional Python/Perl/JDK prerequisites and how to build the loader jar that `metaImport.py` invokes.

In either form `metaImport.py -h` prints the up-to-date list of command-line options. The most commonly used ones are:

| Option | Purpose |
| --- | --- |
| `-s STUDY_DIRECTORY` / `--study_directory` | Path to the study directory to validate and load. |
| `-d DATA_DIRECTORY` / `--data_directory` | Path to a data directory for [incremental upload](Incremental-Data-Loading.md). |
| `-u URL_SERVER` / `--url_server` | URL of the cBioPortal server (default: `http://localhost/cbioportal`). |
| `-p PORTAL_INFO_DIR` / `--portal_info_dir` | Use a local dump of portal info instead of the web API. |
| `-n` / `--no_portal_checks` | Skip checks that require information from a running cBioPortal installation. |
| `-html HTML_TABLE` / `--html_table` | Write an HTML validation report to the given path. |
| `-v` / `--verbose` | Verbose status output during validation. |
| `-o` / `--override_warning` | Continue importing even when the validator only emits warnings. |
| `-r` / `--relaxed_clinical_definitions` | Relax clinical-data header validation. |
| `-m` / `--strict_maf_checks` | Enable strict validation of mutation (MAF) data. |

Refer to `metaImport.py -h` for the complete and authoritative list, which also includes options for OncoKB annotation and skipping the database import step.

#### Example of Importing a study

Set `PORTAL_HOME` as explained in [Loading a Sample Cancer Study](deployment/deploy-without-docker/Load-Sample-Cancer-Study.md):

```bash
export PORTAL_HOME=<cbioportal_configuration_folder>
```

Then validate and load a study against a portal running at `http://localhost/cbioportal`:

```bash
python scripts/importer/metaImport.py -s tests/test_data/study_es_0/
```

(Paths are shown relative to a `cbioportal-core` checkout; adjust them for your environment.)

#### Advanced Example

Import a study into a portal on a non-default URL, write an HTML report, and show verbose status messages:

```bash
python scripts/importer/metaImport.py \
    -s tests/test_data/study_es_0/ \
    -u http://localhost:8080 \
    -html myReport.html \
    -v
```

Add `-o` to override validator warnings and continue importing after validation.

#### Incremental Upload

You have to specify `--data_directory` (or `-d`) instead of `--study_directory` (or `-s`) option to load data incrementally.
Incremental upload enables data entries of certain data types to be updated without the need of re-uploading the whole study.
The data directory follows the same structure and data format as the study directory.
It should contain complete information about entries you want to add or update.
Please note that some data types like study are not supported and must not be present in the data directory.
[Here](./Incremental-Data-Loading.md) you can find more details.

## Development / debugging mode
For developers and specific testing purposes, an extra script, `cbioportalImporter.py` (also shipped from [`cbioportal-core`](https://github.com/cBioPortal/cbioportal-core)), is available which imports data regardless of validation results. Check [this](Data-Loading-For-Developers.md) page for more information on how to use it.
