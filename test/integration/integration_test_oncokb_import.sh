#!/usr/bin/env bash

# Test OncoKB import against local portal info dump (requires no Internet) and
# live oncokb.org instance (requires Internet and OncoKB to be up-and-running)

cd "$PORTAL_SOURCE_DIR"/core/src/main/scripts/importer
python3 ./metaImport.py \
  --study_directory="$PORTAL_SOURCE_DIR"/core/src/test/scripts/test_data/study_oncokb_import \
  --portal_info_dir="$PORTAL_SOURCE_DIR"/core/src/test/scripts/test_data/api_json_system_tests \
  --import_oncokb \
  --skip_db_import &&
cd "$PORTAL_SOURCE_DIR"/core/src/test/scripts/test_data/study_oncokb_import &&
test -e data_cna_pd_annotations.txt &&
test -e ONCOKB_IMPORT_BACKUP_data_mutations_extended.maf &&
test -e ONCOKB_IMPORT_BACKUP_meta_cna_discrete.txt &&
test `grep -d skip -l Putative_Passenger * |  wc -l` -eq 2 &&
test `grep -d skip -l cbp_driver * |  wc -l` -eq 2 &&
test `grep -d skip -l data_cna_pd_annotations.txt * |  wc -l` -eq 1
