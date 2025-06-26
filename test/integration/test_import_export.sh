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

echo "Testing import and export of physical study."
run_in_service cbioportal '/cbioportal/test/integration/in_service_import_export_test.sh'

echo "Testing import and export of single-study virtual study."
run_in_service cbioportal '/cbioportal/test/integration/in_service_single_virtual_import_export_test.sh'

echo "Testing import and export of multi-study virtual study."
echo "Loading gene panels and gene sets."
run_in_service cbioportal "cd /core/scripts/ && perl importGenePanel.pl --data \
                          /cbioportal/test/test_data/study_es_0/data_gene_panel_testpanel1.txt"
run_in_service cbioportal "cd /core/scripts/ && perl importGenePanel.pl --data \
                          /cbioportal/test/test_data/study_es_0/data_gene_panel_testpanel2.txt"
run_in_service cbioportal "cd /core/scripts/ && echo yes | ./importGenesetData.pl --data /cbioportal/test/test_data/genesets/study_es_0_genesets.gmt --new-version msigdb_7.5.1 --supp /cbioportal/test/test_data/genesets/study_es_0_supp-genesets.txt"
run_in_service cbioportal "cd /core/scripts/ && ./importGenesetHierarchy.pl --data /cbioportal/test/test_data/genesets/study_es_0_tree.yaml"
run_in_service cbioportal '/cbioportal/test/integration/in_service_multi_virtual_import_export_test.sh'

exit 0
