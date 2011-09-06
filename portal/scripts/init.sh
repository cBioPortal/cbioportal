# Clear the Database
./resetDb.pl

# Load up Users
./importUsers.pl $CGDS_DATA_HOME/users.txt

# Load up Entrez Genes
./importGenes.pl $CGDS_DATA_HOME/human_genes.txt

# Load up all microRNA IDs
./importMicroRna.pl $CGDS_DATA_HOME/microRNA/microRNAs.txt

# Load up Cancer Types
./importTypesOfCancer.pl $CGDS_DATA_HOME/cancers.txt
