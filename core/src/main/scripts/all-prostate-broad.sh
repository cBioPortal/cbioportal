# Load up the PRAD_BROAD Meta Data File
./importCancerStudy.pl $PORTAL_DATA_HOME/private-override/prad_broad/prad_broad.txt

# Imports All Case Lists
./importCaseList.pl $PORTAL_DATA_HOME/private-override/prad_broad/case_lists

# Imports Clinical Data
./importClinicalData.pl $PORTAL_DATA_HOME/private-override/prad_broad/prad_broad_clinical.txt prad_broad

# Imports Mutation Data
./importProfileData.pl --data $PORTAL_DATA_HOME/private-override/prad_broad/data_mutations_extended.txt --meta $PORTAL_DATA_HOME/private-override/prad_broad/meta_mutations_extended.txt --dbmsAction clobber

# Imports Copy Number Data
./importProfileData.pl --data $PORTAL_DATA_HOME/private-override/prad_broad/data_CNA.txt --meta $PORTAL_DATA_HOME/private-override/prad_broad/meta_CNA.txt --dbmsAction clobber

# Copy number segment
#./importCopyNumberSegmentData.pl $PORTAL_DATA_HOME/private-override/prad_broad/prad_broad_scna_hg18.seg prad_broad

# MutSig
./importMutSig.pl $PORTAL_DATA_HOME/private-override/prad_broad/data_mutsig.txt $PORTAL_DATA_HOME/private-override/prad_broad/meta_mutsig.txt
