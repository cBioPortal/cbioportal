# Migration Guide

This page describes various changes deployers will need to make as they deploy newer versions of the portal.

## v6.0.23 -> v6.0.24

- Redis HTTP Session:
    - As part of release [v6.0.24](https://github.com/cBioPortal/cbioportal/releases/tag/v6.0.24), Spring Boot has been upgraded to v3.4 which includes some changes to HTTP sessions. It won't affect most standard single-node deployments of cBioPortal, but if you are running multiple replicas and use Redis to manage HTTP sessions, you will need to clear and restart the Redis session server after upgrading your portal. 

## v5 -> v6

- Override Spring Application Properties:
  - The process of overriding properties has been updated for example previously users could do the following `java -Xms2g -Xmx4g -Dauthenticate=saml -jar webapp-runner.jar`.
  Now users must pass Spring Application properties after the `.jar` and replace `-Dauthenticate=false` with `--authenticate=false`. 
  For example `java -Xms2g -Xmx4g -jar cbioportal/target/cbioportal-exec.jar --spring.config.location=cbioportal/application.properties --authenticate=false`
- `portal.properties` migration needed:
  - `portal.properties` has been renamed to `application.properties`. This is the Spring Boot default name 
  - `authenticate` values of `googleplus`, `social_auth_google` and `social_auth_microsoft` have been replaced by `optional_oauth2`
    - If you used this property before without authorization (unlikely, only the public cBioPortal instance uses this), add the property `always_show_study_group=PUBLIC` and confirm  that all studies in your database you'd like to be be public have `GROUPS` values set to `PUBLIC`
- `Redis HTTP Session`
  - To disable redis session (Must be disabled if redis is not setup) `spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration`
  - To enable `spring.data.redis.host=localhost` `spring.data.redis.port=6379`
- `Security` changes:
  - Properties used to configure Security have changed see [security.properties.EXAMPLE](deployment/customization/security.properties-Reference.md)
  - `SAML` changes:
    - need to generate new keys (`.jks` no longer works)
       - if you have an existing client, update saml keys with newly generate keys
    - if you have an existing client, update redirect_uri to be without port 443
    - You can now use a URL here, instead of metadata XML: `spring.security.saml2.relyingparty.registration.cbio-saml-idp.assertingparty.metadata-uri`

## v5.3 -> v5.4

- Remove `db.host` and `db.portal_db_name` and `db.use_ssl` properties from the _portal.properties_ file or JVM
  parameters. Update property `db.connection_string` to encode the hostname, port, database and other parameters
  according to [Database Settings](deployment/customization/portal.properties-Reference.md#Database-Settings) documentation and pass via
  _portal.properties_ file or as JVM parameter.

## v4 -> v5

- All fusion profiles are now required to be migrated to structural variant format. One can use
  this [migration tool](https://github.com/cBioPortal/datahub-study-curation-tools/tree/master/fusion-to-sv-converter)
  to migrate the fusion files.
- All fusion files on [datahub](https://github.com/cBioPortal/datahub) were migrated to the structural variant format
  and their molecular profile ids were renamed from `{study_id}_fusion` to `{study_id}_structural_variants`. If you are
  using these datahub files one would need to re-import them.
- Study view user setting will be outdated after migration, please follow `Clear Study View User settings` section
  in [Session Service Management](Session-Service-Management.md#Clear-Study-View-User-settings)
- The new default set of transcripts for each gene has changed from `uniprot` to `mskcc`. See
  the [Mutation Data Annotation Section](./mutation-data-transcript-annotation.md) for more details. To keep the old set
  of default transcripts add the property `genomenexus.isoform_override_source=uniprot`
  to [the properties file](https://docs.cbioportal.org/deployment/customization/portal.properties-reference/#properties).

See the [v5.0.0 release notes](https://github.com/cBioPortal/cbioportal/releases/tag/v5.0.0) for more details.

## v3 -> v4

- Introduces `logback` package for logging. If you don't have any custom log4j.properties file, no changes are necessary
- Cleans up several old databases ([PR](https://github.com/cBioPortal/cbioportal/pull/9360)). In theory the migration
  should be seamless, since the docker container detects an old database version and migrates it automatically.

See the [v4.0.0 release notes](https://github.com/cBioPortal/cbioportal/releases/tag/v4.0.0) for more details.

## v2 -> v3

- Session service is now required to be set up. You can't run it without session service. The recommended way to run
  cBioPortal is to use the Docker Compose instructions.

## v1 -> v2

- Changes cBioPortal to a Single Page App (SPA) written in React, Mobx and bootstrap that uses a REST API. It shouldn't
  change anything for a deployer.
