#!/bin/bash

wget -P $PORTAL_DATA_HOME/reference-data/ ftp://ftp.ncbi.nih.gov//gene/DATA/GENE_INFO/Mammalia/Homo_sapiens.gene_info.gz
gunzip $PORTAL_DATA_HOME/reference-data/Homo_sapiens.gene_info.gz
mv Homo_sapiens.gene_info human-genes.txt

echo "done."
