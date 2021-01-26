## Import data instructions ##
This is an example to import a sample study: `study_es_0`. When trying to import other studies, please follow the same routine:

1. import gene panels (if applicable, studies without gene panels are assumed to be whole exome/genome)
2. import study data

### Step 1 - Import gene panels

To import gene panels for your study, please reference the example commands in [this file](example_commands.md#importing-gene-panel)

These are the commands for importing `study_es_0` gene panels (`data_gene_panel_testpanel1` and `data_gene_panel_testpanel2`):
```shell
docker-compose run \
    cbioportal \
    bash -c 'cd /cbioportal/core/src/main/scripts/ && ./importGenePanel.pl --data /cbioportal/core/src/test/scripts/test_data/study_es_0/data_gene_panel_testpanel1.txt'
```

```shell
docker-compose run \
    cbioportal \
    bash -c 'cd /cbioportal/core/src/main/scripts/ && ./importGenePanel.pl --data /cbioportal/core/src/test/scripts/test_data/study_es_0/data_gene_panel_testpanel2.txt'
```

### Step 2 - Import data

To import data for your study, please reference the example commands in [this file](example_commands.md#importing-data)

Command for importing `study_es_0` data:

```shell
docker-compose run cbioportal metaImport.py -u http://cbioportal:8080 -s /cbioportal/core/src/test/scripts/test_data/study_es_0 -o
```

:warning: after importing a study, remember to restart `cbioportal`
to see the study on the home page. Run `docker-compose restart cbioportal`.

You have now imported the test study `study_es_0`. Note that this study is included inside the cbioportal container. The process for adding a study that is outside of the container is similar. Just make sure to add the data files in the `./study` folder. This folder is mounted as `/study/` inside of the container.

## Frequently Asked Questions

### Gene panel ID is not in database

If you see an error like this when you importing the data:  
`ERROR: data_gene_panel_matrix.txt: lines [2, 3, 4, (10 more)]: Gene panel ID is not in database. Please import this gene panel before loading study data.; values encountered: ['TESTPANEL1', 'TESTPANEL2']`  

please follow the first step to import gene panels (e.g. import `data_gene_panel_testpanel1` and `data_gene_panel_testpanel2` for `study_es_0`), then try to import the data again.

### Error occurred during validation step

Please make sure the seed database was correctly imported.

### Study imported correctly, but got error when trying to query something

Remember to restart the `cbioportal` after data imported.
```shell
docker-compose restart cbioportal
```

### Import GRCh38 data
If you are importing GRCh38 data, please remember to add `-ncbi 38` in your [importing data command](https://docs.cbioportal.org/2.1.1-deploy-with-docker-recommended/example_commands#importing-data). The `-ncbi` is set to `37` by default, adding `-ncbi 37` is optional for importing GRCh37 data.

Description for other parameters can be found by running `docker-compose run cbioportal metaImport.py -h` or following [this link](https://docs.cbioportal.org/5.1-data-loading/data-loading/using-the-metaimport-script#importing-data-into-cbioportal)
