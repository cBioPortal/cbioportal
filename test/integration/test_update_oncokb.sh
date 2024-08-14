#!/bin/bash

# exit when any of these fails
set -e
run_in_service() {
    service=$1
    shift
    docker compose -f docker-compose.yml -f $PORTAL_SOURCE_DIR/test/integration/docker-compose-localbuild.yml \
        run --rm \
        "$service" bash -c "$@"
}

# load study_es_0 using API validation
echo "Testing update of OncoKB annotations..."
run_in_service cbioportal 'metaImport.py -v -u http://cbioportal-container:8080 -o -s /cbioportal/test/test_data/study_oncokb_update/'

# execute updateOncokb script
run_in_service cbioportal 'python3 /core/scripts/importer/updateOncokbAnnotations.py -s study_es_0 -p /cbioportal-webapp/application.properties'

# Check that mutation annotations have been updated
# 2 annotations should be changed to "Putative_Driver" (depends on OncoKB version)
test `run_in_service cbioportal 'mysql -hcbioportal-database -ucbio_user -psomepassword cbioportal -e "SELECT alteration_driver_annotation.DRIVER_FILTER from cbioportal.alteration_driver_annotation inner join genetic_profile on genetic_profile.GENETIC_PROFILE_ID = alteration_driver_annotation.GENETIC_PROFILE_ID inner join cancer_study on cancer_study.CANCER_STUDY_ID = genetic_profile.CANCER_STUDY_ID WHERE cancer_study.CANCER_STUDY_IDENTIFIER = \"study_es_0\";"' | cat | grep -c 'Putative_Driver'` -eq 2
