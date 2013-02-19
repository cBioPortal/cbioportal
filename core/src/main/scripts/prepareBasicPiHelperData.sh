#!/bin/bash

# ***
# Downloads, unzips and puts the files under $CGDS_DATA_HOME/reference-data/pihelper .
#
# Assumes you have unzip and wget installed in your PATH
# ***

# Check if we have all the variables set right
echo -ne "Checking if the variables are set...\t\t" &&
[ ! -z "${PORTAL_HOME} " ] && bash $PORTAL_HOME/core/src/main/scripts/env.sh && export ENV_CHECK=1 || export ENV_CHECK=0

if [ $ENV_CHECK -lt 1 ]
then
	echo "[ failed ]"
	exit -1
else
	echo "[ done ]"
fi

# configurables
TMPDIR="$CGDS_DATA_HOME/reference-data"
TMPDIRDB=${TMPDIR}/pihelper

# Create the dir and cd into that
mkdir -p ${TMPDIR} && 
cd ${TMPDIR} &&

echo -ne "Downloading files...\t\t" &&
# Download drug data
wget --quiet "http://cbio-cancer-genomics-portal.googlecode.com/files/portal-pihelper-data-basic.zip" > /dev/null && 
echo "[ done ]" &&

echo -ne "Unzipping files...\t\t" &&
unzip portal-pihelper-data-basic.zip > /dev/null &&
mv portal-pihelper-data-basic pihelper &&
echo "[ done ]" &&

# Go back to where we were
cd - > /dev/null
