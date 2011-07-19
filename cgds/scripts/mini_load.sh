# Clear the Database
./resetDb.pl

# Load up Entrez Genes
./importGenes.pl ../data/human_genes.txt

########################
# Ovarian
########################

# Load up the Ovarian Meta Data File
./ImportCancerStudy.pl ../data/ovarian/ovarian.txt
./importCaseList.pl $CGDS_HOME/data/ovarian/case_lists
./importClinicalData.pl ../data/ovarian/ova_clinical_20110211.txt
./importProfileData.pl --data $CGDS_HOME/data/ovarian/3-center_OV.Exome_DNASeq.1.Somatic_and_Germline_WU-Annotation.05jan2011a.maf --meta $CGDS_HOME/data/ovarian/meta_mutations_extended.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_HOME/data/ovarian/data_CNA.txt --meta $CGDS_HOME/data/ovarian/meta_CNA.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_HOME/data/ovarian/data_mRNA_median_Zscores.txt --meta $CGDS_HOME/data/ovarian/meta_mRNA_median_Zscores.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_HOME/data/ovarian/data_methylation.txt --meta $CGDS_HOME/data/ovarian/meta_methylation.txt --dbmsAction clobber

########################
# GBM
########################
./ImportCancerStudy.pl ../data/gbm/gbm.txt
./importCaseList.pl ../data/gbm/case_lists
./importClinicalData.pl ../data/gbm/GBM_clinical_portal_20110210.txt
./importProfileData.pl --data ../data/gbm/data_mutations_MAF.txt --meta ../data/gbm/meta_mutations_MAF.txt --dbmsAction clobber

########################
# Prostate
########################

# Imports Clinical Data
./ImportCancerStudy.pl ../data/prostate/prostate.txt
./importCaseList.pl $CGDS_HOME/data/prostate/case_lists
./importProfileData.pl --data $CGDS_HOME/data/prostate/data_mutations.txt --meta  $CGDS_HOME/data/prostate/meta_mutations.txt --dbmsAction clobber
./importClinicalData.pl ../data/prostate/prostate_clinical_portal_20110211.txt
