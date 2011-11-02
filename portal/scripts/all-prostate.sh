# Delete all preprocessed files
rm -v $CGDS_DATA_HOME/prostate/processed_*

# Load up the Prosate Meta Data File
./importCancerStudy.pl $CGDS_DATA_HOME/prostate/prostate.txt

# Pre-process data for import
# Runs:
# /scripts/prostate/gen-rae.py
# -->  this script converts RefSeq IDs to Entrez Gene Ids.
# /scripts/prostate/gen-mrna-expression.py
# -->  this script also converts RefSeq IDs to Entrez Gene Ids.
./prostate/preprocess-all.py

# Imports All Case Lists
./importCaseList.pl $CGDS_DATA_HOME/prostate/case_lists

# Imports Mutation Data
./importProfileData.pl --data $CGDS_DATA_HOME/prostate/data_mutations.txt --meta $CGDS_DATA_HOME/prostate/meta_mutations.txt  --dbmsAction clobber

# Imports Processed RAE Copy Number Data
# We have to import the processed RAE Data file, because the original RAE file using RefSeq IDs,
# and the processed file uses Entrez Gene IDs, and CGDS only understands Entrez Gene Ids
./importProfileData.pl --data $CGDS_DATA_HOME/prostate/processed_CNA.txt --meta $CGDS_DATA_HOME/prostate/meta_CNA.txt --dbmsAction clobber

# Imports MRNA Expression Data Files
./importProfileData.pl --data $CGDS_DATA_HOME/prostate/data_mRNA.txt --meta $CGDS_DATA_HOME/prostate/meta_mRNA.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/prostate/data_microRNA.txt --meta $CGDS_DATA_HOME/prostate/meta_miRNA.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/prostate/data_mRNA_ZbyNorm.txt --meta $CGDS_DATA_HOME/prostate/meta_mRNA_ZbyNorm.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/prostate/data_mRNA_outliers.txt --meta $CGDS_DATA_HOME/prostate/meta_mRNA_outliers.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/prostate/data_microRNA_ZbyNorm.txt --meta $CGDS_DATA_HOME/prostate/meta_miRNA_ZbyNorm.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/prostate/data_microRNA_outliers.txt --meta $CGDS_DATA_HOME/prostate/meta_miRNA_outliers.txt --dbmsAction clobber

# Imports Clinical Data
./importClinicalData.pl $CGDS_DATA_HOME/prostate/prostate_clinical_portal_20110211.txt
