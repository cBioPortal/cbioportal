#!/usr/bin/perl
# file: movePUBLICOverrides.pl
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
movePUBLICOverrides.pl

Move a set of public overrides into the public staging directory.

--publicOverridesDirectory    # directory containing overrides
--publicStagingFilesDirectory # directory which stores staging files
--publicOverridesFile         # file containing cancer_type for custom files

EOT

my( $publicOverridesDirectory, $publicStagingFilesDirectory, $publicOverridesFile );

main();
sub main{
	
    # process arg list
    GetOptions (
        "publicOverridesDirectory=s" => \$publicOverridesDirectory,
        "publicStagingFilesDirectory=s" => \$publicStagingFilesDirectory,
        "publicOverridesFile=s" => \$publicOverridesFile );

    my %customFilesToMove;
    my $f = File::Util->new();
    my @tmp = $f->load_file( $publicOverridesFile, '--as-lines' );
    foreach (@tmp){
    	my $cancerDirectory = $_;
		# construct from directory
		my $fromDirectory = File::Spec->catdir(( $publicOverridesDirectory, $cancerDirectory ));
		# construct to directory
		my $toDirectory = File::Spec->catdir(( $publicStagingFilesDirectory, $cancerDirectory ));
		# if $toDirectory exists, overwrite
		if ( -d $toDirectory) {
		  print "\ndestination directory exists, removing:\n", "  to: $toDirectory\n";
		  system( "rm -rf $toDirectory"); 
		}
		print "\ncopying:\n", "from: $fromDirectory\n", "  to: $toDirectory\n";
		system( "cp -r $fromDirectory $toDirectory"); 
		# remove cvs
		my $cvsDirectory = "$toDirectory/CVS";
		if ( -d $cvsDirectory ) {
		  system( "rm -rf $toDirectory/CVS");
		}
		$cvsDirectory = "$toDirectory/case_lists/CVS";
		if ( -d $cvsDirectory ) {
		  system( "rm -rf $toDirectory/case_lists/CVS");
		}
    }
}
