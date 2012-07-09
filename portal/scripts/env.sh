#!/bin/bash

# rewrite of env.pl in BASH
#
# example usage in BASH scripts:
#	bash env.sh && echo "everthing is OK" || echo "we are missing variables"

# checks to make sure the basic enviroment variables are set
[ ! -z "${JAVA_HOME}" ] &&
[ ! -z "${PORTAL_HOME}" ] &&
[ ! -z "${CGDS_HOME}" ] &&
[ ! -z "${CGDS_DATA_HOME}" ] && exit 0 || exit -1
