# Clear the Database
./resetDb.pl

# Load up Entrez Genes
./importGenes.pl ../data/human_genes.txt

# Load up all microRNA IDs
./importMicroRna.pl ../data/microRNA/microRNAs.txt