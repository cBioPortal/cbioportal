## Importing Data into cBioPortal
The metaImport script should be used to automate the process of validating and loading datasets. It also has some nice features like an extra option to only load datasets that completely pass validation (i.e. with no errors, while warnings can be explicitly allowed by the user). 

#### Running the metaImport Script
To run the metaImport script first go to the importer folder
`<your_cbioportal_dir>/core/src/main/scripts/importer` 
and then run the following command:
```
./metaImport.py -h
```
This will tell you the parameters you can use:
```
$./metaImport.py -h
usage: metaImport.py [-h] -s STUDY_DIRECTORY
                     [-u URL_SERVER | -p PORTAL_INFO_DIR | -n]
                     [-jar JAR_PATH] [-html HTML_TABLE]
                     [-v] [-o] [-r] [-m] [-a MAX_REPORTED_VALUES]

cBioPortal meta Importer

optional arguments:
  -h, --help            show this help message and exit
  -s STUDY_DIRECTORY, --study_directory STUDY_DIRECTORY
                        path to directory.
  -u URL_SERVER, --url_server URL_SERVER
                        URL to cBioPortal server. You can set this if your URL
                        is not http://localhost/cbioportal
  -p PORTAL_INFO_DIR, --portal_info_dir PORTAL_INFO_DIR
                        Path to a directory of cBioPortal info files to be
                        used instead of contacting the web API
  -n, --no_portal_checks
                        Skip tests requiring information from the cBioPortal
                        installation
  -jar JAR_PATH, --jar_path JAR_PATH
                        Path to scripts JAR file (default: locate it relative
                        to the import script)
  -html HTML_TABLE, --html_table HTML_TABLE
                        path to html report
  -v, --verbose         report status info messages while validating
  -o, --override_warning
                        override warnings and continue importing
  -r, --relaxed_clinical_definitions
                        Option to enable relaxed mode for validator when
                        validating clinical data without header definitions
  -m, --strict_maf_checks
                        Option to enable strict mode for validator when
                        validating mutation data
  -a MAX_REPORTED_VALUES, --max_reported_values MAX_REPORTED_VALUES
                        Cutoff in report for the maximum number of line
                        numbers and values encountered to report for each
                        message in the HTML report. For example, set this to a
                        high number to report all genes that could not be
                        loaded, instead of reporting "(GeneA, GeneB, GeneC,
                        213 more)".
```

#### Example of Importing a study
Export `PORTAL_HOME` as explained [here](Load-Sample-Cancer-Study.md), e.g.

```
export PORTAL_HOME=<cbioportal_configuration_folder>
```

and then run (this simple command only works if your cBioPortal is running at http://localhost/cbioportal - if this is not the case, follow the advanced example):

```
./metaImport.py -s ../../../test/scripts/test_data/study_es_0/
```

#### Advanced Example
This example imports the study to the localhost, creates an html report and shows status messages.
```
./metaImport.py -s ../../../test/scripts/test_data/study_es_0/ -u http://localhost:8080 -html myReport.html -v
```

By adding `-o`, warnings will be overridden and import will start after validation.

## Development / debugging mode
For developers and specific testing purposes, an extra script, cbioportalImporter.py, is available which imports data regardless of validation results. Check [this](Data-Loading-For-Developers.md) page for more information on how to use it.
