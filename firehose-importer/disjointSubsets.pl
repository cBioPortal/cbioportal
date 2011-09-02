#!/usr/bin/perl
use strict;
use warnings;
use File::Spec;
use File::Util;
use Data::Dumper;
use Set::Scalar;
# diffs of case_list_ids
# args: dir, set of case_list files
# e.g.: /Users/goldbera/Documents/workspace/cgds/data/cancers/ov/case_lists cases_CGH.txt cases_mRNA.txt cases_sequenced.txt
# output: for each subset of files, cases (elements) in those files and no others

# read files
# get case lists
sub getCaseNameAndSet{
    my $fn = shift;
    my $fileUtil = File::Util->new();
    my @props = $fileUtil->load_file( $fn, '--as-lines');
    my @cases;
    my $name;
    foreach my $l (@props){
    	my( $p, $v ) = split( /\s*:\s*/, $l );
        if( $p =~ /case_list_ids/){
            @cases = split( /\s+/, $v );
        }
        if( $p =~ /case_list_name/){
        	$name = $v;
        }
    }
    print "$name contains: ", join( ',', @cases), "\n";
    return ($name, Set::Scalar->new( @cases ) );
}

# get all name,case_sets
my %nameToSet;
my $dir = shift @ARGV;
foreach my $fn (@ARGV){
	my( $name, $set ) = getCaseNameAndSet( File::Spec->catfile( $dir, $fn ) );
	$nameToSet{$name}=$set;
}

# get union of all case sets
my @cases;
my @names = keys %nameToSet;
my $u = $nameToSet{shift @names};
foreach my $n (@names){
	$u = $u->union( $nameToSet{$n} );
}
# print "union: ", $u, "\n";

# for each case, identify its case_sets (names), and add to hash from names => cases
# complexity is just O( |cases| |sets| )
my %nameSets;
foreach my $c ($u->members){
	my @sets = ();
	foreach my $s (keys %nameToSet){
		if( $nameToSet{$s}->has($c)){
			push @sets, "'$s'";
		}
	}
	my $intersectionSetName = join( " and ", sort @sets); 
	# print "$c is in $intersectionSetName\n";
	if( exists( $nameSets{ $intersectionSetName }) ){
		$nameSets{ $intersectionSetName }->insert( $c );
	}else{
		$nameSets{ $intersectionSetName } = Set::Scalar->new( $c );
	}
}

# dump sets
foreach my $s (keys %nameSets){
	print "$s contains: ", $nameSets{$s}, "\n";
}
