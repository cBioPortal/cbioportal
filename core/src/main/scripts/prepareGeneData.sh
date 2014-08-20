#!/bin/bash

echo "downloading Homo_sapiens.gene_info.gz from ncbi..."
rm $PORTAL_DATA_HOME/reference-data/Homo_sapiens.gene_info.gz
rm $PORTAL_DATA_HOME/reference-data/Homo_sapiens.gene_info
rm $PORTAL_DATA_HOME/reference-data/human-genes.txt
wget -P $PORTAL_DATA_HOME/reference-data/ ftp://ftp.ncbi.nih.gov//gene/DATA/GENE_INFO/Mammalia/Homo_sapiens.gene_info.gz
echo "unzipping..."
gunzip $PORTAL_DATA_HOME/reference-data/Homo_sapiens.gene_info.gz
mv $PORTAL_DATA_HOME/reference-data/Homo_sapiens.gene_info $PORTAL_DATA_HOME/reference-data/human-genes.txt


# loci data for calculating gene length
rm $PORTAL_DATA_HOME/reference-data/gencode.v17.annotation.gtf.gz
rm $PORTAL_DATA_HOME/reference-data/gencode.v17.annotation.gtf
rm $PORTAL_DATA_HOME/reference-data/all_exon_loci.bed

echo "downloading gencode.v17.annotation.gtf.gz from sanger"
wget -P $PORTAL_DATA_HOME/reference-data/ ftp://ftp.sanger.ac.uk/pub/gencode/release_17/gencode.v17.annotation.gtf.gz

echo "unzipping..."
gunzip $PORTAL_DATA_HOME/reference-data/gencode.v17.annotation.gtf.gz

echo "trimming..."
grep -v ^# $PORTAL_DATA_HOME/reference-data/gencode.v17.annotation.gtf | perl -ne 'chomp; @c=split(/\t/); $c[0]=~s/^chr//; $c[3]--; $c[8]=~s/.*gene_name\s\"([^"]+)\".*/$1/; print join("\t",@c[0,3,4,8,5,6])."\n" if($c[2] eq "CDS" or $c[2] eq "exon")' > $PORTAL_DATA_HOME/reference-data/all_exon_loci.bed

rm $PORTAL_DATA_HOME/reference-data/gencode.v17.annotation.gtf

echo "done"