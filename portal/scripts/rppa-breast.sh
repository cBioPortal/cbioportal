#!/bin/sh

# Load RPPA antibody information
./importProteinArrayInfo.pl $CGDS_DATA_HOME/RPPA/RPPA_antibody_list_BRCA.txt $1

# Import RPPA data
./importProteinArrayData.pl $CGDS_DATA_HOME/RPPA/BRCA410_165Ab_RPPA.txt $1
