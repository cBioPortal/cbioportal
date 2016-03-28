#!/bin/bash

echo "downloading sif from pc..."
rm $PORTAL_DATA_HOME/reference-data/pc.sif.gz
rm $PORTAL_DATA_HOME/reference-data/pc.sif
wget http://www.pathwaycommons.org/pc2/downloads/Pathway%20Commons.7.All.EXTENDED_BINARY_SIF.hgnc.sif.gz -O $1
echo "unzipping..."

gunzip $1 > ./pc2UnfilteredData.sif
#Filter out interactions to a small subset since there is about 4.5M interactions in the uncompressed file
python ./processPathwayCommonsInteractions.py ./pc2UnfilteredData.sif $PORTAL_DATA_HOME/reference-data/pc.sif

#Clean-up
rm ./pc2UnfilteredData.sif
rm $1

echo "done"
