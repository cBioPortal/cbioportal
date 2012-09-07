# First, verify if all symbols in the sample genesets are latest
./verifyGeneSets.pl $CGDS_DATA_HOME/reference-data/human_genes.txt

# Clear the Database
./resetDb.pl

# Load up Entrez Genes
./importGenes.pl $CGDS_DATA_HOME/reference-data/human_genes.txt

# Load up MicroRNA IDs
./importMicroRNAIDs.pl $CGDS_DATA_HOME/reference-data/id_mapping_mirbase.txt

# Load up Cancer Types
./importTypesOfCancer.pl $CGDS_DATA_HOME/reference-data/public-cancers.txt

# Load up Sanger Cancer Gene Census
./importSangerCensus.pl $CGDS_DATA_HOME/reference-data/sanger_gene_census.txt

# RPPA
./importProteinArrayInfo.pl $CGDS_DATA_HOME/reference-data/RPPA_antibody_list.txt

# Load UniProt Mapping Data
# You must run:  ./prepareUniProtIdMapping.sh first.
./importUniProtIdMapping.pl $CGDS_DATA_HOME/reference-data/uniprot_id_mapping.txt

# Network
./loadNetwork.sh

# Drug
./importDrugData.pl
