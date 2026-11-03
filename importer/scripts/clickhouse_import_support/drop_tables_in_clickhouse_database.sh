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
unset database_table_list
unset database_name
declare -A my_properties
declare -a database_table_list
database_name=""
# cis_ts_pid is a timestamp and processid suffix defined by clickhouse_client_command_line_functions.sh
database_table_list_filepath="$(pwd)/dtcd_database_table_list_${cis_ts_pid}.txt"
drop_table_result_filepath="$(pwd)/dtcd_drop_table_result_${cis_ts_pid}.txt"
clickhouse_is_responsive_filepath="$(pwd)/cmd_clickhouse_is_responsive_${cis_ts_pid}.txt"
SECONDS_BETWEEN_RESPONSIVENESS_RETRY=$((60))

function usage() {
    echo "usage: drop_tables_in_clickhouse_database.sh properties_filepath [database]" >&2
    echo "         database is optional, but must be in {blue, green} if provided." >&2
}

function initialize_main() {
    local properties_filepath=$1
    local database_to_drop_tables_from=$2
    if ! parse_property_file "$properties_filepath" my_properties ; then
        usage
        return 1
    fi
    if ! initialize_clickhouse_client_command_line_functions "$database_to_drop_tables_from" ; then
        usage
        return 1
    fi
    remove_credentials_from_properties my_properties # no longer needed - remove for security
    if [ -z "$database_to_drop_tables_from" ] ; then
        database_name="${my_properties['clickhouse_database_name']}"
    else
        if [ "$database_to_drop_tables_from" == "blue" ] ; then
            database_name="${my_properties['clickhouse_blue_database_name']}"
        else
            if [ "$database_to_drop_tables_from" == "green" ] ; then
                database_name="${my_properties['clickhouse_green_database_name']}"
            else
                echo "Error : if specified, database must be one of {blue, green}" >&2
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
        echo "Error : could not proceed with dropping of tables because database does not exist: $database_name" >&2
        return 1
    fi
    return 0
}

function set_database_table_list() {
    local statement="SELECT name FROM system.tables WHERE database = '$database_name'"
    rm -f "$database_table_list_filepath"
    if ! execute_sql_statement_via_clickhouse_client "$statement" "$database_table_list_filepath" ; then
        echo "Warning : failed to execute clickhouse statement : $statement" >&2
        return 1
    fi
    unset sql_data_array
    if ! set_clickhouse_sql_data_array_from_file "$database_table_list_filepath" 0 ; then
        return 1
    fi
    database_table_list=(${sql_data_array[@]})
    return 0
}

function drop_all_database_tables() {
    local pos=0
    local table_count=${#database_table_list[@]}
    if [ $table_count -eq 0 ] ; then
        return 0
    fi
    while [ $pos -lt $table_count ] ; do
        local statement="DROP TABLE \`$database_name\`.\`${database_table_list[$pos]}\`"
        rm -f "$drop_table_result_filepath"
        if ! execute_sql_statement_via_clickhouse_client "$statement" "$drop_table_result_filepath" ; then
            echo "Warning : failed to execute clickhouse statement : $statement" >&2
            return 1
        fi
        pos=$(($pos+1))
    done
    return 0
}

function selected_database_is_empty() {
    local table_count=${#database_table_list[@]}
    if [ $table_count -eq 0 ] ; then
        # no tables were present from the beginning .. so no need to check
        echo "Database is already empty (no tables)" >&2
        return 0
    fi
    set_database_table_list
    table_count=${#database_table_list[@]}
    if [ $table_count -ne 0 ] ; then
        echo "Error : after attempts to drop all tables in database $database_name there are still tables present" >&2
        return 1
    fi
    echo "All tables have been dropped" >&2
    return 0
}

function delete_output_stream_files() {
    rm -f "$database_table_list_filepath"
    rm -f "$drop_table_result_filepath"
    rm -f "$clickhouse_is_responsive_filepath"
}

function shutdown_main_and_clean_up() {
    shutdown_clickhouse_client_command_line_functions
    delete_output_stream_files
    unset my_properties
    unset database_table_list
    unset database_name
    unset database_table_list_filepath
    unset drop_table_result_filepath
    unset clickhouse_is_responsive_filepath
    unset SECONDS_BETWEEN_RESPONSIVENESS_RETRY
}

function main() {
    local properties_filepath=$1
    local database_to_drop_tables_from=$2
    local exit_status=0
    if ! initialize_main "$properties_filepath" "$database_to_drop_tables_from" ||
            ! clickhouse_is_responding ||
            ! selected_database_exists ||
            ! set_database_table_list ||
            ! drop_all_database_tables ||
            ! selected_database_is_empty ; then
        exit_status=1
    fi
    shutdown_main_and_clean_up
    return $exit_status
}

main "$1" "$2"
