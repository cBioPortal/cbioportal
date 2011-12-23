# Load up the Bladder Meta Data File
./importCancerStudy.pl $CGDS_DATA_HOME/bladder/bladder.txt

# Imports All Case Lists
./importCaseList.pl $CGDS_DATA_HOME/bladder/case_lists

# Imports Clinical Data
./importClinicalData.pl $CGDS_DATA_HOME/bladder/data_clinical.txt

# Imports Mutation Data
./importProfileData.pl --data $CGDS_DATA_HOME/bladder/data_mutations.txt --meta $CGDS_DATA_HOME/bladder/meta_mutations.txt --dbmsAction clobber

# Imports Copy Number Data
./importProfileData.pl --data $CGDS_DATA_HOME/bladder/data_CNA.txt --meta $CGDS_DATA_HOME/bladder/meta_CNA.txt --dbmsAction clobber

# Imports mRNA Data
./importProfileData.pl --data $CGDS_DATA_HOME/bladder/data_expression.txt --meta $CGDS_DATA_HOME/bladder/meta_expression.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/bladder/data_expression_Zscores.txt --meta $CGDS_DATA_HOME/bladder/meta_expression_Zscores.txt --dbmsAction clobber

# Imports Methylation Data
./importProfileData.pl --data $CGDS_DATA_HOME/bladder/data_methylation.txt --meta $CGDS_DATA_HOME/bladder/meta_methylation.txt --dbmsAction clobber