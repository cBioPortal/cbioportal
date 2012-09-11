# Delete all preprocessed files
rm -v $GDAC_CGDS_STAGING_HOME/ovarian/processed_*

# Load up the Ovarian Meta Data File
./importCancerStudy.pl $GDAC_CGDS_STAGING_HOME/ov_tcga/ov_tcga.txt

# Pre-process data for import
# Runs:
# /scripts/ovarian/gen-rae.py
# -->  this script converts RefSeq IDs to Entrez Gene Ids.
# /scripts/ovarian/gen-mrna-expression.py
# -->  this script also converts RefSeq IDs to Entrez Gene Ids.
./ovarian/preprocess-all.py

# Imports All Case Lists
./importCaseList.pl $GDAC_CGDS_STAGING_HOME/ov_tcga/case_lists

# Imports Clinical Data
./importClinicalData.pl ov_tcga $GDAC_CGDS_STAGING_HOME/ov_tcga/ov_tcga_clinical.txt

# Imports Mutation Data
./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/ov_tcga/data_mutations_extended.txt --meta $GDAC_CGDS_STAGING_HOME/ov_tcga/meta_mutations_extended.txt --dbmsAction clobber --germlineWhiteList $GDAC_CGDS_STAGING_HOME/ov_tcga/ovarianGermlineWhiteList.txt

# Imports Copy Number Data
./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/ov_tcga/data_CNA.txt --meta $GDAC_CGDS_STAGING_HOME/ov_tcga/meta_CNA.txt --dbmsAction clobber
./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/ov_tcga/data_log2CNA.txt --meta $GDAC_CGDS_STAGING_HOME/ov_tcga/meta_log2CNA.txt --dbmsAction clobber

# Imports MRNA Expression Data Files
./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/ov_tcga/data_expression_median.txt --meta $GDAC_CGDS_STAGING_HOME/ov_tcga/meta_expression_median.txt --dbmsAction clobber
./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/ov_tcga/data_mRNA_median_Zscores.txt --meta $GDAC_CGDS_STAGING_HOME/ov_tcga/meta_mRNA_median_Zscores.txt --dbmsAction clobber

# Imports microRNA Expression Data Files
#./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/ov_tcga/data_miRNA.txt --meta $GDAC_CGDS_STAGING_HOME/ov_tcga/meta_miRNA.txt --dbmsAction clobber
#./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/ov_tcga/data_miRNA_median_Zscores.txt --meta $GDAC_CGDS_STAGING_HOME/ov_tcga/meta_miRNA_median_Zscores.txt --dbmsAction clobber

# Import Methylation Data
./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/ov_tcga/data_methylation.txt --meta $GDAC_CGDS_STAGING_HOME/ov_tcga/meta_methylation.txt --dbmsAction clobber
#./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/ov_tcga/data_brca1_binary_methylation.txt --meta $GDAC_CGDS_STAGING_HOME/ov_tcga/meta_brca1_binary_methylation.txt --dbmsAction clobber

# import ovarian protein data
#./importProfileData.pl --data $GDAC_CGDS_STAGING_HOME/ov_tcga/data_protein.txt --meta $GDAC_CGDS_STAGING_HOME/ov_tcga/meta_protein.txt --dbmsAction clobber

# MutSig
./importMutSig.pl $GDAC_CGDS_STAGING_HOME/ov_tcga/data_mutsig.txt $GDAC_CGDS_STAGING_HOME/ov_tcga/meta_mutsig.txt

# Copy number segment
./importCopyNumberSegmentData.pl $GDAC_CGDS_STAGING_HOME/ov_tcga/ov_tcga_scna_minus_germline_cnv_hg19.seg