# Delete all preprocessed files
rm $CGDS_DATA_HOME/gbm/processed_*

# Load up the GBM Meta Data File
./importCancerStudy.pl $CGDS_DATA_HOME/gbm/gbm_tcga.txt

# Load Cases and Clinical Data
./importCaseList.pl $CGDS_DATA_HOME/gbm/case_lists
./importClinicalData.pl $CGDS_DATA_HOME/gbm/gbm_tcga_clinical.txt

# Load Mutation Data
./importProfileData.pl --data $CGDS_DATA_HOME/gbm/data_mutations_extended.txt --meta $CGDS_DATA_HOME/gbm/meta_mutations_extended.txt --dbmsAction clobber

# Load CNA Data
./importProfileData.pl --data $CGDS_DATA_HOME/gbm/data_CNA.txt --meta $CGDS_DATA_HOME/gbm/meta_CNA.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/gbm/data_CNA_RAE.txt --meta $CGDS_DATA_HOME/gbm/meta_CNA_RAE.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/gbm/data_log2CNA.txt --meta $CGDS_DATA_HOME/gbm/meta_log2CNA.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/gbm/data_CNA_consensus.txt --meta $CGDS_DATA_HOME/gbm/meta_CNA_consensus.txt --dbmsAction clobber

# Load mRNA Data
./importProfileData.pl --data $CGDS_DATA_HOME/gbm/data_expression_median.txt --meta $CGDS_DATA_HOME/gbm/meta_expression_median.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/gbm/data_expression_merged_median_Zscores.txt --meta $CGDS_DATA_HOME/gbm/meta_expression_merged_median_Zscores.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/gbm/data_mRNA_median_Zscores.txt --meta $CGDS_DATA_HOME/gbm/meta_mRNA_median_Zscores.txt --dbmsAction clobber

# Load miRNA
./importProfileData.pl --data $CGDS_DATA_HOME/gbm/data_miRNA_median_Zscores.txt --meta $CGDS_DATA_HOME/gbm/meta_miRNA_median_Zscores.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/gbm/data_expression_miRNA.txt --meta $CGDS_DATA_HOME/gbm/meta_expression_miRNA.txt --dbmsAction clobber

# Import Methylation Data
./importProfileData.pl --data $CGDS_DATA_HOME/gbm/data_methylation.txt --meta $CGDS_DATA_HOME/gbm/meta_methylation.txt --dbmsAction clobber
