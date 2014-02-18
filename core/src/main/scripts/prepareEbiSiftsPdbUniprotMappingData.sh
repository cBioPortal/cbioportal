#!/bin/bash

rm $PORTAL_DATA_HOME/reference-data/pdb_chain_uniprot.tsv.gz
rm $PORTAL_DATA_HOME/reference-data/pdb_chain_taxonomy.tsv.gz

echo "downloading..."
wget -P $PORTAL_DATA_HOME/reference-data/ ftp://ftp.ebi.ac.uk/pub/databases/msd/sifts/flatfiles/tsv/pdb_chain_uniprot.tsv.gz
wget -P $PORTAL_DATA_HOME/reference-data/ ftp://ftp.ebi.ac.uk/pub/databases/msd/sifts/flatfiles/tsv/pdb_chain_taxonomy.tsv.gz

rm $PORTAL_DATA_HOME/reference-data/pdb_chain_uniprot.tsv
rm $PORTAL_DATA_HOME/reference-data/pdb_chain_taxonomy.tsv
rm $PORTAL_DATA_HOME/reference-data/pdb_chain_human.tsv

echo "extracting..."
gunzip $PORTAL_DATA_HOME/reference-data/pdb_chain_uniprot.tsv.gz
gunzip $PORTAL_DATA_HOME/reference-data/pdb_chain_taxonomy.tsv.gz

grep '	9606	' $PORTAL_DATA_HOME/reference-data/pdb_chain_taxonomy.tsv > $PORTAL_DATA_HOME/reference-data/pdb_chain_human.tsv

echo "done."
