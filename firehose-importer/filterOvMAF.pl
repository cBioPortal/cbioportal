#!/usr/bin/perl
use strict;
use warnings;
use File::Util;
use Data::Dumper;
use Data::CTable;
use Set::Scalar;

# stand alone program to filter one of the OV maf files

# args: /Users/goldbera/Documents/fullCGDS/sander/cgds/data/ovarian/3-center_OV.Exome_DNASeq.1.Somatic_and_Germline_WU-Annotation.05jan2011a.maf /Users/goldbera/Documents/fullCGDS/sander/cgds/data/ovarian/OvarianWhiteList.txt /Users/goldbera/Documents/fullCGDS/sander/cgds/data/ovarian/3-center_OV.Exome_DNASeq.1.Somatic_and_Germline_WU-Annotation.05jan2011a.filtered.maf 

my $fn = $ARGV[0];
my $whitelist = $ARGV[1];
my $outFile = $ARGV[2];

# read file
my $cTable = Data::CTable->new( { _CacheOnRead   => 0 }, $fn );

my $recs = $cTable->select_all( );
print scalar( @{$cTable->selection()}),  " records\n" ;
# $cTable->out();
print "keep records that have Mutation_Status in { Germline, Somatic } (should reduce count by 26 + 3 = 29)\n";
$cTable->select( Mutation_Status => sub{ /Germline/i || /Somatic/i } );
print scalar( @{$cTable->selection()}),  " records\n" ;

# read whitelist
my @whitelist = File::Util->new()->load_file( $whitelist, '--as-lines' );
my $whitelistSet = Set::Scalar->new(@whitelist);

# $cTable->out();
print "keep records that have Validation_Status = 'Valid' or are on highly mutated whitelist\n";
my $Validation_Status = $cTable->col( 'Validation_Status' );
my $Hugo_Symbol = $cTable->col('Hugo_Symbol');
my $S = [grep 
         { $Validation_Status->[$_] =~ /Valid/i || $whitelistSet->has( $Hugo_Symbol->[$_] )} 
         @{ $cTable->selection() }];
$cTable->selection( $S );
print scalar( @{$cTable->selection()}),  " records\n" ;

# $cTable->out();
print "discard silent mutations\n";
$cTable->omit( Variant_Classification => sub{ m/Silent/i }  );
print scalar( @{$cTable->selection()}),  " records\n" ;

# $cTable->out();
print "discard germline mutations that are Missense_Mutations.\n";
$cTable->col('Mutation_Status_x_Variant_Classification');
$cTable->calc(sub{ $main::Mutation_Status_x_Variant_Classification = "$main::Mutation_Status:$main::Variant_Classification" });
$cTable->omit( Mutation_Status_x_Variant_Classification => sub{ my( $MS, $VC ) = split( /:/, $_); $MS =~ m/Germline/i && $VC =~ m/Missense_Mutation/i }  );
print scalar( @{$cTable->selection()}),  " records\n" ;
$cTable->col_delete('Mutation_Status_x_Variant_Classification');

# $cTable->out();
print "discard germline mutations that are not mutations of BRCA1 or BRCA2.\n";
my $germlineOvaMutations = Set::Scalar->new( qw( BRCA1 BRCA2 ) );
$cTable->col('Symbol_Status');
$cTable->calc(sub{ $main::Symbol_Status = "$main::Hugo_Symbol:$main::Mutation_Status" });
$cTable->omit( Symbol_Status => sub{ my( $HS, $MS ) = split( /:/, $_); $MS =~ m/Germline/i && ! $germlineOvaMutations->has( $HS ) }  );
$cTable->col_delete( 'Symbol_Status' );
print scalar( @{$cTable->selection()}),  " records\n" ;
# $cTable->out();

# output MAF file
my $stringRef = $cTable->format(    ## Named-parameter convention
             _FieldList     => [ qw( Hugo_Symbol Mutation_Status Validation_Status Variant_Classification Result ) ], ## Fields to write; others ignored
             _MaxRecords    => 5,   ## Limit on how many records to write
             );

$cTable->write( $outFile );