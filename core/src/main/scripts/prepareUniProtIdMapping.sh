#!/bin/bash

echo "downloading idmapping.dat from uniprot.org..."
wget -P /tmp/ ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/idmapping/by_organism/HUMAN_9606_idmapping.dat.gz

echo "extracting UniProt --> GeneID mapping..."
gunzip /tmp/HUMAN_9606_idmapping.dat.gz
grep GeneID /tmp/HUMAN_9606_idmapping.dat > /tmp/uniprot-to-geneId.dat
awk '{print $3"\t"$1}' /tmp/uniprot-to-geneId.dat | sort -n > /tmp/uniprot-id-mapping.txt

echo "copying to $PORTAL_DATA_HOME/reference-data../tmp/uniprot-id-mapping.txt..."
cp /tmp/uniprot-id-mapping.txt $PORTAL_DATA_HOME/reference-data/

echo "cleaning up /tmp..."
rm -f /tmp/HUMAN_9606_idmapping.dat
rm -f /tmp/uniprot-to-geneId.dat
rm -f /tmp/uniprot-id-mapping.txt

echo "done."
