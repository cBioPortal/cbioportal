#!/usr/bin/env bash
# halt on error
set -e
# script dir
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
#colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "Making sure all db versions are the same in cgds.sql, pom.xml and migration.sql"

POM_DB_VERSION=$(grep db.version ${DIR}/../pom.xml | cut -d'>' -f2 | cut -d'<' -f1)
echo pom.xml db version is $POM_DB_VERSION

CGDS_DB_SQL_VERSION=$(grep 'INSERT INTO info' ${DIR}/../src/main/resources/db-scripts/cgds.sql | cut -d"'" -f2 | cut -d"'" -f1)
echo src/main/resources/db-scripts/cgds.sql db version is $CGDS_DB_SQL_VERSION

MIGRATION_DB_VERSION=$(grep 'UPDATE `info`' ${DIR}/../src/main/resources/db-scripts/migration.sql | tail -1 | cut -d '"' -f2 | cut -d'"' -f1)
echo src/main/resources/db-scripts/migration.sql db version is $MIGRATION_DB_VERSION

if [ "$POM_DB_VERSION" == "$CGDS_DB_SQL_VERSION" ] && [ "$CGDS_DB_SQL_VERSION" == "$MIGRATION_DB_VERSION" ]
then
    echo -e "${GREEN}db versions match${NC}";
    exit 0;
else
    echo -e "${RED}db versions mismatch${NC}";
    exit 1;
fi
