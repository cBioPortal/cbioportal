# Building from Source

## [Building with Maven](building-with-maven)

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

After this command completes, you will find a `cbioportal.war` file suitable
for Apache Tomcat deployment in `portal/target/`. It is not neccessary to
install Tomcat yourself, since a command line runnable version of Tomcat is
provided as a dependency in `portal/target/dependency/webapp-runner.jar`.

However, if you will be deploying to a standalone Tomcat installation, and
if you have configured Tomcat to use the Redisson client for user session
management, you should expect a clash between the Redisson client being
used for session management and the Redisson client which is embedded in
the cbioportal.war file for the optional "redis" persitence layer caching
mode. In this case, you should avoid using the "redis" option for the portal
property `persistence.cache_type` and you should prevent the Redisson
client from being packaged in cbioportal.war by building with this command
instead:

##### alternative for standalone tomcat deployments which use redis session management
```
mvn -Dexclude-redisson -DskipTests clean install
```

[Next Step: Importing the Seed Database](Import-the-Seed-Database.md)
