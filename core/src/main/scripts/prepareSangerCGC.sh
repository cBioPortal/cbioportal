#!/bin/bash

rm $PORTAL_DATA_HOME/reference-data/cancer_gene_census.csv

echo "downloading..."
wget -P $PORTAL_DATA_HOME/reference-data/ http://cancer.sanger.ac.uk/files/cosmic/current_release/cancer_gene_census.csv

echo "done."