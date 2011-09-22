# Load up the Breast Meta Data File
./importCancerStudy.pl $CGDS_DATA_HOME/breast/breast.txt

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
