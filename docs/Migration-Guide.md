# Migration Guide
This page describes various changes deployers will need to make as they deploy newer versions of the portal. 

## v4 -> v5
- All fusion profiles are now required to be migrated to structural variant format. One can use this [migration tool](https://github.com/cBioPortal/datahub-study-curation-tools/tree/master/fusion-to-sv-converter) to migrate the fusion files.
- All fusion files on [datahub](https://github.com/cBioPortal/datahub) were migrated to the structural variant format and their molecular profile ids were renamed from `{study_id}_fusion` to `{study_id}_structural_variants`. If you are using these datahub files one would need to re-import them.
- Study view user setting will be outdated after migration, please follow `Clear Study View User settings` section in [Session Service Management](Session-Service-Management.md#Clear-Study-View-User-settings)
- The new default set of transcripts for each gene has changed from `uniprot` to `mskcc`. See also the [Mutation Data Annotation Section](./mutation-data-transcript-annotation.md) for more details on this. To keep the old set of default transcripts one can set the property to `genomenexus.isoform_override_source=uniprot`.

See the [v5.0.0 release notes](https://github.com/cBioPortal/cbioportal/releases/tag/v5.0.0) for more details.

## v3 -> v4
- Introduces `logback` package for logging. If you don't have any custom log4j.properties file, no changes are necessary
- Cleans up several old databases ([PR](https://github.com/cBioPortal/cbioportal/pull/9360)). In theory the migration should be seamless, since the docker container detects an old database version and migrates it automatically.

See the [v4.0.0 release notes](https://github.com/cBioPortal/cbioportal/releases/tag/v4.0.0) for more details.

## v2 -> v3
- Session service is now required to be set up. You can't run it without session service. The recommended way to run cBioPortal is to use the Docker Compose instructions.

## v1 -> v2
- Changes cBioPortal to a Single Page App (SPA) written in React, Mobx and bootstrap that uses a REST API. It shouldn't change anything for a deployer.
