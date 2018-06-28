# Loading a Sample Study

Once you have initialized MySQL with the seed database, you are ready to import a sample cancer study. This is recommended to verify that everything is working correctly.

The cBioPortal distribution includes a [small dummy study, `study_es_0`](https://github.com/cBioPortal/cbioportal/tree/master/core/src/test/scripts/test_data/study_es_0), which contains all datatypes supported by cBioPortal. This document describes how to import the prerequisites for the sample study and how to import the study itself.

#### Import Gene Panel for Sample Study

The sample gene panel has to be imported before gene panel study data can be added to the database.

```
cd <your_cbioportal_dir>/core/src/main/scripts
./importGenePanel.pl --data ../../test/scripts/test_data/study_es_0/gene_panel_example.txt
```

More details to load your own gene panel and gene set data can be found here: [Import Gene Panels](Import-Gene-Panels.md).

#### Validating the Sample Study

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

#### Importing the Sample Study

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

[Next Step: Deploying the Web Application](Deploying.md)
