# Legacy Documentation (v6 and earlier)

> ⚠️ **These pages are retained for historical reference only.** They were written for
> cBioPortal v6 and earlier, when MySQL was the database backend. As of v7, ClickHouse is
> the sole supported database and Docker Compose is the only officially supported
> deployment method.
>
> Do not follow these instructions for a v7 deployment. See
> [Deployment](../deployment/README.md) and the
> [v6 to v7 Migration Guide](../Migration-v6-to-v7.md) instead.

## Why these pages still exist

Existing v6 deployments remain on the `maintenance-v6` branch and receive security fixes.
These pages document how those installations were built and maintained. They are not
updated for v7 and their commands, paths, and configuration keys may no longer be valid.

## Contents

### Deploying without Docker (v6)

- [Deploy without Docker — overview](deploy-without-docker-README.md)
- [Software Requirements](Software-Requirements.md)
- [Pre-Build Steps](Pre-Build-Steps.md)
- [Building from Source](Build-from-Source.md)
- [Importing the Seed Database](Import-the-Seed-Database.md)
- [Deploying the Web Application](Deploying.md)
- [Loading a Sample Study](Load-Sample-Cancer-Study.md)

### Other v6 deployment topics

- [Standalone Deployment](standalone-README.md)
- [Authenticating and Authorizing Users using Keycloak in Docker](using-keycloak.md)
- [Deploy with Kubernetes](kubernetes-README.md)
- [Uninstall Docker cBioPortal](Uninstall-Docker-cBioPortal.md)

### v6 maintenance and development

- [Testing](Testing.md)
- [Update genes and gene aliases](Updating-gene-and-gene_alias-tables.md)
- [Deployment Procedure](Deployment-Procedure.md)
- [Backend Code Organization](Backend-Code-Organization.md)
- [portal.properties Reference](portal.properties-Reference.md) — renamed to `application.properties` in v6
