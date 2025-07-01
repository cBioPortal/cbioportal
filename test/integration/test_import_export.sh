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

run_in_service cbioportal '/cbioportal/test/integration/in_service_import_export_test.sh'

exit 0
