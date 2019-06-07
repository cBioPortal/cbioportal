* [Introduction](#introduction)
* [Seed Database](#seed-database)
* [Study staging files](#study-staging-files)
* [Complete cBioPortal database](#complete-cbioportal-database)

# Introduction
This page describes the various files available for download.

# Seed Database
The seed database is a MySQL dump for seeding a new instance of the cBioPortal. Instructions for loading the seed database can be found [here](Import-the-Seed-Database.md). The seed database for human can be downloaded from [cBioPortal Datahub](https://github.com/cBioPortal/datahub/tree/master/seedDB). A mouse version can be found [here](https://github.com/cBioPortal/datahub/tree/master/seedDB_mouse).

# Study staging files
Staging files for the studies on cbioportal.org can be download from the [Data Sets section](https://www.cbioportal.org/data_sets.jsp). These studies can be validated and loaded in a local cBioPortal instances using the [validator and importer](Data-Loading.md). Any issues with a downloaded study can be reported on [cBioPortal DataHub](https://github.com/cBioPortal/datahub/).

#### Example studies
TCGA Provisional studies often contain many different data types. These are excellent examples to use as reference when creating your own staging files. A detailed description on supported data types can be found in the [File Formats documentation](File-Formats.md).

# Complete cBioPortal database
A MySQL database dump of the complete cbioportal.org database can be found here:
http://download.cbioportal.org/mysql-snapshots/public-portal-dump.latest.sql.gz