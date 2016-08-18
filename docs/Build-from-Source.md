# Building from Source

## Building with Maven

To compile the cBioPortal source code run the following maven command:

```
mvn -DskipTests clean install
```

After this command completes, you will find a `cbioportal.war` file suitable for Apache Tomcat deployment in `$PORTAL_HOME/portal/target/`.  

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

Then, rebuild with Maven.

[Next Step: Importing the Seed Database](Import-the-Seed-Database.md)
