#!/bin/bash

# exit when any of these fails
set -e
source "$(dirname "$0")/run_in_service.sh"

# load study_es_0 using API validation
echo "Testing update of OncoKB annotations..."
run_in_service cbioportal 'metaImport.py -v -u http://cbioportal-container:8080 -o -s /cbioportal/test/test_data/study_oncokb_update/'

# execute updateOncokb script
run_in_service cbioportal 'python3 /core/scripts/importer/updateOncokbAnnotations.py -s study_es_0 -p /cbioportal-webapp/application.properties'

# Check that mutation annotations have been updated
# 2 annotations should be changed to "Putative_Driver" (depends on OncoKB version)
test `run_in_service cbioportal 'clickhouse client --host "$CLICKHOUSE_HOST" --port "$CLICKHOUSE_NATIVE_PORT" --user "$CLICKHOUSE_USER" --password "$CLICKHOUSE_PASSWORD" --database "$CLICKHOUSE_DB" --param_study=study_es_0 --query "SELECT alteration_driver_annotation.driver_filter FROM alteration_driver_annotation INNER JOIN genetic_profile ON genetic_profile.genetic_profile_id = alteration_driver_annotation.genetic_profile_id INNER JOIN cancer_study ON cancer_study.cancer_study_id = genetic_profile.cancer_study_id WHERE cancer_study.cancer_study_identifier = {study:String}"' | cat | grep -c 'Putative_Driver'` -eq 2
