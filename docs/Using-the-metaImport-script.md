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
./metaImport.py -h
usage: metaImport.py [-h] -s STUDY_DIRECTORY
                     [-u URL_SERVER | -p PORTAL_INFO_DIR] -jar JAR_PATH
                     [-html HTML_TABLE] [-v] [-o]

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
  -P PORTAL_PROPERTIES, --portal_properties PORTAL_PROPERTIES
                        portal.properties file path (default: assumed hg19)
  -jar JAR_PATH, --jar_path JAR_PATH
                        path to core jar file. You can set PORTAL_HOME variable instead.
  -html HTML_TABLE, --html_table HTML_TABLE
                        path to html report
  -v, --verbose         report status info messages while validating
  -o, --override_warning
                        override warnings and continue importing
```

#### Example of Importing a study
Export PORTAL_HOME, e.g.

```
export PORTAL_HOME=<your_cbioportal_home_dir>
```

and then run (this simple command only works if your cBioPortal is running at http://localhost/cbioportal - if this is not the case, follow the advanced example):

```
./metaImport.py -s ../../../test/scripts/test_data/study_es_0/
```

#### Advanced Example
This example imports the study to the localhost, creates an html report and shows status messages.
```
./metaImport.py -s ../../../test/scripts/test_data/study_es_0/ -u http://localhost:8080/cbioportal -html myReport.html -v
```

By adding `-o`, warnings will be overridden and import will start after validation.

## Development / debugging mode
For developers and specific testing purposes, an extra script, cbioportalImporter.py, is available which imports data regardless of validation results. Check [this](Development,-debugging-and-maintenance-mode-using-cbioportalImporter.md) page for more information on how to use it.
