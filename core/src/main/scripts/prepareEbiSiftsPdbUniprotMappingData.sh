#!/bin/bash

echo "downloading..."
wget -P $PORTAL_DATA_HOME/reference-data/ ftp://ftp.ebi.ac.uk/pub/databases/msd/sifts/flatfiles/tsv/pdb_chain_uniprot.tsv.gz

echo "extracting..."
gunzip $PORTAL_DATA_HOME/reference-data/sifts_tmp/pdb_chain_uniprot.tsv.gz

echo "done."