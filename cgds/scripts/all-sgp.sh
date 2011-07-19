# Delete all preprocessed files
rm -v ../data/sarcoma/processed_*

# Load up the Sarcoma Meta Data File
./ImportCancerStudy.pl ../data/sarcoma/sarcoma.txt

# Imports All Case Lists
./importCaseList.pl $CGDS_HOME/data/sarcoma/case_lists

# Imports Mutation Data
./importProfileData.pl --data $CGDS_HOME/data/sarcoma/data_mutations.txt --meta  $CGDS_HOME/data/sarcoma/meta_mutations.txt --dbmsAction clobber

# Imports RAE Copy Number Data
./importProfileData.pl --data $CGDS_HOME/data/sarcoma/data_CNA.txt --meta $CGDS_HOME/data/sarcoma/meta_CNA.txt --dbmsAction clobber

# Imports MRNA Expression Data Files
./importProfileData.pl --data $CGDS_HOME/data/sarcoma/data_mRNA.txt --meta $CGDS_HOME/data/sarcoma/meta_mRNA.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_HOME/data/sarcoma/data_mRNA_ZbyNormals.txt --meta $CGDS_HOME/data/sarcoma/meta_mrna_ZbyNormals.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_HOME/data/sarcoma/data_mRNA_outlier.txt --meta $CGDS_HOME/data/sarcoma/meta_mrna_outlier.txt --dbmsAction clobber
