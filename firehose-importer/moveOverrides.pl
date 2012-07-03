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

my($overridesDirectory, $stagingFilesDirectory, $overridesFile);

main();
sub main{
	
    # process arg list
    GetOptions (
        "overridesDirectory=s" => \$overridesDirectory,
        "stagingFilesDirectory=s" => \$stagingFilesDirectory,
        "overridesFile=s" => \$overridesFile );

    my %customFilesToMove;
    my $f = File::Util->new();
    my @tmp = $f->load_file($overridesFile, '--as-lines');
    foreach (@tmp){
    	my $cancerDirectory = $_;
		# construct from directory
		my $fromDirectory = File::Spec->catdir($overridesDirectory, $cancerDirectory);
		# construct to directory
		my $toDirectory = File::Spec->catdir($stagingFilesDirectory, $cancerDirectory);
		# if toDirectory exists, remove it
		if (-d $toDirectory) {
		  system("rm -rf $toDirectory");
		}
		print "\ncopying directory:\n", "source: $fromDirectory\n", "  target: $toDirectory\n";
		system("cp -r $fromDirectory $toDirectory");
		# remove CVS directories
		my $cvsDir = File::Spec->catdir($toDirectory, 'CVS');
		if (-d $cvsDir) {
		  system("rm -rf $cvsDir");
		}
		$cvsDir = File::Spec->catdir($toDirectory, 'case_lists', 'CVS');
		if (-d $cvsDir) {
		  system("rm -rf $cvsDir");
		}
    }
}
