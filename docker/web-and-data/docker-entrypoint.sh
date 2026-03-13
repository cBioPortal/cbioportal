#!/usr/bin/env bash
set -eo pipefail
shopt -s nullglob

BAKED_IN_WAR_CONFIG_FILE=/cbioportal-webapp/WEB-INF/classes/application.properties
CUSTOM_PROPERTIES_FILE="$PORTAL_HOME/application.properties"
DEFAULT_PORT=5000

# check to see if this file is being run or sourced from another script
_is_sourced() {
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
        if [[ -n "$prop" ]]; then
            before_equal_sign="${prop%%=*}"
            after_equal_sign="${prop#*=}"
            updated_before_equal_sign="${before_equal_sign//./_}"
            prop="${updated_before_equal_sign}=${after_equal_sign}"
            if [[ $param == spring.datasource.url ]]; then
                echo $(sed -r "s/^([^=]+)=([^\?]+).*/\1=\2/" <<< $prop)
            else 
                echo $prop
            fi
        fi
    done
}

parse_connection_string() {
    readonly URI_REGEX='^(([^:/?#]+):)+?(//((([^:/?#]+)@)?([^:/?#]+)(:([0-9]+))?))?(/([^?#]*))(\?([^#]*))?(#(.*))?'
    echo db_host=$([[ "$1" =~ $URI_REGEX ]] && echo "${BASH_REMATCH[7]}")
    echo db_port=$([[ "$1" =~ $URI_REGEX ]] && echo "${BASH_REMATCH[9]}")
}

check_db_connection() {
    eval $(parse_db_params_from_config_and_command_line $@)
    
    if [[ -n $db_host ]] || [[ -n $db_portal_db_name ]] || [[ -n $db_use_ssl ]]; then
        echo "----------------------------------------------------------------------------------------------------------------"
        echo "-- Connection error:"
        echo "-- Deprecated properties in use. Please use 'db.connection_string' property instead."
        echo "----------------------------------------------------------------------------------------------------------------"
        exit 1
    fi

    if [[ -n $db_connection_string ]]; then 
        eval "$(parse_connection_string $db_connection_string)"
    fi
    
    if [[ -n $spring_datasource_url ]]; then
        eval "$(parse_connection_string $spring_datasource_url)"
    fi

    if [ -z ${db_port+x} ]; then
        if [[ $db_host == *":"* ]]; then
            db_port=$(echo ${db_host} | cut -d: -f2)
        else
            db_port="3306"
        fi
    fi

    while ! mysqladmin ping -s -h$(echo ${db_host} | cut -d: -f1) -P${db_port} -u${spring_datasource_username} -p${spring_datasource_password};
    do
        sleep 5s;
        echo "Attempting reconnect to database..."
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
    pattern1='(java)*(org\.cbioportal\.PortalApplication)'
    pattern2='(java)*(-jar)*(cbioportal-exec.jar)'
    found=false

    for arg in "$@"; do
        if [[ "$arg" =~ $pattern1 ]] || [[ "$arg" =~ $pattern2 ]]; then
            found=true
            break
        fi
    done
    
    if [ "$found" = true ]; then
        echo "Running Migrate DB Script"
        check_db_connection $@
        migrate_db $@
    fi
    
    # Handle custom port configuration
    if [[ -n "$CUSTOM_PORT" ]]; then
        export SERVER_PORT="$CUSTOM_PORT"
        echo "Using custom server port: $CUSTOM_PORT"
    else
        export SERVER_PORT="$DEFAULT_PORT"
        echo "Using default server port: $DEFAULT_PORT"
    fi

    exec "$@"
}

if ! _is_sourced; then
    _main "$@"
fi
