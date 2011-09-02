#!/usr/bin/perl
# file: loadCGDSdata.pl
# author: Arthur Goldberg, goldberg@cbio.mskcc.org

use strict;
use warnings;
use Getopt::Long;
use LoadCGDSdata;

# SUMMARY
# Driver that loads CGDS input files into dbms (loadCGDSdata.pl)
# Simple program that takes command line arguments and calls LoadCGDSdata->run
# Issues:
# Testing: No unit testing

my $usage = <<EOT;
Given a set of well-structured CGDS input files, load them into the CGDS database.

loadCGDSdata.pl usage:
--codeForCGDS <directory>           directory of CGDS code
--CGDSDataDirectory                 name of directory containing CGDS data, i.e., directories containing data for each cancer type 
--Cancers <file containing cancers to process and their meta data>
--Genes <name of gene file>         name of file containing gene mappings
--miRNAfile <name of miRNA file>    name of file containing miRNA mappings
--universalSomaticWhitelist         optional, full filename of file containing universal somatic whitelist

--nameOfPerCancerSomaticWhitelist   optional, name of file containing per-cancer somatic whitelists, to be loaded if present
--nameOfPerCancerGermlineWhitelist  optional, name of file containing per-cancer germline whitelists, to be loaded if present
--acceptRemainingMutations          optional, whether to accept remaining mutations after filter; if present then cgds mutation filter 
                                    will load into the database all mutations not filtered by previous rules; use only for secure portals 
EOT

# current args: e.g., use on buri:
# internal use only TCGA Portal
# loadCGDSdata.pl --codeForCGDS /home/goldberg/workspace/sander/cgds/  --CGDSDataDirectory /home/goldberg/data/TCGAdata/cancers/  
# --Cancers /home/goldberg/data/TCGAdata/cancers/cancers.txt
# --Genes /home/goldberg/data/TCGAdata/cancers/human_gene_info_2011_01_19  --miRNAfile /home/goldberg/data/TCGAdata/cancers/microRNAs.txt
# --acceptRemainingMutations

# public TCGA Portal
# loadCGDSdata.pl --codeForCGDS /home/goldberg/workspace/sander/cgds/  --CGDSDataDirectory /home/goldberg/data/TCGAdata/cancers/  
# --Genes /home/goldberg/data/TCGAdata/cancers/human_gene_info_2011_01_19  --miRNAfile /home/goldberg/data/TCGAdata/cancers/microRNAs.txt
# --universalSomaticWhitelist  /home/goldberg/data/TCGAdata/cancers/universalSomaticGeneWhitelist.txt
# --nameOfPerCancerGermlineWhitelist germlineWhiteList.txt 
# --nameOfPerCancerSomaticWhitelist  somaticWhiteList.txt

my $codeForCGDS;
my $CGDSDataDirectory;
my $Cancers;
my $GeneFile;
my $miRNAfile;
my $MutationFilter;
my $universalSomaticWhitelist;
my $nameOfPerCancerSomaticWhitelist;
my $nameOfPerCancerGermlineWhitelist;
my $acceptRemainingMutations;

GetOptions (
	"codeForCGDS=s" => \$codeForCGDS,
	"CGDSDataDirectory=s" => \$CGDSDataDirectory,
    "Cancers=s" => \$Cancers, 
	"GeneFile=s" => \$GeneFile,
	"miRNAfile=s" => \$miRNAfile,
	"universalSomaticWhitelist=s" => \$universalSomaticWhitelist,
	"nameOfPerCancerSomaticWhitelist=s" => \$nameOfPerCancerSomaticWhitelist,
	"nameOfPerCancerGermlineWhitelist=s" => \$nameOfPerCancerGermlineWhitelist,
	"acceptRemainingMutations" => \$acceptRemainingMutations,
	);

my @args = ( $codeForCGDS, $CGDSDataDirectory, $Cancers, $GeneFile, $miRNAfile ); 
if( defined( $nameOfPerCancerGermlineWhitelist )){
    push @args, $nameOfPerCancerGermlineWhitelist;
}else{
    push @args, undef;
}
if( defined( $nameOfPerCancerSomaticWhitelist )){
    push @args, $nameOfPerCancerSomaticWhitelist;
}else{
    push @args, undef;
}
my $loadMutationArguments="";
if( defined( $universalSomaticWhitelist )){
    $loadMutationArguments .= " --somaticWhiteList $universalSomaticWhitelist ";
}
if( defined( $acceptRemainingMutations )){
    $loadMutationArguments .= " --acceptRemainingMutations ";
}
push @args, $loadMutationArguments;
LoadCGDSdata->run( @args );
        
