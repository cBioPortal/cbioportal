# Versioning and Upgrade Guide

This document explains version compatibility and upgrade expectations for cBioPortal.

## Supported Versions

| Version | Status | Database | Development |
|------|------|------|------|
| v7 | Active | ClickHouse only | Active |
| v6 | Maintenance | MySQL | Security fixes only |

_MSK's cBioPortal deployment uses [ClickHouse Cloud](https://clickhouse.com/cloud) — see the [case study](https://clickhouse.com/blog/how-memorial-sloan-kettering-cancer-center-is-using-clickhouse-to-accelerate-cancer-research) for how ClickHouse accelerates cancer research._

## v7 Database Compatibility

Starting with **v7**, cBioPortal:

- Uses **ClickHouse as the sole database backend**
- Does **not support MySQL**
- Is **not compatible** with:
    - v6 portal configuration files
    - v6 study importer tools
    - Existing MySQL-based deployments

Upgrading from v6 to v7 requires **re-deploying the portal** and **re-importing studies** using v7-compatible tooling.

## v6 Maintenance Policy

v6 is maintained in the `maintenance-v6` branch:

- Only **important security fixes** will be released
- No bug fixes or new features
- No compatibility updates for new data sources

Users who cannot migrate immediately to v7 should remain on the latest v6 maintenance release.

## Choosing the Right Version

- **New deployments**: Use **v6** until the first stabilized v7 release
- **Existing v6 deployments**:
    - May stay on v6 short-term
    - Plan migration to v7
- **Development and contributions**:
    - Target `master` (v7)
    - Do not open PRs against v6 unless requested for security

### Migrating from v6 to v7

> ⚠️ **Important:** cBioPortal v7 uses ClickHouse as its database, which has a fundamentally different schema from the v6 MySQL database. **There is no in-place upgrade path from v6 to v7.** Existing v6 deployments must:

1. Set up a new v7 deployment using [Docker Compose](../deployment/docker/README.md)
2. **Reimport all studies** into the new v7 database using `metaImport.py`
3. Rebuild derived tables after all imports are complete

See the full [v6 to v7 Migration Guide](../Migration-v6-to-v7.md) for step-by-step instructions.

## Migration Notes

Detailed migration guides will be provided as v7 stabilizes. At a minimum, migration involves:

- Switching to ClickHouse
- Updating deployment configuration
- Re-importing studies using v7 tooling
