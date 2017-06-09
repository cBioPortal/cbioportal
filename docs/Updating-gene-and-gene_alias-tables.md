This manual is intended for users that have knowledge about the structure of the cBioPortal seed database.

When loading studies into cBioPortal it is possible for warnings to occur that are caused by an outdated seed database. Gene symbols can be deprecated or be assigned to a different Entrez Gene in a new release. Also Entrez Gene IDs can be added. This markdown explains how to update the seed database, in order to use the most recent Entrez Gene IDs. 

The cBioPortal scripts package provides a method to update the `gene` and `gene_alias` tables. This requires the latest version of the NCBI Gene Info: ftp://ftp.ncbi.nih.gov/gene/DATA/GENE_INFO/Mammalia/Homo_sapiens.gene_info.gz.

## Updating the gene names and aliases

Execute these steps in case you want to reset your database to the most recent genes list from NCBI.

1- Remove all studies from your installation. You can use the [study removal tool](Development%2C-debugging-and-maintenance-mode-using-cbioportalImporter#deleting-a-study). Also empty tables `mutation_event` and `cna_event`
```sql
TRUNCATE TABLE mutation_event;
TRUNCATE TABLE cna_event;
```

Another way of obtaining an empty database is starting a new MySQL database with the previous seed database.

2- If DB engine supports foreign key (FK) constraints, e.g. InnoDB, drop constraints:
```sql
ALTER TABLE cosmic_mutation
  DROP FOREIGN KEY cosmic_mutation_ibfk_1;

ALTER TABLE sanger_cancer_census
  DROP FOREIGN KEY sanger_cancer_census_ibfk_1;

ALTER TABLE uniprot_id_mapping
  DROP FOREIGN KEY uniprot_id_mapping_ibfk_1;
```

3- Empty tables `gene` and `gene_alias`
```sql
TRUNCATE TABLE gene_alias;
TRUNCATE TABLE gene;
```

4- Restart cBioPortal (restart webserver) to clean-up any cached gene lists.

5- You probably also want to update the gene lengths. This is dependent on the reference genome you want to use. Download .annotation.gtf.gz from the latest GENCODE release from http://www.gencodegenes.org/releases/current.html for hg38 / GRCh38. For the hg19 / GRCh37, click on 'Go to GRCh37 version of this release'.

After downloading, go to your downloads directory, decompress the file and add it as an argument (--gtf) in the next step.

6- To import gene data type the following commands when in the folder `<your_cbioportal_dir>/core/src/main/scripts`:

```
 export PORTAL_HOME=<your_cbioportal_dir>
./importGenes.pl --genes <Homo_sapiens_gene_info.txt> --gtf <gencode.v25.annotation.gtf>
```

7- :warning: Check the `gene` and `gene_alias` tables to verify that they are filled correctly.

8- Additionally, there are several other tables you may want to update now.

1. Updating the pfam graphics if you want to see 3D structures, explained in [this document](https://github.com/oplantalech/cbioportal/blob/d74d7159ff6abba67739efa7f905e407b3961ed3/docs/Updating-pfam_graphics-table.md)

2. Updating the COSMIC coding mutations, can be downloaded from [here](http://cancer.sanger.ac.uk/cosmic/download) and require the script `importCosmicData.pl`

9- Clean-up old data:
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

10- If DB engine supports FK constraints, e.g. InnoDB, restore constraints:
```sql
ALTER TABLE cosmic_mutation
  ADD FOREIGN KEY (`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`);

ALTER TABLE sanger_cancer_census
  ADD FOREIGN KEY (`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`);

ALTER TABLE uniprot_id_mapping
  ADD FOREIGN KEY (`ENTREZ_GENE_ID`) REFERENCES `gene` (`ENTREZ_GENE_ID`);
```
