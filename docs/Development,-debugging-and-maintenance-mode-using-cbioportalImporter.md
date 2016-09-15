## Development, debugging and maintenance mode using cbioportalImporter
For developers and specific testing and maintenance purposes, an extra script, cbioportalImporter.py, is available which imports data regardless of validation results and includes extra functionality, such as deleting a study. 

* [Running the cbioportalImporter script](#running-the-cbioportalimporter-script)
* [Importing a complete study automatically](#importing-a-complete-study-automatically)
* [Importing a complete study manually](#importing-a-complete-study-manually)
* [Deleting a study](#deleting-a-study)

###Running the cbioportalImporter script
To run the importer type the following commands when in the folder `<your_cbioportal_dir>/core/src/main/scripts/importer`: 

```
export PORTAL_HOME=<your_cbioportal_dir>
```

and then run the following command:
```
./cbioportalImporter.py -h
```
This will tell you the parameters you can use: 

```
./cbioportalImporter.py -h
usage: cbioportalImporter.py [-h] [-c COMMAND] [-s STUDY_DIRECTORY]
                             [-jar JAR_PATH] [-meta META_FILENAME]
                             [-data DATA_FILENAME]

cBioPortal meta Importer

optional arguments:
  -h, --help            show this help message and exit
  -c COMMAND, --command COMMAND
                        Command for import. Allowed commands: import-cancer-
                        type, import-study, import-study-data, import-case-
                        list or remove-study
  -s STUDY_DIRECTORY, --study_directory STUDY_DIRECTORY
                        Path to Study Directory
  -jar JAR_PATH, --jar_path JAR_PATH
                        Path to core JAR file
  -meta META_FILENAME, --meta_filename META_FILENAME
                        Path to meta file
  -data DATA_FILENAME, --data_filename DATA_FILENAME
                        Path to Data file
```
Note that the -data option is deprecated and should not be used, as the filename has to be specified in the meta file.

### Importing a complete study automatically 
To import a complete study run: 
```
./cbioportalImporter.py -s <path to study directory>
```

#### Example:
```
./cbioportalImporter.py -s ../../../test/scripts/test_data/study_es_0/
```
 
### Importing a complete study manually 
To import a complete study manually, you will need the following commands:

First, if your cancer type does not yet exist, you need to create it:
```
./cbioportalImporter.py -c import-cancer-type -data <path to study directory>/<name of data file>
```

Next, create the study using
```
./cbioportalImporter.py -c import-study -meta <path to study directory>/<name of meta file>
```
The meta file has to contain the study information.

Now you can import your data file(s): 
```
./cbioportalImporter.py -c import-study-data -meta <path to study directory>/<name of meta file> -data <path to study directory>/<name of data file> 
```
:warning: 
Your first data file should always be the clinical data!

Finally, after you've imported all data, import your case lists:
```
./cbioportalImporter.py -c import-case-list -meta <path to study directory>/<path to case lists>
```

#### Example:
```
./cbioportalImporter.py -c import-cancer-type -data /data/brca_small/data_cancer_type.txt
./cbioportalImporter.py -c import-study -meta /data/brca_small/meta_study.txt
./cbioportalImporter.py -c import-study-data -meta /data/brca_small/meta_clinical.txt -data /data/brca_small/data_clinical.txt
./cbioportalImporter.py -c import-study-data -meta /data/brca_small/meta_expression.txt -data /data/brca_small/data_expression.txt
./cbioportalImporter.py -c import-case-list -meta /data/brca_small/case_lists
```

### Deleting a study
To remove a study run: 
```
./cbioportalImporter.py -c remove-study -meta <path to study directory>/<name of meta study file>
```
The meta file should be the meta file used to create the study.

#### Example:
```
./cbioportalImporter.py -c remove-study -meta /data/brca_small/meta_study.txt
```
