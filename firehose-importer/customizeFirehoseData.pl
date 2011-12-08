#!/usr/bin/perl
# file: customizeFirehoseData.pl
# author: Arthur Goldberg, goldberg@cbio.mskcc.org
# customize firehose data

# SUMMARY
# Pre-process custom data before running convertFirehoseData.pl (customizeFirehoseData.pl)
# Driver that cleans up custom data added to a Firehose TCGA dataset, in preparation for running convertFirehoseData.pl
# Issues: Some redundancy with convertFirehoseData.pl
# Testing: no unit testing

use strict;
use warnings;
use Getopt::Long;
use File::Spec;
use File::Util;
use Data::Dumper;
use Data::CTable;

use Utilities;
use CustomizeFirehoseData;
use FirehoseTransformationWorkflow;

my $usage = <<EOT;
usage:
customizeFirehoseData.pl
In general, given some custom data added to a Broad Firehose TCGA dataset, clean up the custom data 
in preparation for running convertFirehoseData.pl. In particular:
1. make sig_genes for new maf files: if a maf file doesn't have a corresponding <CANCER>sig_genes.txt file, 
then create one with the columns gene and n

--FirehoseDirectory <Firehose Directory>        # required; directory which stores firehose data
--Cancers <file containing cancers to process and their descriptions>
                                                # required; name of file listing cancers to process, and their descriptions; other cancers will be ignored
--firehoseTransformationWorkflowFile            # file containing Workflow dependencies
EOT

# current args:
# customizeFirehoseData.pl --FirehoseDirectory /Users/goldbera/Data/firehose/data/copyOfCurrent  --firehoseTransformationWorkflowFile /Users/goldbera/Data/firehose/data/copyOfCurrent/FirehoseTransformationWorkflow.yaml --Cancers /Users/goldbera/Data/firehose/data/copyOfCurrent/cancers

main();
sub main{

	my( $firehoseTransformationWorkflowFile, $FirehoseDirectory, $Cancers );
    # process arg list
    GetOptions (
        "firehoseTransformationWorkflowFile=s" => \$firehoseTransformationWorkflowFile,
        "FirehoseDirectory=s" => \$FirehoseDirectory,
        "Cancers=s" => \$Cancers );
     
    unless( defined( $firehoseTransformationWorkflowFile) && defined( $FirehoseDirectory) && defined( $Cancers )  ){
    	die(  "All options must be provided.\n" . $usage . "\n");
    }         
    
    print "$FirehoseDirectory\n";
    
	# scan directory tree
	# use firehoseTransformationWorkflowFile to define directory names
	my $firehoseTransformationWorkflow = FirehoseTransformationWorkflow->new( $firehoseTransformationWorkflowFile );
	my( $Mutation_Assessor_Dir_pattern, $Mutation_Assessor_File_pattern ) = 
	   get_pattern( $firehoseTransformationWorkflow, 'create_data_mutations_extended'); 
	my( $Mutation_Significance_Dir_pattern, $Mutation_Significance_File_pattern ) = 
	   get_pattern( $firehoseTransformationWorkflow, 'create_mutation_white_list');
	   
    my $fileUtil = File::Util->new();
    
    # find each cancer dir
    my @cancers = listCancers( $Cancers );    

    my @allDirs = $fileUtil->list_dir( $FirehoseDirectory, '--recurse', '--dirs-only' );
    foreach my $cancer (@cancers){
    	
        my $pattern = '\\' . $fileUtil->SL . $cancer . '$';
        my @t = grep( /$pattern/, @allDirs );
        my $CancersFirehoseDataDir = $t[0];

        unless( defined( $CancersFirehoseDataDir ) && -d $CancersFirehoseDataDir ){
        	warn "No data available for $cancer.\n";
        	next;
        }
        
        # get the run directory and run date
        @t = $fileUtil->list_dir( $CancersFirehoseDataDir, '--no-fsdots' );
        # ignore Mac OS X .DS_Store files
        @t = grep( !/.DS_Store/, @t );
        if( scalar( @t ) != 1 ){
            warn "Multiple Firehose runs for $cancer: " . join(' ', @t) . "\n";
            next;
        }
        my $runDir = $t[0];
        my $runDate = $runDir;
        $runDate =~ s/\d\d$//;

        $CancersFirehoseDataDir = File::Spec->catfile( $CancersFirehoseDataDir, $runDir );
	    # find cancers which have Mutation_Assessor but not Mutation_Significance
        my $Full_Mutation_Assessor_File = getLastestVersionOfFile( $CancersFirehoseDataDir,
           $Mutation_Assessor_Dir_pattern, $Mutation_Assessor_File_pattern, $cancer, $runDate );
        my $Full_Mutation_Significance_File = getLastestVersionOfFile( $CancersFirehoseDataDir,
           $Mutation_Significance_Dir_pattern, $Mutation_Significance_File_pattern, $cancer, $runDate );
           
        if( defined( $Full_Mutation_Assessor_File ) && !defined( $Full_Mutation_Significance_File )){
        	print "calling create_a_missing_sig_genes_file() for $cancer\n";
            # then call create_a_missing_sig_genes_file( $cancer, $cancerRootDir, $Mutation_Assessor_Dir, 3 );
            my $new_sig_genes_file = create_a_missing_sig_genes_file( $cancer, $CancersFirehoseDataDir, 
                $fileUtil->strip_path( $fileUtil->return_path( $Full_Mutation_Assessor_File ) ), 1 );
            print `wc $new_sig_genes_file`;
            print `head $new_sig_genes_file`;
        }           
    }
	
}

# given a FirehoseTransformationWorkflow object and subroutine name, return the (first) directory and file pattern for that sub
sub get_pattern{
	my( $firehoseTransformationWorkflow, $sub ) = @_;
	
	my $FirehoseDirectoriesAndFiles = $firehoseTransformationWorkflow->getFirehoseDirectoriesAndFiles( $sub );
	unless( defined( $FirehoseDirectoriesAndFiles )){
		return undef;
	}
    my $dirAndFile = $FirehoseDirectoriesAndFiles->[0];
    return @{$dirAndFile};
}
