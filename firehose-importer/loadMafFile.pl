#!/usr/bin/perl

# author: Arthur Goldberg, goldberg@cbio.mskcc.org

# WARNING: NOT COMPLETE; MAY NOT WORK

# Load a maf file

# Main actions:
# 1. check maf
# 2. make temp firehose directory and copy maf there 
# 3. make significantly mutated genes (sig_genes) for the maf, to be used for mutation filtering
# 4. convert the maf to cgds input format
# 5. use cgds to load the maf into the dbms

# Details:
# [check and correct columns]
# [specify database; rebuild cgds]
# make tmp dir
# make gdac.broadinstitute.org_<CANCER>.Mutation_Assessor.Level_4.<date><version> dir
# cp maf to that dir as <CANCER>.maf.annotated
#
# write tmp cancers file
# run customizeFirehoseData.pl, to make sig_genes for the new maf
#
# create tmp cgds input dir
# run convertFirehoseData.pl on new maf and sig_genes to tmp cgds dir; will make case list
#
# run org.mskcc.cgds.scripts.ImportProfileData with appropriate mutation filtering

use strict;
use warnings;
use Getopt::Long;
use File::Spec;
use File::Util;
use Data::Dumper;
use File::Temp qw/ tempfile tempdir /;

use DataTransformationVectorTable;
use Utilities;
use CustomizeFirehoseData;
use ConvertFirehoseData;
use LoadCGDSdata;

my $usage = <<EOT;
usage:
loadMafFile.pl

--RootDir <root directory>              # optional root directory; all other file names can be absolute or relative -- relative files rooted here
--MafFile <name of maf file>            # name of maf file
--CancerType <cancer type>              # cancer type of the maf file; TCGA abbreviation
--dataTransformationVectorFile <file>   # file containing data transformation vector config
--GeneFile <name of gene file>          # name of file containing gene mappings
--miRNAfile <name of miRNA file>        # name of file containing miRNA mappings
--codeForCGDS <directory>               # directory of CGDS code
--mutationMode <Internal or Public>     # mode of mutation filtering
EOT

# test args:
# --MafFile /Users/goldbera/Documents/fullCGDS/sander/cgds/data/ovarian/3-center_OV.Exome_DNASeq.1.Somatic_and_Germline_WU-Annotation.05jan2011a.maf --CancerType ov --dataTransformationVectorFile /Users/goldbera/tmp/data_transformation_vector_table.forMAFfile.conf --GeneFile /Users/goldbera/Documents/workspace/import_and_convert_Firehose_data/reference_data/human_gene_info_2011_01_19 --miRNAfile /Users/goldbera/Documents/workspace/import_and_convert_Firehose_data/reference_data/microRNAs.txt --codeForCGDS /Users/goldbera/Documents/workspace/cgds --mutationMode  Public
# --MafFile /Users/goldbera/tmp/sample.ov.maf --CancerType ov --dataTransformationVectorFile /Users/goldbera/tmp/data_transformation_vector_table.forMAFfile.conf --GeneFile /Users/goldbera/Documents/workspace/import_and_convert_Firehose_data/reference_data/human_gene_info_2011_01_19 --miRNAfile /Users/goldbera/Documents/workspace/import_and_convert_Firehose_data/reference_data/microRNAs.txt --codeForCGDS /Users/goldbera/Documents/workspace/cgds --mutationMode  Public
# --RootDir /Users/goldbera/Documents/workspace/ --MafFile /Users/goldbera/tmp/sample.ov.maf --CancerType ov --dataTransformationVectorFile /Users/goldbera/tmp/data_transformation_vector_table.forMAFfile.conf --GeneFile ./import_and_convert_Firehose_data/reference_data/human_gene_info_2011_01_19 --miRNAfile ./import_and_convert_Firehose_data/reference_data/microRNAs.txt --codeForCGDS ./cgds --mutationMode Public

main();

sub main {

	my ( $RootDir, $MafFile, $CancerType, $dataTransformationVectorFile, $GeneFile, $miRNAfile, $codeForCGDS,
		$mutationMode );

	GetOptions(
		"RootDir=s"                      => \$RootDir,
		"MafFile=s"                      => \$MafFile,
		"CancerType=s"                   => \$CancerType,
		"dataTransformationVectorFile=s" => \$dataTransformationVectorFile,
		"GeneFile=s"                     => \$GeneFile,
		"miRNAfile=s"                    => \$miRNAfile,
		"codeForCGDS=s"                  => \$codeForCGDS,
		"mutationMode=s"                 => \$mutationMode,
	);

	my $utilities = Utilities->new($usage);
	$utilities->verifyArgumentsAreDefined( $MafFile, $CancerType, $dataTransformationVectorFile, $GeneFile, $miRNAfile,
		$codeForCGDS, $mutationMode );

	# create full pathnames
    $MafFile = $utilities->CreateFullPathname( $RootDir, $MafFile );
    $dataTransformationVectorFile = $utilities->CreateFullPathname( $RootDir, $dataTransformationVectorFile );
    $GeneFile = $utilities->CreateFullPathname( $RootDir, $GeneFile );
    $miRNAfile = $utilities->CreateFullPathname( $RootDir, $miRNAfile );
    $codeForCGDS = $utilities->CreateFullPathname( $RootDir, $codeForCGDS );
    
	# check on files and dirs
	foreach my $inputFile (qw( MafFile dataTransformationVectorFile GeneFile miRNAfile )) {
		no strict 'refs';
		$utilities->CheckOnFile( 'read', eval "\$$inputFile" );
	}
	use strict;
    
    ############
    # TODO: 1. check maf
    
    ############
    # 2. make temp firehose directory and copy maf there 
	# make tmp dir
	my $template = File::Spec->catfile( File::Spec->tmpdir(), "TempFilesForLoadMafFileXXXX" );
    # todo: tmp cleanup: remove this and other temp files
	my $tmpdir = tempdir( $template ); # don't cleanup: , CLEANUP => 1 );

	# make a $CancerType subdir with $runDirectory directory in it, so that create_cgds_input_files works;
	my $tmp = File::Spec->catfile( $tmpdir, $CancerType );
	mkdir $tmp or die "Could not make $tmp: $?";
    my $runDirectory = 'runDirectory';
    my $tmpdirForFirehoseData = File::Spec->catfile( $tmpdir, $CancerType, $runDirectory );
    mkdir $tmpdirForFirehoseData or die "Could not make $tmpdirForFirehoseData: $?";
	print "\$tmpdirForFirehoseData $tmpdirForFirehoseData\n";

	# read dataTransformationVectorFile
	my $dataTransformationVector = DataTransformationVectorTable->new($dataTransformationVectorFile);

	# make gdac.broadinstitute.org_<CANCER>.Mutation_Assessor.Level_4.<date><version> dir
	# get name of dir that contains maf
	my $firehoseDirectoriesAndFiles =
	  $dataTransformationVector->getFirehoseDirectoriesAndFiles('create_data_mutations_extended');
	my ( $firehoseDir, $firehoseFile ) = @{ $firehoseDirectoriesAndFiles->[0] };
	my $ucCANCER = uc($CancerType);
	$firehoseFile =~ s/<CANCER>/$ucCANCER/;
	$firehoseDir  =~ s/<CANCER>/$ucCANCER/;
	$firehoseDir  =~ s/<//g;
	$firehoseDir  =~ s/>//g;
	my $fullFirehoseMafDir = File::Spec->catfile( $tmpdirForFirehoseData, $firehoseDir );
	print "\$fullFirehoseMafDir $fullFirehoseMafDir\n";
	mkdir $fullFirehoseMafDir or die "Could not make $fullFirehoseMafDir: $?";

	# cp maf to that dir as <CANCER>.maf.annotated
	my $fullFirehoseMaf = File::Spec->catfile( $fullFirehoseMafDir, $firehoseFile );
	system("cp $MafFile $fullFirehoseMaf") == 0
	  or die "'cp $MafFile $fullFirehoseMaf' failed: $?";
	print "\$fullFirehoseMaf $fullFirehoseMaf\n";

	# write tmp cancers file
	my ($fileUtil) = File::Util->new();
	my $cancersFile = File::Spec->catfile( $tmpdir, 'cancers.txt' );
	print "\$cancersFile $cancersFile\n";

	$fileUtil->write_file(
		'file'    => $cancersFile,
		'content' => "$CancerType:description for $CancerType\n",
		'bitmask' => 0644
	);
	
	###############
	# 3. make significantly mutated genes (sig_genes) for the maf, to be used for mutation filtering

	# run CustomizeFirehoseData::create_a_missing_sig_genes_file(), to make sig_genes for the new maf
	my $new_sig_genes_file = create_a_missing_sig_genes_file( $CancerType, $tmpdirForFirehoseData, $firehoseDir, 2 );
	print "new_sig_genes_file\n"; system("cat $new_sig_genes_file");

    ###############
    # 4. convert the maf to cgds input format

	# create tmp cgds input dir
    # todo: tmp cleanup: remove this and other temp files
	$template = File::Spec->catfile( File::Spec->tmpdir(), "TempCGDSFilesForLoadMafFileXXXX" );
	my $tmpCGDSdataDir = tempdir( $template ); # don't cleanup: , CLEANUP => 1 );

	# run convertFirehoseData.pl on new maf and sig_genes to tmp cgds dir; will make case list
    my $SummaryFile = '/tmp/loadMafFileSummary.out';

    create_cgds_input_files( $cancersFile, $SummaryFile, $tmpCGDSdataDir, $tmpdir, $runDirectory, 
        $dataTransformationVector, $codeForCGDS, $GeneFile );

    ###############
    # 5. use cgds to load the maf into the dbms
    
    my $dataFile = File::Spec->catfile( $tmpCGDSdataDir, $CancerType, 
        $dataTransformationVector->getCGDS_data_file( 'create_data_mutations_extended' ) );
        
    my $somaticWhiteList = File::Spec->catfile( $tmpCGDSdataDir, $CancerType, 
        $dataTransformationVector->getCGDS_data_file( 'create_mutation_white_list' ) );
    
    loadCGDScancerMutationDataFile( $codeForCGDS, '/', $dataFile, set_up_classpath( $codeForCGDS ), 
        undef, $somaticWhiteList );

}
