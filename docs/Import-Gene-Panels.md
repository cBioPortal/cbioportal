# Import Gene Panels

This page describes how to import a gene panel into the cBioPortal database.  It assumes the following requirements have been satisfied:

1. The cBioPortal software has been correctly [built from source](Build-from-Source.md).
2. The gene panel to import is in the proper file format. See [Gene Panel File format](#gene-panel-file-format) for more information.
3. The `PORTAL_HOME` environment variable has been properly defined.  See [Loading a Sample Study](Load-Sample-Cancer-Study.md#set-the-portal_home-environment-variable) for more information.

## Gene panel file format
The gene panel file follows the format of a meta file with the following fields:
1. **stable_id**: The name of the gene panel. This should be unique across all studies, as gene panels can be globally applied to any sample and any genetic profile.
2. **description**: A description of the gene panel.
3. **gene_list**: Tab separated genes, represented either by all gene symbols or all Entrez gene IDs.

An example gene panel file would be:
```
stable_id: IMPACT410
description: Targeted (410 cancer genes) sequencing of various tumor types via MSK-IMPACT on Illumina HiSeq sequencers.
gene_list: ABL1    ACVR1   AKT1    AKT3 ...
```

#### Import command

In this example, we are loading the example gene panels which resides in the sample dataset `study_es_0`.

```
cd <cbioportal_source_folder>/core/src/main/scripts
./importGenePanel.pl --data ../../test/scripts/test_data/study_es_0/data_gene_panel_testpanel1.txt
./importGenePanel.pl --data ../../test/scripts/test_data/study_es_0/data_gene_panel_testpanel2.txt
```

After loading gene panels into the database, please restart Tomcat so that the validator can retrieve gene panel information from the cBioPortal API. 

#### Reimport existing gene panel

If a gene panel exists in the database with the same name as the one being imported, and there exists cancer study data that refers to this gene panel, this command will abort.  In order to import the gene panel in this situation, either remove the cancer study from the database that refers to this gene panel or explicitly remove the gene panel from the data and then rerun the ImportGenePanel command.  To remove the gene panel from the database, run the following commands from the MySQL console:

```
delete from gene_panel_list where internal_id = (select internal_id from gene_panel where stable_id = "TESTPANEL1");
delete from gene_panel where stable_id = "TESTPANEL1";
```
In this example we are removing the gene panel with the stable_id `TESTPANEL1`, the example gene_panel from the study_es_0 sample dataset.
