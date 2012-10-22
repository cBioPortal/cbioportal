# Clear / Init the Database
./init.sh

########################
# Ovarian
########################

# Load up the Ovarian Meta Data File
./ImportCancerStudy.pl $CGDS_DATA_HOME/ovarian/ovarian.txt
./importCaseList.pl $CGDS_DATA_HOME/ovarian/case_lists
./importClinicalData.pl $CGDS_DATA_HOME/ovarian/ova_clinical_20110211.txt
./importProfileData.pl --data $CGDS_DATA_HOME/ovarian/3-center_OV.Exome_DNASeq.1.Somatic_and_Germline_WU-Annotation.05jan2011a.maf --meta $CGDS_DATA_HOME/ovarian/meta_mutations_extended.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/ovarian/data_CNA.txt --meta $CGDS_DATA_HOME/ovarian/meta_CNA.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/ovarian/data_mRNA_median_Zscores.txt --meta $CGDS_DATA_HOME/ovarian/meta_mRNA_median_Zscores.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/ovarian/data_methylation.txt --meta $CGDS_DATA_HOME/ovarian/meta_methylation.txt --dbmsAction clobber

########################
# GBM
########################
./ImportCancerStudy.pl $CGDS_DATA_HOME/gbm/gbm.txt
./importCaseList.pl $CGDS_DATA_HOME/gbm/case_lists
./importClinicalData.pl $CGDS_DATA_HOME/gbm/GBM_clinical_portal_20110210.txt
./importProfileData.pl --data $CGDS_DATA_HOME/gbm/data_mutations_MAF.txt --meta $CGDS_DATA_HOME/gbm/meta_mutations_MAF.txt --dbmsAction clobber

########################
# Prostate
########################

# Imports Clinical Data
./ImportCancerStudy.pl $CGDS_DATA_HOME/prostate/prostate.txt
./importCaseList.pl $CGDS_DATA_HOME/prostate/case_lists
./importProfileData.pl --data $CGDS_DATA_HOME/prostate/data_mutations.txt --meta  $CGDS_DATA_HOME/prostate/meta_mutations.txt --dbmsAction clobber
./importClinicalData.pl $CGDS_DATA_HOME/prostate/prostate_clinical_portal_20110211.txt
