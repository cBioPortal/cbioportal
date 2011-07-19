# Delete all preprocessed files
rm -v ../data/prostate/processed_*

# Load up the Prosate Meta Data File
./ImportCancerStudy.pl ../data/prostate/prostate.txt

# Pre-process data for import
# Runs:
# /scripts/prostate/gen-rae.py
# -->  this script converts RefSeq IDs to Entrez Gene Ids.
# /scripts/prostate/gen-mrna-expression.py
# -->  this script also converts RefSeq IDs to Entrez Gene Ids.
./prostate/preprocess-all.py

# Imports All Case Lists
./importCaseList.pl $CGDS_HOME/data/prostate/case_lists

# Imports all microRNA IDs
./importMicroRna.pl ../data/microRNA/microRNAs.txt 

# Imports Mutation Data
./importProfileData.pl --data $CGDS_HOME/data/prostate/data_mutations.txt --meta $CGDS_HOME/data/prostate/meta_mutations.txt  --dbmsAction clobber --acceptRemainingMutations

# Imports Processed RAE Copy Number Data
# We have to import the processed RAE Data file, because the original RAE file using RefSeq IDs,
# and the processed file uses Entrez Gene IDs, and CGDS only understands Entrez Gene Ids
./importProfileData.pl --data $CGDS_HOME/data/prostate/processed_CNA.txt --meta $CGDS_HOME/data/prostate/meta_CNA.txt --dbmsAction clobber

# Imports MRNA Expression Data Files
./importProfileData.pl --data $CGDS_HOME/data/prostate/data_mRNA.txt --meta $CGDS_HOME/data/prostate/meta_mRNA.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_HOME/data/prostate/data_microRNA.txt --meta $CGDS_HOME/data/prostate/meta_miRNA.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_HOME/data/prostate/data_mRNA_ZbyNorm.txt --meta $CGDS_HOME/data/prostate/meta_mRNA_ZbyNorm.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_HOME/data/prostate/data_mRNA_outliers.txt --meta $CGDS_HOME/data/prostate/meta_mRNA_outliers.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_HOME/data/prostate/data_microRNA_ZbyNorm.txt --meta $CGDS_HOME/data/prostate/meta_miRNA_ZbyNorm.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_HOME/data/prostate/data_microRNA_outliers.txt --meta $CGDS_HOME/data/prostate/meta_miRNA_outliers.txt --dbmsAction clobber

# Imports Clinical Data
./importClinicalData.pl $CGDS_HOME/data/prostate/prostate_clinical_portal_20110211.txt
