# Importing the Seed Database

The next step is to populate your cBioPortal instance with all the required background data sets. This includes for example gene data, ID mappings, and network interactions. Rather than importing each of these data sets individually, we have provided a simple "seed" database that you can import directly.

## Download the cBioPortal Seed Database

A cBioPortal seed database for human can be found on [the datahub page](https://github.com/cBioPortal/datahub/blob/master/seedDB/README.md). If you are looking for mouse, check [this link](https://github.com/cBioPortal/datahub/blob/master/seedDB_mouse/README.md).

After download, the files can be unzipped by entering the following command:

    gunzip *.sql.gz

## Import the cBioPortal Seed Database

**Important:** Before importing, make sure that you have [followed the pre-build steps](Pre-Build-Steps.md) for creating the `cbioportal` database (see section "Create the cBioPortal MySQL Databases and User").

1. Import the database schema:

    ```
    mysql --user=cbio_user --password=somepassword cbioportal < cgds.sql
    ```

    Note that this may currently fail when using the default character encoding on MySQL 8.0 (`utf8mb4`); this is why MySQL 5.7 (which uses `latin1`) is recommended.

2. Import the main part of the seed database:

    ```
    mysql --user=cbio_user --password=somepassword cbioportal < seed-cbioportal_RefGenome_vX.Y.Z.sql
    ```

    **Important:** Replace `seed-cbioportal_RefGenome_vX.Y.Z.sql` with the downloaded version of the seed database, such as `seed-cbioportal_hg19_v2.3.1.sql` or `seed-cbioportal_mm10_v2.3.1.sql`.

3. (Human only) Import the Protein Data Bank (PDB) part of the seed database. This will enable the visualization of PDB structures in the mutation tab. Loading this file takes more time than loading the previous files, and is optional for users that do not require PDB structures.

    ```
    mysql --user=cbio_user --password=somepassword cbioportal < seed-cbioportal_hg19_vX.Y.Z_only-pdb.sql
    ```
    **Important:** Replace `seed-cbioportal_hg19_vX.Y.Z_only-pdb.sql` with the downloaded version of the PDB database, such as `seed-cbioportal_hg19_v2.3.1_only-pdb.sql`.

**Important:** Please be aware of the version of the seed database. In the [README on datahub](https://github.com/cbioportal/datahub/blob/master/seedDB/README.md), we stated which version of cBioPortal is compatible with the current seed database.

If the database is older than what cBioPortal is expecting, the system will ask you (during startup or data loading) to migrate the database to a newer version. The migration process is described [here](Updating-your-cBioPortal-installation.md#running-the-migration-script).

## Drug-target Data

Due to data provider specific restrictions on data re-distribution, the database setup, as outlined above, will lack some of the drug-gene relationships from specific data-resources. If you would like to obtain the complete the data sets from these resources, we encourage you to take advantage of [PiHelper](http://bitbucket.org/armish/pihelper) for aggregating drug-target associations and use the corresponding data importer `importPiHelperData.sh` which is distributed as part of cBioPortal.

[Next Step: Deploying the Web Application](Deploying.md)
