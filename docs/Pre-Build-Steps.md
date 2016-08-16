# Pre-Build Steps

## Get the Latest Code

Make sure that you have cloned the last code, and make sure you are on the ```master``` branch:

	git clone https://github.com/cBioPortal/cbioportal.git
	git checkout master

## Prepare Property Files

The portal requires two properties files:  one for global configuration (`portal.properties`) and one for logging (`log4j.properties`).  Example files are available within GitHub, but you must take the following steps to prepare them.

    cd src/main/resources
    cp portal.properties.EXAMPLE portal.properties
    cp log4j.properties.EXAMPLE log4j.properties

For more information about the `portal.properties` file, see the following [reference](portal.properties-Reference.md) page.

## Prepare the log4j.properties File

Update the following lines with paths that make sense for your local system.

    log4j.appender.a.rollingPolicy.FileNamePattern = /srv/www/sander-tomcat/tomcat6/logs/public-portal.log.%d.gz
    log4j.appender.a.File = /srv/www/sander-tomcat/tomcat6/logs/public-portal.log

<a name='prepare_database'>

##  Create the cBioPortal MySQL Databases and User

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

## Set the PORTAL_HOME Variable

Prior to building, you must specify an environment variable for `PORTAL_HOME`.  This must point to the root directory containing the portal source code.

For example, add the following to your `.bash_profile`:

    export PORTAL_HOME=/Users/ecerami/dev/cbioportal


[Next Step: Building From Source](Build-from-Source.md)
