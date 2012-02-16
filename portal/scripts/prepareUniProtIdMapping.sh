#!/bin/bash

echo "downloading idmapping.dat from uniprot.org..."
wget ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/idmapping/by_organism/HUMAN_9606_idmapping.dat.gz

echo "extracting UniProt --> GeneID mapping..."
gunzip HUMAN_9606_idmapping.dat.gz
grep GeneID HUMAN_9606_idmapping.dat > uniprot_to_geneId.dat
awk '{print $3"\t"$1}' uniprot_to_geneId.dat | sort -n > uniprot_id_mapping.txt
#echo "found " `wc -l uniprot_id_mapping.txt` " mappings for " `cut -f 1 uniprot_id_mapping.txt | sort -u | wc -l` " entrez gene ids"

echo "copying to ../sample_data/genes/uniprot_id_mapping.txt..."
cp uniprot_id_mapping.txt ../sample_data/genes/

echo "done."
