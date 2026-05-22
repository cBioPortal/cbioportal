# Importing single data files for development
In some cases, for example during development, it may be useful to import a single data file into an existing study. To import one data file at a time, you can use the following command. Note that this process will not validate the data.

## Workflow

First, if your cancer type does not yet exist, you need to create it:
```
docker compose exec cbioportal cbioportalImporter.py -c import-cancer-type -data <path to study directory>/<name of data file>
```

Next, create the study using
```
docker compose exec cbioportal cbioportalImporter.py -c import-study -meta <path to study directory>/<name of meta file>
```
The meta file has to contain the study information.

Now you can import your data file(s): 
```
docker compose exec cbioportal cbioportalImporter.py -c import-study-data -meta <path to study directory>/<name of meta file> -data <path to study directory>/<name of data file> 
```
:warning: 
Your first data file should always be the clinical data!

Finally, after you've imported all data, import your case lists:
```
docker compose exec cbioportal cbioportalImporter.py -c import-case-list -meta <path to study directory>/<path to case lists>
```

> :warning: **After every import, you must rebuild the derived tables** to update the ClickHouse structures that power the study view. Without this step, newly imported data will not appear correctly in the UI:
> ```
> docker compose exec cbioportal metaImport.py derive-tables
> ```

#### Example:
```
docker compose exec cbioportal cbioportalImporter.py -c import-cancer-type -data /study/brca_small/data_cancer_type.txt
docker compose exec cbioportal cbioportalImporter.py -c import-study -meta /study/brca_small/meta_study.txt
docker compose exec cbioportal cbioportalImporter.py -c import-study-data -meta /study/brca_small/meta_clinical.txt -data /study/brca_small/data_clinical.txt
docker compose exec cbioportal cbioportalImporter.py -c import-study-data -meta /study/brca_small/meta_expression.txt -data /study/brca_small/data_expression.txt
docker compose exec cbioportal cbioportalImporter.py -c import-case-list -meta /study/brca_small/case_lists
docker compose exec cbioportal metaImport.py derive-tables
```
