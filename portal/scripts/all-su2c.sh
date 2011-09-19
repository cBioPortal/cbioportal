./init.sh
./all-ovarian.sh

# import ovarian protein data
./importProfileData.pl --data $CGDS_DATA_HOME/ovarian/data_protein.txt --meta $CGDS_DATA_HOME/ovarian/meta_protein.txt --dbmsAction clobber