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

echo "Making sure db_schema_version is in sync across pom.xml, schema.sql, and migrate_schema.sql"

declare -a found_delimited_substrings

# find delimited substrings in a containing string
# positional arguments:
#   - string_with_delimited_substrings
#   - starting_delimiting_character
#   - ending_delimiting_chracter
# example use : find_delimited_substrings 'roses are "red", "white", "pink"' '"' '"'
#      will set array found_delimited_substrings to ('red', 'white', 'pink')
# example use : find_delimited_substrings 'roses are "red", "white", "pink"' '"' ''
#      will set array found_delimited_substrings to:
#           ('roses are ', 'red', ', ', 'white', ', ', 'pink')
# when ending_delimiting_character argument is the empty string ('') then substrings are
# found by breaking the string between each occurrence of the staring delimiting character.
# Otherwise, substrings are found iteratively by locating the first not-previously used
# occurrence of the starting delimiting character and using all characters from that point
# to the next occurrence of the ending_delimiting_character (which is consumed but not
# returned).
#
function find_delimited_substrings() {
    string_with_delimited_substrings=$1
    starting_delimiting_character=$2
    ending_delimiting_character=$3
    if [ ${#ending_delimiting_character} == 0 ] ; then
        IFS=$starting_delimiting_character ; read -ra found_delimited_substrings <<< "$string_with_delimited_substrings"
        return 0
    fi
    found_delimited_substrings=()
    working_string="$string_with_delimited_substrings"
    done=false
    while true; do
        working_string_length=${#working_string}
        startpos=0
        while [ $startpos -lt $working_string_length ] ; do
            if [ "${working_string:$startpos:1}" == "$starting_delimiting_character" ] ; then
                break
            fi
            startpos=$(($startpos+1))
        done
        if [ $startpos -eq $working_string_length ] ; then
            break # no more found
        fi
        endpos=$(($startpos+1))
        while [ $endpos -lt $working_string_length ] ; do
            if [ "${working_string:$endpos:1}" == "$ending_delimiting_character" ] ; then
                break
            fi
            endpos=$(($endpos+1))
        done
        if [ $endpos -eq $working_string_length ] ; then
            break # no more found
        fi
        substring_start=$((startpos+1))
        substring_len=$(($endpos-$startpos-1))
        found_delimited_substrings+=(${working_string:$substring_start:$substring_len})
        next_working_startpos=$((endpos+1))
        working_string=${working_string:$next_working_startpos}
    done
    return 0
}

SCHEMA_SQL=${DIR}/../src/main/resources/db-scripts/clickhouse/init/schema.sql
MIGRATE_SCHEMA_SQL=${DIR}/../src/main/resources/db-scripts/clickhouse/migrate/migrate_schema.sql

# --- pom.xml ---
pom_db_version_line=$(grep '<db.version>' ${DIR}/../pom.xml | tail -n 1)
find_delimited_substrings "$pom_db_version_line" ">" "<"
pom_db_version=${found_delimited_substrings[0]}

# --- schema.sql: INSERT INTO info (`db_schema_version`, `geneset_version`, `gene_table_version`) VALUES ('<version>', ...); ---
schema_sql_info_line=$(grep 'INSERT INTO info' ${SCHEMA_SQL} | tail -n 1)
find_delimited_substrings "$schema_sql_info_line" "'" "'"
schema_sql_db_version=${found_delimited_substrings[0]}

# --- migrate_schema.sql: highest '## db_schema_version: X' section header ---
migrate_schema_sql_db_version=$(grep -oE '^##[[:space:]]*db_schema_version:[[:space:]]*[0-9]+\.[0-9]+\.[0-9]+' ${MIGRATE_SCHEMA_SQL} | \
    sed -E 's/^##[[:space:]]*db_schema_version:[[:space:]]*//' | sort -V | tail -n 1)

echo "pom.xml db.version is $pom_db_version"
echo "schema.sql db_schema_version is $schema_sql_db_version"
echo "migrate_schema.sql highest db_schema_version section is $migrate_schema_sql_db_version"

if [ "$pom_db_version" == "$schema_sql_db_version" ] &&
        [ "$schema_sql_db_version" == "$migrate_schema_sql_db_version" ] ; then
    echo -e "${GREEN}db versions match${NC}";
    exit 0;
else
    echo -e "${RED}db versions mismatch${NC}";
    exit 1;
fi
