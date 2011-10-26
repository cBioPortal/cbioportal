# Load up the Ovarian Meta Data File
./importCancerStudy.pl $CGDS_DATA_HOME/ovarian/ovarian.txt

# Imports All Case Lists
./importCaseList.pl $CBIO_BRCA_HOME/out/4way_case_set.txt

# Imports Clinical Data
./importClinicalData.pl $CBIO_BRCA_HOME/data/clinical/clinical_final.txt

# Imports Mutation Data
./importProfileData.pl --data $CBIO_BRCA_HOME/data/genomic/OV.maf --meta $CBIO_BRCA_HOME/data/genomic/meta_mutations_extended.txt --dbmsAction clobber --germlineWhiteList $CBIO_BRCA_HOME/data/genomic/ovarianGermlineWhiteList.txt