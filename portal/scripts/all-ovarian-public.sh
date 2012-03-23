#################################################
# WARNING - these scripts should not be used
# to create a Public Portal within the cBio Lab.
# The firehose converter should be used instead.
################################################

# Load up the Ovarian Meta Data File
./importCancerStudy.pl $CGDS_DATA_HOME/ov_tcga/ov_tcga.txt

# Imports All Case Lists
./importCaseList.pl $CGDS_DATA_HOME/ov_tcga/case_lists

# Imports Clinical Data
./importClinicalData.pl $CGDS_DATA_HOME/ov_tcga/ov_tcga_clinical.txt

# Imports Mutation Data
./importProfileData.pl --data $CGDS_DATA_HOME/ov_tcga/data_mutations_extended.txt --meta $CGDS_DATA_HOME/ov_tcga/meta_mutations_extended.txt --dbmsAction clobber

# Imports Copy Number Data
./importProfileData.pl --data $CGDS_DATA_HOME/ov_tcga/data_CNA.txt --meta $CGDS_DATA_HOME/ov_tcga/meta_CNA.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/ov_tcga/data_CNA_RAE.txt --meta $CGDS_DATA_HOME/ov_tcga/meta_CNA_RAE.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/ov_tcga/data_log2CNA.txt --meta $CGDS_DATA_HOME/ov_tcga/meta_log2CNA.txt --dbmsAction clobber

# Imports MRNA Expression Data
./importProfileData.pl --data $CGDS_DATA_HOME/ov_tcga/data_expression_median.txt --meta $CGDS_DATA_HOME/ov_tcga/meta_expression_median.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/ov_tcga/data_expression_merged_median_Zscores.txt --meta $CGDS_DATA_HOME/ov_tcga/meta_expression_merged_median_Zscores.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/ov_tcga/data_mRNA_median_Zscores.txt --meta $CGDS_DATA_HOME/ov_tcga/meta_mRNA_median_Zscores.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/ov_tcga/data_mRNA_unified.txt --meta $CGDS_DATA_HOME/ov_tcga/meta_mRNA_unified.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/ov_tcga/data_mRNA_unified_Zscores.txt --meta $CGDS_DATA_HOME/ov_tcga/meta_mRNA_unified_Zscores.txt --dbmsAction clobber

# Imports miRNA Data
./importProfileData.pl --data $CGDS_DATA_HOME/ov_tcga/data_expression_miRNA.txt --meta $CGDS_DATA_HOME/ov_tcga/meta_expression_miRNA.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/ov_tcga/data_miRNA_median_Zscores.txt --meta $CGDS_DATA_HOME/ov_tcga/meta_miRNA_median_Zscores.txt --dbmsAction clobber

# Imports Methylation Data
./importProfileData.pl --data $CGDS_DATA_HOME/ov_tcga/data_methylation.txt --meta $CGDS_DATA_HOME/ov_tcga/meta_methylation.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/ov_tcga/data_brca1_binary_methylation.txt --meta $CGDS_DATA_HOME/ov_tcga/meta_brca1_binary_methylation.txt --dbmsAction clobber

# RPPA
./importProteinArrayData.pl $CGDS_DATA_HOME/ov_tcga/data_rppa.txt ov_tcga
