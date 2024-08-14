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

run_in_service cbioportal "cd /cbioportal && ./integration_test_oncokb_import.sh"
