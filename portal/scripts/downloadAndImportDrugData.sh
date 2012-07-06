#!/bin/bash

# ***
# Downloads, unzips and imports the DrugBank data.
#
# Assumes you have unzip and wget installed in your PATH
# ***

# configurables
TMPDIR="/tmp"

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
unzip target_links.csv.zip > /dev/null &&
echo "[ done ]" &&

# Go back to where we were
cd - > /dev/null &&

# Now try to run the import script
echo "Importing downloaded data..." &&
cd $PORTAL_HOME/scripts/ &&
perl importDrugData.pl ${TMPDIRDB}/drugbank.xml ${TMPDIRDB}/target_links.csv &&
echo "[ done ]" &&
# Go back again
cd - > /dev/null &&

# Clean-up
echo -ne "Cleaning up...\t\t" &&
rm -rf ${TMPDIRDB}
echo "[ done ]"
