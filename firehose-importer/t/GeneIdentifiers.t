#!/usr/bin/perl
# author: Arthur Goldberg, goldberg@cbio.mskcc.org

use strict;
use warnings;
use File::Spec;
use File::Util;
use Data::Dumper;
use Test::More tests => 39;

use Utilities;

# human_gene_info_test is a truncated AND MODIFIED copy of 
# ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/gene_info.gz filtered on human genes, tax_id == 9606

my $expected_error = <<"EOT";
Not using gene symbol A1BG because it maps to duplicate gene IDs: 1 and 29.
More than 2 distinct mappings for A1BG.
Ignoring 1 duplicate Gene Symbol(s) in data/human_gene_info_test.
EOT

my $fileUtil = File::Util->new();

my $correct_GeneMap = File::Spec->catfile( File::Spec->curdir(), qw( data correct_GeneMap.txt ) );
my $GeneMap = checkError( 'GeneIdentifiers', 'new', $expected_error, eval( $fileUtil->load_file( $correct_GeneMap ) ), 'load GeneIdentifiers->new',  
    File::Spec->catfile( File::Spec->curdir(), qw( data human_gene_info_test ) ) );

$GeneMap->loadGeneList( File::Spec->catfile( File::Spec->curdir(), qw( data gene_list_test ) ), 'testGeneList' );

is( $GeneMap->hasGeneList( 'noSuchList' ), undef, 'test hasGeneList' );
is( $GeneMap->hasGeneList( 'testGeneList' ), 1, 'test hasGeneList' );
is( $GeneMap->hasGeneID( ), undef, 'NO args' );
is( $GeneMap->hasGeneID( 'NO_SUCH_testGeneList', 'not_a_gene'), undef, 'NO_SUCH_testGeneList' );
is( $GeneMap->hasGeneID( 'testGeneList', 'not_a_gene'), undef, 'not_a_gene' );
is( $GeneMap->hasGeneID( 'testGeneList', 9 ), 1, '9 NAT1' );
is( $GeneMap->hasGeneID( 'testGeneList', undef ), undef, 'no id or name' );
is( $GeneMap->hasGeneName( 'testGeneList', 'NAT1' ), 1, 'good name' );
is( $GeneMap->hasGeneName( 'testGeneList', 'noSuchGene' ), undef, 'bad name' );

checkError( $GeneMap, 'loadGeneList',  "Utilities::GeneIdentifiers::loadGeneList: Cannot read gene file: 'data/NO_SUCH_gene_list_test'.\n", 
    undef, 'testGeneList', 
    File::Spec->catfile( File::Spec->curdir(), qw( data NO_SUCH_gene_list_test ) ) );

# see: http://stackoverflow.com/questions/533553/perl-build-unit-testing-code-coverage-a-complete-working-example, 26 jan 2011
# Verify module can be included via "use" pragma
BEGIN { use_ok('GeneIdentifiers') }

# Verify module can be included via "require" pragma
require_ok('GeneIdentifiers');

checkError( 'GeneIdentifiers', 'new', "GeneIdentifiers::new() takes a filename at...", undef, 'GeneIdentifiers::new() missing a filename' );

checkError( 'GeneIdentifiers', 'new', 
    "Cannot read gene file: 'no_such_file' at...", undef, 'GeneIdentifiers::new() non-existant file', 'no_such_file' );

# test gene symbol
is( $GeneMap->getGeneID('NAT2'), 10, 'test gene symbol 1' );
is( $GeneMap->getGeneID('NAT2', undef), 10, 'test gene symbol 2' );

# test no mapping
is( $GeneMap->getGeneID('noSuchSymbol'), undef, 'test no mapping 1' );
is( $GeneMap->getGeneID('noSuchSymbol', undef), undef, 'test no mapping 2' );

# test gene synonym
is( $GeneMap->getGeneID('CPAMD5'), 2, 'test gene synonym 1' );
is( $GeneMap->getGeneID('CPAMD5', undef), 2, 'test gene synonym 2' );

# test duplicated gene synonym
is( $GeneMap->getGeneID('ABG'), undef, 'test duplicated gene synonym 1' );
is( $GeneMap->getGeneID('ABG', undef), undef, 'test duplicated gene synonym 2' );

# test symbol and positive ID 
is( $GeneMap->getGeneID('ABG', 3), 3, 'test symbol and positive ID' );

# test symbol and negative ID; get the symbol, for mirs
is( $GeneMap->getGeneID('mirX', -4), 'mirX', 'test symbol and negative ID; get the symbol, for mirs' );

# test just ID
is( $GeneMap->getGeneID( undef, -4), -4, 'just ID 1' );

# test just ID
is( $GeneMap->getGeneID( undef, 14), 14, 'just ID 2' );

# try recovering from bad GeneIDs
my $i = 1;
is( $GeneMap->getGeneID( "ABL2", 0), 27, 'try  recovering from bad GeneIDs ' . $i++ );
is( $GeneMap->getGeneID( "ABL2", undef), 27, 'try  recovering from bad GeneIDs ' . $i++ );
is( $GeneMap->getGeneID( "noSuchGene", 0), undef, 'try  recovering from bad GeneIDs ' . $i++ );
is( $GeneMap->getGeneID( undef, 123456789999 ), undef, 'try  recovering from bad GeneIDs ' . $i++ );

# no mappings of '-'
is( $GeneMap->getGeneID( "-", 0), undef, "no mappings of '-'" );

# dont replace existing mappings
is( $GeneMap->getGeneID( "SERPINA3" ), 12, 'dont replace existing mappings 1' );
is( $GeneMap->getGeneID( "GIG24" ), 32, 'dont replace existing mappings 2' );

# only add unique LocusTag 
is( $GeneMap->getGeneID( "DUP_TAG" ), undef, 'only add unique LocusTag' );
