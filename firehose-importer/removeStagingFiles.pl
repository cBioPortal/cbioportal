#!/usr/bin/perl
# file: removeStagingFiles.pl
# author: Benjamin Gross

use strict;
use warnings;
use File::Spec;
use File::Util;
use Data::Dumper;
use Getopt::Long;

use FirehoseTransformationWorkflow;

my $usage = <<EOT;
usage:
removeStagingFiles.pl

Remove a set of staging files from the staging directory.

--stagingFilesDirectory # directory which stores staging files
--filesToRemoveFile         # file containing cancer_type for custom files

EOT

my( $stagingFilesDirectory, $filesToRemoveFile );

main();
sub main{
	
    # process arg list
    GetOptions (
        "stagingFilesDirectory=s" => \$stagingFilesDirectory,
        "filesToRemoveFile=s" => \$filesToRemoveFile );

    my %customFilesToMove;
    my $f = File::Util->new();
    my @tmp = $f->load_file( $filesToRemoveFile, '--as-lines' );
    foreach (@tmp){
    	my( $stagingFile, $cancerDirectory ) = split( /\s+/, $_ );
		# construct file to remove
		my @directories = ( $stagingFilesDirectory, $cancerDirectory );
		my $stagingFileToRemove = File::Spec->catfile( @directories, $stagingFile );
		# if file to remove exists, delete
		if ( -e $stagingFileToRemove ) {
		  print "\nremoving staging file:\n", "  to: $stagingFileToRemove\n";
		  system( "rm -f $stagingFileToRemove" ); 
		}
    }
}
