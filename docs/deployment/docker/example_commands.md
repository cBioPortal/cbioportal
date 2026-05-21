### Importing gene panel ###

Use this command to import a gene panel. Specify the gene panel file by replacing 
`<path_to_genepanel_file>` with the absolute path to the gene panel file. Another option is to add the 
gene panel files in `cbioportal-docker-compose/study/reference_data` which is mounted inside the container on `/study/reference_data/`.

```shell
mkdir -p <cbioportal-docker-compose repo>/study/reference_data/
cp /path/to/your_gene_panel.txt <cbioportal-docker-compose repo>/study/reference_data/
docker compose exec cbioportal importGenePanel.pl --data /study/reference_data/your_gene_panel.txt
```

### Importing data ###

Use this command to validate a dataset. Add the study to the `cbioportal-docker-compose/study` folder. The
command will connect to the web API of the container `cbioportal-container`, and import 
the study in its associated database.

```shell
# From the cbioportal-docker-compose repo
mkdir -p study/reports
docker compose exec cbioportal metaImport.py -s /study/<name_of_study> -o -html /study/reports/report.html
```
> :warning: After importing a study, remember to restart the `cbioportal` container to see the study on the home page. Run `docker compose restart cbioportal`.

> :warning: **Warning:** When importing large studies, you may run into a Java out-of-memory error on machines with limited RAM. You can try adjusting the Java heap size used by the importer in order to work around this, for example:
>
> ```
> docker compose exec cbioportal metaImport.py -s /study/your_study -o -jvo "-Xms16g -Xmx96g"
> ```

### Incremental Import

To add or update data in an existing study without importing the entire study, you can use the new incremental import functionality. Point the importer to a folder containing a "delta" of study data you would like to add. To load data incrementally, you will specify the `-d` instead of the `-s` option.

```shell
docker compose exec \
    cbioportal \
    metaImport.py -d /study/<study_delta> -o
```

For more details on incremental data loading, see [this page](/data-loading/Incremental-Data-Loading.md).

### Removing Studies, Samples, and Patients

```shell
# Remove a study entirely
docker compose exec \
    cbioportal \
    cbioportalImporter.py remove-study -id study_id

# Remove specific samples from a study
docker compose exec cbioportal cbioportalImporter.py remove-samples --study_ids <study_id> --sample_ids SAMPLE1,SAMPLE2


# Remove specific patients and all their data
docker compose exec cbioportal cbioportalImporter.py remove-patients --study_ids <study_id> --patient_ids PATIENT1,PATIENT2
```

After any removal operation, rebuild derived tables:

```shell
docker compose exec cbioportal metaImport.py derive-tables
```

#### Using cached portal side-data ####

In some setups the data validation step may not have direct access to the web API, for instance when the web API is only accessible to authenticated browser sessions. You can use this command to generate a cached folder of files that the validation script can use instead.

```shell
# From the cbioportal-docker-compose repo
mkdir -p study/portalinfo
docker compose exec cbioportal dumpPortalInfo.pl /study/portalinfo
```

Then, tell the script to use the cached folder instead of the API:

```shell
# From the cbioportal-docker-compose repo
mkdir -p study/reports
docker compose exec cbioportal metaImport.py -p /study/portalinfo -s /study/name_of_study -o -html /study/reports/report.html
```

### Inspecting or adjusting the database ###

```shell
# Set the appropriate variables first
CLICKHOUSE_USER=<your_clickhouse_user>
CLICKHOUSE_PASSWORD=<your_clickhouse_password>
CLICKHOUSE_DB=<your_clickhouse_db_name>

docker compose exec cbioportal-database \
    sh -c 'clickhouse client -u"$CLICKHOUSE_USER" --password="$CLICKHOUSE_PASSWORD" --database="$CLICKHOUSE_DB"'
```

### Deleting a study ###

To remove a study, run:

```shell
docker compose exec \
    cbioportal \
    cbioportalImporter.py -c remove-study -id study_id
```

Where `study_id` is the `cancer_study_identifier` of the study you would like to remove.
