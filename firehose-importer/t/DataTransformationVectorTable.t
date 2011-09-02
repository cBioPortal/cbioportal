#!/usr/bin/perl
# author: Arthur Goldberg, goldberg@cbio.mskcc.org

use strict;
use warnings;
use File::Spec;
use Test::More skip_all => 'fully deprecated';

use Utilities;

# Verify module can be included via "use" pragma
BEGIN { use_ok('DataTransformationVectorTable') }

# Verify module can be included via "require" pragma
require_ok('DataTransformationVectorTable');

my $DataTransformationVectorTable = DataTransformationVectorTable->new(
    File::Spec->catfile( File::Spec->curdir(), qw( data testDataTransformationVectorTable.txt ) ) );

is_deeply( [ $DataTransformationVectorTable->getSubroutines() ], [ qw( create_data_CNA create_data_miRNA 
    create_data_mRNA_median_Zscores ) ], 'test getSubroutines' );
is_deeply( $DataTransformationVectorTable->getFirehoseDirectoriesAndFiles( 'create_data_miRNA' ), 
    [[ qw( dir1x file1x ) ]], 'test getFirehoseDirectoriesAndFiles' );

is_deeply( $DataTransformationVectorTable->getFirehoseDirectoriesAndFiles( 'create_data_mRNA_median_Zscores' ), 
    [[ qw( dir1y file1y ) ], [ qw( dir2 file2 ) ] ], 'test getFirehoseDirectoriesAndFiles2' );

is( $DataTransformationVectorTable->getCGDS_data_file( 'create_data_miRNA' ), 
    'data_miRNA.txt', 'test getCGDS_data_file' );

is( $DataTransformationVectorTable->getCGDS_extra_argument( 'create_data_miRNA' ), 
    'extraArg', 'test getCGDS_extra_argument' );

# todo: test getLastestVersionOfFile AND dataTransformationVectorFile 
my @cancerNrunDate = qw( ov 20110114 );
my $runDir = '2011011400';
my $directoryNamePattern = 'gdac.broadinstitute.org_<cancer>.GDAC_median_mRNA_Expression.Level_4.<date><version>';
my $fileNamePattern = 'PR_GDAC_<CANCER>.medianexp.txt';
my $CancersFirehoseDataDir = File::Spec->catfile( File::Spec->curdir(), 'data', $runDir );
my $fn = getLastestVersionOfFile( $CancersFirehoseDataDir, $directoryNamePattern, $fileNamePattern, @cancerNrunDate );
is( -r $fn, 1, 'got file' );

$directoryNamePattern = 'No_such_dir';
$fn = getLastestVersionOfFile( $CancersFirehoseDataDir, $directoryNamePattern, $fileNamePattern, @cancerNrunDate );
is( $fn, undef, 'No_such_dir' );

# todo: replace with checkError() calls
print STDERR "No problem: will print 'Cannot find Firehose file PR_GDAC_OV.medianexp.txt in gdac.broadinstitute.org_ov.Gistic2.Level_4.2011011401.0.0 at /Users/goldbera/Documents/workspace/import_and_convert_Firehose_data/lib/DataTransformationVectorTable.pm line ... .'\n";
$directoryNamePattern = 'gdac.broadinstitute.org_<cancer>.Gistic2.Level_4.<date><version>';
$fn = getLastestVersionOfFile( $CancersFirehoseDataDir, $directoryNamePattern, $fileNamePattern, @cancerNrunDate );
is( $fn, undef, 'file not in there' );

print STDERR "No problem: will print 'Error: Found multiple Firehose PR_GDAC_OV.maf.annotated.* files in gdac.broadinstitute.org_ov.MutationAssessor.Level_4.2011011401.0.0 at /Users/goldbera/Documents/workspace/import_and_convert_Firehose_data/lib/DataTransformationVectorTable.pm line ... .'\n";
$directoryNamePattern = 'gdac.broadinstitute.org_<cancer>.MutationAssessor.Level_4.<date><version>';
$fileNamePattern = 'PR_GDAC_<CANCER>.maf.annotated<version>';
$fn = getLastestVersionOfFile( $CancersFirehoseDataDir, $directoryNamePattern, $fileNamePattern, @cancerNrunDate );
is( $fn, undef, 'multiple copies' );

