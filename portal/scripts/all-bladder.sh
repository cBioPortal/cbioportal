# Load up the Bladder Meta Data File
./importCancerStudy.pl $CGDS_DATA_HOME/bladder/bladder.txt

# Imports All Case Lists
./importCaseList.pl $CGDS_DATA_HOME/bladder/case_lists

# Imports Mutation Data
./importProfileData.pl --data $CGDS_DATA_HOME/bladder/data_mutations.txt --meta $CGDS_DATA_HOME/bladder/meta_mutations.txt --dbmsAction clobber

# Imports Copy Number Data
./importProfileData.pl --data $CGDS_DATA_HOME/bladder/data_CNA.txt --meta $CGDS_DATA_HOME/bladder/meta_CNA.txt --dbmsAction clobber
