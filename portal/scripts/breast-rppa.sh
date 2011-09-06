# Load RPPA antibody information
./importProteinArrayInfo.pl $CGDS_DATA_HOME/RPPA_antibody_list.txt

# Import RPPA data
./importProteinArrayData.pl $CGDS_DATA_HOME/breast-rppa/BR410_212Ab_RPPA_Median_Centered.txt BRCA_portal
