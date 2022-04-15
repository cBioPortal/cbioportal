# Pre-Build Steps

## Get the Latest Code

Make sure that you have cloned the last code, and make sure you are on the `master` branch:
```
	git clone https://github.com/cBioPortal/cbioportal.git
	git checkout master
```

## Prepare the log4j.properties File

This file configures logging for the portal.
An example file is available within GitHub:
```
    cd src/main/resources
    cp simplelogger.properties.EXAMPLE simplelogger.properties
```

If you don't create your own `simplelogger.properties`, maven will copy the EXAMPLE file to that location when it builds.
If `simplelogger.properties` already exists, it will just use that. This allows us to give you a working, versioned
log config, which you can then override easily.

## Create the cBioPortal MySQL Databases and User

You must create a `cbioportal` database and a `cgds_test` database within MySQL, and a user account with rights to access both databases.  This is done via the `mysql` shell.
```
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
```

[Next Step: Building From Source](Build-from-Source.md)
