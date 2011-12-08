package CustomizeFirehoseData;

# customize firehose data
# some subs used by customizeFirehoseData.pl
# author: Arthur Goldberg, goldberg@cbio.mskcc.org

# SUMMARY
# Pre-processing pass (CustomizeFirehoseData.pm)
# Support custom additions to Firehose data prior to convertFirehoseData.pl
# One function: Given a maf file, create a simplified significantly mutated genes file
# Issues: Not fully general purpose for multiple custom additions
# Testing: Full coverage

require Exporter;
@ISA = qw(Exporter);
@EXPORT = qw( create_a_missing_sig_genes_file ); 

use strict;
use warnings;
use File::Spec;
use File::Util;
use Data::Dumper;
use Data::CTable;

use Utilities;

# given a maf file and the directory containing it, create a simplified sig_genes.txt file, containing only the columns gene and n
# keep only genes mutated in more than $nThreshold cases

# params:
# $cancer                   name of the cancer type
# $cancerRootDir            full pathname of dir containing the mutation assessor directory
# $Mutation_Assessor_Dir    name of the mutation assessor directory
# $nThreshold               threshold number of cases for inclusion in the sig_genes.txt file

# returns pathname to the sig_genes.txt file created
sub create_a_missing_sig_genes_file{
    my( $cancer, $cancerRootDir, $Mutation_Assessor_Dir, $nThreshold ) = @_;

    # full filename for maf file
    my $cancer_UC = uc( $cancer );
    my $maf_file_pathname = File::Spec->catfile( $cancerRootDir, $Mutation_Assessor_Dir, $cancer_UC . ".maf.annotated" );
    
    # name for Mutation_Significance directory
    my $Mutation_Significance_Dir = $Mutation_Assessor_Dir;
    $Mutation_Significance_Dir =~ s/Mutation_Assessor/Mutation_Significance/;
    # create Mutation_Significance_Dir
    my $new_dir = File::Spec->catfile( $cancerRootDir, $Mutation_Significance_Dir );
    unless( -d $new_dir ){
        $new_dir = File::Util->new->make_dir( $new_dir );
    }
    unless( defined( $new_dir ) ){
    	warn "create Mutation_Significance_Dir fails\n";
    	return;
    }
    
    # full filename for sig_genes.txt file
    my $sig_genes_file_pathname = File::Spec->catfile( $cancerRootDir, $Mutation_Significance_Dir, $cancer_UC . ".sig_genes.txt" );
    
    # read maf file, and
    # count occurrences of mutations
    my $mutation_occurrences = {};  # $mutation_occurrences->{gene}->{case} == 1 indicates that 'gene' is mutated in 'case'
    
    my $cTable = Data::CTable->new( { _CacheOnRead   => 0 }, $maf_file_pathname );
    unless( defined( $new_dir ) ){
        warn "create Mutation_Significance_Dir fails\n";
        return;
    }
    $cTable->calc( sub{
    	$mutation_occurrences->{$main::Hugo_Symbol}->{$main::Tumor_Sample_Barcode} = 1;
    });

    my $count_mutation_occurrences = {};  # $count_mutation_occurrences->{gene} indicates the number of cases in which 'gene' is mutated
    foreach my $gene ( keys %{$mutation_occurrences} ){
    	my $n = scalar( keys %{$mutation_occurrences->{$gene}} );
    	if( $nThreshold < $n ){
    		$count_mutation_occurrences->{$gene} = $n;
    	} 
    }

    # write sig_genes.txt file, sorted by occurrence
    open(my $fh, '>', $sig_genes_file_pathname ) or die $!;
    print $fh "gene\tn\n";
    foreach my $gene ( reverse (sort {$count_mutation_occurrences->{$a} <=> $count_mutation_occurrences->{$b} } 
        keys %{$count_mutation_occurrences} ) ){
        	print $fh "$gene\t$count_mutation_occurrences->{$gene}\n";
    }
    close( $fh );
    return $sig_genes_file_pathname;
    
}
1;