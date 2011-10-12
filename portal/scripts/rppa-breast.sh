#!/bin/sh

# Load RPPA antibody information
./importProteinArrayInfo.pl $CGDS_DATA_HOME/RPPA/RPPA_antibody_list_OV_BRCA.txt $1

# Import RPPA data
./importProteinArrayData.pl $CGDS_DATA_HOME/RPPA/BRCA410_212Ab_RPPA_Median_Centered.txt $1
