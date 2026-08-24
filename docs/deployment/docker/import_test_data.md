# Importing test data with Docker

### Instructions

This is an example to import the sample study `study_es_0`. `study_es_0` is a **testing and evaluation dataset** that covers a broad range of cBioPortal data types. It is intended to help ensure that your cBioPortal importer is correctly handling all supported data types. We will follow this flow:

1. import gene sets (if applicable)
2. import gene panels (if applicable, studies without gene panels are assumed to be whole exome/genome)
3. import study data

> **Warning:** Importing gene sets (Step 1) **removes all existing gene set, gene set hierarchy, and gene set genetic profile data** from the database. It is strongly recommended to do this on a fresh database instance rather than one that already contains geneset data you want to keep. Importing other studies may not work after importing `study_es_0` and will require a fresh database to be loaded.

#### Step 1 - Import gene sets

`study_es_0` relies on gene set data, so gene set definitions must be loaded before the study. Place the required reference files in the `./study/reference_data/` directory on the host (mounted as `/study/reference_data/` inside the container), then run:

```shell
docker compose exec cbioportal importGenesetData.pl \
    --data /study/reference_data/study_es_0_genesets.gmt \
    --new-version msigdb_7.5.1 \
    --supp /study/reference_data/study_es_0_supp-genesets.txt
```

```shell
docker compose exec cbioportal importGenesetHierarchy.pl \
    --data /study/reference_data/study_es_0_tree.yaml
```

For studies that do not include gene set data, skip this step.

#### Step 2 - Import gene panels

To import gene panels for your study, please reference the example commands in [this file](example_commands.md#importing-gene-panel)

These are the commands for importing `study_es_0` gene panels (`data_gene_panel_testpanel1` and `data_gene_panel_testpanel2`):

```shell
docker compose exec cbioportal importGenePanel.pl --data /study/reference_data/data_gene_panel_testpanel1.txt
```

```shell
docker compose exec cbioportal importGenePanel.pl --data /study/reference_data/data_gene_panel_testpanel2.txt
```

#### Step 3 - Import data

To import data for your study, please reference the example commands in [this file](example_commands.md#importing-data)

Command for importing `study_es_0` data:

```shell
docker compose exec cbioportal metaImport.py -s /study/study_es_0 -o
```

:warning: after importing a study, remember to restart `cbioportal` to see the study on the home page. Run `docker compose restart cbioportal`.

You have now imported the test study `study_es_0`. The process for adding a study that is outside of the container is similar — place the data files in the `./study` folder on the host, which is mounted as `/study/` inside the container.

### Frequently Asked Questions

#### Gene panel ID is not in database

If you see an error like this when you importing the data:\
`ERROR: data_gene_panel_matrix.txt: lines [2, 3, 4, (10 more)]: Gene panel ID is not in database. Please import this gene panel before loading study data.; values encountered: ['TESTPANEL1', 'TESTPANEL2']`

please follow Step 2 to import gene panels (e.g. import `data_gene_panel_testpanel1` and `data_gene_panel_testpanel2` for `study_es_0`), then try to import the data again.

#### Error occurred during validation step

Please make sure the seed database was correctly imported.

#### Study imported correctly, but got error when trying to query something

Remember to restart the `cbioportal` after data imported.

```shell
docker compose restart cbioportal
```

#### Import GRCh38 data

If you are importing GRCh38 data, please remember to set the `reference_genome: hg38` field in the `meta_study.txt` file. See also [cancer study metadata](/File-Formats.md).
