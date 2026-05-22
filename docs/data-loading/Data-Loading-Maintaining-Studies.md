# Data loading: Importing without validation and deleting studies
For data curators and developers cbioportalImporter.py is available. This script can import data regardless of validation results. If data format is incorrect, the importer may stop with an error or crash, or leave the database in an inconsistent state.

This script can also be used to delete studies.

- [Requirements](#requirements)
- [Importing a study without validation](#importing-a-study-without-validation)
- [Deleting a study](#deleting-a-study)
- [Deleting patients](#deleting-patients)
- [Deleting samples](#deleting-samples)

## Requirements

Make sure that you have followed the instructions in the [Docker Compose setup guide](../deployment/docker/README.md).

## Importing a study without validation

First, copy the study on your host machine into the `study/` directory under `cbioportal-docker-compose`:

```
cp -r /path/to/your_study <cbioportal-docker-compose repo>/study/your_study
```

To import a study without validation, run this from the root of the `cbioportal-docker-compose` repo: 
```
docker compose exec cbioportal cbioportalImporter.py -s /study/your_study
```

> :warning: **After every import, you must rebuild the derived tables** to update the ClickHouse structures that power the study view. Without this step, newly imported data will not appear correctly in the UI:
> ```
> docker compose exec cbioportal metaImport.py derive-tables
> ```

For example:

```
docker compose exec cbioportal cbioportalImporter.py -s /study/lgg_ucsf_2014
```

## Importing part of the data
To import only some new or updated data entries, you can specify `-d` instead `-s` option:
```
docker compose exec cbioportal cbioportalImporter.py -d <path to data directory>
```
Although the -d option accepts a directory that follows the same structure as the study directory, not all data types are supported for incremental upload.
For more details on incremental data loading, see [this page](./Incremental-Data-Loading.md).

## Deleting a study
To remove a study, run: 
```
docker compose exec cbioportal cbioportalImporter.py -c remove-study -meta <path to study directory>/meta_study.txt
```
The `meta_study.txt` file should contain the study ID in `cancer_study_identifier: ` of the study you would like to remove.

For example:
```
docker compose exec cbioportal cbioportalImporter.py -c remove-study -meta /data/brca_small/meta_study.txt
```

If you have the Cancer Study Id of the study, or studies you want to remove, you can also use:
```
docker compose exec cbioportal cbioportalImporter.py -c remove-study -id study1_id
```
Where `study1_id` is the Cancer Study Id of the study you would like to remove.

You can also remove multiple studies at once by passing the Cancer Study Ids separated by commas:
```
docker compose exec cbioportal cbioportalImporter.py -c remove-study -id study1_id,study2_id,study3_id
```
Where `study1_id`, `study2_id` and `study3_id` are the Cancer Study IDs of the studies you would like to remove.

## Deleting patients
To remove patients (and their associated samples and data) from one or more studies, run:
```
docker compose exec cbioportal cbioportalImporter.py remove-patients --study_ids <study_ids> --patient_ids <patient_ids>
```
Where `study_ids` is a comma-separated list of Cancer Study IDs to search and `patient_ids` is a comma-separated list of patient identifiers to delete.

For example:
```
docker compose exec cbioportal cbioportalImporter.py remove-patients --study_ids study1_id --patient_ids patientA,patientB
```

## Deleting samples
To remove specific samples from one or more studies, run:
```
docker compose exec cbioportal cbioportalImporter.py remove-samples --study_ids <study_ids> --sample_ids <sample_ids>
```
Where `study_ids` is a comma-separated list of Cancer Study IDs to search and `sample_ids` is a comma-separated list of sample identifiers to delete.

For example:
```
docker compose exec cbioportal cbioportalImporter.py remove-samples --study_ids study1_id,study2_id --sample_ids sampleX,sampleY
```
