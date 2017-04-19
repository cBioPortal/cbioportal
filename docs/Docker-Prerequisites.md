# Docker Prerequisites

Docker provides a way to run applications securely isolated in a container, packaged with all its dependencies and libraries.
To learn more on Docker, kindly refer here: [What is Docker?](https://www.docker.com/what-docker).

## 1. Install Docker

First, make sure that you have the latest version of Docker installed on your machine. [Get latest version](https://www.docker.com/products/overview#/install_the_platform)

## 2. Download Seed DB

The latest cBioPortal Seed files are available from the [cBioPortal Datahub](https://github.com/cBioPortal/datahub/tree/master/seedDB).    
You can download these files by using the links below:

- **Schema 1.3.0**: [SQL file with create table statements for portal release 1.3.1](https://raw.githubusercontent.com/cBioPortal/cbioportal/v1.3.1/core/src/main/resources/db/cgds.sql) 
- **Seed data, part1**: [cbioportal-seed SQL (.gz) file - part1 (no pdb_ tables)](https://github.com/cbioportal/datahub/raw/b69c86803c40d543080bf31a645721d06c82d08d/seedDB/seed-cbioportal_no-pdb_hg19.sql.gz)
- **Seed data, part2 (optional)** [cbioportal-seed SQL (.gz) file - part2 (only pdb_ tables)](https://github.com/cbioportal/datahub/raw/b69c86803c40d543080bf31a645721d06c82d08d/seedDB/seed-cbioportal_only-pdb.sql.gz)

## 3. Prepare Configuration Files

You will need the following configuration files.

- [portal.properties Reference](Pre-Build-Steps.md#prepare-property-files).
- [Download portal.properties.EXAMPLE](../src/main/resources/portal.properties.EXAMPLE)
- [log4j.properties  Reference](Pre-Build-Steps.md#prepare-the-log4jproperties-file).
- [Download log4j.properties.EXAMPLE](../src/main/resources/log4j.properties.EXAMPLE)
- [settings.xml Reference](Pre-Build-Steps.md#create-a-maven-settings-file)
- [context.xml Reference](Deploying.md#set-up-the-database-connection-pool)
- [gene_sets.txt](../core/src/main/resources/sample_data/gene_sets.txt)