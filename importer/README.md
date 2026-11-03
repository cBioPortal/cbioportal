# cBioPortal importer

This directory contains the cBioPortal importer: the Java code that loads studies into the cBioPortal database, and the Python/Perl scripts (`scripts/`) that users run to validate and import data. It was previously maintained in the separate [cbioportal-core](https://github.com/cBioPortal/cbioportal-core) repository.

For documentation and usage instructions, see https://docs.cbioportal.org/data-loading/

If you want to contribute to the importer, see https://docs.cbioportal.org/data-loading/data-loading-for-developers/

## Building

```
mvn -f importer/pom.xml clean package
```

This produces `importer/core-<version>.jar`, a self-contained JAR that also bundles `scripts/` and `requirements.txt`.

## License

The importer is licensed under the [Apache License 2.0](../LICENSE). Versions of cbioportal-core released before October 2026 were licensed under AGPL-3.0.
