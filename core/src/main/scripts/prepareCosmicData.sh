#!/bin/bash

rm -rf $PORTAL_DATA_HOME/reference-data/cosmic_tmp
mkdir $PORTAL_DATA_HOME/reference-data/cosmic_tmp

echo "downloading..."
wget -P $PORTAL_DATA_HOME/reference-data/cosmic_tmp/ ftp://ngs.sanger.ac.uk/production/cosmic/CosmicCodingMuts*.vcf.gz

echo "extracting..."
gunzip $PORTAL_DATA_HOME/reference-data/cosmic_tmp/CosmicCodingMuts*.vcf.gz

echo "copying..."
mv $PORTAL_DATA_HOME/reference-data/cosmic_tmp/CosmicCodingMuts*.vcf $PORTAL_DATA_HOME/reference-data/CosmicCodingMuts.vcf

echo "cleaning up..."
rm -rf $PORTAL_DATA_HOME/reference-data/cosmic_tmp

echo "done."