
## Building with Maven

First, set your `$PORTAL_HOME` environment variable to point at your cbioportal.org check-out.
(so that `$PORTAL_HOME/src/main/resources/portal.properties` exists and is properly populated)

You can then compile the cBioPortal source code with the following maven command:

```
mvn -DskipTests clean install
```

After this command completes, you will find a `cbioportal.war` file suitable for Apache Tomcat deployment in `$PORTAL_HOME/portal/target/`.  In addition, you will find `core-1.0-SNAPSHOT.jar` in `$PORTAL_HOME/core/target/`, which is required when using our [Importer Tool](Importer-Tool.md) to import study data. 

If you have placed your properties in the default location, e.g. `src/main/resources`, your `PORTAL_HOME` must point to this exact directory.  For example:

    export PORTAL_HOME="/Users/ecerami/dev/cbioportal/src/main/resources"

#### Note for those running Tomcat6

The current version of the code is using an optional feature which is only available if you are running Tomcat7. If you are not, you should remove the following lines from the file portal/src/main/webapp/WEB-INF/web.xml :
```
   <filter>
     <filter-name>CorsFilter</filter-name>
     <filter-class>org.apache.catalina.filters.CorsFilter</filter-class>
   </filter>

   <filter-mapping>
     <filter-name>CorsFilter</filter-name>
     <url-pattern>/*</url-pattern>
   </filter-mapping>
```
Rebuild with Maven if necessary.

[Next Step: Import the Seed Database](Import-the-Seed-Database.md)