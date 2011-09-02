#!/usr/bin/perl
# file: moveMAFs.pl
# author: Arthur Goldberg, goldberg@cbio.mskcc.org

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
moveMAFs.pl

Given a set of maf files in a directory, move them into a Firehose data set.

--DeepFirehoseDirectory <Firehose Directory>        # required; directory which stores firehose data
--MafsDirectory                                     # directory containing mafs
--RunDate                                           # rundate for the Firehose run
--MafsToMoveFile <file containing pairs of maf_file cancer_type>
                                                    # required; 
EOT

# args:
# on laptop, for testing:
# --DeepFirehoseDirectory /Users/goldbera/Data/firehose/data/copyOfCurrent/data
# --MafsDirectory /Users/goldbera/Documents/workspace/cgds/data/MAFs --RunDate 20110327 
# --MafsToMoveFile /Users/goldbera/Data/firehose/data/copyOfCurrent/specialMAFs.txt
# on buri:
# --DeepFirehoseDirectory /scratch/data/goldberg/firehoseData/tcga-data.nci.nih.gov/tcgafiles/ftp_auth/distro_ftpusers/tcga4yeo/other/gdacs/gdacbroad --MafsDirectory /home/goldberg/workspace/sander/cgds/data/MAFs --RunDate 20110421 --MafsToMoveFile /home/goldberg/workspace/sander/import_and_convert_Firehose_data/config/specialMAFs.txt

my( $MafsDirectory, $DeepFirehoseDirectory, $runDate, $MafsToMoveFile );
main();
sub main{
	
    # process arg list
    GetOptions (
        "MafsDirectory=s" => \$MafsDirectory,
        "DeepFirehoseDirectory=s" => \$DeepFirehoseDirectory,
        "runDate=s" => \$runDate,
        "MafsToMoveFile=s" => \$MafsToMoveFile );
        
    my %mafsToMove;
    my $f = File::Util->new();
    my @tmp = $f->load_file( $MafsToMoveFile, '--as-lines' );
    foreach (@tmp){
    	my( $file, $cancer ) = split( /\s+/, $_ );
    	$mafsToMove{$file} = $cancer;
    }
    	
	foreach my $maf (keys %mafsToMove){
	    moveMAF( $maf, $mafsToMove{$maf});
	}
}

sub moveMAF{
	my( $maf, $cancer ) = @_;
	my $fromFile = File::Spec->catdir( $MafsDirectory, $maf );

    # make sure to put the maf file in the latest version of the gdac.broadinstitute.org_<CANCER>.Mutation_Assessor.Level_4.<date><version> directory, 
    # because that's what convertFirehoseData.pl will use via getLastestVersionOfFile()
    my $CancersFirehoseDataDir = File::Spec->catfile( $DeepFirehoseDirectory, $cancer, $runDate . '00' );
    my( $mafDir, $mafFile ) = getNextVersionOfFile( $CancersFirehoseDataDir, 
        'gdac.broadinstitute.org_<CANCER>.Mutation_Assessor.Level_4.<date><version>', '<CANCER>.maf.annotated',
        $cancer, $runDate );
    
    my $toFile = File::Spec->catfile( $mafDir, $mafFile );
    print "\ncopying:\n", "from: $fromFile\n", "  to: $toFile\n";
	print `wc -l $fromFile`; 
    mkdir( $mafDir ); system( "cp $fromFile $toFile"); 
    print `cmp  $fromFile $toFile`;
    
    # must also create an empty directory in which the sig_genes.txt file will be created
    my( $mutSigDir, $mutSigFile ) = getNextVersionOfFile( $CancersFirehoseDataDir, 
        'gdac.broadinstitute.org_<CANCER>.Mutation_Significance.Level_4.<date><version>', '<CANCER>.sig_genes.txt',
        $cancer, $runDate );
    print "making: $mutSigDir\n";
    mkdir( $mutSigDir );
}

# get a lexicographically later version of a file
sub getNextVersionOfFile{
    my( $CancersFirehoseDataDir, $directoryNamePattern, $fileNamePattern, $cancer, $runDate, ) =@_;
    my $latestVersion = getLastestVersionOfFile( $CancersFirehoseDataDir, $directoryNamePattern, $fileNamePattern, $cancer, $runDate );
    my($volume, $latestDir, $latestFile ) = File::Spec->splitpath( $latestVersion );
    my $nextDir = $latestDir;
	#unless ( -d $nextDir) {
	  #my $mafDir = File::Spec->catfile($CancersFirehoseDataDir, $directoryNamePattern);
	  #my $cancer_UC = uc( $cancer );
	  #$mafDir =~ s/<CANCER>/$cancer_UC/;
	  #$mafDir =~ s/<date><version>/$runDate.0.0/;
	  #print "cannot find dir to put maf file, making: $mafDir\n";
	  #$nextDir = File::Util->new->makde_dir($mafDir);
    #}
    # pattern is '.digit.digit/'
    $nextDir =~ /(\d)\.(\d)\/$/;
    unless( defined($1) and defined($2)){
	  die "Did not match directory pattern correctly on $nextDir.";
    }   
    # arbitrarily increment the last digit 
    my $nextVersionNum = $2 + 1;
    $nextDir =~ s|(\d)\.(\d)\/$|$1.$nextVersionNum|;
    return ($nextDir, $latestFile);
}
