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

# other non-local environment variables in use
unset my_properties
unset update_management_database_name
update_management_database_name=""
unset portal_database_name
portal_database_name=""
unset database_currently_in_production
database_currently_in_production=""
declare -A my_properties
# cis_ts_pid is a timestamp and processid suffix defined by clickhouse_client_command_line_functions.sh
record_count_filepath="$(pwd)/gdcip_ch_update_status_record_count_${cis_ts_pid}.txt"
current_production_database_filepath="$(pwd)/gdcip_ch_current_production_database_${cis_ts_pid}.txt"
table_exists_filepath="$(pwd)/gdcip_ch_table_exists_${cis_ts_pid}.txt"

function usage() {
    echo "usage: get_database_currently_in_production.sh properties_filepath" >&2
}

function initialize_main() {
    local properties_filepath=$1
    if [ -z "$properties_filepath" ] ; then
        usage
        return 1
    fi
    if ! [ -r "$properties_filepath" ] ; then
        echo "Error: cannot read specified properties file '$properties_filepath'" >&2
        return 1
    fi
    if ! parse_property_file "$properties_filepath" my_properties ; then
        usage
        return 1
    fi
    update_management_database_name="${my_properties['clickhouse_update_management_database']}"
    if [ -z "$update_management_database_name" ] ; then
        echo "Error : clickhouse_update_management_database not found in properties file" >&2
        usage
        return 1
    fi
    portal_database_name="${my_properties['portal_database_name']}"
    if [ -z "$portal_database_name" ] ; then
        echo "Error : portal_database_name not found in properties file" >&2
        usage
        return 1
    fi
    if ! initialize_clickhouse_client_command_line_functions "system" ; then
        usage
        return 1
    fi
    remove_credentials_from_properties my_properties # no longer needed - remove for security
    return 0
}

function delete_output_stream_files() {
    rm -f "$record_count_filepath"
    rm -f "$current_production_database_filepath"
    rm -f "$table_exists_filepath"
}

function shutdown_main_and_clean_up() {
    shutdown_clickhouse_client_command_line_functions
    delete_output_stream_files
    unset my_properties
    unset update_management_database_name
    unset portal_database_name
    unset database_currently_in_production
    unset record_count_filepath
    unset current_production_database_filepath
    unset table_exists_filepath
}

function process_state_table_is_valid() {
    if ! clickhouse_database_exists "$update_management_database_name" ; then
        echo "Error : could not proceed with getting production database because database does not exist: $update_management_database_name" >&2
        return 1
    fi
    local check_table_statement="SELECT COUNT(*) FROM system.tables WHERE database = '$update_management_database_name' AND name = 'update_status'"
    if ! execute_sql_statement_via_clickhouse_client "$check_table_statement" "$table_exists_filepath" ; then
        echo "Error : could not check for update_status table existence. Clickhouse statement failed : $check_table_statement" >&2
        return 1
    fi
    set_clickhouse_sql_data_array_from_file "$table_exists_filepath" 0
    if [[ "${sql_data_array[0]}" -ne 1 ]] ; then
        echo "Error : could not proceed with getting production database because table 'update_status' does not exist in database : $update_management_database_name" >&2
        return 1
    fi
    local get_record_count_statement="SELECT count(*) FROM \`$update_management_database_name\`.update_status WHERE portal_database = '$portal_database_name'"
    if ! execute_sql_statement_via_clickhouse_client "$get_record_count_statement" "$record_count_filepath" ; then
        echo "Error : could not validate process_state table. Clickhouse statement failed : $get_record_count_statement" >&2
        return 1
    fi
    set_clickhouse_sql_data_array_from_file "$record_count_filepath" 0
    local rowcount="${sql_data_array[0]}"
    if [[ "$rowcount" -eq 0 ]] ; then
        echo "Error : no row found in $update_management_database_name.update_status for portal_database = '$portal_database_name'" >&2
        return 1
    fi
    return 0
}

function get_database_currently_in_production() {
    local get_current_database_statement="SELECT current_database_in_production FROM \`$update_management_database_name\`.update_status WHERE portal_database = '$portal_database_name' LIMIT 1"
    if ! execute_sql_statement_via_clickhouse_client "$get_current_database_statement" "$current_production_database_filepath" ; then
        echo "Error : could not retrieve the current database in production. Clickhouse statement failed : $get_current_database_statement" >&2
        return 1
    fi
    set_clickhouse_sql_data_array_from_file "$current_production_database_filepath" 0
    database_currently_in_production="${sql_data_array[0]}"
    return 0
}

function output_database_currently_in_production() {
    echo "$database_currently_in_production : current production database"
}

function main() {
    local properties_filepath=$1
    local exit_status=0
    if ! initialize_main "$properties_filepath" ||
            ! process_state_table_is_valid ||
            ! get_database_currently_in_production ; then
        exit_status=1
    else
        output_database_currently_in_production
    fi
    shutdown_main_and_clean_up
    return $exit_status
}

main "$1"
