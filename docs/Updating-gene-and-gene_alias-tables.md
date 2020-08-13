# Updating the gene names and aliases tables
This manual is intended for users that have knowledge about the structure of the cBioPortal seed database.

When loading studies into cBioPortal it is possible for warnings to occur that are caused by an outdated seed database. Gene symbols can be deprecated or be assigned to a different Entrez Gene in a new release. Also Entrez Gene IDs can be added. This markdown explains how to update the seed database, in order to use the most recent Entrez Gene IDs.

The cBioPortal scripts package provides a method to update the `gene` and `gene_alias` tables. This requires the latest version of the NCBI Gene Info.

### Human genes
Homo_sapien.gene_info.gz\
ftp://ftp.ncbi.nih.gov/gene/DATA/GENE_INFO/Mammalia/Homo_sapiens.gene_info.gz

### Mouse genes
Mus_musculus.gene_info.gz\
ftp://ftp.ncbi.nih.gov/gene/DATA/GENE_INFO/Mammalia/Mus_musculus.gene_info.gz

## MySQL steps
Execute these steps in case you want to reset your database to the most recent genes list from NCBI.

1- Start a new MySQL database with the previous seed database, which can be found on cBioPortal Datahub for [human](https://github.com/cBioPortal/datahub/tree/master/seedDB) and [mouse](https://github.com/cBioPortal/datahub/tree/master/seedDB_mouse).

2- If DB engine supports foreign key (FK) constraints, e.g. InnoDB, drop constraints:
```sql
ALTER TABLE cosmic_mutation
  DROP FOREIGN KEY cosmic_mutation_ibfk_1;

ALTER TABLE uniprot_id_mapping
  DROP FOREIGN KEY uniprot_id_mapping_ibfk_1;
```

3- Empty tables `gene` and `gene_alias`
```sql
TRUNCATE TABLE gene_alias;
DELETE from genetic_entity;
DELETE from geneset_hierarchy_node;
ALTER TABLE `genetic_entity` AUTO_INCREMENT = 1;
ALTER TABLE `geneset_hierarchy_node` AUTO_INCREMENT = 1;
ALTER TABLE `geneset` AUTO_INCREMENT = 1;
```

4- Restart cBioPortal (restart webserver) to clean-up any cached gene lists.

5- To import gene data type the following commands when in the folder `<cbioportal_source_folder>/core/src/main/scripts`:
```
export PORTAL_HOME=<cbioportal_configuration_folder>
export JAVA_HOME=<jre_installation_folder>
./importGenes.pl --genes <ncbi_species.gene_info> --gtf <gencode.v25.annotation.gtf> --genome-build <GRCh37>
```
**IMPORTANT NOTE**: 
1. The **reference_genome** table needs to be populated before updating the **gene** table. Further details can be found in [this document](import-reference-genome.md). 
2. Use **--species** option when importing genes for a species other than human
3. Use the **gene** table if you query information such as hugo symbols, types of the gene 
4. Use **reference_genome_gene** table if you query information such as chromosome, cytoband, exonic length, or the start or end of the gene
5. Load genes only to the **reference_genome_gene** table without updating the **gene** table, please use the following command:
```
./importGenes.pl --gtf <gencode.v27.annotation.gtf> --genome-build <GRCh38>
```
6- :warning: Check the `gene` and `gene_alias` tables to verify that they are filled correctly.
```sql
SELECT count(*) FROM cbioportal.gene;
SELECT count(*) FROM cbioportal.gene_alias;
```

7- Additionally, there are other tables you may want to update now (only in human).

* Updating the COSMIC coding mutations, can be downloaded from [here](https://cancer.sanger.ac.uk/cosmic/download) and require the script `importCosmicData.pl`

8- Clean-up old data:
```sql
SET SQL_SAFE_UPDATES = 0;
DELETE FROM cosmic_mutation where ENTREZ_GENE_ID not in (SELECT ENTREZ_GENE_ID from gene);
DELETE FROM sanger_cancer_census where ENTREZ_GENE_ID not in (SELECT ENTREZ_GENE_ID from gene);
DELETE FROM uniprot_id_mapping where ENTREZ_GENE_ID not in (SELECT ENTREZ_GENE_ID from gene);
DELETE FROM interaction where GENE_A not in (SELECT ENTREZ_GENE_ID from gene) or GENE_B not in (SELECT ENTREZ_GENE_ID from gene);
DELETE FROM drug_interaction where target not in (SELECT ENTREZ_GENE_ID from gene);
SET SQL_SAFE_UPDATES = 1;
commit;
```

9- If DB engine supports FK constraints, e.g. InnoDB, restore constraints:
```sql
ALTER TABLE cosmic_mutation
  ADD CONSTRAINT cosmic_mutation_ibfk_1 FOREIGN KEY (`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`);

ALTER TABLE uniprot_id_mapping
  ADD CONSTRAINT uniprot_id_mapping_ibfk_1 FOREIGN KEY (`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`);
```

10- You can import new gene sets using the gene set importer. These gene sets are currently only used for gene set scoring. See [Import-Gene-Sets.md](Import-Gene-Sets.md) and [File-Formats.md#gene-set-data](File-Formats.md#gene-set-data).

For example, run in folder `<cbioportal_source_folder>/core/src/main/scripts`:
```bash
./importGenesetData.pl --data ~/Desktop/msigdb.v6.1.entrez.gmt --new-version msigdb_6.1
```
Please make sure the version gene sets is the same as the version used to calculate gene set scores in your data.
