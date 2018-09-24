# Loading a Sample Study

Once you have confirmed that the cBioPortal server is installed,
you are ready to import data. Importing a sample study is recommended
to verify that everything is working correctly.

The cBioPortal distribution includes a [small dummy study, `study_es_0`](https://github.com/cBioPortal/cbioportal/tree/master/core/src/test/scripts/test_data/study_es_0), which contains all datatypes supported by cBioPortal. This document describes how to import the prerequisites for the sample study and how to import the study itself.

## Set the PORTAL_HOME environment variable

Most cBioPortal command-line tools, including the data loading pipeline,
expect the environment variable `$PORTAL_HOME` to point to a folder
containing the `portal.properties` configuration file,
as explained during [the previous step](Deployment.md).

Configure your shell to keep the variable set to the right folder.
On GNU/Linux and macOS this usually means appending a line
like the following to `.bash_profile` in your home directory:

```
export PORTAL_HOME=/Users/johndoe/cbioportal
```

## Import Gene Panel for Sample Study

The sample gene panel has to be imported before gene panel study data can be added to the database.

```
cd <your_cbioportal_dir>/core/src/main/scripts
./importGenePanel.pl --data ../../test/scripts/test_data/study_es_0/gene_panel_example.txt
```

More details to load your own gene panel and gene set data can be found here: [Import Gene Panels](Import-Gene-Panels.md).

## Validating the Sample Study

First it's useful to validate the study `study_es_0`, to check if the data is formatted correctly.

To do so, go to the importer folder: 

```
cd <your_cbioportal_dir>/core/src/main/scripts/importer
```

and then run the following command:

```
./validateData.py -s ../../../test/scripts/test_data/study_es_0/ -n
```

If all goes well, you should see the final output message:

```
Validation of study succeeded with warnings.
```

## Importing the Sample Study

To import the sample study:

```
cd <your_cbioportal_dir>/core/src/main/scripts/importer
```

and then run the following command:

```
./metaImport.py -s ../../../test/scripts/test_data/study_es_0/ -n -o
```

You will see a series of output messages, hopefulling ending with a status message like this:

```
Done.
Total time:  7742 ms
```

[Steps Complete: Return Home](README.md)
