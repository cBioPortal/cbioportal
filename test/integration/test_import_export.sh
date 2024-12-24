#!/bin/bash

# exit when any of these fails
set -e

run_in_service() {
    service=$1
    shift
    docker compose -f docker-compose.yml -f $PORTAL_SOURCE_DIR/test/integration/docker-compose-localbuild.yml \
        run --rm \
        "$service" bash -c "$@"
    return $?  # return the exit code of the last command
}

echo "Importing of the test study with API validation..."
run_in_service cbioportal 'metaImport.py -v -u http://cbioportal-container:8080 -o -s /cbioportal/test/test_data/study_es_0_import_export/'

echo "Exporting of the test study."
run_in_service cbioportal 'curl -s http://cbioportal-container:8080/export/study/study_es_0_import_export.zip > study_es_0_import_export.zip && unzip study_es_0_import_export.zip -d ./output_study_es_0_import_export'

echo "Comparing the original and exported studies."
# TODO ignore order of lines in files
run_in_service cbioportal 'diff --recursive /cbioportal/test/test_data/study_es_0_import_export/ ./output_study_es_0_import_export'

exit 0
