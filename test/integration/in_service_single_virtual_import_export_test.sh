#!/bin/bash

# exit when any of these fails
set -e

echo "Importing test study."
metaImport.py -v -u http://cbioportal-container:8080 -o -s /cbioportal/test/test_data/study_es_0_import_export/

echo "Creating single-study virtual study."

# echo "Exporting of the test study."
# curl -s http://cbioportal-container:8080/export/study/study_es_0_import_export.zip > study_es_0_import_export.zip \
# && unzip study_es_0_import_export.zip -d ./output_study_es_0_import_export

# echo "Sort content of text files from both folders to make order during comparison unimportant."
# ./cbioportal/test/integration/copy_and_sort.sh /cbioportal/test/test_data/study_es_0_import_export/ ./input_study_es_0_import_export_sorted/
# ./cbioportal/test/integration/copy_and_sort.sh ./output_study_es_0_import_export/ ./output_study_es_0_import_export_sorted/

# echo "Comparing the original and exported studies."
# diff --recursive ./input_study_es_0_import_export_sorted/ ./output_study_es_0_import_export_sorted/

exit 0
