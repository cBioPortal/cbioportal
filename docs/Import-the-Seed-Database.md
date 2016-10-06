# Importing the Seed Database

The next step is to populate your cBioPortal instance with all the required background data sets.  This includes, for example:  gene data, ID mappings, and network interactions.  Rather than importing each of these data sets individually, we have provided a simple "seed" database that you can import directly.

## Download the cBioPortal Database

A cBioPortal seed database can be found on the [Downloads](Downloads.md#seed-database) page.

After download, this file can be unzipped by entering the following command:

    gunzip cbioportal-seed.sql.gz

## Import the cBioPortal Database

*Important:*  Before importing, make sure that you have [followed the pre-build steps](Pre-Build-Steps.md#prepare_database) for creating the `cbioportal` database.  

Then import the seed database via the `mysql` command:

    > mysql --user=cbio_user --password=somepassword cbioportal  < cbioportal-seed.sql

## Drug-target Data

Due to data provider specific restrictions on data re-distribution, the database setup, as outlined above, will lack some of the drug-gene relationships from specific data-resources. If you would like to obtain the complete the data sets from these resources, we encourage you to take advantage of [PiHelper](http://bitbucket.org/armish/pihelper) for aggregating drug-target associations and use the corresponding data importer `importPiHelperData.sh` which is distributed as part of cBioPortal.

[Next Step: Loading a Sample Study](Load-Sample-Cancer-Study.md)
