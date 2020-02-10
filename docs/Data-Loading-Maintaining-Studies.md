# Data loading: Importing without validation and deleting studies
For data curators and developers cbioportalImporter.py is available. This script can import data regardless of validation results. If data format is incorrect, the importer may stop with an error or crash, or leave the database in an inconsistent state.

This script can also be used to delete studies.

- [Requirements](#requirements)
- [Importing a study without validation](#importing-a-study-without-validation)
- [Deleting a study](#deleting-a-study)

## Requirements
This script requires `$PORTAL_HOME` to point to the folder containing your
cBioPortal configuration. This can be done with:
```
export PORTAL_HOME=<cbioportal_configuration_folder>
```

The script itself can be found in `<cbioportal_source_folder>/core/src/main/scripts/importer`.

## Importing a study without validation 
To import a study without validation, run: 
```
./cbioportalImporter.py -s <path to study directory>
```

For example:
```
./cbioportalImporter.py -s ../../../test/scripts/test_data/study_es_0/
```

## Deleting a study
To remove a study, run: 
```
./cbioportalImporter.py -c remove-study -meta <path to study directory>/meta_study.txt
```
The `meta_study.txt` file should contain the study ID in `cancer_study_identifier: ` of the study you would like to remove.

For example:
```
./cbioportalImporter.py -c remove-study -meta /data/brca_small/meta_study.txt
```

If you have the Cancer Study Id of the study, or studies you want to remove, you can also use:
```
./cbioportalImporter.py -c remove-study -id study1_id
```
Where `study1_id` is the Cancer Study Id of the study you would like to remove.

You can also remove multiple studies at once by passing the Cancer Study Ids separated by commas:
```
./cbioportalImporter.py -c remove-study -id study1_id,study2_id,study3_id
```
Where `study1_id`, `study2_id` and `study3_id` are the Cancer Study IDs of the studies you would like to remove.
