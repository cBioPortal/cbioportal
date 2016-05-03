# Get the Latest Code

Make sure that you have cloned the last code.

These documents also **only apply to code obtained from the master branch**.

# Prepare the Property File

The portal requires two properties files:  one for global configuration (`portal.properties`) and one for logging (`log4j.properties`).  Example files are available within GitHub, but you must take the following steps to prepare them.

    cd src/main/resources
    cp portal.properties.EXAMPLE portal.properties
    cp log4j.properties.EXAMPLE log4j.properties

For more information about the portal.properties file, see the following [reference](portal.properties-Reference.md) page.

# Prepare the log4j.properties File

Update the following lines with paths that make sense for your local system.

    log4j.appender.a.rollingPolicy.FileNamePattern = /srv/www/sander-tomcat/tomcat6/logs/public-portal.log.%d.gz
    log4j.appender.a.File = /srv/www/sander-tomcat/tomcat6/logs/public-portal.log

<a name='prepare_database'>
#  Create the cBioPortal MySQL Databases and User

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

# Create a Maven Settings File

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

# Add the MySQL JDBC Driver to Apache Tomcat

A proper JDBC driver will also need to be accessible by Apache Tomcat.  If using MySQL, the [Connector/J](http://dev.mysql.com/downloads/connector/j/) driver jar file should be placed in `$CATALINA_HOME/lib`.

More information on configuring Apache Tomcat connection pooling can be found [here](http://tomcat.apache.org/tomcat-7.0-doc/jndi-datasource-examples-howto.html).

***We have reports that the Tomcat package that comes with (at least) Ubuntu 14.04 cannot handle the connection pool from resources.  If you are encountering this is, we suggest you download the Tomcat archive from Apache and install from there.***

# Configure the Database Connection Pool Resource to Apache Tomcat

Apache Tomcat provides the database database connection pool to the cBioPortal. To setup a database connection pool managed by Tomcat, add the following line to $CATALINA_HOME/conf/context.xml, where USER, PASSWORD, DRIVER_NAME, LOCALHOST, and DATABASE_NAME are properties local to your environment (note if using the MySQL Connector/J driver, the DRIVER_NAME would be com.mysql.jdbc.Driver):

    <Context>

     <Resource name="jdbc/cbioportal" auth="Container" type="javax.sql.DataSource"
      maxActive="100" maxIdle="30" maxWait="10000"
      username="USER" password="PASSWORD" driverClassName="DRIVER_NAME"
      connectionProperties="zeroDateTimeBehavior=convertToNull;"
      url="jdbc:mysql://LOCALHOST:3306/DATABASE_NAME"/>
      ...
      ...
    </Context>

# Set the PORTAL_HOME Variable

Prior to building, you must specify an environment variable for `PORTAL_HOME`.  This must point to the root directory containing the portal source code.

For example, add the following to your `.bash_profile`:

    export PORTAL_HOME=/Users/ecerami/dev/cbioportal

This environment variable should also be set within Apache Tomcat.  Edit your Tomcat startup file (typically $CATALINA_HOME/bin/catalina.sh) and add the export statement anywhere within this file (we typically add it near the JAVA_OPTS statements).  We have had reports that on RHEL 7, this export had to be placed at the top of /usr/libexec/tomcat/preamble.

[Next Step: Build From Source](Build-from-Source.md)