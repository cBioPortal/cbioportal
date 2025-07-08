#!/bin/bash

# exit when any of these fails
set -e

echo "Creating a virtual study."
create_vs_response=$(curl -s 'http://cbioportal-container:8080/api/session/virtual_study' \
    -H 'Content-Type: application/json' \
    --data-raw '{"name":"Test published virtual study","description":"Test published virtual study","studyViewFilter":{"studyIds":["study_es_0_import_export"],"alterationFilter":{"copyNumberAlterationEventTypes":{"AMP":true,"HOMDEL":true},"mutationEventTypes":{"any":true},"structuralVariants":null,"includeDriver":true,"includeVUS":true,"includeUnknownOncogenicity":true,"includeUnknownTier":true,"includeGermline":true,"includeSomatic":true,"includeUnknownStatus":true,"tiersBooleanMap":{}}},"origin":["study_es_0_import_export"],"studies":[{"id":"study_es_0_import_export","samples":["TCGA-A1-A0SB-01"]}],"dynamic":false}')
hash=$(echo "$create_vs_response" | grep -o '"id":"[^"]*"' | cut -d':' -f2 | tr -d '"')
echo "Virtual study created with id: $hash"

echo "Publishing virtual study with id: $hash"
publish_status=$(curl -X POST -o /dev/null -s -w "%{http_code}" "http://cbioportal-container:8080/api/public_virtual_studies/$hash" \
    -H 'X-PUBLISHER-API-KEY: TEST')
if [ "$publish_status" -eq 200 ]; then
    echo "Virtual study with id $hash published successfully."
else
    echo "Failed with status $publish_status"
fi

echo "Unpublishing virtual study with id: $hash"
unpublish_status=$(curl -X DELETE -o /dev/null -s -w "%{http_code}" "http://cbioportal-container:8080/api/public_virtual_studies/$hash" \
    -H 'X-PUBLISHER-API-KEY: TEST')
if [ "$unpublish_status" -eq 200 ]; then
    echo "Virtual study with id $hash unpublished successfully."
else
    echo "Failed with status $unpublish_status"
fi

exit 0