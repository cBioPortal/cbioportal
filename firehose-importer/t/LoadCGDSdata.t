#!/usr/bin/perl
# author: Arthur Goldberg, goldberg@cbio.mskcc.org

use strict;
use warnings;
use Test::More  skip_all => 'not implemented';
use File::Spec;
use File::Util;
use Data::Dumper;
use Data::CTable;

use LoadCGDSdata;

# Verify module can be included via "use" pragma
BEGIN { use_ok('LoadCGDSdata') }

# Verify module can be included via "require" pragma
require_ok('LoadCGDSdata');

# todo: make general purpose
# load a data_mutations_extended.txt;
my @args = qw( 
		/Users/goldbera/Documents/workspace/cgds  
		/Users/goldbera/Data/firehose/data/CGDSinput/cancers
		/Users/goldbera/Data/firehose/data/CGDSinput/cancers/human_gene_info_2011_01_19  
		/Users/goldbera/Data/firehose/data/CGDSinput/cancers/microRNAs.txt
		germlineWhiteList.txt 
		 significantlyMutatedSomaticGenesWhitelist.txt
	);

# LoadCGDSdata->run( @args );

push @args, 'somaticWhiteList /Users/goldbera/Data/firehose/data/CGDSinput/cancers/universalSomaticGeneWhitelist.txt';


push @args, " --acceptRemainingMutations ";

# LoadCGDSdata->run( @args );

exit;
# get size of files
# all files
my @cgdsFiles = File::Util->new->list_dir( '/Users/goldbera/Data/firehose/data/CGDSinput/cancers', '--recurse', '--files-only', '--no-fsdots' );
my $truncSize = 200;
# size of each
print "Truncating all to $truncSize\n";
foreach my $f (@cgdsFiles){
	my(@contents) = File::Util->new->load_file( $f,'--as-lines');
	my $s = scalar(@contents);
	
    print $s, ": $f\n";
    if( $truncSize < $s ){
	    system( 'head', " -n $truncSize > /tmp/data" );
	    system( 'cp', " /tmp/data $f" );
    }
	
}

