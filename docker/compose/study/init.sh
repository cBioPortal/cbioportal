#!/usr/bin/env bash
# download data hub study and import

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

DATAHUB_STUDIES="${DATAHUB_STUDIES:-lgg_ucsf_2014}"
for study in ${DATAHUB_STUDIES}; do
    mkdir -p ${study} && \
        cd ${study} && \
        wget -O ${study}.tar.gz "http://download.cbioportal.org/${study}.tar.gz"
        tar xvfz ${study}.tar.gz
done
