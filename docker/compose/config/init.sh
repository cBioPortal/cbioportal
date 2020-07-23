#!/usr/bin/env bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

cat ${SCRIPT_DIR}/../../../src/main/resources/portal.properties.EXAMPLE | \
    sed 's/db.host=.*/db.host=cbioportal_database:3306/g' | \
    sed 's|db.connection_string=.*|db.connection_string=jdbc:mysql://cbioportal_database:3306/|g' \
> portal.properties
