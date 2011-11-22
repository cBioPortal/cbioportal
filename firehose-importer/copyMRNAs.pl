#!/usr/bin/perl
# file: copyMRNAs.pl
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
copyMRNAs.pl

Given a set of mRNAs files in a directory, move them into a Firehose data set.

--DeepFirehoseDirectory <Firehose Directory>        # required; directory which stores firehose data
--mRNAsDirectory                                     # directory containing mRNAs
--RunDate                                           # rundate for the Firehose run
--mRNAsToMoveFile <file containing pairs of mRNA_file cancer_type> # required; 
EOT

my( $mRNAsDirectory, $DeepFirehoseDirectory, $runDate, $mRNAsToMoveFile );
main();
sub main{
	
    # process arg list
    GetOptions (
        "mRNAsDirectory=s" => \$mRNAsDirectory,
        "DeepFirehoseDirectory=s" => \$DeepFirehoseDirectory,
        "runDate=s" => \$runDate,
        "mRNAsToMoveFile=s" => \$mRNAsToMoveFile );
        
    my %mRNAsToMove;
    my $f = File::Util->new();
    my @tmp = $f->load_file( $mRNAsToMoveFile, '--as-lines' );
    foreach (@tmp){
    	my( $file, $cancer ) = split( /\s+/, $_ );
    	$mRNAsToMove{$file} = $cancer;
    }
    	
	foreach my $mRNA (keys %mRNAsToMove){
	    moveMRNA( $mRNA, $mRNAsToMove{$mRNA});
	}
}

sub moveMRNA{
	my( $mRNA, $cancer ) = @_;
	my $fromFile = File::Spec->catdir( $mRNAsDirectory, $mRNA );

    # make sure to put the mRNA file in the latest version of the gdac.broadinstitute.org_<CANCER>.Merge_transcriptome__agilentg4502a_07_<version>__unc_edu__Level_3__unc_lowess_normalization_gene_level__data.Level_3.<date><version> directory, 
    # because that's what convertFirehoseData.pl will use via getLastestVersionOfFile()
    my $CancersFirehoseDataDir = File::Spec->catfile( $DeepFirehoseDirectory, $cancer, $runDate . '00' );
    my( $mRNADir, $mRNAFile ) = getNextVersionOfFile( $CancersFirehoseDataDir, 
        'gdac.broadinstitute.org_<CANCER>.RNA_Seq.<date><version>', '<CANCER>_rnaseq.txt',
        $cancer, $runDate );
    
    my $toFile = File::Spec->catfile( $mRNADir, $mRNAFile );
    print "\ncopying:\n", "from: $fromFile\n", "  to: $toFile\n";
	print `wc -l $fromFile`; 
    mkdir( $mRNADir ); system( "cp $fromFile $toFile"); 
    print `cmp  $fromFile $toFile`;
}

# get a lexicographically later version of a file
sub getNextVersionOfFile{
    my( $CancersFirehoseDataDir, $directoryNamePattern, $fileNamePattern, $cancer, $runDate, ) =@_;
    my $latestVersion = getLastestVersionOfFile( $CancersFirehoseDataDir, $directoryNamePattern, $fileNamePattern, $cancer, $runDate );
    my($volume, $latestDir, $latestFile ) = File::Spec->splitpath( $latestVersion );
    my $nextDir = $latestDir;
	unless ( -d $nextDir) {
	  my $mRNADir = File::Spec->catfile($CancersFirehoseDataDir, $directoryNamePattern);
	  my $cancer_UC = uc( $cancer );
	  $mRNADir =~ s/<CANCER>/$cancer_UC/;
	  my $dateVersion = $runDate . "00.0.0";
	  $mRNADir =~ s/<date><version>/$dateVersion/;
	  print "cannot find dir to put maf file, making: $mRNADir\n";
	  $nextDir = File::Util->new->make_dir($mRNADir);
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
