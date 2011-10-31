# Delete all preprocessed files
rm -v $CGDS_DATA_HOME/ovarian/processed_*

# Load up the Ovarian Meta Data File
./importCancerStudy.pl $CGDS_DATA_HOME/ovarian/ovarian.txt

# Imports All Case Lists
#./importCaseList.pl $CBIO_BRCA_HOME/data/case_list.txt

# Imports Clinical Data
#./importClinicalData.pl $CBIO_BRCA_HOME/data/clinical_final.txt

# Imports Mutation Data
./importProfileData.pl --data $CGDS_DATA_HOME/gdac-override/mafs/OV.maf --meta $CGDS_DATA_HOME/ovarian/meta_mutations_extended.txt --dbmsAction clobber --germlineWhiteList $CGDS_DATA_HOME/ovarian/ovarianGermlineWhiteList.txt --acceptRemainingMutations

