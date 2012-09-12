#################################################
# Inits and Loads a Small Subset of GBM Data
#
# This script loads up sample data located in:
# portal/sample_data
#################################################

SAMPLE_DATA=$PORTAL_HOME/src/main/resources/sample_data
TEST_DATA=$PORTAL_HOME/src/test/resources

# Clear the Database
./resetDb.pl

# Load up Entrez Genes
./importGenes.pl $SAMPLE_DATA/genes/human_genes.txt

# Load Entrez Gene ID --> UniProt ID mapping
./importUniProtIdMapping.pl $SAMPLE_DATA/genes/uniprot_id_mapping.txt

# Load up All Cancer Types
./importTypesOfCancer.pl $SAMPLE_DATA/cancers.txt

# Load up the GBM Meta Data File
./importCancerStudy.pl $SAMPLE_DATA/gbm/gbm.txt

# Load Cases and Clinical Data
./importCaseList.pl $SAMPLE_DATA/gbm/case_lists
./importClinicalData.pl $SAMPLE_DATA/gbm/GBM_clinical_portal_20110210.txt

# Load Mutation Data
./importProfileData.pl --data $SAMPLE_DATA/gbm/data_mutations_MAF.txt --meta $SAMPLE_DATA/gbm/meta_mutations_MAF.txt --dbmsAction clobber 

# Load CNA Data
./importProfileData.pl --data $SAMPLE_DATA/gbm/data_CNA_consensus.txt --meta $SAMPLE_DATA/gbm/meta_CNA_consensus.txt --dbmsAction clobber

# Load network data
./importSif.pl $SAMPLE_DATA/network/cell-map.sif CELLMAP
./importSif.pl $SAMPLE_DATA/network/nci-nature.sif NCI_NATURE

# Load MutSig data
./importMutSig.pl $TEST_DATA/test_mut_sig_data.txt $TEST_DATA/testCancerStudy.txt

# Load Gistic data
./importGistic.pl $TEST_DATA/testCancerStudy.txt $TEST_DATA/test-gistic-amp.txt $TEST_DATA/test-gistic-table-amp.txt 
