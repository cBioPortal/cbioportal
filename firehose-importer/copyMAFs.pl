#!/usr/bin/perl
# file: copyMAFs.pl
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
copyMAFs.pl

Secure copies MAFs from the firehose directory to a given distribution area.

--DeepFirehoseDirectory <Firehose Directory>        # directory which stores firehose data
--DestinationDirectory <Destination Directory>      # directory which stores destination staging files
--RunDate                                           # rundate for the Firehose run
--Host                                              # destination host
--User                                              # destintation host user

EOT

my( $DeepFirehoseDirectory, $DestinationDirectory, $RunDate, $Host, $User );

main();
sub main{
	
    # process arg list
    GetOptions (
        "DeepFirehoseDirectory=s" => \$DeepFirehoseDirectory,
        "DestinationDirectory=s" => \$DestinationDirectory,
        "RunDate=s" => \$RunDate,
		"Host=s" => \$Host,
		"User=s" => \$User );

    my $f = File::Util->new();

	my $sourceDir = 'gdac.broadinstitute.org_<CANCER>.Mutation_Assessor.Level_4.<date><version>';
	my $sourceFile = '<CANCER>.maf.annotated';

	my @cancerDirectories = $f->list_dir( $DeepFirehoseDirectory, '--dirs-only' );
	foreach my $cancerDirectory (@cancerDirectories) { 
	  unless ($cancerDirectory =~ /\./ || $cancerDirectory =~ /\.\./) {
		copyMAFsFile($cancerDirectory, $sourceDir, $sourceFile );
	  }
	}
}

sub copyMAFsFile{
	my($cancer, $sourceDir, $sourceFile ) = @_;

	my $destFile = 'data_mutations_extended.txt';
    my $CancersFirehoseDataDir = File::Spec->catdir( $DeepFirehoseDirectory, $cancer, $RunDate . '00' );
	my $latestVersionOfMAFFile = getLastestVersionOfFile( $CancersFirehoseDataDir, $sourceDir, $sourceFile, $cancer, $RunDate );

	if (defined ($latestVersionOfMAFFile)) {
	  print "lastest version of MAF: $latestVersionOfMAFFile\n";
	  my $destinationFile = File::Spec->catfile( ($DestinationDirectory, $cancer . '_tcga'), $destFile );
	  print "\nsecure copying MAF:\n", "from: $latestVersionOfMAFFile\n", "  to: $destinationFile\n";
	  print "host: $Host\n", "User: $User\n";
	  system( "scp $latestVersionOfMAFFile $User\@$Host:$destinationFile" );
	}
}
