#!/bin/sh

# Load RPPA antibody information
./importProteinArrayInfo.pl $CGDS_DATA_HOME/RPPA/RPPA_antibody_list_KIRC_UCEC.txt $1

# Import RPPA data
./importProteinArrayData.pl $CGDS_DATA_HOME/RPPA/KIRC454_162Ab_RPPA_Median_Centered.txt $1
