# Pre-Build Steps

## Get the Latest Code

Make sure that you have cloned the last code, and make sure you are on the ```master``` branch:

	git clone https://github.com/cBioPortal/cbioportal.git
	git checkout master

## Prepare the log4j.properties File

This file configures logging for the portal.
An example file is available within GitHub:

    cd src/main/resources
    cp log4j.properties.EXAMPLE log4j.properties

But you must update the following lines with paths that make sense for the systems your build should target.

    log4j.appender.a.rollingPolicy.FileNamePattern = ${catalina.base}/logs/public-portal.log.%d.gz
    log4j.appender.a.File = ${catalina.base}/logs/public-portal.log

## Create the cBioPortal MySQL Databases and User

You must create a `cbioportal` database and a `cgds_test` database within MySQL, and a user account with rights to access both databases.  This is done via the `mysql` shell.

    > mysql -u root -p
    Enter password: ********

    Welcome to the MySQL monitor.  Commands end with ; or \g.
    Your MySQL connection id is 64
    Server version: 5.6.23 MySQL Community Server (GPL)

    Copyright (c) 2000, 2015, Oracle and/or its affiliates. All rights reserved.

    mysql> create database cbioportal;
    Query OK, 1 row affected (0.00 sec)

    mysql> create database cgds_test;
    Query OK, 1 row affected (0.00 sec)

    mysql> CREATE USER 'cbio_user'@'localhost' IDENTIFIED BY 'somepassword';
    Query OK, 0 rows affected (0.00 sec)

    mysql> GRANT ALL ON cbioportal.* TO 'cbio_user'@'localhost';
    Query OK, 0 rows affected (0.00 sec)

    mysql> GRANT ALL ON cgds_test.* TO 'cbio_user'@'localhost';
    Query OK, 0 rows affected (0.00 sec)

    mysql>  flush privileges;
    Query OK, 0 rows affected (0.00 sec)

## Create a Maven Settings File

In order to access your database, you must create a Maven settings.xml file, and populate it with your database username and password.

The file must be located under:  `~/.m2`, and named:  `settings.xml`.

A sample file is shown below:

    <settings>
      <servers>
        <server>
          <id>settingsKey</id>
          <username>cbio_user</username>
          <password>somepassword</password>
        </server>
      </servers>
    </settings>

[Next Step: Building From Source](Build-from-Source.md)
