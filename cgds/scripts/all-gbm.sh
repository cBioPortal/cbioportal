# Delete all preprocessed files
rm -v $CGDS_DATA_HOME/gbm/processed_*

# Load up the GBM Meta Data File
./ImportCancerStudy.pl $CGDS_DATA_HOME/gbm/gbm.txt

# Load Cases and Clinical Data
./importCaseList.pl $CGDS_DATA_HOME/gbm/case_lists
./importClinicalData.pl $CGDS_DATA_HOME/gbm/GBM_clinical_portal_20110210.txt

# Load Mutation Data
./importProfileData.pl --data $CGDS_DATA_HOME/gbm/data_mutations_MAF.txt --meta $CGDS_DATA_HOME/gbm/meta_mutations_MAF.txt --dbmsAction clobber  --somaticWhiteList $CGDS_DATA_HOME/universalSomaticGeneWhitelist.txt --somaticWhiteList $CGDS_DATA_HOME/universalSomaticGeneWhitelist.txt

# Load CNA Data
./importProfileData.pl --data $CGDS_DATA_HOME/gbm/data_CNA_consensus.txt --meta $CGDS_DATA_HOME/gbm/meta_CNA_consensus.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/gbm/data_CNA_RAE.txt --meta $CGDS_DATA_HOME/gbm/meta_CNA_RAE.txt --dbmsAction clobber

# Load mRNA and microRNA Data
./importProfileData.pl --data $CGDS_DATA_HOME/gbm/data_mRNA.txt --meta $CGDS_DATA_HOME/gbm/meta_mRNA.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/gbm/data_miRNA.txt --meta $CGDS_DATA_HOME/gbm/meta_miRNA.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/gbm/data_mRNA_ZbyDiploidTumors.txt --meta $CGDS_DATA_HOME/gbm/meta_mRNA_ZbyDiploidTumors.txt --dbmsAction clobber

