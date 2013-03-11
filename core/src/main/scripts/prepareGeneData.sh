#!/bin/bash

rm -rf $PORTAL_DATA_HOME/reference-data/gene_info_tmp
mkdir $PORTAL_DATA_HOME/reference-data/gene_info_tmp

echo "downloading gene_info.gz from ncbi.nih.gov..."
wget -P $PORTAL_DATA_HOME/reference-data/gene_info_tmp/ ftp://ftp.ncbi.nih.gov/gene/DATA/gene_info.gz

echo "extracting gene_info.gz and selecting human gene data..."
gunzip $PORTAL_DATA_HOME/reference-data/gene_info_tmp/gene_info.gz
grep ^9606 $PORTAL_DATA_HOME/reference-data/gene_info_tmp/gene_info > $PORTAL_DATA_HOME/reference-data/gene_info_tmp/human-genes.txt

echo "copying to $PORTAL_DATA_HOME/reference-data, $PORTAL_DATA_HOME/reference-data/gene_info_tmp/human-genes.txt..."
cp $PORTAL_DATA_HOME/reference-data/gene_info_tmp/human-genes.txt $PORTAL_DATA_HOME/reference-data/

echo "cleaning up /tmp..."
rm -rf $PORTAL_DATA_HOME/reference-data/gene_info_tmp

echo "done."
