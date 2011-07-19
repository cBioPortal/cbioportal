# Delete all preprocessed files
rm -v ../data/gbm/processed_*

# Load up the GBM Meta Data File
./ImportCancerStudy.pl ../data/gbm/gbm.txt

# Load Cases and Clinical Data
./importCaseList.pl ../data/gbm/case_lists
./importClinicalData.pl $CGDS_HOME/data/gbm/GBM_clinical_portal_20110210.txt

# Load Mutation Data
./importProfileData.pl --data ../data/gbm/data_mutations_MAF.txt --meta ../data/gbm/meta_mutations_MAF.txt --dbmsAction clobber  --somaticWhiteList $CGDS_HOME/data/universalSomaticGeneWhitelist.txt --somaticWhiteList $CGDS_HOME/data/universalSomaticGeneWhitelist.txt

# Load CNA Data
./importProfileData.pl --data ../data/gbm/data_CNA_consensus.txt --meta ../data/gbm/meta_CNA_consensus.txt --dbmsAction clobber
./importProfileData.pl --data ../data/gbm/data_CNA_RAE.txt --meta ../data/gbm/meta_CNA_RAE.txt --dbmsAction clobber

# Load mRNA and microRNA Data
./importProfileData.pl --data ../data/gbm/data_mRNA.txt --meta ../data/gbm/meta_mRNA.txt --dbmsAction clobber
./importProfileData.pl --data ../data/gbm/data_miRNA.txt --meta ../data/gbm/meta_miRNA.txt --dbmsAction clobber
./importProfileData.pl --data ../data/gbm/data_mRNA_ZbyDiploidTumors.txt --meta ../data/gbm/meta_mRNA_ZbyDiploidTumors.txt --dbmsAction clobber

