#!/bin/bash

# exit when any of these fails
set -e

study="exp_ov_tcga_pub"

echo "Sort content of text files from both folders to make order during comparison unimportant."
./copy_and_sort.sh "./downloaded_studies/${study}/" "./downloaded_studies/${study}_sorted/"
./copy_and_sort.sh "./re-downloaded_studies/${study}/" "./re-downloaded_studies/${study}_sorted/"

echo "Comparing the original and exported studies."
diff --recursive "./downloaded_studies/${study}_sorted/" "./re-downloaded_studies/${study}_sorted/" > diff_outputs/diff_${study}_output.txt

exit 0