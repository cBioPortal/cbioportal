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
		$f->make_dir( $toDirectory, '--if-not-exists' );
		# iterate over all .txt files in from dir
		my @allDataFiles = $f->list_dir( $fromDirectory, '--pattern=data_.*\.txt$' );
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
