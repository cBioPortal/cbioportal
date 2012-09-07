# Load up the PRAD_BROAD Meta Data File
./importCancerStudy.pl $GDAC_CGDS_STAGING_HOME/prad_broad/prad_broad.txt

# Imports All Case Lists
./importCaseList.pl $GDAC_CGDS_STAGING_HOME/prad_broad/case_lists

# Imports Mutation Data
./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/prad_broad/data_mutations_extended.txt --meta $GDAC_CGDS_STAGING_HOME/prad_broad/meta_mutations_extended.txt --dbmsAction clobber

# Imports Copy Number Data
./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/prad_broad/data_CNA.txt --meta $GDAC_CGDS_STAGING_HOME/prad_broad/meta_CNA.txt --dbmsAction clobber

# Copy number segment
./importCopyNumberSegmentData.pl $GDAC_CGDS_STAGING_HOME/prad_broad/prad_broad_scna_hg18.seg