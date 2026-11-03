#!/usr/bin/env bash
#
# This file defines function parse_property_file() in the current processing shell.
#
# Usage: parse_property_file property_file_path associative_array_name
#
# The file at the indicated path will be parsed for property settings and results will be returned in the associative array
# with the indicated name. The associative array must be declared by the caller prior to call.
#
# Parsing will ignore lines which are only whitespace, or which have "#" or "!" as the first non-whitespace character
# Other lines must begin (after ignored whitespace) with a key name, and can contain a delimiter followed by a value string.
# If no delimiter is present, the entire line is considered a key name, which will be set in the array with an empty string value.
# A delimiter is the first encountered "=" characdter or ":" character. (a tab character is not recognized as a delimiter)
# key names may contain interior spaces, but are prohibited from containing the apostrophe character "'".
# The value assigned to a key name may contain any characters, but leading and trailing whitespace will be removed from all
# key names and values. Multi line values are not constructed using the conventional "end line with a backslash" semantics.
# Instead the multiline values can be constructed on a single line in the file by using string "\u000A" as an encoded linefeed
# and/or string "\u000D" as an encoded carriage return. "\u0009" can also be used for an encoded tab character within a value.
# Other escape sequences such as '\t', '\r', '\n', or other unicode characters encodings are not interpreted (remain literal).

function variable_name_refers_to_an_associative_array() {
    local variable_name=$1
    local declare_command_output="$(declare -p $variable_name 2>/dev/null)"
    if [[ "$declare_command_output" == "declare -A"* ]] ; then
        return 0
    else
        return 1
    fi
}

unset trimmed_whitespace_string
trimmed_whitespace_string=""

function set_trimmed_whitespace_string() {
    local string=$1
    trimmed_whitespace_string="$(echo $string | xargs -0)"
}

function property_line_is_commented() {
    local line=$1
    set_trimmed_whitespace_string "$line"
    local trimmed_line="$trimmed_whitespace_string"
    if [ ${#trimmed_line} == 0 ] ; then
        return 0 # empty lines are commented
    fi
    local first_nonspace_character=${trimmed_line:0:1} 
    if [ "$first_nonspace_character" == "#" ] || [ "$first_nonspace_character" == "!" ] ; then
        return 0 # start with comment character
    fi
    return 1
}

unset index_of_property_line_delimiter
index_of_property_line_delimiter=-1

function find_and_set_index_of_property_line_delimiter() {
    local line=$1
    local line_length=${#line}
    index_of_property_line_delimiter=-1 # default / not found
    local pos=0
    while [ $pos -lt $line_length ] ; do
        local character=${line:pos:1}
        if [ "$character" == "=" ] || [ "$character" == ":" ] ; then
            index_of_property_line_delimiter=$pos
            break
        fi
        pos=$((pos+1))
    done
}

unset escaped_string_for_eval
escaped_string_for_eval=""

function set_escaped_string_for_eval() {
    local string=$1
    local string_length=${#string}
    escaped_string_for_eval=""
    local pos=0
    local TAB=$'\t'
    local CR=$'\r'
    local LF=$'\n'
    while [ $pos -lt $string_length ] ; do
        local character_at_position="${string:$pos:1}"
        if [ "$character_at_position" == "'" ] ; then
            escaped_string_for_eval+="'\"'\"'"
        else
            if [ "$character_at_position" == "\\" ] ; then
                local candidate_escape_string="${string:$pos:6}"
                if [ ${#candidate_escape_string} -eq 6 ] ; then
                    if [ "$candidate_escape_string" == "\\u0009" ] ; then
                        escaped_string_for_eval+="'"
                        escaped_string_for_eval+="$'\t'"
                        escaped_string_for_eval+="'"
                        pos=$(($pos+6))
                        continue
                    fi
                    if [ "$candidate_escape_string" == "\\u000A" ] ; then
                        escaped_string_for_eval+="'"
                        escaped_string_for_eval+="$'\n'"
                        escaped_string_for_eval+="'"
                        pos=$(($pos+6))
                        continue
                    fi
                    if [ "$candidate_escape_string" == "\\u000D" ] ; then
                        escaped_string_for_eval+="'"
                        escaped_string_for_eval+="$'\r'"
                        escaped_string_for_eval+="'"
                        pos=$(($pos+6))
                        continue
                    fi
                fi
            fi
            escaped_string_for_eval+="$character_at_position"
        fi
        pos=$(($pos+1))
    done
}

function string_contains_apostrophe() {
    local string=$1
    local apostrophe="'"
    [[ "$string" == *"$apostrophe"* ]]
}

function parse_property_line() {
    local line=$1
    local associative_array_name=$2
    local key_name=""
    local value=""
    if property_line_is_commented "$line" ; then
        return 0
    fi
    find_and_set_index_of_property_line_delimiter "$line"
    if [ $index_of_property_line_delimiter -eq 0 ] ; then
        echo "warning: ignoring property file ($property_file_path) line beginning with delimiter: $line" >&2
        return 1
    fi
    if [ $index_of_property_line_delimiter -eq -1 ] ; then
        # no delimiter .. so key_name is entire line (trimming whitespace), with no value
        set_trimmed_whitespace_string "$line"
        key_name="$trimmed_whitespace_string"
    else
        key_length=$index_of_property_line_delimiter
        local key_name_untrimmed=${line:0:$key_length}
        set_trimmed_whitespace_string "$key_name_untrimmed"
        key_name="$trimmed_whitespace_string"
        local line_length=${#line}
        local value_start_pos=$(($index_of_property_line_delimiter+1))
        local value_length=$(($line_length-$index_of_property_line_delimiter-1))
        local value_untrimmed=${line:$value_start_pos:$value_length}
        set_trimmed_whitespace_string "$value_untrimmed"
        local value_unescaped="$trimmed_whitespace_string"
        set_escaped_string_for_eval "$value_unescaped"
        value="$escaped_string_for_eval"
    fi
    if string_contains_apostrophe "$key_name" ; then
        echo "warning: ignoring property file ($property_file_path) key name which contains the apostrophe character: $key_name" >&2
        return 1
    fi
    local assignment_command="$associative_array_name['$key_name']='$value'"
    eval $assignment_command
}

function parse_property_file() {
    local property_file_path=$1
    local associative_array_name=$2 # array names must be proper identifiers (no spaces)
    if ! [ -r $property_file_path ] ; then
        echo "error: filepath $property_file_path was passed to function parse_property_file() but did not refer to a readable file" >&2
        return 1
    fi
    if ! variable_name_refers_to_an_associative_array $associative_array_name ; then
        echo "error: variable name '$associative_array_name' was passed to function parse_property_file() but was not available in the environment, or did not refer to a created associative array." >&2
        return 1
    fi
    while read line; do
        parse_property_line "$line" "$associative_array_name"
    done < $property_file_path
    local detect_mixed_mysql_properties_expression_1="if [ ! -z \${$associative_array_name[\"mysql_database_name\"]} ] && [ ! -z \${$associative_array_name[\"mysql_blue_database_name\"]} ] ; then echo \"both\" ; fi"
    local mysql_output_1=$( eval $detect_mixed_mysql_properties_expression_1 )
    local detect_mixed_mysql_properties_expression_2="if [ ! -z \${$associative_array_name[\"mysql_database_name\"]} ] && [ ! -z \${$associative_array_name[\"mysql_green_database_name\"]} ] ; then echo \"both\" ; fi"
    local mysql_output_2=$( eval $detect_mixed_mysql_properties_expression_2 )
    local detect_mixed_clickhouse_properties_expression_1="if [ ! -z \${$associative_array_name[\"clickhouse_database_name\"]} ] && [ ! -z \${$associative_array_name[\"clickhouse_blue_database_name\"]} ] ; then echo \"both\" ; fi"
    local clickhouse_output_1=$( eval $detect_mixed_clickhouse_properties_expression_1 )
    local detect_mixed_clickhouse_properties_expression_2="if [ ! -z \${$associative_array_name[\"clickhouse_database_name\"]} ] && [ ! -z \${$associative_array_name[\"clickhouse_green_database_name\"]} ] ; then echo \"both\" ; fi"
    local clickhouse_output_2=$( eval $detect_mixed_clickhouse_properties_expression_2 )
    if [ "$mysql_output_1" == "both" ] || [ "$mysql_output_2" == "both" ] || [ "$clickhouse_output_1" == "both" ] || [ "$clickhouse_output_2" == "both" ] ; then
        echo "warning: property file ($property_file_path) contains settings for both single-database and for dual-database properties. This is error-prone and should be corrected. Read the commented documentation." >&2
    fi
    return 0
}

function remove_credentials_from_properties() {
    local associative_array_name=$1 # array names must be proper identifiers (no spaces)
    if ! variable_name_refers_to_an_associative_array $associative_array_name ; then
        echo "error: variable name '$associative_array_name' was passed to function parse_property_file() but was not available in the environment, or did not refer to a created associative array." >&2
        return 1
    fi
    for key_name in "mysql_server_username" "mysql_server_password" "clickhouse_server_username"  "clickhouse_server_password" ; do
        local unset_command="unset $associative_array_name['$key_name']"
        eval $unset_command
    done
}
