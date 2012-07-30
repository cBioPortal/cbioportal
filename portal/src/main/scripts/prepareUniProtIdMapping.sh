#!/bin/bash

echo "downloading idmapping.dat from uniprot.org..."
wget -P /tmp/ ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/idmapping/by_organism/HUMAN_9606_idmapping.dat.gz

echo "extracting UniProt --> GeneID mapping..."
gunzip /tmp/HUMAN_9606_idmapping.dat.gz
grep GeneID /tmp/HUMAN_9606_idmapping.dat > /tmp/uniprot_to_geneId.dat
awk '{print $3"\t"$1}' /tmp/uniprot_to_geneId.dat | sort -n > /tmp/uniprot_id_mapping.txt
#echo "found " `wc -l uniprot_id_mapping.txt` " mappings for " `cut -f 1 uniprot_id_mapping.txt | sort -u | wc -l` " entrez gene ids"

echo "copying to $CGDS_DATA_HOME/reference-data../tmp/uniprot_id_mapping.txt..."
cp /tmp/uniprot_id_mapping.txt $CGDS_DATA_HOME/reference-data/

echo "cleaning up /tmp..."
rm -f /tmp/HUMAN_9606_idmapping.dat
rm -f /tmp/uniprot_to_geneId.dat
rm -f /tmp/uniprot_id_mapping.txt

echo "done."
