#!/bin/bash

# exit when any of these fails
set -e

source "$(dirname "$0")/run_in_service.sh"

run_in_service cbioportal "cd /cbioportal && ./integration_test_oncokb_import.sh"
