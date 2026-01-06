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

pom_db_version_line=$(grep db.version ${DIR}/../pom.xml | tail -n 1)
find_delimited_substrings "$pom_db_version_line" ">" "<"
pom_db_version=${found_delimited_substrings[0]}

pom_derived_table_version_line=$(grep derived_table.version ${DIR}/../pom.xml | tail -n 1)
find_delimited_substrings "$pom_derived_table_version_line" ">" "<"
pom_derived_table_version=${found_delimited_substrings[0]}

cgds_db_sql_version_line=$(grep -A1 'INSERT INTO `info`' ${DIR}/../src/main/resources/db-scripts/cgds.sql | tail -n 1)
find_delimited_substrings "$cgds_db_sql_version_line" "'" "'"
cgds_db_sql_version=${found_delimited_substrings[0]}

migration_db_version_line=$(grep 'UPDATE `info` SET `DB_SCHEMA_VERSION`' ${DIR}/../src/main/resources/db-scripts/migration.sql | tail -n 1 )
find_delimited_substrings "$migration_db_version_line" '"' '"'
migration_db_version=${found_delimited_substrings[0]}

# Extract derived table version from first line of clickhouse.sql
clickhouse_derived_table_version_line=$(head -n 1 ${DIR}/../src/main/resources/db-scripts/clickhouse/clickhouse.sql)
# Extract version number from comment like: -- version 1.0.6 of derived table schema and data definition
clickhouse_derived_table_version=$(echo "$clickhouse_derived_table_version_line" | grep -oE '[0-9]+\.[0-9]+\.[0-9]+')

echo "pom.xml db version is $pom_db_version"
echo "src/main/resources/db-scripts/cgds.sql db version is $cgds_db_sql_version"
echo "src/main/resources/db-scripts/migration.sql db version is $migration_db_version"
echo "pom.xml derived_table version is $pom_derived_table_version"
echo "src/main/resources/db-scripts/clickhouse/clickhouse.sql derived_table version is $clickhouse_derived_table_version"

if [ "$pom_db_version" == "$cgds_db_sql_version" ] &&
        [ "$cgds_db_sql_version" == "$migration_db_version" ] ; then
    db_versions_all_match="yes"
else
    db_versions_all_match="no"
fi

if [ "$pom_derived_table_version" == "$clickhouse_derived_table_version" ] ; then
    derived_table_versions_all_match="yes"
else
    derived_table_versions_all_match="no"
fi

if [ $db_versions_all_match == "yes" ] && [ $derived_table_versions_all_match == "yes" ] ; then
    echo -e "${GREEN}db versions match${NC}";
    exit 0;
else
    echo -e "${RED}db versions mismatch${NC}";
    exit 1;
fi
