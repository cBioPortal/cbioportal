# Clears all data in the database
./resetDb.pl

# Delete all preprocessed files
rm -v $CGDS_DATA_HOME/breast/processed_*

# Load up the Breast Meta Data File
./ImportCancerStudy.pl $CGDS_DATA_HOME/breast/breast.txt

# Pre-process data for import
# Runs:
# /scripts/ovarian/gen-rae.py
# -->  this script converts RefSeq IDs to Entrez Gene Ids.
# /scripts/ovarian/gen-mrna-expression.py
# -->  this script also converts RefSeq IDs to Entrez Gene Ids.
#./ovarian/preprocess-all.py

# Imports All Case Lists
./importCaseList.pl $CGDS_DATA_HOME/breast/case_lists

# Imports Mutation Data
./importProfileData.pl --data $CGDS_DATA_HOME/breast/data_mutations.txt --meta $CGDS_DATA_HOME/breast/meta_mutations.txt --dbmsAction clobber

# Imports Copy Number Data
./importProfileData.pl --data $CGDS_DATA_HOME/breast/data_aCGH.txt --meta $CGDS_DATA_HOME/breast/meta_aCGH.txt --dbmsAction clobber

# Imports MRNA Expression Data Files
./importProfileData.pl --data $CGDS_DATA_HOME/breast/data_mRNA_DBCG.txt --meta $CGDS_DATA_HOME/breast/meta_mRNA_DBCG.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/breast/data_mRNA_DBCG_Z.txt --meta $CGDS_DATA_HOME/breast/meta_mRNA_DBCG_Z.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/breast/data_mRNA_FW_MDG.txt --meta $CGDS_DATA_HOME/breast/meta_mRNA_FW_MDG.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/breast/data_mRNA_MicMa.txt --meta $CGDS_DATA_HOME/breast/meta_mRNA_MicMa.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/breast/data_mRNA_ULL.txt --meta $CGDS_DATA_HOME/breast/meta_mRNA_ULL.txt --dbmsAction clobber

# Calculate Mutation Frequencies
#./calculateMutationFrequencies.py ova_4way_complete
