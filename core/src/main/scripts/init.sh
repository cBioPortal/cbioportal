# First, verify if all symbols in the sample genesets are latest
./verifyGeneSets.pl $PORTAL_DATA_HOME/reference-data/human_genes.txt

# Clear the Database
./resetDb.pl

# Load up Entrez Genes
./importGenes.pl $PORTAL_DATA_HOME/reference-data/human-genes.txt

# Load up MicroRNA IDs
./importMicroRNAIDs.pl $PORTAL_DATA_HOME/reference-data/id_mapping_mirbase.txt

# Load up Cancer Types
./importTypesOfCancer.pl $PORTAL_DATA_HOME/reference-data/public-cancers.txt

# Load up Sanger Cancer Gene Census
./importSangerCensus.pl $PORTAL_DATA_HOME/reference-data/sanger_gene_census.txt

# Load UniProt Mapping Data
# You must run:  ./prepareUniProtIdMapping.sh first.
./importUniProtIdMapping.pl $PORTAL_DATA_HOME/reference-data/uniprot-id-mapping.txt

# Network
./loadNetwork.sh

# Drug
./importPiHelperData.pl
