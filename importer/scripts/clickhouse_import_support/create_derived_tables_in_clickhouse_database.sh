#!/usr/bin/env bash

# load dependencies
unset this_script_dir
this_script_dir="$(dirname "$(readlink -f $0)")"
if ! source "$this_script_dir/parse_property_file_functions.sh" ; then
    echo "Error : unable to load dependency : $this_script_dir/parse_property_file_functions.sh" >&2
    exit 1
fi
if ! source "$this_script_dir/clickhouse_client_command_line_functions.sh" ; then
    echo "Error : unable to load dependency : $this_script_dir/clickhouse_client_command_line_functions.sh" >&2
    exit 1
fi
unset this_script_dir

# non-local environment variables in use
unset my_properties
unset database_name
unset properties_filepath
unset database_to_create_derived_tables_in
unset derived_table_sql_filepath
declare -A my_properties
database_name=""
properties_filepath=""
database_to_create_derived_tables_in=""
derived_table_sql_filepath=""
# cis_ts_pid is a timestamp and processid suffix defined by clickhouse_client_command_line_functions.sh
clickhouse_is_responsive_filepath="$(pwd)/cdtcd_cmd_clickhouse_is_responsive_${cis_ts_pid}.txt"
SECONDS_BETWEEN_RESPONSIVENESS_RETRY=$((60))

function usage() {
    echo "usage: create_derived_tables_in_clickhouse_database.sh properties_filepath [database] create_derived_sql_filepath" >&2
    echo "         database should be omitted for single-database installations, or else must be in {blue, green} for dual-database installations" >&2
    echo "         create_derived_sql_filepath is the path to a single sql file for creating derived tables in clickhouse" >&2
}

function initialize_main() {
    if ! parse_property_file "$properties_filepath" my_properties ; then
        usage
        return 1
    fi
    if ! initialize_clickhouse_client_command_line_functions "$database_to_create_derived_tables_in" ; then
        usage
        return 1
    fi
    remove_credentials_from_properties my_properties # no longer needed - remove for security
    if [ -z "$database_to_create_derived_tables_in" ] ; then
        database_name="${my_properties['clickhouse_database_name']}"
    else
        if [ "$database_to_create_derived_tables_in" == "blue" ] ; then
            database_name="${my_properties['clickhouse_blue_database_name']}"
        else
            if [ "$database_to_create_derived_tables_in" == "green" ] ; then
                database_name="${my_properties['clickhouse_green_database_name']}"
            else
                echo "Error : when provided, database must be one of {blue, green}" >&2
                usage
                return 1
            fi
        fi
    fi
    return 0
}

function clickhouse_is_responding() {
    local remaining_try_count=3
    local statement="SELECT 1"
    while [ $remaining_try_count -ne 0 ] ; do
        if execute_sql_statement_via_clickhouse_client "$statement" "$clickhouse_is_responsive_filepath" ; then
            unset sql_data_array
            if set_clickhouse_sql_data_array_from_file "$clickhouse_is_responsive_filepath" 0 ; then
                local clickhouse_response=${sql_data_array[0]}
                if [ "$clickhouse_response" == "1" ] ; then
                    return 0
                fi
            fi
        fi
        remaining_try_count=$((remaining_try_count-1))
        if [ $remaining_try_count -gt 0 ] ; then
            sleep $SECONDS_BETWEEN_RESPONSIVENESS_RETRY
        fi
    done
    return 1
}

function selected_database_exists() {
    if ! clickhouse_database_exists "$database_name" ; then
        echo "Error : could not proceed with creation of derived tables because database does not exist: $database_name" >&2
        return 1
    fi
    return 0
}

function create_all_derived_tables() {
    if ! clickhouse client --config-file="$configured_clickhouse_config_file_path" --queries-file="$derived_table_sql_filepath" --param_optimize_backoff_secs="$CLICKHOUSE_OPTIMIZE_BACKOFF_SECS" --echo --send_logs_level=information ; then
        echo "Error : failure occurred during execution of sql statements in file $derived_table_sql_filepath" >&2
        return 1
    fi
    return 0
}

function shutdown_main_and_clean_up() {
    shutdown_clickhouse_client_command_line_functions
    rm -f "$clickhouse_is_responsive_filepath"
    unset my_properties
    unset database_name
    unset clickhouse_is_responsive_filepath
    unset SECONDS_BETWEEN_RESPONSIVENESS_RETRY
    unset properties_filepath
    unset database_to_create_derived_tables_in
    unset derived_table_sql_filepath
}

function main() {
    local exit_status=0
    properties_filepath=$1
    shift 1
    database_to_create_derived_tables_in=$1
    if [ "$database_to_create_derived_tables_in" == "blue" ] || [ "$database_to_create_derived_tables_in" == "green" ] ; then
        shift 1
    else
        database_to_create_derived_tables_in=""
    fi
    if [ $# -eq 0 ] ; then
        usage
        exit 1
    fi
    derived_table_sql_filepath=$1
    shift 1
    if [ $# -gt 0 ] ; then
        echo "Error : only one sql file may be provided, but multiple were given" >&2
        usage
        exit 1
    fi
    if ! initialize_main ||
            ! clickhouse_is_responding ||
            ! selected_database_exists ||
            ! create_all_derived_tables ; then
        exit_status=1
    fi
    shutdown_main_and_clean_up
    return $exit_status
}

main "$@"
