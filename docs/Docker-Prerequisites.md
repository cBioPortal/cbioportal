# Docker Prerequisites

Docker provides a way to run applications securely isolated in a container, packaged with all its dependencies and libraries.
To learn more on Docker, kindly refer here: [What is Docker?](https://www.docker.com/what-docker).

## 1. Install Docker

First, make sure that you have the latest version of Docker installed on your machine. [Get latest version](https://www.docker.com/products/overview#/install_the_platform)

## 2. Download Seed DB

The latest cBioPortal seed file and database schema are available from [cBioPortal Datahub](https://github.com/cBioPortal/datahub/tree/master/seedDB).

## 3. Prepare Configuration Files

You will need the following configuration files.

- [portal.properties Reference](Pre-Build-Steps.md#prepare-property-files).
- [Download portal.properties.EXAMPLE](../src/main/resources/portal.properties.EXAMPLE)
- [log4j.properties  Reference](Pre-Build-Steps.md#prepare-the-log4jproperties-file).
- [Download log4j.properties.EXAMPLE](../src/main/resources/log4j.properties.EXAMPLE)
- [settings.xml Reference](Pre-Build-Steps.md#create-a-maven-settings-file)
- [context.xml Reference](Deploying.md#set-up-the-database-connection-pool)
