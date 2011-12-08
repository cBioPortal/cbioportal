#!/usr/bin/perl
use strict;
use warnings;
use File::Spec;
use File::Util;
use Data::Dumper;
use Set::Scalar;
# diffs of case_list_ids
# args: case_list1 file   case_list2 file   
# output: case_list1 - case_list2

# read files
# get case lists
sub getCaseSet{
	my $fn = shift;
	my $fileUtil = File::Util->new();
	my @props = $fileUtil->load_file( $fn, '--as-lines');
	my @cases;
	foreach my $p (@props){
		if( $p =~ /case_list_ids/){
        print Dumper( $p );
			my( $p, $v ) = split( /:/, $p );
			@cases = split( /\s+/, $v );
        print Dumper( @cases );
		}
	}
	return Set::Scalar->new( @cases );
}

# take difference
print "Difference: $ARGV[0] - $ARGV[1]\n";
print getCaseSet($ARGV[0])->difference( getCaseSet($ARGV[1]) );
