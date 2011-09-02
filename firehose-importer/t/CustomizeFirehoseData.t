#!/usr/bin/perl
# author: Arthur Goldberg, goldberg@cbio.mskcc.org

use strict;

use warnings;
use Test::More tests => 3;
use File::Util;
use Data::Dumper;

# Verify module can be included via "use" and "require" pragmas
BEGIN { use_ok('CustomizeFirehoseData') }
require_ok('CustomizeFirehoseData');

my $cancer = 'ov';
my $cancerRootDir = File::Spec->catfile( File::Spec->curdir(), qw( data test_create_a_missing_sig_genes_file ) );
my $Mutation_Assessor_Dir = 'gdac.broadinstitute.org_OV.Mutation_Assessor.Level_4.2011032701.0.0';

# remove file and dir being created
my $Mutation_Significance_Dir = $Mutation_Assessor_Dir;
$Mutation_Significance_Dir =~ s/Mutation_Assessor/Mutation_Significance/;
my $cancer_UC = uc( $cancer );
# full filename for sig_genes.txt file
my $sig_genes_file_pathname = File::Spec->catfile( $cancerRootDir, $Mutation_Significance_Dir, $cancer_UC . ".sig_genes.txt" );
unlink( $sig_genes_file_pathname );
my $dir = File::Spec->catfile( $cancerRootDir, $Mutation_Significance_Dir );
# todo: make this rmdir work?
# rmdir( File::Spec->catfile( $cancerRootDir, $Mutation_Significance_Dir ) ) || die "Could not remove $dir.";

create_a_missing_sig_genes_file( $cancer, $cancerRootDir, $Mutation_Assessor_Dir, 3 );

my $testName = "CustomizeFirehoseData::create_a_missing_sig_genes_file( $cancer, $cancerRootDir, $Mutation_Assessor_Dir, 3 );";
# say ok iff diff runs ok
my $correctOutputFile = File::Spec->catfile( File::Spec->curdir(), qw( data test_create_a_missing_sig_genes_file ), 
    'correct_' . $cancer_UC . ".sig_genes.txt" );
my $cmd      = "diff $sig_genes_file_pathname $correctOutputFile;";
if ( 0 == system($cmd) ) {
    my $diffSays = `diff $sig_genes_file_pathname $correctOutputFile;`;
    is( $diffSays, '', $testName );
} else {
    fail($testName);
}

    