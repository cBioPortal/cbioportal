## Import data instructions ##
This is an example to import a sample study: `study_es_0`.
When trying to import other studies, please follow the same routine:
1. import gene panels (if applicable, studies without gene panels are assumed to be whole exome/genome)
2. import study data

### Step 1 - Import gene panels

To import gene panels for your study, please reference the example commands in [this file](example_commands.md#importing-gene-panel)

These are the commands for importing `study_es_0` gene panels (`data_gene_panel_testpanel1` and `data_gene_panel_testpanel2`):
```shell
docker run -it --rm \
    --net cbio-net \
    -v /<path_to_config_file>/portal.properties:/cbioportal/portal.properties:ro \
    cbioportal/cbioportal:latest \
    bash -c 'cd /cbioportal/core/src/main/scripts/ && ./importGenePanel.pl --data /cbioportal/core/src/test/scripts/test_data/study_es_0/data_gene_panel_testpanel1.txt'
```

```shell
docker run -it --rm \
    --net cbio-net \
    -v /<path_to_config_file>/portal.properties:/cbioportal/portal.properties:ro \
    cbioportal/cbioportal:latest \
    bash -c 'cd /cbioportal/core/src/main/scripts/ && ./importGenePanel.pl --data /cbioportal/core/src/test/scripts/test_data/study_es_0/data_gene_panel_testpanel2.txt'
```

### Step 2 - Import data

To import data for your study, please reference the example commands in [this file](example_commands.md#importing-data)

Command for importing `study_es_0` data:

```shell
docker run -it --rm --net cbio-net \
    -v /<path_to_config_file>/portal.properties:/cbioportal/portal.properties:ro \
    cbioportal/cbioportal:latest \
    metaImport.py -u http://cbioportal-container:8080 -s /cbioportal/core/src/test/scripts/test_data/study_es_0
```

:warning: after importing a study, remember to restart `cbioportal-container`
to see the study on the home page. Run `docker restart cbioportal-container`.

You have now imported the test study `study_es_0`. This study is included inside the cbioportal container. The process for adding a study that is outside of the container is almost the same. The difference is that one needs to add the `-v` parameter to mount the folder with the study data inside the container. See [How to import a real study](import_data.md#how-to-import-a-real-study) section for an example.

## Frequently Asked Questions

### Gene panel ID is not in database

If you see an error like this when you importing the data:  
`ERROR: data_gene_panel_matrix.txt: lines [2, 3, 4, (10 more)]: Gene panel ID is not in database. Please import this gene panel before loading study data.; values encountered: ['TESTPANEL1', 'TESTPANEL2']`  

please follow the first step to import gene panels (e.g. import `data_gene_panel_testpanel1` and `data_gene_panel_testpanel2` for `study_es_0`), then try to import the data again.

### Error occurred during validation step

Please make sure this line was included when [setting up the database](README.md#step-2-run-mysql-with-seed-database):

`-v /<path_to_seed_database>/seed-cbioportal_<genome_build>_<seed_version>.sql.gz:/docker-entrypoint-initdb.d/seed_part1.sql.gz:ro \`.

### Study imported correctly, but got error when trying to query something

Remember to restart the `cbioportal-container` after data imported.
```shell
docker restart cbioportal-container
```

### How to import a real study

You can find all public studies at [cbioportal.org](http://www.cbioportal.org/data_sets.jsp) a zipped folder with staging files from each study can be downloaded. These zip files are compressed versions of the study folders in [datahub](https://github.com/cBioPortal/datahub).

Download and extract the zip file for study `Cholangiocarcinoma (TCGA, PanCancer Atlas)`. Study `Cholangiocarcinoma (TCGA, PanCancer Atlas)` is a whole exome study. You can therefore skip [step 1 from the import data instructions section](import_data.md#step-1-import-gene-panels): the default gene panel is whole exome. Proceed with the command from step2.

Specify the study directory by replacing 
`<path_to_study_directory>` with the absolute path to the study folder (e.g. path of folder `chol_tcga_pan_can_atlas_2018`).
```shell
docker run -it --rm --net cbio-net \
    -v /<path_to_config_file>/portal.properties:/cbioportal/portal.properties:ro \
    -v "<path_to_study_directory>:/study:ro" \
    cbioportal/cbioportal:latest \
    metaImport.py -u http://cbioportal-container:8080 -s /study -o --html=/report/report.html
```
after data imported successfully:
```shell
docker restart cbioportal-container
```
