#!/bin/bash

echo "downloading sif from pc..."
rm $PORTAL_DATA_HOME/reference-data/pc.sif.gz
rm $PORTAL_DATA_HOME/reference-data/pc.sif
wget http://www.pathwaycommons.org/pc2/downloads/Pathway%20Commons.7.All.EXTENDED_BINARY_SIF.hgnc.sif.gz -O $1
echo "unzipping..."
gunzip $1

echo "done"
