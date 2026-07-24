> ⚠️ **Legacy documentation (cBioPortal v6 and earlier).** This page predates cBioPortal v7, which replaced MySQL with ClickHouse and made Docker Compose the only officially supported deployment method. It is retained for historical reference only — do not rely on it for a v7 deployment. See the [current deployment documentation](/deployment/README.md) and the [v6-to-v7 migration guide](/Migration-v6-to-v7.md).

# Building from Source

## Building with Maven

To compile the cBioPortal source code, move into the root directory and run the following maven command.

```
mvn -DskipTests clean install
```

Note: cBioPortal 6.X requires Java 21