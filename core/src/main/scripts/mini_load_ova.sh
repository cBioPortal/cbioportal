# Clear /Init the Database
./init.sh

########################
# Ovarian
########################

# Load up the Ovarian Meta Data File
./ImportCancerStudy.pl $PORTAL_DATA_HOME/ovarian/ovarian.txt
./importCaseList.pl $PORTAL_DATA_HOME/ovarian/case_lists
./importClinicalData.pl $PORTAL_DATA_HOME/ovarian/ova_clinical_20110211.txt ovarian
./importProfileData.pl --data $PORTAL_DATA_HOME/ovarian/data_CNA.txt --meta $PORTAL_DATA_HOME/ovarian/meta_CNA.txt --dbmsAction clobber
