#!/usr/bin/perl
# file: moveOverrides.pl
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
moveOverrides.pl

Move a set of public (private)  overrides into the public (private) staging directory.

--overridesDirectory    # directory containing overrides
--stagingFilesDirectory # directory which stores staging files
--overridesFile         # file containing cancer_type for custom files

EOT

my( $overridesDirectory, $stagingFilesDirectory, $overridesFile );

main();
sub main{
	
    # process arg list
    GetOptions (
        "overridesDirectory=s" => \$overridesDirectory,
        "stagingFilesDirectory=s" => \$stagingFilesDirectory,
        "overridesFile=s" => \$overridesFile );

    my %customFilesToMove;
    my $f = File::Util->new();
    my @tmp = $f->load_file( $overridesFile, '--as-lines' );
    foreach (@tmp){
    	my $cancerDirectory = $_;
		# construct from directory
		my $fromDirectory = File::Spec->catdir(( $overridesDirectory, $cancerDirectory ));
		# construct to directory
		my $toDirectory = File::Spec->catdir(( $stagingFilesDirectory, $cancerDirectory ));
		$f->make_dir( $toDirectory, '--if-not-exists' );
		# iterate over all .txt files in from dir
		my @allDataFiles = $f->list_dir( $fromDirectory, '--pattern=.*\.txt$' );
		foreach my $dataFile ( @allDataFiles ) {
		  my $FullDataFile = File::Spec->catfile( $fromDirectory, $dataFile);
		  print "\ncopying:\n", "from: $FullDataFile\n", "  to: $toDirectory\n";
		  system( "cp $FullDataFile $toDirectory"); 
		}
		# cp seg file
		print "\ncopying:\n", "from: $fromDirectory/*.seg\n", "  to: $toDirectory\n";
		system( "cp $fromDirectory/*.seg $toDirectory"); 
    }
}
