# Data Loading
## Introduction
This page is the starting point for data loading. The [General Overview](#general-overview) section below contains all the required steps to get you started. 

## General Overview
Getting your study data into cBioPortal requires four steps:

1. [Setting up the validator](#setting-up-the-validator)
2. [Preparing your study data](#preparing-study-data)
3. [Validating your study data](#validating-the-study)
4. [Loading your study data](#loading-data)

## Setting up the validator
### Installation

If you have a git clone of cBioPortal, the relevant scripts can be found in the folder: `<your_cbioportal_dir>/core/src/main/scripts/importer`

### Dependencies
The scripts run in Python 3.4 or newer, and they require the module `requests`.
You can use this command to install the module:
```console
$ sudo python3 -m pip install requests
```

If you want the scripts to be able to generate html reports (recommended way for reading the validation errors, if any), then you will also need to install `Jinja2`. You can use this command:
```console
$ sudo python3 -m pip install Jinja2
```

## Preparing Study Data 
A study to be loaded in cBioPortal can basically consist of a directory where all the data files are located. 
Each *data* file needs a *meta* file that refers to it and both files need to comply to the format required for the specific data type. The format and fields expected for each file are documented in the [File Formats page](File-Formats.md). Below is an example of the files in such a directory.

```
dir
|-meta_study.txt
|-meta_cancer_type.txt -> cancer_type.txt
|-meta_clinical.txt -> data_clinical.txt
|-meta_[expression|mutations|CNA|etc] -> data_[expression|mutations|CNA|etc]
```
#### Rules
There are just a few rules to follow:
- meta_study, meta_clinical and respective clinical data file are the only **mandatory** files.
- cancer type files can be mandatory if the study is referring to a cancer type that does not yet exist in the DB.
- meta files can be named anything, as long as it starts or ends with name 'meta'. E.g. meta_test, meta.test, test.meta are all fine; metal_test and metastudy are wrong.
- data files can be named anything and are referenced by a property `data_filename` set in the meta file. 

## Validating the study
Once all files are in place and follow the proper format, you can [validate your files using the dataset validator script](Using-the-dataset-validator.md). 

The validation can be run standalone, but it is also integrated into the [metaImport script](Using-the-metaImport-script.md), which validates the data and then loads it if validation succeeds. 

## Loading Data
To load the data into cBioPortal, the [metaImport script](Using-the-metaImport-script.md) has to be used. This script first validates the data and, if validation succeeds, loads the data. 

## Removing a Study
To remove a study, the [cbioportalImporter script](Data-Loading-Maintaining-Studies.md#deleting-a-study) can be used.

## Example studies
Examples for the different types of data are available on the [File Formats](File-Formats.md) page. The Provisional TCGA studies, downloadable from the [Data Sets section](http://www.cbioportal.org/data_sets.jsp) are complete studies that can be used as reference when creating data files.
