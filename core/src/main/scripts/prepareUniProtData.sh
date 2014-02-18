#!/bin/bash

rm $PORTAL_DATA_HOME/reference-data/HUMAN_9606_idmapping.dat.gz

echo "downloading idmapping.dat from uniprot.org..."
wget -P $PORTAL_DATA_HOME/reference-data/ ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/idmapping/by_organism/HUMAN_9606_idmapping.dat.gz

echo "extracting UniProt --> GeneID mapping..."
gunzip $PORTAL_DATA_HOME/reference-data/HUMAN_9606_idmapping.dat.gz
grep GeneID $PORTAL_DATA_HOME/reference-data/HUMAN_9606_idmapping.dat > $PORTAL_DATA_HOME/reference-data/uniprot-to-geneId.dat
awk '{print $3"\t"$1}' $PORTAL_DATA_HOME/reference-data/uniprot-to-geneId.dat | sort -n > $PORTAL_DATA_HOME/reference-data/uniprot-id-mapping.txt

echo "cleaning up /tmp..."
rm -f $PORTAL_DATA_HOME/reference-data/HUMAN_9606_idmapping.dat
rm -f $PORTAL_DATA_HOME/reference-data/uniprot-to-geneId.dat
rm -f $PORTAL_DATA_HOME/reference-data/uniprot-id-mapping.txt

echo "done."
