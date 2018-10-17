# Deploying the Web Application

## Set Environment Variables

The following environment variable is referenced in this document and is required for successful portal setup:

* `CATALINA_HOME`: points to the home directory of your Apache Tomcat installation.

To make it available to your bash shell, add the following to your `.bash_profile`:

    export CATALINA_HOME=/path/to/tomcat

Note:  If you are following the [recommended Ubuntu instructions](https://www.digitalocean.com/community/tutorials/how-to-install-apache-tomcat-8-on-ubuntu-14-04):  you should set ```export CATALINA_HOME=/opt/tomcat```

## Prepare the global configuration file

The portal is configured using a global configuration file, `portal.properties`.
An example file is available in the `src/main/resources` folder.
Use it as a template to create your own:

    cd src/main/resources
    cp portal.properties.EXAMPLE $HOME/cbioportal/portal.properties

For more information about the `portal.properties` file,
see the [reference](portal.properties-Reference.md) page.

## Set environment variables for Tomcat

The `PORTAL_HOME` environment variable needs to be available to
the `cbioportal.war` file which runs within the Tomcat server,
so that it can find the configuration file. To make it available to Tomcat,
edit your Tomcat startup file (typically `$CATALINA_HOME/bin/setenv.sh`)
and add a line like the following, pointing it to the folder containing
`portal.properties`:

    export PORTAL_HOME=/Users/johndoe/cbioportal

In additon, add a line to make Tomcat pass `-Dauthenticate=false`
as a JVM argument, or replace the word ‘false’ by the method to use:

    CATALINA_OPTS='-Dauthenticate=false'

## Add the MySQL JDBC Driver to Apache Tomcat

A proper JDBC driver will also need to be accessible by Apache Tomcat.  If using MySQL, the [Connector/J](http://dev.mysql.com/downloads/connector/j/) driver jar file should be placed in `$CATALINA_HOME/lib`.

More information on configuring Apache Tomcat connection pooling can be found [here](http://tomcat.apache.org/tomcat-7.0-doc/jndi-datasource-examples-howto.html).

***We have reports that the Tomcat package that comes with (at least) Ubuntu 14.04 cannot handle the connection pool from resources.  If you are encountering this is, we suggest you [download the Tomcat archive from Apache and install from there](https://www.digitalocean.com/community/tutorials/how-to-install-apache-tomcat-8-on-ubuntu-14-04).***

## Set up the Database Connection Pool

Apache Tomcat provides the database database connection pool to the cBioPortal.  To setup a database connection pool managed by Tomcat, add the following line to `$CATALINA_HOME/conf/context.xml`, making sure that the properties match your system:

     <Context>
         ...
         <Resource name="jdbc/cbioportal" auth="Container" type="javax.sql.DataSource"
            maxActive="100" maxIdle="30" maxWait="10000"
            username="cbio_user" password="somepassword" driverClassName="com.mysql.jdbc.Driver"
            connectionProperties="zeroDateTimeBehavior=convertToNull;"
            testOnBorrow="true"
            validationQuery="SELECT 1"
            url="jdbc:mysql://localhost:3306/cbioportal"/>
    ...
    </Context>

## Deploy the cBioPortal WAR

A tomcat server is usually started by running the following command:

	$CATALINA_HOME/bin/catalina.sh start

or, if you are following the [recommended Ubuntu instructions](https://www.digitalocean.com/community/tutorials/how-to-install-apache-tomcat-8-on-ubuntu-14-04)

	sudo initctl restart tomcat

After the tomcat server has been started, to deploy the WAR file, run the following command:

```
sudo cp portal/target/cbioportal-*-SNAPSHOT.war $CATALINA_HOME/webapps/cbioportal.war
```

After doing this, you can look in the tomcat log file (`$CATALINA_HOME/logs/catalina.out`) to see if the portal has been proper deployed.  You should see something like:

    INFO: Deployment of web application archive /Users/ecerami/libraries/apache-tomcat-7.0.59/webapps/cbioportal.war has finished in 13,009 ms

## Verify the Web Application

Lastly, open a browser and go to:  
<http://localhost:8080/cbioportal/>

## Important

- Each time you modify any java code, you must recompile and redeploy the WAR file.
- Each time you modify any properties (see customization options), you must restart tomcat.
- Each time you add new data, you must restart tomcat.

## Developer Tip

If you are actively developing for cBioPortal, you may notice OutOfMemory issues after you re-deploy your WAR file a few times.  Best option for dealing with this is increase your PermGen space settings.

To do so, create a file:  `$CATALINA_HOME/setenv.sh`, and add the following line:

    export CATALINA_OPTS="$CATALINA_OPTS -XX:MaxPermSize=256m"

## Gotcha:  Broken MySQL Pipe after long periods of inactivity

By default, MySQL will automatically close database connections after 8 hours of inactivity.  If no one accesses your instance of cBioPortal for 8 hours, users may be unable to access your data, and your log files may show "Broken Pipe" errors.  As described [here](http://juststuffreally.blogspot.com/2007/10/broken-pipes-with-tomcat-and-dbcp.html), and [here](http://stackoverflow.com/questions/20848219/tomcat-mysql-java-servlet-application-getting-500-error-after-some-hours-of-inac), the recommended solution is to add:

    testOnBorrow="true"
    validationQuery="SELECT 1"

to the JNDI configuration.

According to the stack overflow answer:

> The first, `testOnBorrow` tells the datasource to validate the connection before handing it back to the application. The second, `validationQuery` is the SQL which is used to validate the connection. Note the SQL of SELECT 1 is a valid value for MySQL databases.

The default JNDI settings above therefore include `testOnBorrow` and `validationQuery`, and you should therefore not see any broken pipe errors.  Should you see these errors, despite the settings, best to consult [here](http://juststuffreally.blogspot.com/2007/10/broken-pipes-with-tomcat-and-dbcp.html), and [here](http://stackoverflow.com/questions/20848219/tomcat-mysql-java-servlet-application-getting-500-error-after-some-hours-of-inac).

[Next Step: Loading a Sample Study](Load-Sample-Cancer-Study.md)
