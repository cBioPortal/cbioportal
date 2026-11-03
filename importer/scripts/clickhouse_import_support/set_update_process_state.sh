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
declare -A my_properties
# cis_ts_pid is a timestamp and processid suffix defined by clickhouse_client_command_line_functions.sh
record_count_filepath="$(pwd)/sups_ch_update_status_record_count_${cis_ts_pid}.txt"
current_status_filepath="$(pwd)/sups_ch_current_status_${cis_ts_pid}.txt"
current_color_filepath="$(pwd)/sups_ch_current_color_${cis_ts_pid}.txt"
table_exists_filepath="$(pwd)/sups_ch_table_exists_${cis_ts_pid}.txt"
update_status_filepath="$(pwd)/sups_ch_update_status_${cis_ts_pid}.txt"

function usage() {
    echo "usage: set_update_process_state.sh properties_filepath state" >&2
    echo "         state must be in {running, complete, abandoned}" >&2
}

function initialize_main() {
    local properties_filepath=$1
    local state=$2
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
    if ! [ "$state" == "running" ] && ! [ "$state" == "complete" ] && ! [ "$state" == "abandoned" ] ; then
        echo "Error : state must be one of {running, complete, abandoned}" >&2
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
    rm -f "$current_status_filepath"
    rm -f "$current_color_filepath"
    rm -f "$table_exists_filepath"
    rm -f "$update_status_filepath"
}

function shutdown_main_and_clean_up() {
    shutdown_clickhouse_client_command_line_functions
    delete_output_stream_files
    unset my_properties
    unset update_management_database_name
    unset portal_database_name
    unset record_count_filepath
    unset current_status_filepath
    unset current_color_filepath
    unset table_exists_filepath
    unset update_status_filepath
}

function process_state_table_is_valid() {
    if ! clickhouse_database_exists "$update_management_database_name" ; then
        echo "Error : could not proceed with setting update status because database does not exist: $update_management_database_name" >&2
        return 1
    fi
    local check_table_statement="SELECT COUNT(*) FROM system.tables WHERE database = '$update_management_database_name' AND name = 'update_status'"
    if ! execute_sql_statement_via_clickhouse_client "$check_table_statement" "$table_exists_filepath" ; then
        echo "Error : could not check for update_status table existence. Clickhouse statement failed : $check_table_statement" >&2
        return 1
    fi
    set_clickhouse_sql_data_array_from_file "$table_exists_filepath" 0
    if [[ "${sql_data_array[0]}" -ne 1 ]] ; then
        echo "Error : could not proceed with setting update status because table 'update_status' does not exist in database : $update_management_database_name" >&2
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

function set_state_in_status_table() {
    local state=$1

    # Get current status
    local get_status_statement="SELECT update_process_status FROM \`$update_management_database_name\`.update_status WHERE portal_database = '$portal_database_name' LIMIT 1"
    if ! execute_sql_statement_via_clickhouse_client "$get_status_statement" "$current_status_filepath" ; then
        echo "Error : could not retrieve current update_process_status. Clickhouse statement failed : $get_status_statement" >&2
        return 1
    fi
    set_clickhouse_sql_data_array_from_file "$current_status_filepath" 0
    local current_status="${sql_data_array[0]}"

    local alter_statement=""
    if [ "$state" == "running" ] ; then
        if [ "$current_status" != "complete" ] ; then
            echo "Error : cannot set process status to running because current status is '$current_status'. You should determine which database is actually in production currently, and manually correct the update_status table in database $update_management_database_name" >&2
            return 1
        fi
        alter_statement="ALTER TABLE \`$update_management_database_name\`.update_status UPDATE update_process_status = 'running' WHERE portal_database = '$portal_database_name' SETTINGS mutations_sync = 1"
    elif [ "$state" == "complete" ] ; then
        if [ "$current_status" != "running" ] ; then
            echo "Warning : cannot set process status to complete because current status is '$current_status'. You should determine which database is actually in production currently, and validate the update_status table in database $update_management_database_name" >&2
            return 0
        fi
        # Get current color to flip it
        local get_color_statement="SELECT current_database_in_production FROM \`$update_management_database_name\`.update_status WHERE portal_database = '$portal_database_name' LIMIT 1"
        if ! execute_sql_statement_via_clickhouse_client "$get_color_statement" "$current_color_filepath" ; then
            echo "Error : could not retrieve current_database_in_production. Clickhouse statement failed : $get_color_statement" >&2
            return 1
        fi
        set_clickhouse_sql_data_array_from_file "$current_color_filepath" 0
        local current_color="${sql_data_array[0]}"
        local new_color
        if [ "$current_color" == "blue" ] ; then
            new_color="green"
        else
            new_color="blue"
        fi
        alter_statement="ALTER TABLE \`$update_management_database_name\`.update_status UPDATE update_process_status = 'complete', current_database_in_production = '$new_color', time_of_last_update_process_completion = toString(now()) WHERE portal_database = '$portal_database_name' SETTINGS mutations_sync = 1"
    else
        # abandoned: revert to complete without flipping color
        alter_statement="ALTER TABLE \`$update_management_database_name\`.update_status UPDATE update_process_status = 'complete' WHERE portal_database = '$portal_database_name' AND update_process_status = 'running' SETTINGS mutations_sync = 1"
    fi

    if ! execute_sql_statement_via_clickhouse_client "$alter_statement" "$update_status_filepath" ; then
        echo "Error : failed to execute SQL statement : $alter_statement" >&2
        return 1
    fi
    echo "Status table updated: portal_database='$portal_database_name' state set to '$state'"
    return 0
}

function main() {
    local properties_filepath=$1
    local state=$2
    local exit_status=0
    if ! initialize_main "$properties_filepath" "$state" ||
            ! process_state_table_is_valid ||
            ! set_state_in_status_table "$state" ; then
        exit_status=1
    fi
    shutdown_main_and_clean_up
    return $exit_status
}

main "$1" "$2"
