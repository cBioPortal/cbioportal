#!/bin/bash

# exit when any of these fails
set -e

source "$(dirname "$0")/run_in_service.sh"

echo "Testing import and export of physical study."
run_in_service cbioportal '/cbioportal/test/integration/in_service_import_export_test.sh'

echo "Testing import and export of single-study virtual study."
run_in_service cbioportal '/cbioportal/test/integration/in_service_single_virtual_import_export_test.sh'

echo "Testing import and export of multi-study virtual study."
run_in_service cbioportal '/cbioportal/test/integration/in_service_multi_virtual_import_export_test.sh'

exit 0
