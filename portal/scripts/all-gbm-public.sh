#################################################
# WARNING - these scripts should not be used
# to create a Public Portal within the cBio Lab.
# The firehose converter should be used instead.
################################################

# Load up the GBM_TCGA Meta Data File
./importCancerStudy.pl $CGDS_DATA_HOME/gbm_tcga/gbm_tcga.txt

# Load Cases and Clinical Data
./importCaseList.pl $CGDS_DATA_HOME/gbm_tcga/case_lists
./importClinicalData.pl $CGDS_DATA_HOME/gbm_tcga/gbm_tcga_clinical.txt

# Load Mutation Data
./importProfileData.pl --data $CGDS_DATA_HOME/gbm_tcga/data_mutations_extended.txt --meta $CGDS_DATA_HOME/gbm_tcga/meta_mutations_extended.txt --dbmsAction clobber

# Load CNA Data
./importProfileData.pl --data $CGDS_DATA_HOME/gbm_tcga/data_CNA.txt --meta $CGDS_DATA_HOME/gbm_tcga/meta_CNA.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/gbm_tcga/data_CNA_RAE.txt --meta $CGDS_DATA_HOME/gbm_tcga/meta_CNA_RAE.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/gbm_tcga/data_log2CNA.txt --meta $CGDS_DATA_HOME/gbm_tcga/meta_log2CNA.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/gbm_tcga/data_CNA_consensus.txt --meta $CGDS_DATA_HOME/gbm_tcga/meta_CNA_consensus.txt --dbmsAction clobber

# Load mRNA Data
./importProfileData.pl --data $CGDS_DATA_HOME/gbm_tcga/data_expression_median.txt --meta $CGDS_DATA_HOME/gbm_tcga/meta_expression_median.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/gbm_tcga/data_expression_merged_median_Zscores.txt --meta $CGDS_DATA_HOME/gbm_tcga/meta_expression_merged_median_Zscores.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/gbm_tcga/data_mRNA_median_Zscores.txt --meta $CGDS_DATA_HOME/gbm_tcga/meta_mRNA_median_Zscores.txt --dbmsAction clobber

# Load miRNA
./importProfileData.pl --data $CGDS_DATA_HOME/gbm_tcga/data_miRNA_median_Zscores.txt --meta $CGDS_DATA_HOME/gbm_tcga/meta_miRNA_median_Zscores.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/gbm_tcga/data_expression_miRNA.txt --meta $CGDS_DATA_HOME/gbm_tcga/meta_expression_miRNA.txt --dbmsAction clobber

# Import Methylation Data
./importProfileData.pl --data $CGDS_DATA_HOME/gbm_tcga/data_methylation.txt --meta $CGDS_DATA_HOME/gbm_tcga/meta_methylation.txt --dbmsAction clobber

# RPPA
./importProteinArrayData.pl $CGDS_DATA_HOME/gbm_tcga/data_rppa.txt gbm_tcga
