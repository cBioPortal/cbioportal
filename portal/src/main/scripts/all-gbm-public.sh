#################################################
# WARNING - these scripts should not be used
# to create a Public Portal within the cBio Lab.
# The firehose converter should be used instead.
################################################

# Load up the GBM_TCGA Meta Data File
./importCancerStudy.pl $GDAC_CGDS_STAGING_HOME/gbm_tcga/gbm_tcga.txt

# Load Cases and Clinical Data
./importCaseList.pl $GDAC_CGDS_STAGING_HOME/gbm_tcga/case_lists
./importClinicalData.pl gbm_tcga $GDAC_CGDS_STAGING_HOME/gbm_tcga/gbm_tcga_clinical.txt

# Load Mutation Data
./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/gbm_tcga/data_mutations_extended.txt --meta $GDAC_CGDS_STAGING_HOME/gbm_tcga/meta_mutations_extended.txt --dbmsAction clobber

# Load CNA Data
./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/gbm_tcga/data_CNA.txt --meta $GDAC_CGDS_STAGING_HOME/gbm_tcga/meta_CNA.txt --dbmsAction clobber
./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/gbm_tcga/data_log2CNA.txt --meta $GDAC_CGDS_STAGING_HOME/gbm_tcga/meta_log2CNA.txt --dbmsAction clobber

# Load mRNA Data
#./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/gbm_tcga/data_expression_median.txt --meta $GDAC_CGDS_STAGING_HOME/gbm_tcga/meta_expression_median.txt --dbmsAction clobber
#./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/gbm_tcga/data_expression_merged_median_Zscores.txt --meta $GDAC_CGDS_STAGING_HOME/gbm_tcga/meta_expression_merged_median_Zscores.txt --dbmsAction clobber
./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/gbm_tcga/data_mRNA_median_Zscores.txt --meta $GDAC_CGDS_STAGING_HOME/gbm_tcga/meta_mRNA_median_Zscores.txt --dbmsAction clobber

# Load miRNA
#./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/gbm_tcga/data_miRNA_median_Zscores.txt --meta $GDAC_CGDS_STAGING_HOME/gbm_tcga/meta_miRNA_median_Zscores.txt --dbmsAction clobber
#./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/gbm_tcga/data_expression_miRNA.txt --meta $GDAC_CGDS_STAGING_HOME/gbm_tcga/meta_expression_miRNA.txt --dbmsAction clobber

# Import Methylation Data
./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/gbm_tcga/data_methylation.txt --meta $GDAC_CGDS_STAGING_HOME/gbm_tcga/meta_methylation.txt --dbmsAction clobber

# RPPA
./importProteinArrayData.pl $GDAC_CGDS_STAGING_HOME/gbm_tcga/data_rppa.txt gbm_tcga

# MutSig
./importMutSig.pl $GDAC_CGDS_STAGING_HOME/gbm_tcga/data_mutsig.txt $GDAC_CGDS_STAGING_HOME/gbm_tcga/meta_mutsig.txt

# Copy number segment
./importCopyNumberSegmentData.pl $GDAC_CGDS_STAGING_HOME/gbm_tcga/gbm_tcga_scna_minus_germline_cnv_hg19.seg