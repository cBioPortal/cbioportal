Once you have initialized MySQL with the seed database, you are ready to import a sample cancer study.  This is recommended, so that you can verify everything is working.

# Sample Study:  study_es_0

The cBioPortal distribution includes a [very sample sample study, study_es_0](https://github.com/cBioPortal/cbioportal/tree/master/core/src/test/scripts/test_data/study_es_0), which you can use to verfiy that everything is working propertly.

# Running the validator

First, validate 'study_es_0':

To do so, go to the importer folder <your_cbioportal_dir>/core/src/main/scripts/importer and then run the following command:

./validateData.py


[Next Step: Deploying the Web Application](Deploying.md)
