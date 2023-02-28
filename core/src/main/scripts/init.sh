# First, verify if all symbols in the sample genesets are latest
#./verifyGeneSets.pl $PORTAL_DATA_HOME/reference-data/human-genes.txt

# Clear the Database
./resetDb.pl

# Load up Entrez Genes
./importGenes.pl $PORTAL_DATA_HOME/reference-data/human-genes.txt $PORTAL_DATA_HOME/reference-data/id_mapping_mirbase.txt $PORTAL_DATA_HOME/reference-data/all_exon_loci.bed

# Load up Cancer Types
./importTypesOfCancer.pl $PORTAL_DATA_HOME/reference-data/public-cancers.txt

# Network
./loadNetwork.sh

# Drug
./importPiHelperData.pl

# Cosmic
# ./prepareCosmicData.sh
./importCosmicData.pl $PORTAL_DATA_HOME/reference-data/CosmicCodingMuts.vcf
