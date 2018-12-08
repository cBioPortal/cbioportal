# Hardware and Software Requirements

This page describes various system software required to run the cBioPortal.

## Hardware

Hardware requirements will vary depending on the volume of users you anticipate will access your cBioPortal instance.  As a guideline, we run [cbioportal.org](http://www.cbioportal.org) on an Intel Xeon 2.60GHz/8-core processor with 192GB of RAM. Minimally, 2GB of RAM is needed to run a cBioPortal instance. The public database consumes ~50 GB of disk space. If you do not plan to import public studies, depending on the size of your private data, 10GB of disk space may be sufficient.

## MySQL

The cBioPortal software should run properly on MySQL version 5.x.x; to avoid a known issue while loading the database schema, 5.7.x or lower is recommended.
The software can be found and downloaded from the [MySQL website](http://www.mysql.com/).

On Ubuntu:  ```sudo apt-get install mysql-server```

## Java

As of this writing, the cBioPortal can be compiled and run from Java 8.0 and above.  The software can be found and download from the [Oracle](http://www.oracle.com/us/technologies/java/overview/index.html) website.

On Ubuntu:  ```sudo apt-get install default-jdk```

## Apache Tomcat

As of this writing, the cBioPortal runs properly on Apache Tomcat version 8 and above.  The software can be found and downloaded from the [Apache Tomcat website](http://tomcat.apache.org).

On Ubuntu:  [See Recommended Installation Instructions](https://www.digitalocean.com/community/tutorials/how-to-install-apache-tomcat-8-on-ubuntu-14-04)

## Apache Maven

The cBioPortal source code is an [Apache Maven](https://maven.apache.org/) driven project.  The software needs to be downloaded and installed prior to building the application from source code.  It can be found on the [Apache Maven](https://maven.apache.org/download.cgi) website. We are currently using version 3.5.4.

On Ubuntu:  ```sudo apt-get install maven```

## Git

You will need a git client to download the cBioPortal source code.

On Ubuntu:  ```sudo apt-get install git```

[Next Step: Pre-Build Steps](Pre-Build-Steps.md)
