# Data Loading
## Introduction
This page is the starting point for data loading. The [General Overview](#general-overview) section below contains all the required steps to get you started. 

## General Overview
Getting your study data into cBioPortal requires four steps:

1. [Setting up the validator](#setting-up-the-validator)
2. [Preparing your study data](#preparing-study-data)
3. [Validating your study data](#validating-your-study-data)
4. [Loading your study data](#loading-data)

ClickHouse is the main database used by cBioPortal v7. The `metaImport.py` script (see [Using the metaImport script](./Using-the-metaImport-script.md)) handles all data loading, including import into ClickHouse. For additional ClickHouse setup and import utilities, see the [tools](https://github.com/cBioPortal/cbioportal-core/blob/main/scripts/clickhouse_import_support/README.md) in the cbioportal-core repository and the [ClickHouse setup guide](../deployment/clickhouse/README.md).

## Setting up the validator
### Installation

If you have a git clone of cBioPortal, the relevant scripts can be found in the folder: `<your_cbioportal_dir>/core/src/main/scripts/importer`

### Dependencies
The scripts run in Python 3.4 or newer, and they require the modules `requests` and `pyyaml`.
You can use this command to install those modules:
```console
$ sudo python3 -m pip install requests pyyaml
```

If you want the scripts to be able to generate html reports (recommended way for reading the validation errors, if any), then you will also need to install `Jinja2`. You can use this command:
```console
$ sudo python3 -m pip install Jinja2
```

## Preparing Study Data 

When running cBioPortal via Docker Compose, it expects to find study data in the `cbioportal-docker-compose/study` directory. This is mounted to the path `/study` _inside the Docker container_. Both your study data and any required reference data (e.g. gene panels) must live under this path so that the importer can access them. Copy the study you would like to load there before importing:

```shell
cp -r /path/to/your_study cbioportal-docker-compose/study/
docker compose exec cbioportal metaImport.py -s /study/your_study -o
```

> **Note:** `-o` overrides validation warnings and proceeds with the import. If you are confident your data will pass all validation checks without warnings, you can drop `-o`.
>
> Note that `-o` only overrides validation _warnings_-- if there are validation errors, then the importer still won't run.

The `-s` flag here refers to the path of the study _as seen inside the Docker container_.

## Understanding Derived Tables

cBioPortal v7 uses a two-stage data loading approach:

1. **Base tables** — These store your study data as imported (genetic profiles, samples, patients, clinical data, etc.)
2. **Derived tables** — These are precomputed, denormalized tables built from the base tables to accelerate Study View queries.

Derived tables are standalone tables (analogous to materialized views) that collapse joins across multiple base tables. They make Study View queries much faster but have no automatic refresh mechanism — they must be rebuilt explicitly.

### Key facts

- `metaImport.py` automatically rebuilds derived tables after every successful import
- You can use `--no-derive-tables` to skip this step when importing multiple studies in a batch, then run `metaImport.py derive-tables` once at the end
- After any base table changes (imports, deletions, updates), derived tables **must be rebuilt** for data consistency — the website will display stale or inaccurate data otherwise
- Derived tables **cannot be incrementally updated** — they are always fully rebuilt from scratch

For a complete list of derived tables and technical details, see the [ClickHouse Setup Guide](../deployment/clickhouse/README.md).

## Study Structure

A study to be loaded in cBioPortal can basically consist of a directory where all the data files are located. 
Each *data* file needs a *meta* file that refers to it and both files need to comply to the format required for the specific data type. The format and fields expected for each file are documented in the [File Formats page](../File-Formats.md). Below is an example of the files in such a directory.

```
dir
|-meta_study.txt
|-meta_cancer_type.txt -> cancer_type.txt
|-meta_clinical.txt -> data_clinical.txt
|-meta_[expression|mutations|CNA|etc] -> data_[expression|mutations|CNA|etc]
```
#### Rules
There are just a few rules to follow:
- meta_study, meta_clinical and respective clinical data file are the only **mandatory** files.
- cancer type files can be mandatory if the study is referring to a cancer type that does not yet exist in the DB.
- meta files can be named anything, as long as it starts or ends with name 'meta'. E.g. meta_test, meta.test, test.meta are all fine; metal_test and metastudy are wrong.
- data files can be named anything and are referenced by a property `data_filename` set in the meta file. 

## Validating your study data
Once all files are in place and follow the proper format, you can [validate your files using the dataset validator script](Using-the-dataset-validator.md). 

The validation can be run standalone, but it is also integrated into the [metaImport script](./Using-the-metaImport-script.md), which validates the data and then loads it if validation succeeds. 

## Loading Data
To load the data into cBioPortal, the [metaImport script](./Using-the-metaImport-script.md) has to be used. This script first validates the data and, if validation succeeds, loads the data.

### Loading Data to a portal that has Authentication enabled

See also [Offline validation](./Using-the-dataset-validator.md#offline-validation).

### Incremental Loading

You can incorporate data entries of certain data types without re-uploading the whole study.
To do this, you have to specify `--data_directory` (or `-d`) instead of `--study_directory` (or `-s`) option for the [metaImport script](./Using-the-metaImport-script.md).

## Removing a Study
To remove a study, the [cbioportalImporter script](/data-loading/Data-Loading-Maintaining-Studies.md#deleting-a-study) can be used.

## Example studies
Examples for the different types of data are available on the [File Formats](../File-Formats.md) page. The Provisional TCGA studies, downloadable from the [Data Sets section](https://www.cbioportal.org/datasets) are complete studies that can be used as reference when creating data files.

## Public Virtual Studies
If your new study data is a subset or a combination of existing studies in the system, consider using [Public Virtual Studies](../Create-And-Publish-Virtual-Study.md) instead of duplicating data. 
