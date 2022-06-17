:warning:  ***Warning: this way of loading data is deprecated. See [Data loading](Data-Loading.md) for the recommended method.***

The page describes how to import data into the cBioPortal using Python scripts found in our [scripts](https://github.com/cBioPortal/cbioportal/tree/stable/core/src/main/scripts) directory.  The follow assumptions have been made:

1. The cBioPortal software has been correctly [built from source](/deployment/deploy-without-docker/Build-from-Source.md).
2. The data to import is in the proper [File Format](/File-Formats.md).
3. The `PORTAL_HOME` environment variable has been properly defined.

## Import Cancer Type

The following command is used to import cancer type metadata into the cBioPortal database:

```
$PORTAL_HOME/core/src/main/scripts/cbioportalImporter.py --jvm-args "-Dspring.profiles.active=dbcp -cp $PORTAL_HOME/core/target/core-1.0-SNAPSHOT.jar" --command import-cancer-type --meta-filename /path-to-cancer-type-file/cancer_type.txt
```

The [cancer_type.txt](File-Formats.md#cancer-study) file should conform to the file format describe on the File Formats wiki page.

## Import Cancer Study

The following command is used to import cancer study metadata into the cBioPortal database:

```
$PORTAL_HOME/core/src/main/scripts/cbioportalImporter.py --jvm-args "-Dspring.profiles.active=dbcp -cp $PORTAL_HOME/core/target/core-1.0-SNAPSHOT.jar" --command import-study --meta-filename /path-to-meta_study-file/meta_study.txt
```

The [meta_study.txt](File-Formats.md#cancer-study) file should conform to the file format describe on the File Formats wiki page.

## Remove Cancer Study

The following command is used to remove a cancer study from the cBioPortal database:

```
$PORTAL_HOME/core/src/main/scripts/cbioportalImporter.py --jvm-args "-Dspring.profiles.active=dbcp -cp $PORTAL_HOME/core/target/core-1.0-SNAPSHOT.jar" --command remove-study --meta-filename /path-to-meta_study-file/meta_study.txt
```

The [meta_study.txt](File-Formats.md#cancer-study) file should conform to the file format describe on the File Formats wiki page.

## Import Study Data

The following command is used to import all types of genomic and clinicla data described on our [File Formats](File-Formats.md) wiki page.  Below are examples of import copy number and mutation data:

```
$PORTAL_HOME/core/src/main/scripts/cbioportalImporter.py --jvm-args "-Dspring.profiles.active=dbcp -cp $PORTAL_HOME/core/target/core-1.0-SNAPSHOT.jar" --command import-study-data --meta-filename /path-to-meta_CNA.txt/meta_CNA.txt --data-filename /path-to-data_CNA.txt/data_CNA.txt
$PORTAL_HOME/core/src/main/scripts/cbioportalImporter.py --jvm-args "-Dspring.profiles.active=dbcp -cp $PORTAL_HOME/core/target/core-1.0-SNAPSHOT.jar" --command import-study-data --meta-filename /path-to-meta_mutations_extended.txt/meta_mutations_extended.txt --data-filename /path-to-data_mutations_extended.txt/data_mutations_extended.txt
```

## Import Case Lists

The following command is used to import case lists into the cBioPortal database:

```
$PORTAL_HOME/core/src/main/scripts/cbioportalImporter.py --jvm-args "-Dspring.profiles.active=dbcp -cp $PORTAL_HOME/core/target/core-1.0-SNAPSHOT.jar" --command import-case-list --meta-filename /path-to-case-list/case-list.txt
```

The [case-list.txt](File-Formats.md#case-lists) file should conform to the file format describe on the File Formats wiki page.

Note - the import-case-list command can take a path to a folder that contains a set of case-list files in addition to a single case-list file.
