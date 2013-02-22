# First, verify if all symbols in the sample genesets are latest
$PORTAL_HOME/core/src/main/scripts/verifyGeneSets.pl $PORTAL_DATA_HOME/reference-data/human_genes.txt

# Clear the Database
$PORTAL_HOME/core/src/main/scripts/resetDb.pl

# Load up Entrez Genes
$PORTAL_HOME/core/src/main/scripts/importGenes.pl $PORTAL_DATA_HOME/reference-data/human_genes.txt

# Load up MicroRNA IDs
$PORTAL_HOME/core/src/main/scripts/importMicroRNAIDs.pl $PORTAL_DATA_HOME/reference-data/id_mapping_mirbase.txt

# Load up Cancer Types
$PORTAL_HOME/core/src/main/scripts/importTypesOfCancer.pl $PORTAL_DATA_HOME/reference-data/public-cancers.txt

# Load up Sanger Cancer Gene Census
$PORTAL_HOME/core/src/main/scripts/importSangerCensus.pl $PORTAL_DATA_HOME/reference-data/sanger_gene_census.txt

# Load UniProt Mapping Data
# You must run:  $PORTAL_HOME/core/src/main/scripts/prepareUniProtIdMapping.sh first.
$PORTAL_HOME/core/src/main/scripts/importUniProtIdMapping.pl $PORTAL_DATA_HOME/reference-data/uniprot_id_mapping.txt

# Network
$PORTAL_HOME/core/src/main/scripts/loadNetwork.sh

# Drug
$PORTAL_HOME/core/src/main/scripts/importPiHelperData.pl
