> ⚠️ **Outdated Documentation:** This page was written for an earlier version of cBioPortal. As of v7, these instructions are out of date and must be revised before relying on them. Docker Compose is the only officially supported deployment method for v7. See [Deployment](../README.md).

# Building from Source

## Building with Maven

To compile the cBioPortal source code, move into the root directory and run the following maven command.

```
mvn -DskipTests clean install
```

Note: cBioPortal 6.X requires Java 21
