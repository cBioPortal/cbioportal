# Load up the PRAD_BROAD Meta Data File
./importCancerStudy.pl $CGDS_DATA_HOME/private-override/prad_broad/prad_broad.txt

# Imports All Case Lists
./importCaseList.pl $CGDS_DATA_HOME/private-override/prad_broad/case_lists

# Imports Clinical Data
./importClinicalData.pl prad_broad $CGDS_DATA_HOME/private-override/prad_broad/prad_broad_clinical.txt

# Imports Mutation Data
./importProfileData.pl --data $CGDS_DATA_HOME/private-override/prad_broad/data_mutations_extended.txt --meta $CGDS_DATA_HOME/private-override/prad_broad/meta_mutations_extended.txt --dbmsAction clobber

# Imports Copy Number Data
./importProfileData.pl --data $CGDS_DATA_HOME/private-override/prad_broad/data_CNA.txt --meta $CGDS_DATA_HOME/private-override/prad_broad/meta_CNA.txt --dbmsAction clobber

# Copy number segment
./importCopyNumberSegmentData.pl $CGDS_DATA_HOME/private-override/prad_broad/prad_broad_scna_hg18.seg

# MutSig
./importMutSig.pl $CGDS_DATA_HOME/private-override/prad_broad/data_mutsig.txt $CGDS_DATA_HOME/private-override/prad_broad/meta_mutsig.txt
