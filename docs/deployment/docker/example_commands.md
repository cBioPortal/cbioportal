### Importing gene panel ###

Use this command to import a gene panel. Specify the gene panel file by replacing 
`<path_to_genepanel_file>` with the absolute path to the gene panel file. Another option is to add the 
gene panel files in `./study` which is mounted inside the container on `/study/.

```shell
docker compose run --rm \
    -v <path_to_genepanel_file>:/gene_panels/gene_panel.txt:ro \
    cbioportal \
    bash -c 'cd /core/scripts/ && ./importGenePanel.pl --data /gene_panels/gene_panel.txt'
```

### Importing data ###

Use this command to validate a dataset. Add the study to the `./study` folder. The
command will connect to the web API of the container `cbioportal-container`, and import 
the study in its associated database. Make sure to replace `<path_to_report_folder>` with 
the absolute path were the html report of the validation will be saved.

```shell
docker compose run --rm \
    -v "<path_to_report_folder>:/report" \
    cbioportal \
    metaImport.py -s /study/name_of_study --html=/report/report.html -u http://cbioportal:8080 -o
```
:warning: after importing a study, remember to restart `cbioportal-container`
to see the study on the home page. Run `docker compose restart cbioportal`.

To load data incrementally, specify `-d` instead of `-s` option.
For more details on incremental data loading, see [this page](/data-loading/Incremental-Data-Loading.md).

### Incremental Import

To add or update data in an existing study without re-importing the entire study:

```shell
docker compose exec \
    cbioportal \
    metaImport.py -d /study/name_of_study -o
```

### Removing Studies, Samples, and Patients

```shell
# Remove a study entirely
docker compose exec \
    cbioportal \
    cbioportalImporter.py -c remove-study -id study_id

# Remove specific samples from a study
docker compose exec \
    cbioportal \
    cbioportalImporter.py -c remove-samples --study-id study_id --samples SAMPLE1,SAMPLE2

# Remove specific patients and all their data
docker compose exec \
    cbioportal \
    cbioportalImporter.py -c remove-patients --study-id study_id --patients PATIENT1,PATIENT2
```

After any removal operation, rebuild derived tables:

```shell
docker compose exec cbioportal metaImport.py derive-tables
```

#### Using cached portal side-data ####

In some setups the data validation step may not have direct access to the web API, for instance when the web API is only accessible to authenticated browser sessions. You can use this command to generate a cached folder of files that the validation script can use instead. Make sure to replace `<path_to_portalinfo>` with the absolute path where the cached folder is going to be generated.

```shell
docker compose run --rm \
    -v "<path_to_portalinfo>/portalinfo:/portalinfo" \
    -w /core/scripts/ \
    cbioportal \
    ./dumpPortalInfo.pl /portalinfo
```

Then, grant the validation/loading command access to this folder and tell the script to use it instead of the API:

```shell
docker compose run --rm \
    -v "<path_to_report_folder>:/report" \
    -v "<path_to_portalinfo>/portalinfo:/portalinfo:ro" \
    cbioportal \
    metaImport.py -p /portalinfo -s /study/name_of_study --html=/report/report.html -u http://cbioportal:8080 -o
```

### Inspecting or adjusting the database ###

```shell
docker compose exec cbioportal-database \
    sh -c 'clickhouse client -u"$CLICKHOUSE_USER" --password="$CLICKHOUSE_PASSWORD" --query="SHOW DATABASES"'
```

### Deleting a study ###

To remove a study, run:

```shell
docker compose exec \
    cbioportal \
    cbioportalImporter.py -c remove-study -id study_id
```

Where `study_id` is the `cancer_study_identifier` of the study you would like to remove.
