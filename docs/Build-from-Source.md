# Building from Source

## Building with Maven

To compile the cBioPortal source code run the following maven command:

```
mvn -DskipTests clean install
```

After this command completes, you will find a `cbioportal.war` file suitable for Apache Tomcat deployment in `$PORTAL_HOME/portal/target/`.  

[Next Step: Importing the Seed Database](Import-the-Seed-Database.md)
