# Curation Guide
This guide is to help data curators learn how to curate a study on their own computer.

## Load an example study
First follow all the steps [here](docker/README.md) to set up a local cBioPortal instance using Docker and import an example study. You will end up with the study [Low-Grade Gliomas (UCSF, Science 2014)
](https://www.cbioportal.org/study/summary?id=lgg_ucsf_2014) loaded locally.

Now let's try to import another study:

1. Choose another example study from the [datahub](https://github.com/cBioPortal/datahub/tree/master/public). Note that the name of the folder which is identical to the study id defined in [meta_study.txt](https://github.com/cBioPortal/datahub/blob/master/public/lgg_ucsf_2014/meta_study.txt#L2).
2. Change the id here: https://github.com/cBioPortal/cbioportal-docker-compose/blob/master/study/init.sh#L6 to the one you picked in 1.
3. Run `./study/init.sh` and follow remaining steps

## Curate a new study
This example shows how one can create a new study for import into cBioPortal.

## Datahub
The cBioPortal team has curated many published studies in formats suitable for import in cBioPortal. These can
be found on the [datahub](https://github.com/cbioportal/datahub) and can serve as an example of how our curation
processes works.
