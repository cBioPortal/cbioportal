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

Removes all data_mutations_extended.txt (MAFs) from the given staging area as well as all case files found in the overridesDirectory that exist in the staging area.

--stagingFilesDirectory # directory which stores staging files
--overridesDirectory    # directory containing overrides

EOT

my($stagingFilesDirectory, $overridesDirectory);

main();
sub main{
	
    # process arg list
    GetOptions (
        "stagingFilesDirectory=s" => \$stagingFilesDirectory,
        "overridesDirectory=s" => \$overridesDirectory );

    my $f = File::Util->new();
	my @allCancerDirs = $f->list_dir($stagingFilesDirectory, '--dirs-only');
    foreach (@allCancerDirs){
		# construct staging files cancer directory
    	my $cancerDirectory = $_;
		my $stagingAreaCancerDirectory = File::Spec->catdir($stagingFilesDirectory, $cancerDirectory);

		# remove the MAF if found
		my $toRemove = File::Spec->catfile($stagingAreaCancerDirectory, 'data_mutations_extended.txt');
		if (-e $toRemove) {
			print "removing MAF from staging area: $toRemove\n";
			system("rm -f $toRemove"); 
		}

		# construct case list directories
		my $stagingAreaCaseListDirectory = File::Spec->catdir($stagingAreaCancerDirectory, 'case_lists');
		my $overrideCaseListDirectory = File::Spec->catdir($overridesDirectory, $cancerDirectory, 'case_lists');

		if (-d $stagingAreaCaseListDirectory) {
			# remove any case lists in the staging area that exist in the override area - these are custom lists
			my @allCaseListFiles = $f->list_dir($stagingAreaCaseListDirectory, '--pattern=.*\.txt$');
			foreach my $caseListFile ( @allCaseListFiles ) {
				# check if exists in overrides
				my $caseListInOverrides = File::Spec->catfile($overrideCaseListDirectory, $caseListFile);
				if (-e $caseListInOverrides) {
					print "removing case list file from staging area: $caseListInOverrides\n";
					my $toRemove = File::Spec->catfile($stagingAreaCaseListDirectory, $caseListFile);
					system("rm -f $toRemove"); 
				}
			}
		}
    }
}
