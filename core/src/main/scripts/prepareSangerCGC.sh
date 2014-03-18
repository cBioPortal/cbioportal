#!/bin/bash

rm $PORTAL_DATA_HOME/reference-data/sanger_gene_census.txt

echo "downloading..."
wget -P $PORTAL_DATA_HOME/reference-data/ ftp://ftp.sanger.ac.uk/cancer_gene_census.tsv

mv $PORTAL_DATA_HOME/reference-data/cancer_gene_census.tsv $PORTAL_DATA_HOME/reference-data/sanger_gene_census.txt

echo "done."