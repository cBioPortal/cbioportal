# Load up the Endometrioid (UCEC_TCGA) Meta Data File
./importCancerStudy.pl $GDAC_CGDS_STAGING_HOME/ucec_tcga/ucec_tcga.txt

# Imports All Case Lists
./importCaseList.pl $GDAC_CGDS_STAGING_HOME/ucec_tcga/case_lists

# Imports Clinical Data
./importClinicalData.pl $GDAC_CGDS_STAGING_HOME/ucec_tcga/ucec_tcga_clinical.txt ucec_tcga

# Imports Mutation Data
./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/ucec_tcga/data_mutations_extended.txt --meta $GDAC_CGDS_STAGING_HOME/ucec_tcga/meta_mutations_extended.txt --dbmsAction clobber

# Imports Copy Number Data
./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/ucec_tcga/data_CNA.txt --meta $GDAC_CGDS_STAGING_HOME/ucec_tcga/meta_CNA.txt --dbmsAction clobber
#./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/ucec_tcga/data_log2CNA.txt --meta $GDAC_CGDS_STAGING_HOME/ucec_tcga/meta_log2CNA.txt --dbmsAction clobber

# Imports MRNA Expression Data
#./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/ucec_tcga/data_expression_median.txt --meta $GDAC_CGDS_STAGING_HOME/ucec_tcga/meta_expression_median.txt --dbmsAction clobber
#./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/ucec_tcga/data_RNA_Seq_expression_median.txt --meta $GDAC_CGDS_STAGING_HOME/ucec_tcga/meta_RNA_Seq_expression_median.txt --dbmsAction clobber
#./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/ucec_tcga/data_mRNA_median_Zscores.txt --meta $GDAC_CGDS_STAGING_HOME/ucec_tcga/meta_mRNA_median_Zscores.txt --dbmsAction clobber
./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/ucec_tcga/data_RNA_Seq_mRNA_median_Zscores.txt --meta $GDAC_CGDS_STAGING_HOME/ucec_tcga/meta_RNA_Seq_mRNA_median_Zscores.txt --dbmsAction clobber

# Imports Methylation Data
./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/ucec_tcga/data_methylation.txt --meta $GDAC_CGDS_STAGING_HOME/ucec_tcga/meta_methylation.txt --dbmsAction clobber

# MutSig
./importMutSig.pl $GDAC_CGDS_STAGING_HOME/ucec_tcga/data_mutsig.txt $GDAC_CGDS_STAGING_HOME/ucec_tcga/meta_mutsig.txt

# Gistic
./importGistic.pl $GDAC_CGDS_STAGING_HOME/ucec_tcga/data_GISTIC_GENE_AMPS.txt ucec_tcga
./importGistic.pl $GDAC_CGDS_STAGING_HOME/ucec_tcga/data_GISTIC_GENE_DELS.txt ucec_tcga

# Copy number segment
./importCopyNumberSegmentData.pl $GDAC_CGDS_STAGING_HOME/ucec_tcga/ucec_tcga.seg ucec_tcga
