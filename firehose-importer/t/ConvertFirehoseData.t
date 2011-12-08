#!/usr/bin/perl
# author: Arthur Goldberg, goldberg@cbio.mskcc.org

use strict;
use warnings;
use File::Spec;
use Data::Dumper; 
use File::Util;
use File::Temp qw/ tempfile tempdir /;
use Test::More tests => 3; # qw( no_plan); # skip_all => 'not implemented';
use FirehoseTransformationWorkflow;

use Utilities;
use ConvertFirehoseData;

# Verify module can be included via "use" pragma
BEGIN { use_ok('ConvertFirehoseData') }

# Verify module can be included via "require" pragma
require_ok('ConvertFirehoseData');

# test create_cgds_input_files from ConvertFirehoseData.t
# this tests the driver programs but not the functions called in CreateDataFiles.pm which are tested by CreateDataFiles.t
# make test directory of Firehose data (4 cancers, including 2 with miRNA - ov and gbm - and 2 without)
my $test_root_dir = File::Spec->catfile( File::Spec->curdir(), qw( data TestConvertFirehoseData ) );
my $Cancers = File::Spec->catfile( $test_root_dir, 'cancers.txt' );
my $Summary = File::Spec->catfile( $test_root_dir, 'summary_of_results.tab' );
my $template = File::Spec->catfile( File::Spec->tmpdir(), 'Test_Directory_For_ConvertFirehoseData_XXXX' );

# make tmp directory of correct cgds output
my $CGDSDataDirectory = tempdir( $template ); # TODO: , CLEANUP => 1 );     # automatically deleted, on exit
my $CancerDataDir = File::Spec->catfile( $test_root_dir, 'firehose', 'firehoseWithSmallFiles' ); 
my $FTW = FirehoseTransformationWorkflow->new( File::Spec->catfile( $test_root_dir, 'FirehoseTransformationWorkflow.yaml' )  );
# this depends on storing import_and_convert_Firehose_data in the same directory as CGDS; otherwise it could be found with an ENV variable
my $codeForCGDS = File::Spec->catfile( File::Spec->updir(), File::Spec->updir(), 'cgds' );
my $GenesFile = File::Spec->catfile( $test_root_dir, 'human_gene_info' );
my $runDate = '20110327'; 
my $runDirectory = '2011032700';

# run create_cgds_input_files 
print STDERR "\nNo problem: will print some error messages.\n";
create_cgds_input_files( $Cancers, $Summary, $CGDSDataDirectory, $CancerDataDir, 
    $runDirectory, $FTW, $codeForCGDS, $GenesFile, $runDate );

# compare actual and expected summary files
# compareFiles( $CGDSfile, $correctOutputFile, $testName );
compareFiles( $Summary, File::Spec->catfile( $test_root_dir, 'correct_summary_of_results.tab'), 'compare actual and expected summary files' );
