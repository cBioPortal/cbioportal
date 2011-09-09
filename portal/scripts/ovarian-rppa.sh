#!/bin/sh

# Load RPPA antibody information
./importProteinArrayInfo.pl $CGDS_DATA_HOME/RPPA_antibody_list.txt

# Import RPPA data
./importProteinArrayData.pl $CGDS_DATA_HOME/ovarian/OV412_212Ab_RPPA_Median_Centered.txt $1
