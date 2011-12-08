#!/usr/bin/perl
# author: Arthur Goldberg, goldberg@cbio.mskcc.org

use strict;
use warnings;
use Test::More tests => 54;
use File::Spec;
use File::Spec;
use Data::Dumper;
use Data::CTable;

use CaseIDs;
use Utilities;

# Verify module can be included via "use" pragma
BEGIN { use_ok('CaseIDs') }

# Verify module can be included via "require" pragma
require_ok('CaseIDs');

# each subroutine should recognize the corresponding case IDs, and reject the rest
my $exampleCaseIDs = {
	tumorCaseID =>             [ qw( TCGA-04-1332-01A-01D-0428-01 TCGA-A1-A0SE-01A-11R-A084-07 TCGA-04-1331-01A-01D-0428-01 GBM-02-2466-Tumor TCGA-13-1481-01 ) ],   # good tumorCaseIDs
    matchedNormalCaseID  =>    [ qw( TCGA-04-1331-10 GBM-02-2466-Normal ) ],                          # good normal blood case-IDs
    normalTissueCaseID  =>     [ qw( TCGA-04-1331-11 ) ],                                          # good normal tissue case-ID
    recurrentTumorCaseID =>    [ qw( TCGA-04-1338-02 TCGA-04-1338-01R ) ],                         # good recurrent Tumor case IDs
};

# for each list of case IDs, try to get a positive recognition with the corresponding subroutine, and a negative one 
# with each other subroutine
foreach my $caseIDrecognitionSub (keys %{$exampleCaseIDs}){
	
	# positive recognition 
	foreach my $caseID (@{$exampleCaseIDs->{$caseIDrecognitionSub}}){
		my $e = "$caseIDrecognitionSub( '$caseID' );";
		ok( eval $e, "$caseIDrecognitionSub should accept $caseID");
	}
	
	# negative recogntion
	foreach my $otherSub (keys %{$exampleCaseIDs}){
		if( $otherSub ne $caseIDrecognitionSub){
		    foreach my $caseID (@{$exampleCaseIDs->{$otherSub}}){
		        my $e = "!$caseIDrecognitionSub( '$caseID' );";
		        ok( eval $e, "$caseIDrecognitionSub should not accept $caseID");
		    }
		}
	}
} 

# is( convertCaseID( 'TCGA-04-1331-10'), 'TCGA-04-1331-10',  'normal std' );

test_convert_case_ID_headers( 'test_all_thresholded.by_genes.txt', 
    [qw( TCGA-02-0001-01C-01D-0182-01   TCGA-02-0002-01A-01D-0182-01    TCGA-02-0003-01A-01D-0182-01 ) ],
    [qw( TCGA-02-0001 TCGA-02-0002 TCGA-02-0003 ) ] );

# a hack: use ideas from Utilities::checkError to save and check error report
# todo: get this to work using Utilities::checkError
 
# First, save away STDERR
my $stderrOutput;
no warnings; # repress warning: 'Name "main::SAVEERR" used only once: possible typo at ... '
open SAVEERR, ">&STDERR"; 
use warnings;
close STDERR;
open STDERR, ">", \$stderrOutput or die "What the heck?\n";

# call sub, with output redirected to $stderrOutput
test_convert_case_ID_headers( 'testPR_GDAC_GBM.withdupes.medianexp.txt', 
    [qw( TCGA-02-0001-01C-01R   TCGA-02-0002-01A-01R    TCGA-02-0001-01A-01R ) ],
    [qw( TCGA-02-0001 TCGA-02-0002 ) ] );

# Close and restore STDERR to original condition.
close STDERR;
open STDERR, ">&SAVEERR";

my $expected_error = "Deleting column with duplicate case TCGA-02-0001-01A-01R in testPR_GDAC_GBM.withdupes.medianexp.txt.\n";

# test that error was produced
is( $stderrOutput, $expected_error );   

sub test_convert_case_ID_headers{
	my( $file, $startingFieldlist, $finishingFieldlist) = @_;
	my $fn = File::Spec->catfile( File::Spec->curdir(), 'data', $file );
	my $cTable = Data::CTable->new( { _CacheOnRead   => 0 }, $fn ); 
	convert_case_ID_headers( $fn, $cTable );
	foreach my $f (@{$startingFieldlist}){
	    ok( !defined( $cTable->{ $f }), "$f removed");
	}
	foreach my $f (@{$finishingFieldlist}){
	    ok( defined( $cTable->{ $f }), "renamed to $f");
	}
}
