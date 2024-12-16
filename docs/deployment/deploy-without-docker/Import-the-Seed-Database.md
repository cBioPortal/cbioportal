# Importing the Seed Database

The next step is to populate your cBioPortal instance with all the required background data sets. This includes for example gene data, ID mappings, and network interactions. Rather than importing each of these data sets individually, we have provided a simple "seed" database that you can import directly.

## Download the cBioPortal Seed Database

A cBioPortal seed database for human can be found on [the datahub page](https://github.com/cBioPortal/datahub/blob/master/seedDB/README.md). If you are looking for mouse, check [this link](https://github.com/cBioPortal/datahub/blob/master/seedDB_mouse/README.md).

After download, the files can be unzipped by entering the following command:

    gunzip *.sql.gz

## Import the cBioPortal Seed Database

**Important:** Before importing, make sure that you have [followed the pre-build steps](Pre-Build-Steps.md) for creating the `cbioportal` database (see section "Create the cBioPortal MySQL Databases and User").

1. Import the database schema (/db-scripts/src/main/resources/cgds.sql):

    ```
    mysql --user=cbio --password=P@ssword1 cbioportal < cgds.sql
    ```

    Note that this may currently fail when using the default character encoding on MySQL 8.0 (`utf8mb4`); this is why MySQL 5.7 (which uses `latin1`) is recommended.

2. Import the main part of the seed database:

    ```
    mysql --user=cbio --password=P@ssword1 cbioportal < seed-cbioportal_RefGenome_vX.Y.Z.sql
    ```

    **Important:** Replace `seed-cbioportal_RefGenome_vX.Y.Z.sql` with the downloaded version of the seed database, such as `seed-cbioportal_hg19_v2.3.1.sql` or `seed-cbioportal_mm10_v2.3.1.sql`.

3. (Human only) Import the Protein Data Bank (PDB) part of the seed database. This will enable the visualization of PDB structures in the mutation tab. Loading this file takes more time than loading the previous files, and is optional for users that do not require PDB structures.

    ```
    mysql --user=cbio --password=P@ssword1 cbioportal < seed-cbioportal_hg19_vX.Y.Z_only-pdb.sql
    ```
    **Important:** Replace `seed-cbioportal_hg19_vX.Y.Z_only-pdb.sql` with the downloaded version of the PDB database, such as `seed-cbioportal_hg19_v2.3.1_only-pdb.sql`.

4. (optional : support for microRNA genomic profiles) Import constructed gene table records for microRNA genomic profiles. Currently, cBioPortal supports the combined display of copy number alterations (generally reported for microRNA precursors) and expression (generally reported for microRNA mature forms) by adding gene table records which represent the combination of microRNA precursor and microRNA mature form. Appropriate aliases are added to the gene_alias table so that both the name of the precursor and the name of the mature form are recognized references to the combination.

This involves downloading the cBioPortal Core code located [here](https://github.com/cBioPortal/cbioportal-core). 

After the code has been successfully configured and built, you can import the needed microRNA records by running the following command from the cBioPortal core directory:

    java -cp scripts/target/scripts-*.jar org.mskcc.cbio.portal.scripts.ImportGeneData -microrna core/src/main/resources/micrornas.tsv
    

**Important:** Please be aware of the version of the seed database. In the [README on datahub](https://github.com/cbioportal/datahub/blob/master/seedDB/README.md), we stated which version of cBioPortal is compatible with the current seed database.

If the database is older than what cBioPortal is expecting, the system will ask you (during startup or data loading) to migrate the database to a newer version. The migration process is described [here](/Updating-your-cBioPortal-installation.md#running-the-migration-script).
