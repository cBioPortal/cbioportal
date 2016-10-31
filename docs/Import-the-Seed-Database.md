# Importing the Seed Database

The next step is to populate your cBioPortal instance with all the required background data sets.  This includes, for example:  gene data, ID mappings, and network interactions.  Rather than importing each of these data sets individually, we have provided a simple "seed" database that you can import directly.

## Download the cBioPortal Database

A cBioPortal seed database can be found on [the datahub page](https://github.com/cbioportal/datahub/blob/master/seedDB/README.md).

After download, the files can be unzipped by entering the following command:

    gunzip *.sql.gz

## Import the cBioPortal Database

*Important:*  Before importing, make sure that you have [followed the pre-build steps](Pre-Build-Steps.md#prepare_database) for creating the `cbioportal` database.  

Then import the seed database via the `mysql` commands:

    > mysql --user=cbio_user --password=somepassword cbioportal  < cgds.sql

and:

    > mysql --user=cbio_user --password=somepassword cbioportal  < seed-cbioportal_no-pdb_hg19.sql
    
and (this command takes a bit longer to import PDB data that will enable the visualization of PDB structures in the mutation tab): 

    > mysql --user=cbio_user --password=somepassword cbioportal  < seed-cbioportal_only-pdb.sql

:information_source: please be aware of the version of the seed DB. If it is different from what the cBioPortal system is expecting, the system will at some point ask you to run a migration step. The system will automatically give you a clear message about this (with instructions) if a migration is needed. 

## Drug-target and Clinical Trial Data

Due to data provider specific restrictions on data re-distribution, the database setup, as outlined above, will lack some of the drug-gene relationships from specific data-resources and also all clinical trial information. If you would like to obtain the complete the data sets from these resources, we encourage you to take advantage of [PiHelper](http://bitbucket.org/armish/pihelper) for aggregating drug-target associations and [NCI's Data Dissemination Program](http://www.cancer.gov/publications/pdq), PDQ, for clinical trials data; and use the corresponding data importers, `importPiHelperData.sh` and `importClinicalTrialData.pl`, that are distributed as part of cBioPortal.

[Next Step: Loading a Sample Study](Load-Sample-Cancer-Study.md)
