# Downloads

This page describes the various files available for download. The first section is targeted towards [users](Downloads.md#user-downloads) of cBioPortal. The second section towards [maintainers](Downloads.md#instance-maintainer-downloads) of cBioPortal instances.

### User Downloads

There are several ways in which one can download data from cBioPortal including manual and programmatic approaches. See options outlined below.

#### Datasets Page

A zip file for each study on [cbioportal.org](https://www.cbioportal.org) can be download from the [Datasets Page](https://www.cbioportal.org/datasets). One can also use the R client [cBioPortalData](/API-and-API-Clients.md#r-client) to programmatically download all of these files.

#### Datahub

The files for each study are also available from our [datahub repository](https://github.com/cBioPortal/datahub). This is basically the extracted version of the zip files in the [Datasets Page](https://www.cbioportal.org/datasets). Note that this is a git LFS repo so if you are familiar with git you might prefer using this option.

#### API and API Clients

Besides downloading all the study data one can also request slices of the data using the API. A slice of the data could e.g. be "give me all the mutation data for one patient" or "get me all EGFR mutations for a particular group of samples". There are API clients available in a variety of languages including bash, R and Python. See for more information the [API documentation](/API-and-API-Clients.md#api-and-api-clients).

### Instance Maintainer Downloads

As an instance maintainer of cBioPortal there are a variety of files that might be helpful. See below.

#### Study staging files

Staging files for the studies on cbioportal.org can be download from the [Datasets Page](https://www.cbioportal.org/datasets). These studies can be validated and loaded in a local cBioPortal instances using the [validator and importer](Data-Loading.md). Any issues with a downloaded study can be reported on [cBioPortal DataHub](https://github.com/cBioPortal/datahub/).

**Example studies**

TCGA Provisional studies often contain many different data types. These are excellent examples to use as reference when creating your own staging files. A detailed description on supported data types can be found in the [File Formats documentation](/File-Formats.md).

#### Complete cBioPortal database

A MySQL database dump of the complete cbioportal.org database can be found here: https://public-db-dump.assets.cbioportal.org/

#### Seed Database

The seed database is a MySQL dump for seeding a new instance of the cBioPortal. Instructions for loading the seed database can be found [here](/deployment/deploy-without-docker/Import-the-Seed-Database.md). The seed database for human can be downloaded from [cBioPortal Datahub](https://github.com/cBioPortal/datahub/tree/master/seedDB). A mouse version can be found [here](https://github.com/cBioPortal/datahub/tree/master/seedDB\_mouse).
