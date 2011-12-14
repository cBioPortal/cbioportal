#!/usr/bin/perl
# file: moveGDACOverrides.pl
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
moveGDACOverrides.pl

Given a set of override files in a directory, move them into a Firehose data set.

--DeepFirehoseDirectory <Firehose Directory>        # required; directory which stores firehose data
--customFileType                                    # indicator of the custom file type to move (Agilent MRNA, RNA Seq, CNA, MAF)
--customDirectory                                   # directory containing overrides
--RunDate                                           # rundate for the Firehose run
--customFilesToMoveFile <file containing pairs of custom_file cancer_type>
                                                    # required; 
EOT

# args:
# on laptop, for testing:
# --DeepFirehoseDirectory /Users/goldbera/Data/firehose/data/copyOfCurrent/data
# --customDirectory /Users/goldbera/Documents/workspace/cgds/data/MAFs --RunDate 20110327 
# --customFilesToMoveFile /Users/goldbera/Data/firehose/data/copyOfCurrent/specialMAFs.txt
# on buri:
# --DeepFirehoseDirectory /scratch/data/goldberg/firehoseData/tcga-data.nci.nih.gov/tcgafiles/ftp_auth/distro_ftpusers/tcga4yeo/other/gdacs/gdacbroad --customDirectory /home/goldberg/workspace/sander/cgds/data/MAFs --RunDate 20110421 --customFilesToMoveFile /home/goldberg/workspace/sander/import_and_convert_Firehose_data/config/specialMAFs.txt

my( $customDirectory, $customFileType, $DeepFirehoseDirectory, $runDate, $customFilesToMoveFile );

# make sure to put the customFile file in the proper gdac directory, 
# because that's what convertFirehoseData.pl will use via getLastestVersionOfFile()

# map - key is customFileType, value is gdac dir - file pair
my $customFileProperties = {
	'AGILENT-MRNA' => [ 'gdac.broadinstitute.org_<CANCER>.Merge_transcriptome__agilentg4502a_07_3__unc_edu__Level_3__unc_lowess_normalization_gene_level__data.Level_3.<date><version>', '<CANCER>.transcriptome__agilentg4502a_07_3__unc_edu__Level_3__unc_lowess_normalization_gene_level__data.data.txt'],
	'RNA-SEQ' => [ 'gdac.broadinstitute.org_<CANCER>.RNA_Seq.<date><version>', '<CANCER>_rnaseq.txt'],
	'CNA' => [ 'gdac.broadinstitute.org_<CANCER>.CopyNumber_Gistic2.Level_4.<date><version>', 'all_thresholded.by_genes.txt'],
	'MAF' => [ 'gdac.broadinstitute.org_<CANCER>.Mutation_Assessor.Level_4.<date><version>', '<CANCER>.maf.annotated'],
};

main();
sub main{
	
    # process arg list
    GetOptions (
        "customDirectory=s" => \$customDirectory,
		"customFileType=s" => \$customFileType,
        "DeepFirehoseDirectory=s" => \$DeepFirehoseDirectory,
        "runDate=s" => \$runDate,
        "customFilesToMoveFile=s" => \$customFilesToMoveFile );

	unless(exists($customFileProperties->{$customFileType})) {
		warn "customFileType: $customFileType is not recognized.";
		exit;
	}
        
    my %customFilesToMove;
    my $f = File::Util->new();
    my @tmp = $f->load_file( $customFilesToMoveFile, '--as-lines' );
    foreach (@tmp){
    	my( $file, $cancer ) = split( /\s+/, $_ );
    	$customFilesToMove{$file} = $cancer;
    }

	my $destDir = $customFileProperties->{$customFileType}->[0];
	my $destFile = $customFileProperties->{$customFileType}->[1];

	foreach my $customFile (keys %customFilesToMove){
	    moveGDACOverridesFile( $customFile, $customFilesToMove{$customFile}, $destDir, $destFile );
	}
}

sub moveGDACOverridesFile{
	my( $customFile, $cancer, $destDir, $destFile ) = @_;
	# in override dir, cancer directory is called <CANCER>_tcga
	my @directories = ( $customDirectory, $cancer . '_tcga' );
	my $fromFile = File::Spec->catdir( @directories, $customFile );

    my $CancersFirehoseDataDir = File::Spec->catfile( $DeepFirehoseDirectory, $cancer, $runDate . '00' );

	# if we are using custom CNA, we will need to move over Log2CNA,
	# lets get latest version before we get next version
	my $latestVersionOfLog2CNAFile;
	if ( $customFileType eq "CNA" ) {
	  $latestVersionOfLog2CNAFile = getLastestVersionOfFile( $CancersFirehoseDataDir, $destDir, "all_data_by_genes.txt", $cancer, $runDate );
	  print "latest version of Log2CNA: $latestVersionOfLog2CNAFile\n";
	}

    my( $customFileDir, $customFileFile ) = getNextVersionOfFile( $CancersFirehoseDataDir, 
																  $destDir, $destFile,
																  $cancer, $runDate );
    
    my $toFile = File::Spec->catfile( $customFileDir, $customFileFile );
    print "\ncopying:\n", "from: $fromFile\n", "  to: $toFile\n";
	print `wc -l $fromFile`; 
    mkdir( $customFileDir ); system( "cp $fromFile $toFile"); 
    print `cmp  $fromFile $toFile`;

	# if using custom CNA, lets now copy over Log2CNA
	if ( $customFileType eq "CNA"  && defined($latestVersionOfLog2CNAFile) )
	{
	  my $newLog2CNAFile = File::Spec->catfile( $customFileDir, "all_data_by_genes.txt" );
	  print "\ncopying Log2CNA:\n", "from: $latestVersionOfLog2CNAFile\n", "  to: $newLog2CNAFile\n";
	  system( "cp $latestVersionOfLog2CNAFile $newLog2CNAFile"); 
	  print `cmp  $latestVersionOfLog2CNAFile $newLog2CNAFile`;
	}
	else {
	  warn "Copying custom CNA and cannot find Log2CNA data\n";
	}
	
	# must also create an empty directory in which the sig_genes.txt file will be created    
	if ( $customFileType eq "MAF" ) {
		my( $mutSigDir, $mutSigFile ) = getNextVersionOfFile( $CancersFirehoseDataDir, 
															  'gdac.broadinstitute.org_<CANCER>.Mutation_Significance.Level_4.<date><version>', '<CANCER>.sig_genes.txt',
															  $cancer, $runDate );
		print "making: $mutSigDir\n";
		mkdir( $mutSigDir );
	}
}

# get a lexicographically later version of a file
sub getNextVersionOfFile{
    my( $CancersFirehoseDataDir, $directoryNamePattern, $fileNamePattern, $cancer, $runDate ) =@_;
    my $latestVersion = getLastestVersionOfFile( $CancersFirehoseDataDir, $directoryNamePattern, $fileNamePattern, $cancer, $runDate );
    my($volume, $latestDir, $latestFile ) = File::Spec->splitpath( $latestVersion );
    my $nextDir = $latestDir;
	unless ( -d $nextDir) {
		my $customFileDir = File::Spec->catfile($CancersFirehoseDataDir, $directoryNamePattern);
		my $cancer_UC = uc( $cancer );
		$customFileDir =~ s/<CANCER>/$cancer_UC/;
		my $dateVersion = $runDate . "00.0.0";
		$customFileDir =~ s/<date><version>/$dateVersion/;
		print "cannot find dir to put customFile file, making: $customFileDir\n";
		$nextDir = File::Util->new->make_dir($customFileDir);
		# latest dir/file did not exist, we need to set latestFile properly here
		$latestFile = $fileNamePattern;
		$latestFile =~ s/<CANCER>/$cancer_UC/;
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
