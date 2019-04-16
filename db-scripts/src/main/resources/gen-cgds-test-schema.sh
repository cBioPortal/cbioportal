#!/bin/bash

# The purpose of this script is to create a test version of the database schema to load in:
#     `./persistence/persistence-mybatis/src/test/resources/testContextDatabase.xml`
#
# The only difference between the production version of `./db-scripts/src/main/resources/cgds.sql` and the
# version loaded for testing is the dropping of the partial index on `mutation_event`.`TUMOR_SEQ_ALLELE`.
#
# This is done to accommodate the H2 MySQL driver since the latest version (1.4.197) does not yet
# support partial indexing.
#
# When the H2 MySQL driver finally does support partial indexing, this script can be removed and the production
# version can be used for testing again.
#
# author: ochoaa
# last update: 2019/02/13

CGDS_SCHEMA_FILE=$(find . -type f -name "cgds.sql" | grep -v target)
DIR=$(dirname $CGDS_SCHEMA_FILE)
CGDS_TEST_SCHEMA_FILE=$DIR/cgds-test.sql
grep -v "KEY_MUTATION_EVENT_DETAILS" $CGDS_SCHEMA_FILE > $CGDS_TEST_SCHEMA_FILE
exit 0
