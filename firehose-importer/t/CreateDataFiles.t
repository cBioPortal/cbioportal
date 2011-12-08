#!/usr/bin/perl
# author: Arthur Goldberg, goldberg@cbio.mskcc.org

use strict;

# use warnings; # skip, because of incorrect warning on STDERR redirect
use Test::More tests => 36;
use Data::CTable;
use File::Util;
use Data::Dumper;
use File::Temp qw/ tempfile tempdir /;

use CreateDataFiles;
use ConvertFirehoseData;
use FirehoseTransformationWorkflow;
use GeneIdentifiers;
use Utilities;

BEGIN { use_ok('ConvertFirehoseData') }
require_ok('ConvertFirehoseData');

# Verify module can be included via "use" and "require" pragmas
BEGIN { use_ok('CreateDataFiles') }
require_ok('CreateDataFiles');

# TODO: add other tests to FirehoseTransformationWorkflow.yaml
my $FirehoseTransformationWorkflow = FirehoseTransformationWorkflow->new( 
	File::Spec->catfile( File::Spec->curdir(), qw( data TestConvertFirehoseData FirehoseTransformationWorkflow.yaml ) ) );

# print STDERR "\nNo problem: will print some 'Not using gene symbol' warning messages.\n";
# trap STDERR
my $stderrOutput;
no warnings; # repress warning: 'Name "main::SAVEERR" used only once: possible typo at ... '
open SAVEERR, ">&STDERR"; 
use warnings;
close STDERR;
open STDERR, ">", \$stderrOutput or die "What the heck?\n";
my $GeneMap = GeneIdentifiers->new( File::Spec->catfile( File::Spec->curdir(), qw( data human_gene_info ) ) );
# Close and restore STDERR to original condition.
close STDERR;
open STDERR, ">&SAVEERR";

# need 'ImportantMutatedGenes' to test create_data_mutations
$GeneMap->loadGeneList( File::Spec->catfile( File::Spec->curdir(), qw( data ImportantMutatedGenes.txt ) ),
	'ImportantMutatedGenes' );

my $createDataFiles = CreateDataFiles->new($GeneMap);

my $globalHash = {};

# subroutine test_input_file correct_output_file
# all test files stored in ./data/

# cannot use FirehoseTransformationWorkflow->next_sub() here, because data isn't organized in root / [cancer] / subroutine_dir / subroutine_file
foreach my $sub ( $FirehoseTransformationWorkflow->get_subroutine_sequence() ) {
	print "\nTesting: sub $sub\{ ... \}\n";

	my $subsTestRootDir = File::Spec->catfile( File::Spec->curdir(), qw( data TestCreateDataFiles ), "test_$sub" );
    #	print "\$subsTestRootDir $subsTestRootDir\n";
	my @testDirs = File::Util->new()->list_dir( $subsTestRootDir, '--dirs-only', '--no-fsdots' );

	# in each dir, get input file(s) and corresponding output file
	testDirectory:
	foreach my $dir (@testDirs) {

	    # put output in a file in a temp dir
	    my $template = File::Spec->catfile( File::Spec->tmpdir(), "TempCGDSFilesForCreateDataFilesTestXXXX" );
	    my $CGDSdir = tempdir( $template, CLEANUP => 1 );     # automatically deleted, on exit

		# input files
		my @inputFiles;
		my @fullFiles;
		my @cTables;
		my @ffms;

		my $FirehoseDirectoriesAndFiles = $FirehoseTransformationWorkflow->getFirehoseDirectoriesAndFiles($sub);

		# returns a ref to a list of refs to pairs of the form [directory, file]
		foreach my $dirAndFile ( @{$FirehoseDirectoriesAndFiles} ) {
			my( $fhDir, $file ) = @{$dirAndFile};
			my $inputFile = prepareFilenameForGrep( $file, 'Generic_Cancer', 'Run_Date' );
			$inputFile =~ s/\.\*//g;

			my $fullFn = File::Spec->catfile( $subsTestRootDir, $dir, $inputFile );
			if( $fullFn =~ /CVS/ ){
                next testDirectory;
			}
			unless( -f $fullFn ) {
                print "missing $fullFn\n";
                next testDirectory;
			}

            push @inputFiles, $inputFile;
            push @fullFiles, $fullFn;
			push @cTables, Data::CTable->new( { _CacheOnRead => 0 }, $fullFn );

            if( $FirehoseTransformationWorkflow->getOutputFile($sub, 'Generic_Cancer') =~ /^$Utilities::dataFilePrefix/ ){
	            my $ffm = FirehoseFileMetadata->new( $file, $fullFn );
	            if( defined( $ffm ) ){
	                push @ffms, $ffm;
	            }  
            }
		}

		my $CGDSfile = File::Spec->catfile( $CGDSdir, $FirehoseTransformationWorkflow->getOutputFile($sub, 'Generic_Cancer') );

        ###############
        # TEST calling sub declared in $FirehoseTransformationWorkflow

        # todo: find a better way than an environment variable, but that's the best for now
        my $CGDScodeDir = $ENV{'CGDS_HOME'};
        # TODO: HIGH: REPLACE THIS HARDCODED PROBLEM (NEEDED INSIDE ECLIPSE)
        unless( defined( $CGDScodeDir )){
            $CGDScodeDir = '/Users/goldbera/Documents/workspace/cgds';
        }

        my $correctOutputFile =
          File::Spec->catfile( $subsTestRootDir, $dir,
            'correct_out_' . $FirehoseTransformationWorkflow->getOutputFile($sub, 'Generic_Cancer') );

        my $extraArg = $FirehoseTransformationWorkflow->getArgs( $sub );
        unless( defined( $extraArg )){
        	$extraArg = 'undef'; 
        }
        my $testName =
          "testing in directory $dir: $sub( globalHash, [firehoseFiles], [cTables], "
          . $FirehoseTransformationWorkflow->getOutputFile($sub, 'Generic_Cancer') . ', [CGDS code dir], '
          . $extraArg . ' )'; 

        # test that correct error is produced
        # if correct output file only has one line, then it's an error message: check that we got it
        if ( 1 == File::Util->new()->line_count($correctOutputFile) ) {
            my $error = File::Util->new()->load_file($correctOutputFile);

            # temporarily catch STDERR in memory, to compare with expected
            # works with 'warn', but not code that writing directly to the C file descriptor 2 (STDERR); no way to capture that in Perl
            my $stderrOutput;

            # First, save away STDERR
            open SAVEERR, ">&STDERR";
            close STDERR;
            open STDERR, ">", \$stderrOutput or die "What the hell?\n";

            # call sub, with output redirected to $stderrOutput
            $createDataFiles->$sub( $globalHash, [@fullFiles], [@cTables], $CGDSfile, $CGDScodeDir,
                $FirehoseTransformationWorkflow->getArgs( $sub ) ); 

            # Close and restore STDERR to original condition.
            close STDERR;
            open STDERR, ">&SAVEERR";

            is( $stderrOutput, $error, $testName );
            next;
        }

		# call sub( globalHash, [ firehose files ], [cTables ], CGDSfile, CGDScodeDir, extraArgs ) 
		print "\nCalling: $sub( \n\t[ globalHash, ", join( ' ', @fullFiles ),
		  "], \n\t[ <cTables> ],\n\t$CGDSfile,\n\t$CGDScodeDir,\n\t$extraArg )\n";
		  $createDataFiles->$sub( $globalHash, [@fullFiles], [@cTables], $CGDSfile, $CGDScodeDir,
    		  $FirehoseTransformationWorkflow->getArgs( $sub ) ); 

		# compare actual and correct
		compareFiles( $CGDSfile, $correctOutputFile, $testName );

        #################
	    # test sub createMetaFile

        # set $genes and $cases
		my $genes;
		my $cases; 
        foreach my $ffm ( @ffms ) {
        	# same as in ConvertFirehoseData.pm
        	# num cases and genes for the last FirehoseFileMetadata object
			$genes = $ffm->numGenes();
			$cases = $ffm->numCases();
        	
        }

        if( $FirehoseTransformationWorkflow->getOutputFile($sub, 'Generic_Cancer') =~ /^$Utilities::dataFilePrefix/ ){

	    createMetaFile( 'GENERIC_CANCER', $CGDSdir, 
	       $FirehoseTransformationWorkflow->getOutputFile($sub, 'Generic_Cancer'), $cases, $genes );
        }

        # get correct meta file
        my $metaFilename = $FirehoseTransformationWorkflow->getOutputFile($sub, 'Generic_Cancer');
        $metaFilename =~ s/$Utilities::dataFilePrefix/$Utilities::metaFilePrefix/; 
        my $metaFile = File::Spec->catfile( $CGDSdir, $metaFilename );

        my $correctMetaOutputFile =
          File::Spec->catfile( $subsTestRootDir, $dir, 'correct_out_' . $metaFilename );

        # compare actual and correct files
        compareFiles( $CGDSfile, $correctOutputFile, "test compareFiles for $sub" );
        
        # TODO: tests for the following subroutines
        # create_many_to_one_case_lists
        # create_one_to_one_case_lists

	}
}

