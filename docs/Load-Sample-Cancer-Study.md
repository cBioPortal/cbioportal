Once you have initialized MySQL with the seed database, you are ready to import a sample cancer study.  This is recommended, so that you can verify everything is working.

# Sample Study

The cBioPortal distribution includes a [small dummy study, study_es_0](https://github.com/cBioPortal/cbioportal/tree/master/core/src/test/scripts/test_data/study_es_0), which you can use to verify that everything is working properly.

# Validating the Sample Study

First, validate `study_es_0`.

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

# Importing the Sample Study

[Under construction]

[Next Step: Deploying the Web Application](Deploying.md)
