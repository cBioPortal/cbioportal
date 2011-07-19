# Delete all preprocessed files
rm -v $CGDS_DATA_HOME/data/sarcoma/processed_*

# Load up the Sarcoma Meta Data File
./ImportCancerStudy.pl $CGDS_DATA_HOME/sarcoma/sarcoma.txt

# Imports All Case Lists
./importCaseList.pl $CGDS_DATA_HOME/sarcoma/case_lists

# Imports Mutation Data
./importProfileData.pl --data $CGDS_DATA_HOME/sarcoma/data_mutations.txt --meta  $CGDS_DATA_HOME/sarcoma/meta_mutations.txt --dbmsAction clobber

# Imports RAE Copy Number Data
./importProfileData.pl --data $CGDS_DATA_HOME/sarcoma/data_CNA.txt --meta $CGDS_DATA_HOME/sarcoma/meta_CNA.txt --dbmsAction clobber

# Imports MRNA Expression Data Files
./importProfileData.pl --data $CGDS_DATA_HOME/sarcoma/data_mRNA.txt --meta $CGDS_DATA_HOME/sarcoma/meta_mRNA.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/sarcoma/data_mRNA_ZbyNormals.txt --meta $CGDS_DATA_HOME/sarcoma/meta_mrna_ZbyNormals.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/sarcoma/data_mRNA_outlier.txt --meta $CGDS_DATA_HOME/sarcoma/meta_mrna_outlier.txt --dbmsAction clobber
