# Load up the PRAD_TCGA Meta Data File
./importCancerStudy.pl $GDAC_CGDS_STAGING_HOME/prad_tcga/prad_tcga.txt

# Imports All Case Lists
./importCaseList.pl $GDAC_CGDS_STAGING_HOME/prad_tcga/case_lists

# Imports Clinical Data
./importClinicalData.pl $GDAC_CGDS_STAGING_HOME/prad_tcga/prad_tcga_clinical.txt prad_tcga

# Imports Mutation Data
./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/prad_tcga/data_mutations_extended.txt --meta $GDAC_CGDS_STAGING_HOME/prad_tcga/meta_mutations_extended.txt --dbmsAction clobber

# Imports Copy Number Data
./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/prad_tcga/data_CNA.txt --meta $GDAC_CGDS_STAGING_HOME/prad_tcga/meta_CNA.txt --dbmsAction clobber
./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/prad_tcga/data_log2CNA.txt --meta $GDAC_CGDS_STAGING_HOME/prad_tcga/meta_log2CNA.txt --dbmsAction clobber

# MutSig
./importMutSig.pl $GDAC_CGDS_STAGING_HOME/prad_tcga/data_mutsig.txt $GDAC_CGDS_STAGING_HOME/prad_tcga/meta_mutsig.txt

# Gistic
./importGistic.pl $GDAC_CGDS_STAGING_HOME/prad_tcga/data_GISTIC_GENE_AMPS.txt prad_tcga
./importGistic.pl $GDAC_CGDS_STAGING_HOME/prad_tcga/data_GISTIC_GENE_DELS.txt prad_tcga

# Copy number segment
./importCopyNumberSegmentData.pl $GDAC_CGDS_STAGING_HOME/prad_tcga/prad_tcga_scna_minus_germline_cnv_hg19.seg prad_tcga
