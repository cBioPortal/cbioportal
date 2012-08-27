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
--cancerMAFFilesToSkipFile # file containing cancer studies for which the MAF should not be removed
--cancerCaseFilesToSkipFile # file containing cancer studies for which case studies should not be removed

EOT

my($stagingFilesDirectory, $overridesDirectory, $cancerMAFFilesToSkipFile, $cancerCaseFilesToSkipFile);

main();
sub main{
	
    # process arg list
    GetOptions (
        "stagingFilesDirectory=s" => \$stagingFilesDirectory,
        "overridesDirectory=s" => \$overridesDirectory,
        "cancerMAFFilesToSkipFile=s" => \$cancerMAFFilesToSkipFile,
        "cancerCaseFilesToSkipFile=s" => \$cancerCaseFilesToSkipFile );

    my $f = File::Util->new();

	my @tmpList;
    my @tmp = $f->load_file( $cancerMAFFilesToSkipFile, '--as-lines' );
    foreach (@tmp){
	  push(@tmpList, $_);
    }
	my %cancerMAFFilesToSkip = map { $_, 1 } @tmpList;

    @tmp = $f->load_file( $cancerCaseFilesToSkipFile, '--as-lines' );
    foreach (@tmp){
	  push(@tmpList, $_);
    }
	my %cancerCaseFilesToSkip = map { $_, 1 } @tmpList;

	my @allCancerDirs = $f->list_dir($stagingFilesDirectory, '--dirs-only');
    foreach (@allCancerDirs){
		# construct staging files cancer directory
    	my $cancerDirectory = $_;
		my $stagingAreaCancerDirectory = File::Spec->catdir($stagingFilesDirectory, $cancerDirectory);

		# remove the MAF & meta if found
		my $toRemove = File::Spec->catfile($stagingAreaCancerDirectory, 'data_mutations_extended.txt');
		if (-e $toRemove) {
			if (exists($cancerMAFFilesToSkip{$cancerDirectory})) {
				print "skipping removal of maf file: $toRemove\n";
			}
			else {
				print "removing MAF from staging area: $toRemove\n";
				system("rm -f $toRemove"); 
				$toRemove = File::Spec->catfile($stagingAreaCancerDirectory, 'meta_mutations_extended.txt');
				if (-e $toRemove) {
					print "removing meta file from staging area: $toRemove\n";
					system("rm -f $toRemove"); 
				}
			}
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
				if (-e $caseListInOverrides || $caseListFile eq 'cases_sequenced.txt') {
				  if ( (exists($cancerCaseFilesToSkip{$cancerDirectory}) && $caseListFile ne 'cases_sequenced.txt') ||
					  ($caseListFile eq 'cases_sequenced.txt' && exists($cancerMAFFilesToSkip{$cancerDirectory}))) {
					print "skipping removal of case file $caseListFile from: $cancerDirectory\n";
					next;
				  }
				  print "removing case list file from staging area: $caseListInOverrides\n";
				  my $toRemove = File::Spec->catfile($stagingAreaCaseListDirectory, $caseListFile);
				  system("rm -f $toRemove"); 
				}
			  }
		}
    }
}
