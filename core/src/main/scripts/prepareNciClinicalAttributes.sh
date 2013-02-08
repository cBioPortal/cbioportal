#!/bin/bash

echo "downloading BCR Data Dictionary (XML)..."
echo
wget -NP $PORTAL_DATA_HOME/reference-data https://tcga-data.nci.nih.gov/docs/dictionary/TCGA_BCR_DataDictionary.xml
