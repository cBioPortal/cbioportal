# Delete all preprocessed files
rm -v $CGDS_DATA_HOME/Gray-lab-cell-line/processed_*

# Load up the GBM Meta Data File
./importCancerStudy.pl $CGDS_DATA_HOME/Gray-lab-cell-line/GrayCellLine.txt

# Load Cases
./importCaseList.pl $CGDS_DATA_HOME/Gray-lab-cell-line/case_lists

# Load CNA Data
#./importProfileData.pl --data $CGDS_DATA_HOME/Gray-lab-cell-line/data_CNA_consensus.txt --meta $CGDS_DATA_HOME/Gray-lab-cell-line/meta_CNA_consensus.txt --dbmsAction clobber
#./importProfileData.pl --data $CGDS_DATA_HOME/Gray-lab-cell-line/data_CNA_RAE.txt --meta $CGDS_DATA_HOME/Gray-lab-cell-line/meta_CNA_RAE.txt --dbmsAction clobber

# Load mRNA and microRNA Data
./importProfileData.pl --data $CGDS_DATA_HOME/Gray-lab-cell-line/data_mRNA_LBNL_Gray_BCCL_exon_09Jul.txt --meta $CGDS_DATA_HOME/Gray-lab-cell-line/meta_mRNA_LBNL_Gray_BCCL_exon_09Jul.txt --dbmsAction clobber
