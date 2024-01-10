# MSK Maintenance (In Progress)
We provide our cBioPortal's maintenance page publicly in the hope that it may be useful to others

## Database Migration
In the database migration process, we are going to have two main steps: building importers and updating database scheme.

### Building Importers
- Codebases:
    - [cbioportal](https://github.com/cBioPortal/cbioportal)
    - [genome nexus annotation pipeline](https://github.com/genome-nexus/genome-nexus-annotation-pipeline)
    - [pipelines](https://github.com/knowledgesystems/pipelines)
- Building single importer:
    - Take the cbioportal hash and add it to genome nexus annotation pipeline POM -> push to fork
    - Take the cbioportal hash and new genome nexus annotation pipeline hash (from above) and propagate to pipelines POM; also update db version if db migration is part of change
    - Login to pipelines server
    - Sets general env variables needed for building
        - Run `sh /data/portal-cron/scripts/automation-environment.sh`
    - Specific to cbioportal build, looks for properties and xml files off of $PORTAL_HOME
        - Run `export PORTAL_HOME=/data/portal-cron/git-repos/cbioportal`
    - Make sure the local cbioportal codebase is on the correct git hash at `/data/portal-cron/git-repos/cbioportal`
    - Specify importer to be build
        - Copy importer properties to `/data/portal-cron/git-repos/cbioportal/src/main/resources` (e.g. triage `/data/portal-cron/git-repos/pipelines-configuration/properties/import-triage/*`)
    - Navigate to pipelines folder
        - `cd /data/portal-cron/git-repos/pipelines`
    - Build importer
        - mvn clean install -DskipTests
- Build multiple importers
    - Take the cbioportal hash and add it to genome nexus annotation pipeline POM -> push to fork
    - Take the cbioportal hash and new genome nexus annotation pipeline hash (from above) and propagate to pipelines POM; also update db version if db migration is part of change
    - Login to pipelines server
    - Sets general env variables needed for building
        - Run `sh /data/portal-cron/scripts/automation-environment.sh`
    - Specific to cbioportal build, looks for properties and xml files off of $PORTAL_HOME
        - Run `export PORTAL_HOME=/data/portal-cron/git-repos/cbioportal`
    - Make sure the local cbioportal codebase is on the correct git hash at `/data/portal-cron/git-repos/cbioportal`
    - Go to build importer jars folder
        - `cd /data/portal-cron/git-repo/pipelines-configuration/build-importer-jars`
    - (Optional) Remove existing jars
        - `rm *.jar`
    - Build all importers at once (build all importers except cmo-pipelines)
        - `sh buildproductionjars.sh -sd=true -sgp=true -b=importers`
        - Available parameters:
          - [--cbioportal-git-hash|-cgh=<cbioportal_commit_hash>]
          - [--skip-deployment|-sd=<true|false>]
          - [--skip-git-pull|-sgp=<true|false>]
          - [--build|-b=<build_specifier>], build_specifier should be one of the following
            - all (build for all artifacts)
            - importers (all importers except cmo-pipelines)
            - cmo-pipelines (cmo-pipelines artifacts only)
            - triage-cmo-importer
            - msk-dmp-importer
            - msk-cmo-importer
            - public-importer
            - genie-aws-importer
            - genie-archive-importer
            - hgnc-importer

### Updating Database Scheme
Database needs to be updated one by one, we have four main databases: triage, private, genie, and public. Take triage database as an example.
- Migrate one database (e.g. triage)
    - SSH into pipeline server
    - Checkout to the commit that contains the latest database scheme
    - Check if property sets up correctly to the right database (triage)
        - `vi /data/portal-cron/git-repos/cbioportal/src/main/resources/application.properties`
    - Move to directory
        - `cd /data/portal-cron/git-repos/cbioportal`
    - Run database migration using script:
        - `python3 core/src/main/scripts/migrate_db.py --properties-file src/main/resources/application.properties --sql db-scripts/src/main/resources/migration.sql`
    - Monitor the DB migration process and look for possible errors
    - Access database and verify the DB scheme is updated
