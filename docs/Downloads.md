* [Introduction](#introduction)
* [Seed Database](#seed-database)
* [MAF Example](#maf-example)
* [Public Study Data](#public-study-data)

# Introduction

This page describes the various files available for download.

# Seed Database

The seed database is a MySQL dump for seeding a new instance of the cBioPortal. Instructions for loading the seed database can be found [here](Import-the-Seed-Database.md). The seed database for human can be downloaded from [cBioPortal Datahub](https://github.com/cBioPortal/datahub/tree/master/seedDB). A mouse version can be found [here](https://github.com/cBioPortal/datahub/tree/master/seedDB_mouse).

# MAF Example

This is an example of the file format used to import mutation data into the cBioPortal.  It is derived from the [Mutation Annotation Format](https://wiki.nci.nih.gov/display/TCGA/Mutation+Annotation+Format+%28MAF%29+Specification) created as part of the [TCGA](https://wiki.nci.nih.gov/display/TCGA/TCGA+Home) project. The example MAF file is part of the cBioPortal test study and available here:

[brca_tcga_pub.maf](https://github.com/cBioPortal/cbioportal/blob/master/core/src/test/scripts/test_data/study_es_0/brca_tcga_pub.maf)

[vcf2maf](https://github.com/mskcc/vcf2maf) can be used to convert a VCF to MAF. More information on the MAF format used in cBioPortal can be found on the [File Formats](File-Formats.md#mutation-data) page.

# Public Study Data

Staging files for the studies on [www.cbioportal.org][www.cbioportal.org] are available on [cBioPortal Datahub](https://github.com/cBioPortal/datahub/tree/master/public). The Provisional TCGA studies (\*\_tcga.tar.gz) are complete studies which can be used as reference when creating cBioPortal staging files.
