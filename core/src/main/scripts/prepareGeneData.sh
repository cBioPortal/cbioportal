#!/bin/bash

echo "downloading gene_info.gz from ncbi.nih.gov..."
wget -P /tmp/ ftp://ftp.ncbi.nih.gov/gene/DATA/gene_info.gz

echo "extracting gene_info.gz and selecting human gene data..."
gunzip /tmp/gene_info.gz
grep ^9606 /tmp/gene_info > /tmp/human-genes.txt

echo "copying to $PORTAL_DATA_HOME/reference-data, /tmp/human-genes.txt..."
cp /tmp/human-genes.txt $PORTAL_DATA_HOME/reference-data/

echo "cleaning up /tmp..."
rm -f /tmp/gene_info
rm -f /tmp/human-genes.txt

echo "done."
