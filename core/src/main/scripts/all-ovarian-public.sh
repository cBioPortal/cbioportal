#################################################
# WARNING - these scripts should not be used
# to create a Public Portal within the cBio Lab.
# The firehose converter should be used instead.
################################################

# Load up the Ovarian Meta Data File
./importCancerStudy.pl $PORTAL_DATA_HOME/public-override/ov_tcga_pub/ov_tcga_pub.txt

# Imports All Case Lists
./importCaseList.pl $PORTAL_DATA_HOME/public-override/ov_tcga_pub/case_lists

# Imports Clinical Data
./importClinicalData.pl $PORTAL_DATA_HOME/public-override/ov_tcga_pub/ov_tcga_clinical.txt ov_tcga_pub

# Imports Mutation Data
./importProfileData.pl --data $PORTAL_DATA_HOME/public-override/ov_tcga_pub/data_mutations_extended.txt --meta $PORTAL_DATA_HOME/public-override/ov_tcga_pub/meta_mutations_extended.txt --dbmsAction clobber

# Imports Copy Number Data
./importProfileData.pl --data $PORTAL_DATA_HOME/public-override/ov_tcga_pub/data_CNA.txt --meta $PORTAL_DATA_HOME/public-override/ov_tcga_pub/meta_CNA.txt --dbmsAction clobber
./importProfileData.pl --data $PORTAL_DATA_HOME/public-override/ov_tcga_pub/data_CNA_RAE.txt --meta $PORTAL_DATA_HOME/public-override/ov_tcga_pub/meta_CNA_RAE.txt --dbmsAction clobber
./importProfileData.pl --data $PORTAL_DATA_HOME/public-override/ov_tcga_pub/data_log2CNA.txt --meta $PORTAL_DATA_HOME/public-override/ov_tcga_pub/meta_log2CNA.txt --dbmsAction clobber

# Imports MRNA Expression Data
./importProfileData.pl --data $PORTAL_DATA_HOME/public-override/ov_tcga_pub/data_expression_median.txt --meta $PORTAL_DATA_HOME/public-override/ov_tcga_pub/meta_expression_median.txt --dbmsAction clobber
./importProfileData.pl --data $PORTAL_DATA_HOME/public-override/ov_tcga_pub/data_expression_merged_median_Zscores.txt --meta $PORTAL_DATA_HOME/public-override/ov_tcga_pub/meta_expression_merged_median_Zscores.txt --dbmsAction clobber
./importProfileData.pl --data $PORTAL_DATA_HOME/public-override/ov_tcga_pub/data_mRNA_median_Zscores.txt --meta $PORTAL_DATA_HOME/public-override/ov_tcga_pub/meta_mRNA_median_Zscores.txt --dbmsAction clobber
./importProfileData.pl --data $PORTAL_DATA_HOME/public-override/ov_tcga_pub/data_mRNA_unified.txt --meta $PORTAL_DATA_HOME/public-override/ov_tcga_pub/meta_mRNA_unified.txt --dbmsAction clobber
./importProfileData.pl --data $PORTAL_DATA_HOME/public-override/ov_tcga_pub/data_mRNA_unified_Zscores.txt --meta $PORTAL_DATA_HOME/public-override/ov_tcga_pub/meta_mRNA_unified_Zscores.txt --dbmsAction clobber

# Imports miRNA Data
./importProfileData.pl --data $PORTAL_DATA_HOME/public-override/ov_tcga_pub/data_expression_miRNA.txt --meta $PORTAL_DATA_HOME/public-override/ov_tcga_pub/meta_expression_miRNA.txt --dbmsAction clobber
./importProfileData.pl --data $PORTAL_DATA_HOME/public-override/ov_tcga_pub/data_miRNA_median_Zscores.txt --meta $PORTAL_DATA_HOME/public-override/ov_tcga_pub/meta_miRNA_median_Zscores.txt --dbmsAction clobber

# Imports Methylation Data
./importProfileData.pl --data $PORTAL_DATA_HOME/public-override/ov_tcga_pub/data_methylation.txt --meta $PORTAL_DATA_HOME/public-override/ov_tcga_pub/meta_methylation.txt --dbmsAction clobber
./importProfileData.pl --data $PORTAL_DATA_HOME/public-override/ov_tcga_pub/data_brca1_binary_methylation.txt --meta $PORTAL_DATA_HOME/public-override/ov_tcga_pub/meta_brca1_binary_methylation.txt --dbmsAction clobber

# Copy number segment
#./importCopyNumberSegmentData.pl $PORTAL_DATA_HOME/public-override/ov_tcga_pub/ov_tcga_pub_scna_hg18.seg ov_tcga_pub
