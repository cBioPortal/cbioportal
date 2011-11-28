#!/usr/bin/perl
# file: copyCNAs.pl
# author: Arthur Goldberg, goldberg@cbio.mskcc.org
# author: Benjamin Gross, grossb@cbio.mskcc.org

use strict;
use warnings;
use File::Spec;
use File::Util;
use Data::Dumper;
use Getopt::Long;

use FirehoseTransformationWorkflow;
        # TODO: ACCESS CONTROL: change to CANCER_STUDY

my $usage = <<EOT;
usage:
copyCNAs.pl

Given a set of CNAs files in a directory, move them into a Firehose data set.

--DeepFirehoseDirectory <Firehose Directory>        # required; directory which stores firehose data
--CNAsDirectory                                     # directory containing CNAs
--RunDate                                           # rundate for the Firehose run
--CNAsToMoveFile <file containing pairs of CNA_file cancer_type> # required; 
EOT

my( $CNAsDirectory, $DeepFirehoseDirectory, $runDate, $CNAsToMoveFile );
main();
sub main{
	
    # process arg list
    GetOptions (
        "CNAsDirectory=s" => \$CNAsDirectory,
        "DeepFirehoseDirectory=s" => \$DeepFirehoseDirectory,
        "runDate=s" => \$runDate,
        "CNAsToMoveFile=s" => \$CNAsToMoveFile );
        
    my %CNAsToMove;
    my $f = File::Util->new();
    my @tmp = $f->load_file( $CNAsToMoveFile, '--as-lines' );
    foreach (@tmp){
    	my( $file, $cancer ) = split( /\s+/, $_ );
    	$CNAsToMove{$file} = $cancer;
    }
    	
	foreach my $CNA (keys %CNAsToMove){
	    moveCNA( $CNA, $CNAsToMove{$CNA});
	}
}

sub moveCNA{
	my( $CNA, $cancer ) = @_;
	my $fromFile = File::Spec->catdir( $CNAsDirectory, $CNA );

    # make sure to put the CNA file in the latest version of the gdac.broadinstitute.org_<CANCER>.CopyNumber_Gistic2.Level_4.<date><version> directory, 
    # because that's what convertFirehoseData.pl will use via getLastestVersionOfFile()
    my $CancersFirehoseDataDir = File::Spec->catfile( $DeepFirehoseDirectory, $cancer, $runDate . '00' );
    my( $CNADir, $CNAFile ) = getNextVersionOfFile( $CancersFirehoseDataDir, 
        'gdac.broadinstitute.org_<CANCER>.CopyNumber_Gistic2.Level_4.<date><version>', 'all_thresholded.by_genes.txt',
        $cancer, $runDate );
    
    my $toFile = File::Spec->catfile( $CNADir, $CNAFile );
    print "\ncopying:\n", "from: $fromFile\n", "  to: $toFile\n";
	print `wc -l $fromFile`; 
    mkdir( $CNADir ); system( "cp $fromFile $toFile"); 
    print `cmp  $fromFile $toFile`;
}

# get a lexicographically later version of a file
sub getNextVersionOfFile{
    my( $CancersFirehoseDataDir, $directoryNamePattern, $fileNamePattern, $cancer, $runDate, ) =@_;
    my $latestVersion = getLastestVersionOfFile( $CancersFirehoseDataDir, $directoryNamePattern, $fileNamePattern, $cancer, $runDate );
    my($volume, $latestDir, $latestFile ) = File::Spec->splitpath( $latestVersion );
    my $nextDir = $latestDir;
	unless ( -d $nextDir) {
	  my $CNADir = File::Spec->catfile($CancersFirehoseDataDir, $directoryNamePattern);
	  my $cancer_UC = uc( $cancer );
	  $CNADir =~ s/<CANCER>/$cancer_UC/;
	  my $dateVersion = $runDate . "00.0.0";
	  $CNADir =~ s/<date><version>/$dateVersion/;
	  print "cannot find dir to put maf file, making: $CNADir\n";
	  $nextDir = File::Util->new->make_dir($CNADir);
	}
    # pattern is '.digit.digit/'
    $nextDir =~ /(\d)\.(\d)\/?$/;
    unless( defined($1) and defined($2)){
	  die "Did not match directory pattern correctly on $nextDir.";
    }   
    # arbitrarily increment the last digit 
    my $nextVersionNum = $2 + 1;
    $nextDir =~ s|(\d)\.(\d)\/$|$1.$nextVersionNum|;
    return ($nextDir, $latestFile);
}
