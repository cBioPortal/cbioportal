# Building from Source

## Building with Maven

While building, you must point the environment variable `PORTAL_HOME` to
the root directory containing the portal source code.

For example, run a command like the following if on macOS:
```
export PORTAL_HOME=/Users/ecerami/dev/cbioportal
```

To compile the cBioPortal source code, move into the source directory and
run the following maven command:

```
mvn -DskipTests clean install
```

After this command completes, you will find a `cbioportal.war` file
suitable for Apache Tomcat deployment in `portal/target/`.

[Next Step: Importing the Seed Database](Import-the-Seed-Database.md)
