# First, verify if all symbols in the sample genesets are latest
#./verifyGeneSets.pl $PORTAL_DATA_HOME/reference-data/human-genes.txt

# Clear the Database
./resetDb.pl

# Load up Entrez Genes
./importGenes.pl $PORTAL_DATA_HOME/reference-data/human-genes.txt $PORTAL_DATA_HOME/reference-data/all_exon_loci.bed

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

# Pfam Graphic Data
./importPfamGraphicsData.pl $PORTAL_DATA_HOME/reference-data/pfam-graphics.txt

# Cosmic
# ./prepareCosmicData.sh
./importCosmicData.pl $PORTAL_DATA_HOME/reference-data/CosmicCodingMuts.vcf
