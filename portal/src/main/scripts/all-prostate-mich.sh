# Load up the PRAD_BROAD Meta Data File
./importCancerStudy.pl $CGDS_DATA_HOME/private-override/prad_mich/prad_mich.txt

# Imports All Case Lists
./importCaseList.pl $CGDS_DATA_HOME/private-override/prad_mich/case_lists

# Imports Clinical Data
./importClinicalData.pl prad_mich $CGDS_DATA_HOME/private-override/prad_mich/prad_mich_clinical.txt

# Imports Mutation Data
./importProfileData.pl --data $CGDS_DATA_HOME/private-override/prad_mich/data_mutations_extended.txt --meta $CGDS_DATA_HOME/private-override/prad_mich/meta_mutations_extended.txt --dbmsAction clobber

# Imports Copy Number Data
./importProfileData.pl --data $CGDS_DATA_HOME/private-override/prad_mich/data_CNA.txt --meta $CGDS_DATA_HOME/private-override/prad_mich/meta_CNA.txt --dbmsAction clobber

# Copy number segment
#./importCopyNumberSegmentData.pl $CGDS_DATA_HOME/private-override/prad_mich/prad_mich_scna_hg18.seg coadread_tcga

# Imports MRNA Expression Data
./importProfileData.pl --data $CGDS_DATA_HOME/private-override/prad_mich/data_expression_median.txt --meta $CGDS_DATA_HOME/private-override/prad_mich/meta_expression_median.txt --dbmsAction clobber

# MutSig
./importMutSig.pl $CGDS_DATA_HOME/private-override/prad_mich/data_mutsig.txt $CGDS_DATA_HOME/private-override/prad_mich/meta_mutsig.txt
