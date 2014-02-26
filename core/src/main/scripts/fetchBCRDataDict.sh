#!/bin/bash

echo "downloading BCR Data Dictionary (XML)..."
echo
wget --no-check-certificate -O $PORTAL_DATA_HOME/reference-data/TCGA_BCR_DataDictionary.html https://tcga-data.nci.nih.gov/docs/dictionary/
