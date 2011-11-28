#################################################
# Inits and Loads a Small Subset of GBM Data
#
# This script loads up sample data located in:
# portal/sample_data
#################################################

# Clear the Database
./resetDb.pl

# Load up Entrez Genes
./importGenes.pl ../sample_data/genes/human_genes.txt

# Load Entrez Gene ID --> UniProt ID mapping
./importUniProtIdMapping.pl ../sample_data/genes/uniprot_id_mapping.txt

# Load up All Cancer Types
./importTypesOfCancer.pl ../sample_data/cancers.txt

# Load up the GBM Meta Data File
./importCancerStudy.pl ../sample_data/gbm/gbm.txt

# Load Cases and Clinical Data
./importCaseList.pl ../sample_data/gbm/case_lists
./importClinicalData.pl ../sample_data/gbm/GBM_clinical_portal_20110210.txt

# Load Mutation Data
./importProfileData.pl --data ../sample_data/gbm/data_mutations_MAF.txt --meta ../sample_data/gbm/meta_mutations_MAF.txt --dbmsAction clobber  --somaticWhiteList ../sample_data/genes/universalSomaticGeneWhitelist.txt --somaticWhiteList ../sample_data/genes/universalSomaticGeneWhitelist.txt

# Load CNA Data
./importProfileData.pl --data ../sample_data/gbm/data_CNA_consensus.txt --meta ../sample_data/gbm/meta_CNA_consensus.txt --dbmsAction clobber
