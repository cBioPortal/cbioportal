# Load up the COADREAD Meta Data File
./importCancerStudy.pl $CGDS_DATA_HOME/coadread/coadread.txt

# Load Cases and Clinical Data
./importCaseList.pl $CGDS_DATA_HOME/coadread/case_lists
./importClinicalData.pl $CGDS_DATA_HOME/coadread/coadread_clinical.txt

# Load Mutation Data
./importProfileData.pl --data $CGDS_DATA_HOME/coadread/data_mutations_extended.txt --meta $CGDS_DATA_HOME/coadread/meta_mutations_extended.txt --dbmsAction clobber

# Load CNA Data
./importProfileData.pl --data $CGDS_DATA_HOME/coadread/data_CNA.txt --meta $CGDS_DATA_HOME/coadread/meta_CNA.txt --dbmsAction clobber

# Load mRNA and microRNA Data
./importProfileData.pl --data $CGDS_DATA_HOME/coadread/data_expression_median.txt --meta $CGDS_DATA_HOME/coadread/meta_expression_median.txt --dbmsAction clobber
./importProfileData.pl --data $CGDS_DATA_HOME/coadread/data_mRNA_median_Zscores.txt --meta $CGDS_DATA_HOME/coadread/meta_mRNA_median_Zscores.txt --dbmsAction clobber

# Load Methylation Data
./importProfileData.pl --data $CGDS_DATA_HOME/coadread/data_methylation.txt --meta $CGDS_DATA_HOME/coadread/meta_methylation.txt --dbmsAction clobber
