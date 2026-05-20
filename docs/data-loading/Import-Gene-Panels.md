# Import Gene Panels

This page describes how to import a gene panel into the cBioPortal database. The gene panel to import must be in the proper file format — see [Gene Panel File format](#gene-panel-file-format) for more information.

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

In this example, we are loading the example gene panels which reside in the sample dataset `study_es_0`.

```bash
docker compose exec cbioportal importGenePanel.pl --data /study/reference_data/data_gene_panel_testpanel1.txt
docker compose exec cbioportal importGenePanel.pl --data /study/reference_data/data_gene_panel_testpanel2.txt
```

For gene panels that are not bundled with a study (i.e. standalone reference panels), place the panel files in the `cbioportal-docker-compose/study/reference_data/` directory on the host. They will be mounted as `/study/reference_data/<your_panel.txt>` inside the container through a Docker volume.

After loading gene panels into the database, please restart the portal to see updates.

#### Update existing gene panel

> **Note:** The documentation in this section is outdated-- there is no longer an `updateGenePanel.pl` script. If you want to re-import a gene panel that is referred to by an existing study, it is recommended to clear the database of that study first and then run `importGenePanel.pl`.

If a gene panel exists in the database with the same name as the one being imported, and there exists cancer study data that refers to this gene panel, the ImportGenePanel command will abort.  In order to reimport the gene panel in this situation, run the UpdateGenePanel command.  

If the incoming gene panel is the same as the original gene panel, whether through importing or updating, then no changes shall be made to the gene panel.  If the incoming gene panel is empty, then the script will abort.  Genes in the incoming gene panel that were not in the original shall be added to the existing gene panel. Conversely, genes not in the incoming gene panel that were in the original shall be removed from the existing gene panel.  The UpdateGenePanel command will prompt twice to confirm changes made to the gene panel, such as genes to be added or removed.  

```bash
docker compose exec cbioportal updateGenePanel.pl --data /study/reference_data/data_gene_panel_testpanel1.txt
```
