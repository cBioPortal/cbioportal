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
unset clickhouse_source_database_name
unset clickhouse_destination_database_name
declare -A my_properties
# cis_ts_pid is a timestamp and processid suffix defined by clickhouse_client_command_line_functions.sh
missing_user_records_result_filepath="$(pwd)/missing_user_records_${cis_ts_pid}.txt"
missing_authority_records_result_filepath="$(pwd)/missing_authority_records_${cis_ts_pid}.txt"
insert_user_record_result_filepath="$(pwd)/insert_user_result_${cis_ts_pid}.txt"
insert_authority_record_result_filepath="$(pwd)/insert_authority_result_${cis_ts_pid}.txt"
clickhouse_is_responsive_filepath="$(pwd)/cdtcd_cmd_clickhouse_is_responsive_${cis_ts_pid}.txt"
SECONDS_BETWEEN_RESPONSIVENESS_RETRY=$((60))

function usage() {
    echo "usage: synchronize_user_tables_between_databases.sh properties_filepath database_to_synchronize_from database_to_synchronize_to" >&2
    echo "         database_to_synchronize_from and database_to_synchronize_to must be in {blue, green}" >&2
    echo "         This tool copies over any user records added to the active/production database during the time it took to update the non-production database." >&2
}

function initialize_main() {
    local properties_filepath=$1
    local database_to_synchronize_from=$2
    local database_to_synchronize_to=$3
    if ! parse_property_file "$properties_filepath" my_properties ; then
        usage
        return 1
    fi
    if ! initialize_clickhouse_client_command_line_functions "$database_to_synchronize_to" ; then
        usage
        return 1
    fi
    remove_credentials_from_properties my_properties # no longer needed - remove for security
    if [ "$database_to_synchronize_from" == "blue" ] ; then
        clickhouse_source_database_name="${my_properties['clickhouse_blue_database_name']}"
    else
        if [ "$database_to_synchronize_from" == "green" ] ; then
            clickhouse_source_database_name="${my_properties['clickhouse_green_database_name']}"
        else
            echo "Error : database_to_synchronize_from must be one of {blue, green}" >&2
            usage
            return 1
        fi
    fi
    if [ "$database_to_synchronize_to" == "blue" ] ; then
        clickhouse_destination_database_name="${my_properties['clickhouse_blue_database_name']}"
    else
        if [ "$database_to_synchronize_to" == "green" ] ; then
            clickhouse_destination_database_name="${my_properties['clickhouse_green_database_name']}"
        else
            echo "Error : database_to_synchronize_to must be one of {blue, green}" >&2
            usage
            return 1
        fi
    fi
    if [ "$database_to_synchronize_to" == "$database_to_synchronize_from" ] ; then
        echo "Error : database_to_synchronize_to cannot be the same as database_to_synchronize_from" >&2
        return 1
    fi
    return 0
}

function delete_output_stream_files() {
    rm -f "$missing_user_records_result_filepath"
    rm -f "$missing_authority_records_result_filepath"
    rm -f "$insert_user_record_result_filepath"
    rm -f "$insert_authority_record_result_filepath"
    rm -f "$clickhouse_is_responsive_filepath"
}

function shutdown_main_and_clean_up() {
    shutdown_clickhouse_client_command_line_functions
    delete_output_stream_files
    unset my_properties
    unset clickhouse_source_database_name
    unset clickhouse_destination_database_name
    unset missing_user_records_result_filepath
    unset missing_authority_records_result_filepath
    unset insert_user_record_result_filepath
    unset insert_authority_record_result_filepath
    unset clickhouse_is_responsive_filepath
    unset SECONDS_BETWEEN_RESPONSIVENESS_RETRY
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

function clickhouse_destination_database_exists() {
    if ! clickhouse_database_exists "$clickhouse_destination_database_name"; then
        echo "Error : could not proceed with creation of derived tables because database does not exist: $clickhouse_destination_database_name" >&2
        return 1
    fi
    return 0
}

function find_records_missing_in_destination_database_users_table() {
    local source_table="\`$clickhouse_source_database_name\`.\`users\`"
    local dest_table="\`$clickhouse_destination_database_name\`.\`users\`"
    local statement="SELECT src.email, src.name, src.enabled FROM $source_table AS src LEFT JOIN $dest_table AS dest ON src.email = dest.email WHERE dest.email = '';"
    if ! execute_sql_statement_via_clickhouse_client "$statement" "$missing_user_records_result_filepath" ; then
        return 1
    fi
    return 0
}

function find_records_missing_in_destination_database_authorities_table() {
    local source_table="\`$clickhouse_source_database_name\`.\`authorities\`"
    local dest_table="\`$clickhouse_destination_database_name\`.\`authorities\`"
    local statement="SELECT src.email, src.authority FROM $source_table AS src LEFT JOIN $dest_table AS dest ON src.email = dest.email AND src.authority = dest.authority WHERE dest.email = '';"
    if ! execute_sql_statement_via_clickhouse_client "$statement" "$missing_authority_records_result_filepath" ; then
        return 1
    fi
    return 0
}

function insert_missing_user_record_into_destination_database() {
    local email=$1
    local name=$2
    local enabled=$3
    local dest_table="\`$clickhouse_destination_database_name\`.\`users\`"
    local statement="INSERT INTO $dest_table (email, name, enabled) VALUES ('$email', '$name', '$enabled');"
    if ! execute_sql_statement_via_clickhouse_client "$statement" "$insert_user_record_result_filepath" ; then
        return 1
    fi
    return 0
}

function insert_missing_authority_record_into_destination_database() {
    local email=$1
    local authority=$2
    local dest_table="\`$clickhouse_destination_database_name\`.\`authorities\`"
    local statement="INSERT INTO $dest_table (email, authority) VALUES ('$email', '$authority');"
    if ! execute_sql_statement_via_clickhouse_client "$statement" "$insert_authority_record_result_filepath" ; then
        return 1
    fi
    return 0
}

function insert_missing_records_into_destination_database_users_table() {
    unset sql_data_array
    if ! set_clickhouse_sql_data_array_from_file "$missing_user_records_result_filepath" 0 ; then
        return 1
    fi
    local email_list=(${sql_data_array[@]})
    unset sql_data_array
    if ! set_clickhouse_sql_data_array_from_file "$missing_user_records_result_filepath" 1 ; then
        return 1
    fi
    local name_list=(${sql_data_array[@]})
    unset sql_data_array
    if ! set_clickhouse_sql_data_array_from_file "$missing_user_records_result_filepath" 2 ; then
        return 1
    fi
    local enabled_list=(${sql_data_array[@]})

    local pos=0
    local num_missing_records=${#email_list[@]}
    while [ $pos -lt $num_missing_records ]; do
        echo "Adding user '${name_list[$pos]}' with email '${email_list[$pos]}' to $clickhouse_destination_database_name users table"
        insert_missing_user_record_into_destination_database ${email_list[$pos]} ${name_list[$pos]} ${enabled_list[$pos]}
        pos=$(($pos+1))
    done
    return 0
}

function insert_missing_records_into_destination_database_authorities_table() {
    unset sql_data_array
    if ! set_clickhouse_sql_data_array_from_file "$missing_authority_records_result_filepath" 0 ; then
        return 1
    fi
    local email_list=(${sql_data_array[@]})
    unset sql_data_array
    if ! set_clickhouse_sql_data_array_from_file "$missing_authority_records_result_filepath" 1 ; then
        return 1
    fi
    local authority_list=(${sql_data_array[@]})

    local pos=0
    local num_missing_records=${#email_list[@]}
    while [ $pos -lt $num_missing_records ]; do
        echo "Adding email '${email_list[$pos]}' with authority '${authority_list[$pos]}' to $clickhouse_destination_database_name authorities table"
        insert_missing_authority_record_into_destination_database ${email_list[$pos]} ${authority_list[$pos]}
        pos=$(($pos+1))
    done
    return 0
}

function main() {
    local properties_filepath=$1
    local database_to_synchronize_from=$2
    local database_to_synchronize_to=$3
    local exit_status=0
    if ! initialize_main "$properties_filepath" "$database_to_synchronize_from" "$database_to_synchronize_to" ||
            ! clickhouse_is_responding ||
            ! clickhouse_destination_database_exists ||
            ! find_records_missing_in_destination_database_users_table ||
            ! find_records_missing_in_destination_database_authorities_table ||
            ! insert_missing_records_into_destination_database_users_table ||
            ! insert_missing_records_into_destination_database_authorities_table; then
        exit_status=1
    fi
    shutdown_main_and_clean_up
    return $exit_status
}

main "$1" "$2" "$3"
