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
./importProfileData.pl --data $CGDS_HOME/data/ovarian/data_CNA.txt --meta $CGDS_HOME/data/ovarian/meta_CNA.txt --dbmsAction clobber
