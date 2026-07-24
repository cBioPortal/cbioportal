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

- [Deploy without Docker — overview](/legacy/deployment/deploy-without-docker/README.md)
- [Software Requirements](/legacy/deployment/deploy-without-docker/Software-Requirements.md)
- [Pre-Build Steps](/legacy/deployment/deploy-without-docker/Pre-Build-Steps.md)
- [Building from Source](/legacy/deployment/deploy-without-docker/Build-from-Source.md)
- [Importing the Seed Database](/legacy/deployment/deploy-without-docker/Import-the-Seed-Database.md)
- [Deploying the Web Application](/legacy/deployment/deploy-without-docker/Deploying.md)
- [Loading a Sample Study](/legacy/deployment/deploy-without-docker/Load-Sample-Cancer-Study.md)

### Other v6 deployment topics

- [Standalone Deployment](/legacy/deployment/standalone/README.md)
- [Authenticating and Authorizing Users using Keycloak in Docker](/legacy/deployment/docker/using-keycloak.md)
- [Deploy with Kubernetes](/legacy/deployment/kubernetes/README.md)
- [Uninstall Docker cBioPortal](Uninstall-Docker-cBioPortal.md)

### v6 maintenance and development

- [Testing](Testing.md)
- [Importer Tool](Importer-Tool.md) — superseded by [metaImport.py](../data-loading/README.md)
- [Updating your cBioPortal installation](Updating-your-cBioPortal-installation.md) — the MySQL `migrate_db.py` workflow
- [Update genes and gene aliases](Updating-gene-and-gene_alias-tables.md)
- [Deployment Procedure](/legacy/development/Deployment-Procedure.md)
- [Backend Code Organization](/legacy/development/Backend-Code-Organization.md)
- [portal.properties Reference](/legacy/deployment/customization/portal.properties-Reference.md) — renamed to `application.properties` in v6
- [Entity-relationship Diagram](/legacy/development/cBioPortal-ER-Diagram.md) — depicts the v6 MySQL schema (db schema 2.13.1)

## Recently retired (v7 cleanup)

- [Authenticating Users via SAML](deployment/authorization-and-authentication/Authenticating-Users-via-SAML.md) — pre-v7 OneLogin/Tomcat SAML setup
- [Authenticating and Authorizing Users via Keycloak](deployment/authorization-and-authentication/Authenticating-and-Authorizing-Users-via-keycloak.md) — pre-v7 Keycloak/SAML auth setup
- [Database Versioning](development/Database-Versioning.md) — MySQL-era schema-versioning policy
- [MSK Maintenance](MSK-Maintenance.md) — MSK-internal ops runbook
