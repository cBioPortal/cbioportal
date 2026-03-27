#!/bin/bash

# exit when any of these fails
set -e
source "$(dirname "$0")/run_in_service.sh"

# load study_es_0 using API validation
echo "Testing update of OncoKB annotations..."
run_in_service cbioportal 'metaImport.py -v -u http://cbioportal-container:8080 -o -s /cbioportal/test/test_data/study_oncokb_update/'

# execute updateOncokb script
run_in_service cbioportal 'python3 /core/scripts/importer/updateOncokbAnnotations.py -s study_es_0 -p /cbioportal-webapp/application.properties'

# Check that mutation annotations have been updated via ClickHouse HTTP API.
# 2 annotations should be changed to "Putative_Driver" (depends on OncoKB version).
# Uses ClickHouse HTTP API instead of mysql CLI — there is no MySQL container in this setup.
PUTATIVE_DRIVER_COUNT=$(run_in_service cbioportal \
  'curl -sf "http://cbioportal-clickhouse-database:8123/?user=cbio_user&password=somepassword&database=cbioportal" \
   --data "SELECT driver_filter FROM alteration_driver_annotation \
     INNER JOIN genetic_profile ON genetic_profile.genetic_profile_id = alteration_driver_annotation.genetic_profile_id \
     INNER JOIN cancer_study ON cancer_study.cancer_study_id = genetic_profile.cancer_study_id \
     WHERE cancer_study.cancer_study_identifier = '"'"'study_es_0'"'"'" \
  | grep -c "Putative_Driver" || true')
test "$PUTATIVE_DRIVER_COUNT" -eq 2
