# Load up the PRAD_BROAD Meta Data File
./importCancerStudy.pl $CGDS_DATA_HOME/public-override/prad_mskcc/prad_mskcc.txt

# Imports All Case Lists
./importCaseList.pl $CGDS_DATA_HOME/public-override/prad_mskcc/case_lists

# Imports Clinical Data
./importClinicalData.pl prad_mskcc $CGDS_DATA_HOME/public-override/prad_mskcc/prad_mskcc_clinical.txt

# Imports Mutation Data
./importProfileData.pl --data $CGDS_DATA_HOME/public-override/prad_mskcc/data_mutations_extended.txt --meta $CGDS_DATA_HOME/public-override/prad_mskcc/meta_mutations_extended.txt --dbmsAction clobber

# Imports Copy Number Data
./importProfileData.pl --data $CGDS_DATA_HOME/public-override/prad_mskcc/data_CNA.txt --meta $CGDS_DATA_HOME/public-override/prad_mskcc/meta_CNA.txt --dbmsAction clobber

# Copy number segment
./importCopyNumberSegmentData.pl $CGDS_DATA_HOME/public-override/prad_mskcc/prad_mskcc_scna_hg18.seg

# Imports MRNA Expression Data
./importProfileData.pl --data $CGDS_DATA_HOME/public-override/prad_mskcc/data_mRNA_ZbyNorm.txt --meta $CGDS_DATA_HOME/public-override/prad_mskcc/meta_mRNA_ZbyNorm.txt --dbmsAction clobber
