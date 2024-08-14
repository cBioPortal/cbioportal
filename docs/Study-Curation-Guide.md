# Study Curation Guide
This guide is to help data curators learn how to curate a study on their own computer

## Prerequisites
- To follow this guide the curator should have some familiarity with running commands on the command line. We will be using Docker. No Docker knowledge is required, one will obtain some basic understanding by following the guide.
- Learn how to setup cBioPortal locally [here](docker/README.md) first.

## Load an example study
After having followed the steps in the [Docker Deployment instructions](docker/README.md), you will end up with the study [Low-Grade Gliomas (UCSF, Science 2014)
](https://www.cbioportal.org/study/summary?id=lgg_ucsf_2014) loaded locally. Now let's try to import another study:

1. Choose another example study from the [datahub](https://github.com/cBioPortal/datahub/tree/master/public). Note the name of the folder which is identical to the study id defined in [meta_study.txt](https://github.com/cBioPortal/datahub/blob/master/public/lgg_ucsf_2014/meta_study.txt#L2).
2. From the root of the `cbioportal-docker-compose` folder run `DATAHUB_STUDIES=my_study_id ./study/init.sh`. Change `my_study_id` to the study you picked in 1. The study should now be downloaded in `./study/`.
3. Import the study by running `docker compose exec cbioportal metaImport.py -u http://cbioportal:8080 -s study/my_study_id/ -o`. Again change `my_study_id` to the study you picked in 1. This should import the study.
4. Restart the cbioportal instance `docker compose restart cbioportal` and see if the new study shows up on http://localhost:8080

## Curate a new study
The cBioPortal team has curated many published studies in formats suitable for import in cBioPortal. These can
be found on the [datahub](https://github.com/cbioportal/datahub) and can serve as an example of how our curation
processes works. You can find a step by step description of how to curate a new study [here](https://github.com/cBioPortal/datahub/blob/master/docs/curation-process.md).
