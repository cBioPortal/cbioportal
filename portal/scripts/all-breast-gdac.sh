# Load up the Breast Meta Data File
./importCancerStudy.pl $CGDS_STAGING_HOME/brca/brca.txt

# Imports All Case Lists
./importCaseList.pl $CGDS_STAGING_HOME/brca/case_lists

# Imports Mutation Data
./importProfileData.pl --data $CGDS_STAGING_HOME/brca/data_mutations_extended.txt --meta $CGDS_STAGING_HOME/brca/meta_mutations_extended.txt --dbmsAction clobber

# Imports Copy Number Data
./importProfileData.pl --data $CGDS_STAGING_HOME/brca/data_CNA.txt --meta $CGDS_STAGING_HOME/brca/meta_CNA.txt --dbmsAction clobber

# Imports MRNA Expression Data
./importProfileData.pl --data $CGDS_STAGING_HOME/brca/data_expression_median.txt --meta $CGDS_STAGING_HOME/brca/meta_expression_median.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_STAGING_HOME/brca/data_mRNA_median_Zscores.txt --meta $CGDS_STAGING_HOME/brca/meta_mRNA_median_Zscores.txt --dbmsAction clobber

# Imports Methylation Data
./importProfileData.pl --data $CGDS_STAGING_HOME/brca/data_methylation.txt --meta $CGDS_STAGING_HOME/brca/meta_methylation.txt --dbmsAction clobber
