#!/usr/bin/env bash
set -eo pipefail
shopt -s nullglob

BAKED_IN_WAR_CONFIG_FILE=/cbioportal-webapp/WEB-INF/classes/application.properties
CUSTOM_PROPERTIES_FILE="$PORTAL_HOME/application.properties"

# check to see if this file is being run or sourced from another script
_is_sourced() {
    # https://unix.stackexchange.com/a/215279
    [ "${#FUNCNAME[@]}" -ge 2 ] \
            && [ "${FUNCNAME[0]}" = '_is_sourced' ] \
                && [ "${FUNCNAME[1]}" = 'source' ]
}

parse_db_params_from_command_line() {
    echo $@ | sed 's/-D/\n-D/g' | grep -- '-Dspring' | sed 's/-D//g' | grep db.
}

parse_db_params_from_config_and_command_line() {
    if [[ -f $CUSTOM_PROPERTIES_FILE ]]; then
        PROPERTIES_FILE=$CUSTOM_PROPERTIES_FILE
    else
        PROPERTIES_FILE=$BAKED_IN_WAR_CONFIG_FILE
    fi
    for param in db.host spring.datasource.username db.portal_db_name spring.datasource.password spring.datasource.url; do
        if $(parse_db_params_from_command_line $@ | grep -q $param); then
            prop=$(parse_db_params_from_command_line $@ | grep "^$param" || [[ $? == 1 ]])
        else
            prop=$(grep -v '^#' $PROPERTIES_FILE | grep "^$param" || [[ $? == 1 ]])
        fi
        if [[ -n "$prop" ]]
        then
            # Replace dot in parameter name with underscore.
            #prop=$(sed "s/\([^=]*\)\./\1_/g" <<< "$prop")
            before_equal_sign="${prop%%=*}"
            after_equal_sign="${prop#*=}"
            updated_before_equal_sign="${before_equal_sign//./_}"
            prop="${updated_before_equal_sign}=${after_equal_sign}"
            if [[ $param == spring.datasource.url ]]
            then
                # Remove the parameters (?...) from the connection URL.
                echo $(sed -r "s/^([^=]+)=([^\?]+).*/\1=\2/" <<< $prop)
            else 
                echo $prop
            fi
        fi
    done
}

parse_connection_string() {
    # Adapted from: https://stackoverflow.com/a/45977232
    readonly URI_REGEX='^(([^:/?#]+):)+?(//((([^:/?#]+)@)?([^:/?#]+)(:([0-9]+))?))?(/([^?#]*))(\?([^#]*))?(#(.*))?'
    #                    ↑↑             ↑  ↑↑↑            ↑         ↑ ↑            ↑ ↑        ↑  ↑        ↑ ↑
    #                    |2 scheme      |  ||6 userinfo   7 host    | 9 port       | 11 rpath |  13 query | 15 fragment
    #                    1 scheme:      |  |5 userinfo@             8 :…           10 path    12 ?…       14 #…
    #                                   |  4 authority
    #                                   3 //…
    echo db_host=$([[ "$1" =~ $URI_REGEX ]] && echo "${BASH_REMATCH[7]}")
    echo db_port=$([[ "$1" =~ $URI_REGEX ]] && echo "${BASH_REMATCH[9]}")
}

check_db_connection() {
    eval $(parse_db_params_from_config_and_command_line $@)
    
    if [[ -n $db_host ]] || [[ -n $db_portal_db_name ]] || [[ -n $db_use_ssl ]]
    then
        echo "----------------------------------------------------------------------------------------------------------------"
        echo "-- Connection error:"
        echo "-- You try to connect to the database using the deprecated 'db.host', 'db.portal_db_name' and 'db.use_ssl' properties."
        echo "-- Please remove these properties and use the 'db.connection_string' property instead. See https://docs.cbioportal.org/deployment/customization/application.properties-reference/"
        echo "-- for assistance on building a valid connection string."
        echo "------------------------------------------------------------f---------------------------------------------------"
        exit 1
    fi

    if [[ -n $db_connection_string ]]
    then 
        eval "$(parse_connection_string $db_connection_string)"
    fi
    
    if [[ -n $spring_datasource_url ]]
    then
        eval "$(parse_connection_string $spring_datasource_url)"
    fi

    if [ -z ${db_port+x} ] # is $db_port unset?
    then 
        if [[ $db_host == *":"* ]]; then # does $db_host contain a ':'?
            db_port=$(echo ${db_host} | cut -d: -f2) # grab what's after the ':'
        else
            db_port="3306" # use default port
        fi
    fi

    while ! mysqladmin ping -s -h$(echo ${db_host} | cut -d: -f1) -P${db_port} -u${spring_datasource_username} -p${spring_datasource_password};
    do
        sleep 5s;
        if [ -n "$SHOW_DEBUG_INFO" ] && [ "$SHOW_DEBUG_INFO" != "false" ]; then
            echo mysqladmin ping -s -h$(echo ${db_host} | cut -d: -f1) -P${db_port} -u${spring_datasource_username} -p${spring_datasource_password}
        fi
        echo "Database not available yet (first time can take a few minutes to load seed database)... Attempting reconnect..."
    done
    echo "Database connection success"
}

migrate_db() {
    echo "Migrating database if necessary..."
    POTENTIAL_DB_PARAMS=$@

    if [[ -f $CUSTOM_PROPERTIES_FILE ]]; then
        python3 /core/scripts/migrate_db.py -y -p $CUSTOM_PROPERTIES_FILE -s /cbioportal/db-scripts/migration.sql
    else
        python3 /core/scripts/migrate_db.py -y -p <(parse_db_params_from_config_and_command_line $POTENTIAL_DB_PARAMS) -s /cbioportal/db-scripts/migration.sql
    fi
}

_main() {
    # when running the webapp, check db and do migration first
    # check if command is something like "java -jar webapp-runner.jar"
    
    # Define the regex pattern
    pattern1='(java)*(org\.cbioportal\.PortalApplication)'
    pattern2='(java)*(-jar)*(cbioportal-exec.jar)'
    found=false
    
    # Loop through all arguments
    for arg in "$@"; do
        if [[ "$arg" =~ $pattern1 ]] || [[ "$arg" =~ $pattern2 ]]; then
            found=true
            break
        fi
    done
    
    # Check if the application is found in the arguments
    if [ "$found" = true ]; then
        echo "Running Migrate DB Script"
        # Custom logic to handle the case when "org.cbioportal.PortalApplication" is present
        # Parse database config. Use command line parameters (e.g. -Ddb.host) if
        # available, otherwise use application.properties
        if [ -n "$SHOW_DEBUG_INFO" ] && [ "$SHOW_DEBUG_INFO" != "false" ]; then
            echo "Using database config:"
            parse_db_params_from_config_and_command_line $@
        fi

        check_db_connection $@
        migrate_db $@

        if [ -n "$SHOW_DEBUG_INFO" ] && [ "$SHOW_DEBUG_INFO" != "false" ]; then
            echo Running: "$@"
        fi
    fi
    exec "$@"
}

# If we are sourced from elsewhere, don't perform any further actions
if ! _is_sourced; then
    _main "$@"
fi
