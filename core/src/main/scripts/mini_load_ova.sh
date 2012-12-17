# Clear /Init the Database
./init.sh

########################
# Ovarian
########################

# Load up the Ovarian Meta Data File
./ImportCancerStudy.pl $CGDS_DATA_HOME/ovarian/ovarian.txt
./importCaseList.pl $CGDS_DATA_HOME/ovarian/case_lists
./importClinicalData.pl $CGDS_DATA_HOME/ovarian/ova_clinical_20110211.txt ovarian
./importProfileData.pl --data $CGDS_DATA_HOME/ovarian/data_CNA.txt --meta $CGDS_DATA_HOME/ovarian/meta_CNA.txt --dbmsAction clobber
