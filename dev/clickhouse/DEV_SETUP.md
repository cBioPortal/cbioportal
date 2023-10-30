# Setup for development on Clickhouse integration

## Pre-requisites

1. cBioPortal database:

- provisioned with one or more study in the _cbioportal_ database.
- user: _cbio_ password: _P@ssword1_ with all permissions on the _cbioportal_ database.
- available on port 3306 on the host system.

2. System with docker and docker compose installed.

## Clickhouse provisioning

All commands start from the root repository location.

1. Start cBioPortal database.
2. Create MySQL views in the cBioPortal database by running the commands
   in [cbio_database_views.sql](mysql_provisioning/cbio_database_views.sql).
3. Start Clickhouse (provisioned automatically from MySQL).

```
cd ./dev/clickhouse
docker compose up -d
```

This will start a Clickhouse instance that is available on the host system:
- port: _8123_
- database: _cbioportal_
- username: _cbio_
- password: _P@ssword1_

## cBioPortal integration with Clickhouse

To connect the Clickhouse database to the cBioportal instance, set the following properties in the portal.properties file:

```
db.clickhouse.user=cbio
db.clickhouse.password=P@ssword1
db.clickhouse.connection_string=jdbc:clickhouse://clickhouse:8123/cbioportal?useSSL=false&allowPublicKeyRetrieval=true&allowLoadLocalInfile=true
```