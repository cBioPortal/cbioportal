# Import Gene Panels

This page describes how to import a gene panel into the cBioPortal database.  It assumes the following requirements have been satisfied:

1. The cBioPortal software has been correctly [built from source](Build-from-Source.md).
2. The gene panel to import is in the proper file format.  See [File Format](File-Formats.md#gene-panel-data) for more information.
3. The `PORTAL_HOME` environment variable has been properly defined.  See [Loading a Sample Study](Load-Sample-Cancer-Study.md#set-the-portal_home-environment-variable) for more information.

#### Import command

In this example, we are loading the example gene panel which resides in the sample dataset `study_es_0`.

```
cd <cbioportal_source_folder>/core/src/main/scripts
./importGenePanel.pl --data ../../test/scripts/test_data/study_es_0/gene_panel_example.txt
```

#### Reimport existing gene panel

If a gene panel exists in the database with the same name as the one being imported, and there exists cancer study data that refers to this gene panel, this command will abort.  In order to import the gene panel in this situation, either remove the cancer study from the database that refers to this gene panel or explicitly remove the gene panel from the data and then rerun the ImportGenePanel command.  To remove the gene panel from the database, run the following commands fromthe MySQL console:

```
delete from gene_panel_list where internal_id = (select internal_id from gene_panel where stable_id = "example_gene_panel_stable_id");
delete from gene_panel where stable_id = "example_gene_panel_stable_id";
```
In this example we are removing the gene panel with the stable_id `example_gene_panel_stable_id`, the example gene_panel from the study_es_0 sample dataset.
