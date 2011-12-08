# Load up the Endometrioid (UCEC) Meta Data File
./importCancerStudy.pl $CGDS_STAGING_HOME/ucec/ucec.txt

# Imports All Case Lists
./importCaseList.pl $CGDS_STAGING_HOME/ucec/case_lists

# Imports Mutation Data
./importProfileData.pl --data $CGDS_STAGING_HOME/ucec/data_mutations_extended.txt --meta $CGDS_STAGING_HOME/ucec/meta_mutations_extended.txt --dbmsAction clobber

# Imports Copy Number Data
./importProfileData.pl --data $CGDS_STAGING_HOME/ucec/data_CNA.txt --meta $CGDS_STAGING_HOME/ucec/meta_CNA.txt --dbmsAction clobber

# Imports MRNA Expression Data
./importProfileData.pl --data $CGDS_STAGING_HOME/ucec/data_expression_median.txt --meta $CGDS_STAGING_HOME/ucec/meta_expression_median.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_STAGING_HOME/ucec/data_mRNA_median_Zscores.txt --meta $CGDS_STAGING_HOME/ucec/meta_mRNA_median_Zscores.txt --dbmsAction clobber

# Imports Methylation Data
./importProfileData.pl --data $CGDS_STAGING_HOME/ucec/data_methylation.txt --meta $CGDS_STAGING_HOME/ucec/meta_methylation.txt --dbmsAction clobber
