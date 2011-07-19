
# Delete all preprocessed files
rm -v ../data/breast/processed_*

# Load up the Breast Meta Data File
./ImportCancerStudy.pl ../data/breast/breast.txt

# Pre-process data for import
# Runs:
# /scripts/ovarian/gen-rae.py
# -->  this script converts RefSeq IDs to Entrez Gene Ids.
# /scripts/ovarian/gen-mrna-expression.py
# -->  this script also converts RefSeq IDs to Entrez Gene Ids.
#./ovarian/preprocess-all.py

# Imports all NCI Human Genes
./importGenes.pl ../data/human_genes.txt

# Imports all microRNA IDs
./importMicroRna.pl ../data/microRNA/microRNAs.txt

# Imports All Case Lists
./importCaseList.pl $CGDS_HOME/data/breast/case_lists

# Imports Mutation Data
./importProfileData.pl --data $CGDS_HOME/data/breast/data_mutations.txt --meta $CGDS_HOME/data/breast/meta_mutations.txt --dbmsAction clobber

# Imports Copy Number Data
./importProfileData.pl --data $CGDS_HOME/data/breast/data_aCGH.txt --meta $CGDS_HOME/data/breast/meta_aCGH.txt --dbmsAction clobber

# Imports MRNA Expression Data Files
./importProfileData.pl --data $CGDS_HOME/data/breast/data_mRNA_DBCG.txt --meta $CGDS_HOME/data/breast/meta_mRNA_DBCG.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_HOME/data/breast/data_mRNA_DBCG_Z.txt --meta $CGDS_HOME/data/breast/meta_mRNA_DBCG_Z.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_HOME/data/breast/data_mRNA_FW_MDG.txt --meta $CGDS_HOME/data/breast/meta_mRNA_FW_MDG.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_HOME/data/breast/data_mRNA_MicMa.txt --meta $CGDS_HOME/data/breast/meta_mRNA_MicMa.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_HOME/data/breast/data_mRNA_ULL.txt --meta $CGDS_HOME/data/breast/meta_mRNA_ULL.txt --dbmsAction clobber

# add give users access rights
./importAccessRights.pl $CGDS_HOME/data/breast/users.txt

# Calculate Mutation Frequencies
#./calculateMutationFrequencies.py ova_4way_complete
