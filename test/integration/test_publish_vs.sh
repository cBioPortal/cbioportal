#!/bin/bash

# exit when any of these fails
set -e

source "$(dirname "$0")/run_in_service.sh"

echo "Testing import and export of physical study."
run_in_service cbioportal '/cbioportal/test/integration/in_service_publish_vs.sh'

exit 0
