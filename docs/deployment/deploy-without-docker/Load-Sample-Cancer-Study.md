# Loading a Sample Study

Once you have confirmed that the cBioPortal server is installed,
you are ready to import data. Importing a sample study is recommended
to verify that everything is working correctly.

cBioPortal Core has a [small dummy study, `study_es_0`](https://https://github.com/cBioPortal/cbioportal-core/tree/main/src/test/scripts/test_data/study_es_0), which contains all datatypes supported by cBioPortal. This document describes how to import the prerequisites for the sample study and how to import the study itself.


## Download and Build cBioPortal Core

```
	git clone https://github.com/cBioPortal/cbioportal-core.git
	cd cbioportal-core
	git checkout main
	mvn -DskipTests clean install
```



## Set the PORTAL_HOME environment variable

Most cBioPortal command-line tools, including the data loading pipeline,
expect the environment variable `$PORTAL_HOME` to point to a folder
containing the `application.properties` configuration file,
as explained during [the previous step](./Deploying.md).

Configure your shell to keep the variable set to the right folder.
On GNU/Linux and macOS this usually means appending a line
like the following to `.bash_profile` in your home directory:

```
export PORTAL_HOME=/Users/johndoe/cbioportal
```

## Import Gene Panel for Sample Study

The sample gene panel has to be imported before gene panel study data can be added to the database.

```
cd cbioportal-core/src/main/resources/scripts 
./importGenePanel.pl --data ../../test/scripts/test_data/study_es_0/data_gene_panel_testpanel1.txt
./importGenePanel.pl --data ../../test/scripts/test_data/study_es_0/data_gene_panel_testpanel2.txt
```

After loading gene panels into the database, please restart Tomcat or call the `/api/cache` endpoint with a `DELETE` http-request
(see [here](/deployment/customization/application.properties-Reference.md#evict-caches-with-the-apicache-endpoint) for more information)
so that the validator can retrieve gene panel information from the cBioPortal API.

More details to load your own gene panel and gene set data can be found here: [Import Gene Panels](/Import-Gene-Panels.md).

## Validating the Sample Study

First it's useful to validate the study `study_es_0`, to check if the data is formatted correctly.

To do so, go to the importer folder: 

```
cd cbioprtal-core/src/main/scripts/importer
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
cd cbioportal-core/src/main/scripts/importer
```

and then run the following command:

```
./metaImport.py -s ../../../test/scripts/test_data/study_es_0/ -n -o
```

You will see a series of output messages, hopefully ending with a status message like this:

```
Done.
Total time:  7742 ms
```

After loading the study data, please restart the app  or call the `/api/cache` endpoint with a `DELETE` http-request
(see [here](/deployment/customization/application.properties-Reference.md#evict-caches-with-the-apicache-endpoint) for more information).
