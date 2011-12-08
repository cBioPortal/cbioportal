#!/usr/bin/perl
# author: Arthur Goldberg, goldberg@cbio.mskcc.org

use strict;
use warnings;
use Test::More tests => 57;
use File::Spec;
use Data::Dumper;

# author: Arthur Goldberg, goldberg@cbio.mskcc.org

use CaseIDs;
use Utilities;

# Verify module can be included via "use" pragma
BEGIN { use_ok('FirehoseFileMetadata') }

# Verify module can be included via "require" pragma
require_ok('FirehoseFileMetadata');

# automatically check STDERR, as in CreateDataFiles.t
checkError( 'FirehoseFileMetadata', 'new', "FirehoseFileMetadata::new(): Cannot read no_such_file at ...",
    undef, "not enough headers error", '<CANCER>.maf.annotated', 'no_such_file' );

checkError( 'FirehoseFileMetadata', 'new', "Not enough data rows to delete 1 extra header row(s). at ...",
    undef, "not enough headers error", '<CANCER>.mirna__h_mirna_8x15k<version>__unc_edu__Level_3__unc_DWD_Batch_adjusted__data.data.txt',  File::Spec->catfile( 
    File::Spec->curdir(), 'data', 'TestFirehoseFileMetadata', 'empty_file.data.txt' ) );

checkError( 'FirehoseFileMetadata', 'new', "Error: FirehoseFileMetadata::new(): could not find type for file: empty_file.data.txt at ...",
    undef, 'no such file type', 'no_such_file_type',  
    File::Spec->catfile( File::Spec->curdir(), 'data', 'TestFirehoseFileMetadata', 'empty_file.data.txt' ) );

checkError( 'FirehoseFileMetadata', 'new', "FirehoseFileMetadata::new(): Cannot read no_such_file at ...",
    undef, 'cannot read error', '<CANCER>.maf.annotated', 'no_such_file' );

# list of (test filename, its file (data type) pattern) pairs
my @testFiles = (
    [    'test_all_thresholded.by_genes.txt' => 'all_thresholded.by_genes.txt' ],
    [    'test_medianexp.txt' => '<CANCER>.medianexp.txt'],
    [       'test_mirna.data.txt' =>
        '<CANCER>.mirna__h_mirna_8x15k<version>__unc_edu__Level_3__unc_DWD_Batch_adjusted__data.data.txt' ],
    [    'test_maf.annotated' => '<CANCER>.maf.annotated' ],
    [    'testPR_GDAC_GBM.withdupes.medianexp.txt' => '<CANCER>.medianexp.txt' ],
    [    'test_methylation__humanmethylation27__jhu_usc_edu__Level_3__within_bioassay_data_set_function__data.data.txt' => 
        '<CANCER>.methylation__humanmethylation27__jhu_usc_edu__Level_3__within_bioassay_data_set_function__data.data.txt' ],
);

my $casesRaw = [
	[qw( TCGA-02-0001-01C-01D-0182-01    TCGA-02-0002-01A-01D-0182-01    TCGA-02-0003-01A-01D-0182-01 ) ],
	[qw( TCGA-02-0001-01C-01R   TCGA-02-0002-01A-01R    TCGA-02-0003-01A-01R ) ],
	[qw( TCGA-02-0001-01C-01T-0179-07   TCGA-02-0003-01A-01T-0179-07    TCGA-02-0004-01A-01T-0301-07) ],
	[qw( TCGA-04-1331-01 TCGA-04-1332-01 ) ],
    [qw( TCGA-02-0001-01C-01R   TCGA-02-0002-01A-01R    TCGA-02-0001-01A-01R ) ],
    [qw( TCGA-18-3406-01A-01D-0979-05   TCGA-18-3406-01A-01D-0979-05    TCGA-18-3406-01A-01D-0979-05    TCGA-18-3406-01A-01D-0979-05    TCGA-18-3407-01A-01D-0979-05    TCGA-18-3407-01A-01D-0979-05    TCGA-18-3407-01A-01D-0979-05    TCGA-18-3407-01A-01D-0979-05 ) ] # methylation
];

my $genesRaw = [
    [qw( 2 10 34 35  ) ],
    [qw(  AACS FSTL1 ELMO2 CREB3L1 RPS11 ) ],
    [qw(  DarkCorner dmr_285 ebv-miR-BART1-5p ebv-miR-BART10 ) ],
    [qw(  576 10489 127254 117178 22854 114771  ) ],
    [qw(  AACS FSTL1 ELMO2 CREB3L1 RPS11 ) ],
    [qw(  BST2    DDX43   MKRN3   NoSuchGene ) ] # methylation
];

my @numGenes = qw( 4 5 4 6 5 4 );

my $casesSimplified = [
	[ qw( TCGA-02-0001 TCGA-02-0002 TCGA-02-0003 ) ], 
	[ qw( TCGA-02-0001 TCGA-02-0002 TCGA-02-0003 ) ], 
	[ qw( TCGA-02-0001 TCGA-02-0003 TCGA-02-0004 ) ], 
	[ qw( TCGA-04-1331 TCGA-04-1332 ) ], 
    [ qw( TCGA-02-0001 TCGA-02-0002 ) ], # cases in     testPR_GDAC_GBM.withdupes.medianexp.txt, with dupe removed    
    [ qw( TCGA-18-3406 TCGA-18-3407 ) ] # methylation
];

# parallel list of errors expected from FirehoseFileMetadata->simplifiedCases
my @errors = (
    undef,
    undef,
    undef,
    undef,
    "case TCGA-02-0001 duplicated in testPR_GDAC_GBM.withdupes.medianexp.txt\n",
    undef,
);

my @numCases = qw( 3 3 3 2 2 2 );

my $i=0;
my %ffmObjects;
my $fh;
foreach my $fileTypePair (@testFiles){
	my( $f, $type ) = @{$fileTypePair};
	
	$fh = FirehoseFileMetadata->new( $type,  
        File::Spec->catfile( File::Spec->curdir(), 'data', 'TestFirehoseFileMetadata', $f ) );
        
    # print Dumper( $fh->cases(), $casesRaw->[$i] );
    is_deeply( $fh->cases(), $casesRaw->[$i], 'case list test for '. $f);
    if( defined( $errors[$i] ) ){
        checkError( $fh, 'simplifiedCases', $errors[$i], $casesSimplified->[$i], 'simplifid case list test for '. $f );
    }else{
        is_deeply( $fh->simplifiedCases(), $casesSimplified->[$i], 'simplifid case list test for '. $f);
    }
    is_deeply( $fh->genes(), $genesRaw->[$i], 'gene list test for '. $f);
    is( $fh->numGenes(), $numGenes[$i], "num genes test for $f" );
    is( $fh->numCases(), $numCases[$i], "num cases test for $f" );
    $ffmObjects{$f} = $fh;
    $i++;
}

is_deeply( [ FirehoseFileMetadata::union_of_case_lists( () )  ], [ () ], "union of empty");
is_deeply( [ FirehoseFileMetadata::intersection_of_case_lists( () )  ], [ () ], "intersection of empty");

# check the type-checking routine, '_typecheck_FirehoseFileMetadataInstances_elements'
checkError( 'FirehoseFileMetadata', 'intersection_of_case_lists', 'Some argument not a FirehoseFileMetadata at ...', undef, 
     "Some argument not a FirehoseFileMetadata", ( 3, values %ffmObjects ) );

checkError( 'FirehoseFileMetadata', 'intersection_of_case_lists', 'Some argument not a FirehoseFileMetadata at ...', undef, 
     "Some argument not a FirehoseFileMetadata", ( values %ffmObjects, 4 ) );

checkError( 'FirehoseFileMetadata', 'union_of_case_lists', 'Some argument not a FirehoseFileMetadata at ...', undef, 
     "Some argument not a FirehoseFileMetadata", ( 3, values %ffmObjects ) );

checkError( 'FirehoseFileMetadata', 'union_of_case_lists', 'Some argument not a FirehoseFileMetadata at ...', undef, 
     "Some argument not a FirehoseFileMetadata", ( values %ffmObjects, 4 ) );

test_set_operation( \%ffmObjects, 
    [ qw(     
        test_medianexp.txt 
        test_mirna.data.txt 
    ) ],
    [ qw( TCGA-02-0001   TCGA-02-0003 ) ], 'intersection' );    

test_set_operation( \%ffmObjects,
    [ qw(     
        test_all_thresholded.by_genes.txt 
        test_medianexp.txt 
        test_mirna.data.txt 
        test_maf.annotated ) ],
    [ () ], 'intersection' );    

test_set_operation( \%ffmObjects, [ qw( test_all_thresholded.by_genes.txt test_medianexp.txt ) ],
    [ qw( TCGA-02-0001 TCGA-02-0002 TCGA-02-0003 ) ], 'intersection' );    

test_set_operation( \%ffmObjects, [ qw(     test_all_thresholded.by_genes.txt test_mirna.data.txt ) ],
    [ qw( TCGA-02-0001   TCGA-02-0002    TCGA-02-0003 TCGA-02-0004 ) ], 'union' );    

test_set_operation( \%ffmObjects, 
    [ qw(
	    test_all_thresholded.by_genes.txt 
	    test_medianexp.txt 
	    test_mirna.data.txt 
	    test_maf.annotated ) ],
    [ qw( TCGA-02-0001   TCGA-02-0002    TCGA-02-0003 TCGA-02-0004 TCGA-04-1331 TCGA-04-1332 ) ], 'union' );    

test_set_operation( \%ffmObjects, [ qw( test_all_thresholded.by_genes.txt test_medianexp.txt ) ],
    [ qw( TCGA-02-0001 TCGA-02-0002 TCGA-02-0003 ) ], 'union' );

# test the set operations
# apply them to convertCaseID()s of cases
sub test_set_operation{
	my( $all_ffm_Objects,
	$firehoseFiles,
	$expectedCases,
	$operation ) = @_; 
	
	my @FFMinstances;
	foreach my $ff (@{$firehoseFiles}){
		if( exists( $all_ffm_Objects->{$ff} )){
			push @FFMinstances, $all_ffm_Objects->{$ff};
		}
	}

#   print Dumper( 'actual:', sort( FirehoseFileMetadata::union_of_case_lists( @FFMinstances ) ) );
# 	print Dumper( 'expected:', sort( map {convertCaseID( $_ )} @{$expectedCases} ) );
	my $testName = $operation . " on " . join( ' ', map { $_->getFilename()} @FFMinstances );
    if( $operation eq 'union'){
		is_deeply( [ sort( FirehoseFileMetadata::union_of_case_lists( @FFMinstances ) ) ],
		    [ sort( map {convertCaseID( $_ )} @{$expectedCases} ) ], $testName );
    }

    if( $operation eq 'intersection'){
        is_deeply( [ sort( FirehoseFileMetadata::intersection_of_case_lists( @FFMinstances ) )  ],
            [ sort( map {convertCaseID( $_ )} @{$expectedCases} ) ], $testName );
    }
}
