# Load up the Ovarian Meta Data File
./importCancerStudy.pl $CGDS_DATA_HOME/ovarian/ovarian.txt

# Imports All Case Lists
./importCaseList.pl $CBIO_BRCA_HOME/out/cases

# Imports Clinical Data
./importClinicalData.pl $CBIO_BRCA_HOME/out/merged_clinical.txt

# Imports Mutation Data
./importProfileData.pl --data $CBIO_BRCA_HOME/data/genomic/OV.maf --meta $CBIO_BRCA_HOME/data/genomic/meta_mutations_extended.txt --dbmsAction clobber --germlineWhiteList $CBIO_BRCA_HOME/data/genomic/ovarianGermlineWhiteList.txt

# Import Methylation Data
./importProfileData.pl --data $CBIO_BRCA_HOME/data/genomic/data_brca1_binary_methylation.txt --meta $CBIO_BRCA_HOME/data/genomic/meta_brca1_binary_methylation.txt --dbmsAction clobber

# Imports MRNA Expression Data Files
./importProfileData.pl --data $CGDS_DATA_HOME/ovarian/data_mRNA_median_Zscores.txt --meta $CGDS_DATA_HOME/ovarian/meta_mRNA_median_Zscores.txt --dbmsAction clobber
