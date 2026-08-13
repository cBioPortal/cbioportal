#!/usr/bin/env bash
# halt on error
set -e
# script dir
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
#colors
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

echo "Making sure every TRUNCATE TABLE statement in ClickHouse SQL scripts forces synchronous"
echo "completion (SETTINGS alter_sync = 2), so a later statement can't race a still-in-progress"
echo "TRUNCATE."

# Files that may contain TRUNCATE TABLE statements against ClickHouse-managed tables. Add new
# files here if a future migrate_schema.sql section (or any other db-scripts SQL file) starts
# using TRUNCATE.
FILES_TO_CHECK=(
    "${DIR}/../src/main/resources/db-scripts/clickhouse/populate_derived_tables.sql"
    "${DIR}/../src/main/resources/db-scripts/clickhouse/migrate/migrate_schema.sql"
)

found_violation=0

for f in "${FILES_TO_CHECK[@]}"; do
    if [ ! -f "$f" ]; then
        continue
    fi
    while IFS= read -r line; do
        if echo "$line" | grep -qiE '^[[:space:]]*TRUNCATE[[:space:]]+TABLE\b'; then
            if ! echo "$line" | grep -qiE 'SETTINGS[[:space:]]+alter_sync[[:space:]]*=[[:space:]]*2'; then
                echo -e "${RED}Missing 'SETTINGS alter_sync = 2' on TRUNCATE statement in ${f}:${NC}"
                echo "    $line"
                found_violation=1
            fi
        fi
    done < "$f"
done

if [ "$found_violation" -eq 0 ]; then
    echo -e "${GREEN}All TRUNCATE TABLE statements set alter_sync = 2${NC}"
    exit 0
else
    echo -e "${RED}One or more TRUNCATE TABLE statements are missing SETTINGS alter_sync = 2${NC}"
    exit 1
fi
