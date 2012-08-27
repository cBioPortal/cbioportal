#!/bin/bash

# ***
# Downloads, unzips and puts the files under $CGDS_DATA_HOME/reference-data/ (DrugBank).
#
# Assumes you have unzip and wget installed in your PATH
# ***

# Check if we have all the variables set right
echo -ne "Checking if the variables are set...\t\t" &&
[ ! -z "${PORTAL_HOME} " ] && bash $PORTAL_HOME/portal/scripts/env.sh && export ENV_CHECK=1 || export ENV_CHECK=0

if [ $ENV_CHECK -lt 1 ]
then
	echo "[ failed ]"
	exit -1
else
	echo "[ done ]"
fi

# configurables
TMPDIR="$CGDS_DATA_HOME/reference-data"

# Here goes the importing process
TMPDIRDB=${TMPDIR}/drugbank


# Create the dir and cd into that
mkdir -p ${TMPDIRDB} && 
cd ${TMPDIRDB} &&

echo -ne "Downloading files...\t\t" &&
# Download drug data
wget --quiet "http://www.drugbank.ca/system/downloads/current/drugbank.xml.zip" > /dev/null && 
# Download gene target information
wget --quiet "http://www.drugbank.ca/system/downloads/current/target_links.csv.zip" > /dev/null &&
# Unzip those two
echo "[ done ]" &&

echo -ne "Unzipping files...\t\t" &&
unzip drugbank.xml.zip > /dev/null &&
rm -f drugbank.xml.zip &&
unzip target_links.csv.zip > /dev/null &&
rm -f target_links.csv.zip &&
echo "[ done ]" &&

# Go back to where we were
cd - > /dev/null
