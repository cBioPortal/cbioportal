# Load up the PRAD_BROAD Meta Data File
./importCancerStudy.pl $PORTAL_DATA_HOME/public-override/prad_mskcc/prad_mskcc.txt

# Imports All Case Lists
./importCaseList.pl $PORTAL_DATA_HOME/public-override/prad_mskcc/case_lists

# Imports Clinical Data
./importClinicalData.pl $PORTAL_DATA_HOME/public-override/prad_mskcc/prad_mskcc_clinical.txt prad_mskcc

# Imports Mutation Data
./importProfileData.pl --data $PORTAL_DATA_HOME/public-override/prad_mskcc/data_mutations_extended.txt --meta $PORTAL_DATA_HOME/public-override/prad_mskcc/meta_mutations_extended.txt --dbmsAction clobber

# Imports Copy Number Data
./importProfileData.pl --data $PORTAL_DATA_HOME/public-override/prad_mskcc/data_CNA.txt --meta $PORTAL_DATA_HOME/public-override/prad_mskcc/meta_CNA.txt --dbmsAction clobber

# Copy number segment
#./importCopyNumberSegmentData.pl $PORTAL_DATA_HOME/public-override/prad_mskcc/prad_mskcc_scna_hg18.seg prad_mskcc

# Imports MRNA Expression Data
./importProfileData.pl --data $PORTAL_DATA_HOME/public-override/prad_mskcc/data_mRNA_ZbyNorm.txt --meta $PORTAL_DATA_HOME/public-override/prad_mskcc/meta_mRNA_ZbyNorm.txt --dbmsAction clobber

# MutSig
./importMutSig.pl $PORTAL_DATA_HOME/public-override/prad_mskcc/data_mutsig.txt $PORTAL_DATA_HOME/public-override/prad_mskcc/meta_mutsig.txt