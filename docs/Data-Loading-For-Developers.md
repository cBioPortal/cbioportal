# Importing single data files for development
In some cases, for example during development, it may be useful to import a single data file into an existing study. To import one data file at a time, you can use the following command. Note that this process will not validate the data.

This can be done by running `cbioportalImporter.py` from `<cbioportal_source_folder>/core/src/main/scripts/importer/`.

## Requirements

This script requires `$PORTAL_HOME` to point to the folder containing your
cBioPortal configuration. This can be done with:
```
export PORTAL_HOME=<cbioportal_configuration_folder>
```

## Workflow

First, if your cancer type does not yet exist, you need to create it:
```
./cbioportalImporter.py -c import-cancer-type -data <path to study directory>/<name of data file>
```

Next, create the study using
```
./cbioportalImporter.py -c import-study -meta <path to study directory>/<name of meta file>
```
The meta file has to contain the study information.

Now you can import your data file(s): 
```
./cbioportalImporter.py -c import-study-data -meta <path to study directory>/<name of meta file> -data <path to study directory>/<name of data file> 
```
:warning: 
Your first data file should always be the clinical data!

Finally, after you've imported all data, import your case lists:
```
./cbioportalImporter.py -c import-case-list -meta <path to study directory>/<path to case lists>
```

#### Example:
```
./cbioportalImporter.py -c import-cancer-type -data /data/brca_small/data_cancer_type.txt
./cbioportalImporter.py -c import-study -meta /data/brca_small/meta_study.txt
./cbioportalImporter.py -c import-study-data -meta /data/brca_small/meta_clinical.txt -data /data/brca_small/data_clinical.txt
./cbioportalImporter.py -c import-study-data -meta /data/brca_small/meta_expression.txt -data /data/brca_small/data_expression.txt
./cbioportalImporter.py -c import-case-list -meta /data/brca_small/case_lists
```
