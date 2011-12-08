# Delete all preprocessed files
rm -v $CGDS_DATA_HOME/gbm/processed_*

# Load up the GBM Meta Data File
./importCancerStudy.pl $CGDS_DATA_HOME/gbm/gbm.txt

# Load Cases and Clinical Data
./importCaseList.pl $CGDS_DATA_HOME/gbm/case_lists
./importClinicalData.pl $CGDS_DATA_HOME/gbm/GBM_clinical_portal_20110210.txt

# Load Mutation Data
./importProfileData.pl --data $CGDS_DATA_HOME/gbm/data_mutations.txt --meta $CGDS_DATA_HOME/gbm/meta_mutations_MAF.txt --dbmsAction clobber

# Load CNA Data
./importProfileData.pl --data $CGDS_DATA_HOME/gbm/data_CNA.txt --meta $CGDS_DATA_HOME/gbm/meta_CNA.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/gbm/data_CNA_RAE.txt --meta $CGDS_DATA_HOME/gbm/meta_CNA_RAE.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/gbm/data_log2CNA.txt --meta $CGDS_DATA_HOME/gbm/meta_log2CNA.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/gbm/data_CNA_consensus.txt --meta $CGDS_DATA_HOME/gbm/meta_CNA_consensus.txt --dbmsAction clobber

# Load mRNA Data
./importProfileData.pl --data $CGDS_DATA_HOME/gbm/data_mRNA.txt --meta $CGDS_DATA_HOME/gbm/meta_mRNA.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/gbm/data_mRNA_median_Zscores --meta $CGDS_DATA_HOME/gbm/meta_median_Zscores.txt --dbmsAction clobber

# Import Methylation Data
./importProfileData.pl --data $CGDS_DATA_HOME/gbm/data_methylation.txt --meta $CGDS_DATA_HOME/gbm/meta_methylation.txt --dbmsAction clobber
