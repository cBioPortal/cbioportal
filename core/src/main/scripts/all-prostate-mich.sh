# Load up the PRAD_MICH Meta Data File
./importCancerStudy.pl $PORTAL_DATA_HOME/studies/prad/mich/meta_study.txt

# Imports All Case Lists
./importCaseList.pl $PORTAL_DATA_HOME/studies/prad/mich/case_lists

# Imports Clinical Data
./importClinicalData.pl $PORTAL_DATA_HOME/studies/prad/mich/data_clinical.txt prad_mich

# Imports Mutation Data
./importProfileData.pl --data $PORTAL_DATA_HOME/studies/prad/mich/data_mutations_extended.txt --meta $PORTAL_DATA_HOME/studies/prad/mich/meta_mutations_extended.txt --dbmsAction clobber

# Imports Copy Number Data
./importProfileData.pl --data $PORTAL_DATA_HOME/studies/prad/mich/data_CNA.txt --meta $PORTAL_DATA_HOME/studies/prad/mich/meta_CNA.txt --dbmsAction clobber

# Copy number segment
#./importCopyNumberSegmentData.pl $PORTAL_DATA_HOME/studies/prad/mich/prad_mich_scna_hg18.seg prad_mich

# Imports MRNA Expression Data
./importProfileData.pl --data $PORTAL_DATA_HOME/studies/prad/mich/data_expression_median.txt --meta $PORTAL_DATA_HOME/studies/prad/mich/meta_expression_median.txt --dbmsAction clobber

# MutSig
./importMutSig.pl $PORTAL_DATA_HOME/studies/prad/mich/data_mutsig.txt $PORTAL_DATA_HOME/studies/prad/mich/meta_mutsig.txt
