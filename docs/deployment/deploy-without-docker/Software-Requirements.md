# Software Requirements

This page describes various system software required to run the cBioPortal.

## MySQL

The cBioPortal software should run properly on MySQL version 5.7.x. Versions higher than 5.7.x can cause an issue
while loading the database schema. Minor versions lower than 5.7.x will cause issues with persistent cache invalidation.
The software can be found and downloaded from the [MySQL website](https://www.mysql.com/).

On Ubuntu:  ```sudo apt-get install mysql-server```

## MongoDB

The session service uses MongoDB 3.6.6

## Java

cBioPortal requires Java 12 and above.  The software can be found and download from the [Oracle](https://www.oracle.com/us/technologies/java/overview/index.html) website.

On Ubuntu:  ```sudo apt-get install default-jdk```

## Apache Maven

The cBioPortal source code is an [Apache Maven](https://maven.apache.org/) driven project.  The software needs to be downloaded and installed prior to building the application from source code.  It can be found on the [Apache Maven](https://maven.apache.org/download.cgi) website. We are currently using version 3.9.5.

On Ubuntu:  ```sudo apt-get install maven```

## Git

You will need a git client to download the cBioPortal source code.

On Ubuntu:  ```sudo apt-get install git```
