#!/usr/bin/perl

# author: Arthur Goldberg, goldberg@cbio.mskcc.org

# stand alone program
# examines a set of maf files 
# checks that they have 
# 1) the right columns
# 2) the right data in each column (as determined by RE pattern matching)
# reports on statistics for the above
#
# fields and patterns defined in a hash

use strict;
use warnings;
use File::Spec;
use File::Util;
use Data::Dumper;
use Getopt::Long;
use Data::CTable;

my $usage = <<EOT;
usage:

Check on a list of maf files. Arguments:
--mafs              # list of maf files
--fields            # [optional] comma-separated list of fields to examine
--mismatches        # [optional] if set, show mismatches
EOT

# check each file
# report # of rows
# for each needed header
# check header is present, rows are non-empty, and rows have data of right format

# list of fields and patterns they must meet; some fields
# are alternatives, one of the set must be present
# structure:
# field_concept => {
#   one_possible_field => 'legal pattern for that field',
#   another_possible_field => 'legal pattern for it',	
# }
my $neededFields = {
	gene_id => {
		Entrez_Gene_Id => '\d+'
	},
    gene_symbol => {
        Hugo_Symbol => '\w+'
    },
    sequencing_center => {
        Center => '\w+'
    },
    # todo: need patterns for these:	
	sample_identifier => {
		Tumor_Sample_Barcode => '' # could put CaseID function here
	},
    verification_status => {
        Verification_Status => '' 
    },
    validation_status => {
        Validation_Status => 'Valid' 
    },
    mutation_status => {
        Mutation_Status => 'Somatic|Germline' 
    },
	amino_acid_change => {
		'MA_MSA' => 'var=.+$|^--$|^NA$',
		'MA:link.MSA' => 'var=.+$|^--$',
		Protein_Change => '^p\.(.+)$|^(\w+)$|(\d\w+(\d|\*))$|^(e\d+.\d+)$|^NULL$',
	},
	ma_functional_impact => {
        'MA_Func.Impact' => '^high$|^medium$|^low$|^neutral$|^H$|^M$|^L$|^N$|^NA$|^--$',
        'MA:FImpact' => '^high$|^medium$|^low$|^neutral$',		
	},
    ma_alignment_link => {
        MA_MSA => '^((http://|)mutationassessor.org/.*var=\w+$)|^--$|^NA$',
        'MA:link.MSA' => '^(http://|)mutationassessor.org/.*var=\w+$',       
    },
    ma_protein_structure_link => {
        MA_PDB => '^((http://|)mutationassessor.org/pdb.php.*var=\w+$)|^--$|^NA$',
        'MA:link.PDB' => '^(http://|)mutationassessor.org/pdb.php.*var=\w+$',
    },
    genomic_change => {
    	'MA:variant' => '^(X|^\d+),\d+,[ACGT],[ACGT]$|^(X|^\d+),\d+$' 
    }
	
};

my @fieldConcepts = qw( gene_id  gene_symbol  sequencing_center  sample_identifier  
    verification_status  validation_status  mutation_status  amino_acid_change ma_functional_impact 
    ma_alignment ma_protein_structure );

 #        Sequencer Chromosome Start_position End_position Variant_Classification

my( @mafs, $fields, $mismatches ); 
main();
sub main{
    # process arg list
    GetOptions (
        "mafs=s{1,}" => \@mafs,
        "fields=s" => \$fields,
        "mismatches" => \$mismatches
    );
    unless(  @mafs ){
    	die "$usage\n";
    }
    my @fields = @fieldConcepts;
    if( defined( $fields ) ){
    	@fields = split( /,/, $fields );
    }
    
    dumpFields( $neededFields, @fields );
    
    # my $t = Data::CTable->new(); $t->progress_class(0);
    # Data::CTable->progress_class(0);
    listFields( $neededFields, \@fields );
    my $columnNumbers;
    for my $maf (@mafs){
    	print "\nExamining '$maf':\n";
    	my $fieldMap = getColumnNumbers( $maf );
    	# open file
    	my $mafData = Data::CTable->new( { _CacheOnRead   => 0, _Progress => 0 } );
        $mafData->progress_set(0);
        $mafData->read( $maf );
    	
    	checkHeadersAndData( $mafData, $neededFields, \@fields, $mismatches, $fieldMap );
    	print $mafData->sel_len() . " rows.\n";
        
    }
}

sub checkHeadersAndData{
    my( $mafData, $neededFields, $fields, $mismatches, $fieldMap ) = @_;
    
    my $format = "%20s: %-50s\n";
    printf STDOUT $format, "header concept", 'actual column(col#):mismatched rows/non-null rows/\'first bad one\'';

    # for each header concept
    for my $headerConcept (@{$fields}){
        
        # which alternative headers are present?
        my @actualHeaders;
        for my $alternativeHeader (keys %{$neededFields->{$headerConcept}}){
        	if( $mafData->col_exists( $alternativeHeader ) ){
        		my $columnInfo = "$alternativeHeader(" . $fieldMap->{$alternativeHeader} . '):' ;
        		my @allMismatches = ();

                # how many rows satisfy the pattern?
                my $defined = 0;
                my $notMatching = 0;
                my $pattern = $neededFields->{$headerConcept}->{$alternativeHeader};
                if( defined( $pattern ) and $pattern ne '' ){
                	my $firstBadOne = undef;
	                $mafData->calc( sub{
	                        package main;
	                        no strict 'vars';
	                        no strict 'refs';
	                        if( defined(${$alternativeHeader}) ){
	                        	$defined++;
	                        	unless( ${$alternativeHeader} =~ $pattern ){
	                        		$notMatching++;
	                        		unless( defined( $firstBadOne ) ){
	                        			$firstBadOne = ${$alternativeHeader}; 
	                        		}
	                        		if( defined( $mismatches ) ){
	                        			push @allMismatches, ${$alternativeHeader};
	                        		}
	                        	}
	                        }
	                }, undef, [ $alternativeHeader ] );
                    $columnInfo .= "$notMatching/$defined/";
                    if( defined( $firstBadOne )){
                        $columnInfo .= "'$firstBadOne'";
                    }else{
                        $columnInfo .= "--";
                    }
                }
                push @actualHeaders, $columnInfo;
		        if( defined( $mismatches ) ){
		            print "$alternativeHeader mismatches: ", join( ', ', @allMismatches ), "\n";
		        }
        	}
        }
        
        printf STDOUT $format, $headerConcept, join( ', ', @actualHeaders );
    }
}

sub listFields{
    my( $neededFields, $fields ) = @_;
    print "Headers and data patterns\n";
    for my $headerConcept (@{$fields}){
        
        my @actualHeaders;
        for my $alternativeHeader (keys %{$neededFields->{$headerConcept}}){
            my $pattern = $neededFields->{$headerConcept}->{$alternativeHeader};
            if( defined( $pattern ) and $pattern ne '' ){
                push @actualHeaders, "$alternativeHeader:'$pattern'";
            }else{
                push @actualHeaders, "$alternativeHeader";
            }
        }
        printf STDOUT "%20s: %-30s\n", $headerConcept, join( ', ', @actualHeaders );
    }
    print "\n";
}

sub dumpFields{
    my( $neededFields, @fields ) = @_;
    for my $headerConcept (@fields){
        for my $alternativeHeader (keys %{$neededFields->{$headerConcept}}){
        	print "$headerConcept\t$alternativeHeader\t" . $neededFields->{$headerConcept}->{$alternativeHeader} . "\n";
        }
    }
}

# returns hash from field name to column number
sub getColumnNumbers{
	my( $fn ) = @_;

	# open file
	my $f = File::Util->new();
	my $FH = $f->open_handle('file' => $fn, 'mode' => 'read' );

	# parse first line
	my @fields = split( /\t/, <$FH> );
    close $FH;
    $f->unlock_open_handle( $FH );
    
    my $fieldColumns = {};
    my $i = 1;
    for my $f (@fields){
    	chomp $f;
    	$fieldColumns->{$f} = $i++;
    }

    return $fieldColumns;
}