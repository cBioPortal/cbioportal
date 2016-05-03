# Environment Variables

The following environment variable is referenced in this document and is required for successful portal setup:

* `CATALINA_HOME`: points to the home directory of your Apache Tomcat installation.

To make it available to your bash shell, add the following to your `.bash_profile`:

    export CATALINA_HOME=/path/to/tomcat

# Adding PORTAL_HOME to Tomcat

The `PORTAL_HOME` environment variable needs to be available to the `cbioportal.war` file which runs within the Tomcat server. To make it available to Tomcat, edit your Tomcat startup file (typically `$CATALINA_HOME/bin/catalina.sh`) and add the following line anywhere within this file (we typically add it near the `JAVA_OPTS` statements):

    export PORTAL_HOME= $CATALINA_HOME + "/webapps/cbioportal/WEB-INF/classes/"

# Set up the Database Connection Pool

Apache Tomcat provides the database database connection pool to the cBioPortal.  To setup a database connection pool managed by Tomcat, add the following line to `$CATALINA_HOME/conf/context.xml`, making sure that the properties match your system (note if using the MySQL Connector/J driver described below, the DRIVER_NAME would be com.mysql.jdbc.Driver) :

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

# Deploy the cBioPortal WAR

A tomcat server is usually started by running the following command:

    $CATALINA_HOME/bin/catalina.sh start

After the tomcat server has been started, to deploy the WAR file, run the following command:

    cp portal/target/cbioportal.war $CATALINA_HOME/webapps/

After doing this, you can look in the tomcat log file (`$CATALINA_HOME/logs/catalina.out`) to see if the portal has been proper deployed.  You should see something like:

    INFO: Deployment of web application archive /Users/ecerami/libraries/apache-tomcat-7.0.59/webapps/cbioportal.war has finished in 13,009 ms

# Verify the Web Application

Lastly, open a browser and go to:  

[http://localhost:8080/cbioportal/](http://localhost:8080/cbioportal/)

# Important

Each time you add new data or modify any code, you must redeploy the WAR file.

# Developer Tip

If you are actively developing for cBioPortal, you may notice OutOfMemory issues after you re-deploy your WAR file a few times.  Best option for dealing with this is increase your PermGen space settings.

To do so, create a file:  `$CATALINA_HOME/setenv.sh`, and add the following line:

    export CATALINA_OPTS="$CATALINA_OPTS -XX:MaxPermSize=256m"

# Gotcha:  Broken MySQL Pipe after long periods of inactivity

By default, MySQL will automatically close database connections after 8 hours of inactivity.  If no one accesses your instance of cBioPortal for 8 hours, users may be unable to access your data, and your log files may show "Broken Pipe" errors.  As described [here](http://juststuffreally.blogspot.com/2007/10/broken-pipes-with-tomcat-and-dbcp.html), and [here](http://stackoverflow.com/questions/20848219/tomcat-mysql-java-servlet-application-getting-500-error-after-some-hours-of-inac), the recommended solution is to add:

    testOnBorrow="true"
    validationQuery="SELECT 1"

to the JNDI configuration.

According to the stack overflow answer:

> The first, `testOnBorrow` tells the datasource to validate the connection before handing it back to the application. The second, `validationQuery` is the SQL which is used to validate the connection. Note the SQL of SELECT 1 is a valid value for MySQL databases.

The default JNDI settings above therefore include `testOnBorrow` and `validationQuery`, and you should therefore not see any broken pipe errors.  Should you see these errors, despite the settings, best to consult [here](http://juststuffreally.blogspot.com/2007/10/broken-pipes-with-tomcat-and-dbcp.html), and [here](http://stackoverflow.com/questions/20848219/tomcat-mysql-java-servlet-application-getting-500-error-after-some-hours-of-inac).

[Steps Complete: Return Home](README.md)