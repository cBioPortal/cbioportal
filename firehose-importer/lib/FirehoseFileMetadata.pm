package FirehoseFileMetadata;
require Exporter;
@ISA = qw(Exporter);
@EXPORT = qw( cases simplifiedCases genes numCases numGenes 
    intersection_of_case_lists union_of_case_lists getFilename ); 

use strict;
use warnings;

use Set::Scalar;
use File::Util;
use Data::Dumper;
use Data::CTable;

use Utilities;
use CaseIDs;

# given a Firehose File, represented by its complete pathname, 
# read the file and cache its metadata, such as case list, gene list, etc.
# handle structures of different files

# basic properties of firehose file types, indexed by the filename type from the FirehoseTransformationWorkflow config file
# in this hash, columns and rows count from 1
# a 'list' file is a file of records, with fields containing gene symbol, gene ID, case ID, etc. 
# a 'profile' file is a matrix of V[gene][case], with some header rows and header columns
my $fileProperties = {
    '<CANCER>.transcriptome__agilentg4502a_07_3__unc_edu__Level_3__unc_lowess_normalization_gene_level__data.data.txt' => {
        example => 'BRCA.transcriptome__agilentg4502a_07_3__unc_edu__Level_3__unc_lowess_normalization_gene_level__data.data.txt',
        structure => 'profile',
        numHeaderRows => 2,
        geneIDcol => undef,
        geneSymbolCol => 'Hybridization REF',
    },
    # the following type is used when running generate case lists on already created staging files
	'data_mRNA_ULL.txt' => {
        example => 'data_mRNA_ULL.txt',
        structure => 'profile',
        numHeaderRows => 1,
        geneIDcol => 'Entrez_Gene_Id',
        geneSymbolCol => undef,
    },
    # the following type is used when running generate case lists on already created staging files
	'data_mRNA_MicMa.txt' => {
        example => 'data_mRNA_MicMa.txt',
        structure => 'profile',
        numHeaderRows => 1,
        geneIDcol => 'Entrez_Gene_Id',
        geneSymbolCol => undef,
    },
    # the following type is used when running generate case lists on already created staging files
	'data_mRNA_FW_MDG.txt' => {
        example => 'data_mRNA_FW_MDG.txt',
        structure => 'profile',
        numHeaderRows => 1,
        geneIDcol => 'Entrez_Gene_Id',
        geneSymbolCol => undef,
    },
    # the following type is used when running generate case lists on already created staging files
	'data_mRNA_DBCG.txt' => {
        example => 'data_mRNA_DBCG.txt',
        structure => 'profile',
        numHeaderRows => 1,
        geneIDcol => 'Entrez_Gene_Id',
        geneSymbolCol => undef,
    },
    # the following type is used when running generate case lists on already created staging files
	'data_mRNA_DBCG_Z.txt' => {
        example => 'data_mRNA_DBCG_Z.txt',
        structure => 'profile',
        numHeaderRows => 1,
        geneIDcol => 'Entrez_Gene_Id',
        geneSymbolCol => undef,
    },
    # the following type is used when running generate case lists on already created staging files
	'data_expression_median.txt' => {
        example => 'data_expression_median.txt',
        structure => 'profile',
        numHeaderRows => 1,
        geneIDcol => 'Gene_ID',
        geneSymbolCol => undef,
    },
    # the following type is used when running generate case lists on already created staging files
	'data_expression_miRNA.txt' => {
        example => 'data_expression_miRNA.txt',
        structure => 'profile',
        numHeaderRows => 1,
        geneIDcol => 'miRNA',
        geneSymbolCol => undef,
    },
    # the following type is used when running generate case lists on already created staging files
	'data_miRNA_median_Zscores.txt' => {
        example => 'data_miRNA_median_Zcores.txt',
        structure => 'profile',
        numHeaderRows => 1,
        geneIDcol => undef,
        geneSymbolCol => 'geneSymbol',
    },
    # the following type is used when running generate case lists on already created staging files
	'data_expression_merged_median_Zscores.txt' => {
        example => 'data_expression_merged_median_Zscores.txt',
        structure => 'profile',
        numHeaderRows => 1,
        geneIDcol => undef,
        geneSymbolCol => 'geneSymbol',
    },
    # the following type is used when running generate case lists on already created staging files
	'data_expression_Zscores.txt' => {
        example => 'data_expression_Zscores.txt',
        structure => 'profile',
        numHeaderRows => 1,
        geneIDcol => undef,
        geneSymbolCol => 'geneSymbol',
    },
    '<CANCER>.rnaseq__illumina<RNA-SEQ-PLATFORM>_rnaseq__unc_edu__Level_3__gene_expression__data.data.txt' => {
        example => 'BRCA.rnaseq__illumina<RNA-SEQ-PLATFORM>_rnaseq__unc_edu__Level_3__gene_expression__data.data.txt',
        structure => 'profile',
        numHeaderRows => 2,
        geneIDcol => undef,
        geneSymbolCol => 'Hybridization REF',
    },
    # the following type is used when running generate case lists on already created staging files
	'data_RNA_Seq_expression_median.txt' => {
        example => 'data_RNA_Seq_expression_median.txt',
        structure => 'profile',
        numHeaderRows => 1,
        geneIDcol => 'Gene_ID',
        geneSymbolCol => undef,
    },
    'all_thresholded.by_genes.txt' => {
        example => 'all_thresholded.by_genes.txt',
        structure => 'profile',
        numHeaderRows => 1,
        geneIDcol => 'Locus ID',
        geneSymbolCol => 'Gene Symbol',
    },
    # the following type is used when running generate case lists on already created staging files
	'data_CNA.txt' => {
        example => 'data_CNA.txt',
        structure => 'profile',
        numHeaderRows => 1,
        geneIDcol => 'Locus ID',
        geneSymbolCol => undef,
    },
    'all_data_by_genes.txt' => {
        example => 'all_data_by_genes.txt',
        structure => 'profile',
        numHeaderRows => 1,
        geneIDcol => 'Locus ID',
        geneSymbolCol => 'Gene Symbol',
    },
    'table_amp.conf_99.txt' => {
        example => 'table_amp.conf_99.txt',
        structure => 'profile',
        numHeaderRows => 1,
        geneIDcol => undef,
        geneSymbolCol => undef,
    },
    'table_del.conf_99.txt' => {
        example => 'table_del.conf_99.txt',
        structure => 'profile',
        numHeaderRows => 1,
        geneIDcol => undef,
        geneSymbolCol => undef,
    },
    'amp_genes.conf_99.txt' => {
        example => 'map_genes.conf_99.txt',
        structure => 'profile',
        numHeaderRows => 1,
        geneIDcol => undef,
        geneSymbolCol => undef,
    },
    'del_genes.conf_99.txt' => {
        example => 'del_genes.conf_99.txt',
        structure => 'profile',
        numHeaderRows => 1,
        geneIDcol => undef,
        geneSymbolCol => undef,
    },
    # the following type is used when running generate case lists on already created staging files
	'data_log2CNA.txt' => {
        example => 'data_log2CNA.txt',
        structure => 'profile',
        numHeaderRows => 1,
        geneIDcol => 'Locus ID',
        geneSymbolCol => undef,
    },
    '<CANCER>.mirna__h_mirna_8x15k<version>__unc_edu__Level_3__unc_DWD_Batch_adjusted__data.data.txt' => {  
        example => 'OV.mirna__h_mirna_8x15kv2__unc_edu__Level_3__unc_DWD_Batch_adjusted__data.data.txt',
        structure => 'profile',
        numHeaderRows => 2,
        geneIDcol => undef,
        geneSymbolCol => 'Hybridization REF',
    },
    '<CANCER>.maf.annotated' => {
        example => 'OV.maf.annotated',
        structure => 'list',
        numHeaderRows => 1,
        geneIDcol => 'Entrez_Gene_Id',
        geneSymbolCol => 'Hugo_Symbol',
        caseIDcols => 'Tumor_Sample_Barcode'
    },
    # the following type is used when running generate case lists on already created staging files
    'data_mutations_extended.txt' => {
        example => 'data_mutations_extended.txt',
        structure => 'list',
        numHeaderRows => 1,
        geneIDcol => 'Entrez_Gene_Id',
        geneSymbolCol => 'Hugo_Symbol',
        caseIDcols => 'Tumor_Sample_Barcode'
    },
	'<CANCER>.methylation__humanmethylation27__jhu_usc_edu__Level_3__within_bioassay_data_set_function__data.data.txt' => {
	    structure => 'profile',
	    numHeaderRows => 1,
	},
    # the following type is used when running generate case lists on already created staging files
    'data_methylation.txt' => {
        example => 'data_methylation.txt',
        structure => 'profile',
        numHeaderRows => 1,
        geneIDcol => 'Entrez_Gene_Id',
        geneSymbolCol => 'Gene',
    },
	# known types, but cannot, or do not need to, get metadata:
    '<CANCER>.sig_genes.txt' => {
        structure => 'unstructured'
    },
    'data_mutsig.txt' => {
        structure => 'unstructured'
    },
    'Correlate_Methylation_vs_mRNA_<CANCER>_matrix.txt' => {
        structure => 'unstructured'
    },
    # the following type is used when running generate case lists on already created staging files
	'data_rppa.txt' => {
        example => 'data_rppa.txt',
        structure => 'profile',
        numHeaderRows => 1,
        geneIDcol => 'sample',
        geneSymbolCol => undef,
    },
};

sub new {
    unless( 3 <= scalar(@_) ){
        warn "FirehoseFileMetadata::new() takes: filename pattern from transformation table, " .
        "absolute pathname of a data file, [reference to open CTable]";
        return undef;
    }
    
    my( $class, $filenamePattern, $pathname, $existingcTable ) = @_;
	
	my $self  = {
        PATHNAME    => $pathname,
        FILENAME    => undef,
        TYPE        => undef,
        CASES       => undef,
        GENES       => undef,
	};
	
	# check that the file's readable
	unless( -r $self->{PATHNAME}){
		warn "FirehoseFileMetadata::new(): Cannot read " . $self->{PATHNAME};
		return undef;
	}
	
    # get filename
    my($volume,$directories,$file) = File::Spec->splitpath( $self->{PATHNAME} );
    $self->{FILENAME} = $file;
    
    # use full filename pattern from transformation file
    # get structure and properties
    unless( exists( $fileProperties->{$filenamePattern} ) ){
        warn "Error: @{[__PACKAGE__]}::new(): could not find type for file: " . $file;
        return undef;
    }

    my $properties;
	$self->{TYPE} = $filenamePattern;
	$properties = $fileProperties->{$filenamePattern};
    if( $properties->{structure} eq 'unstructured' ){
    	print "@{[__PACKAGE__]}::new(): $file is unstructured.\n";
    	return undef;
    }
    
    # for a big speedup, use an existing cTable if passed in
    my $cTable;
    if( defined( $existingcTable )){
    	# todo: unit test this case
        $cTable = $existingcTable;
    }else{
	    # read with CTable; but don't keep -- may be too big
	    $cTable = Data::CTable->new( { _CacheOnRead   => 0 }, $self->{PATHNAME} ); 
    }
            	
	# delete non-data rows
	my $numRowsToDelete = $properties->{numHeaderRows} - 1;
	if( scalar( @{$cTable->all()}) <= $numRowsToDelete ){
        warn "Not enough data rows to delete ", $properties->{numHeaderRows} - 1, " extra header row(s).";
        return;
	}else{
	    for( my $i = 0; $i < $numRowsToDelete; $i++ ){
	        $cTable->row_delete( 0 );
	    }
	}

	# get list of cases
	my $cases = [];
	# we want to add normal tissue case id's if we are processing expression or methylation files
	my $addNormalTissueCaseIDs = (($filenamePattern eq 'data_expression_median.txt') ||
								  ($filenamePattern eq 'data_methylation.txt'));
    # profile files:
    if( $properties->{structure} eq 'profile' ){

	    # cases: headers which are case-IDs 
	    foreach my $f ( @{$cTable->fieldlist()} ){
	        if( tumorCaseID( $f ) ){
                push @{$cases}, $f;
	        }
			if($addNormalTissueCaseIDs && normalTissueCaseID( $f ) ) {
			  push @{$cases}, $f;
			}
	    }
	    $self->{CASES} = $cases;
    }    
    
    # list files: 
    if( $properties->{structure} eq 'list' ){
	    
	    # cases: are in given column(s)
	    my @caseCols = split( /\|/, $properties->{caseIDcols} );
	    foreach my $caseCol (@caseCols){
            push @{$cases}, @{$cTable->col_get($caseCol)};
	    }

	    # dupes are expected; remove them
	    $self->{CASES} = [ removeDupes( @{$cases} ) ];
    }

	# gene IDs, if available: start after header rows; 
	# for now, just list of raw gene IDs, or symbols if IDs not available
	# todo: use GeneIdentifiers->getGeneID
	my $geneCol = $properties->{geneIDcol}; 
	if( !defined( $geneCol )){
	    $geneCol = $properties->{geneSymbolCol};
	}
	
	if( $filenamePattern eq 
	    '<CANCER>.methylation__humanmethylation27__jhu_usc_edu__Level_3__within_bioassay_data_set_function__data.data.txt' ){
        # special case, because in methylation__humanmethylation27_... gene column isn't headed by an identifier, and CTable won't column by an integer!; sigh!

        # read file
	    my $f = File::Util->new();
	    my $FH = $f->open_handle('file' => $self->{PATHNAME}, 'mode' => 'read' );

        # skip 2 rows
        my $skip = <$FH>; $skip = <$FH>;
        
        # get genes from 3rd column
        my $genes = [];
        while( my $line = <$FH>){
            my @fields = split( /\t/, $line );
            if( scalar(@fields) < 3 ){
            	warn "FirehoseFileMetadata: Not enough columns to get genes from " . $self->{PATHNAME} . "\n";
            	return undef;
            }else{
            	push @{$genes}, $fields[2];
            }
        }

        # eliminate duplicates
        $self->{GENES} = [ removeDupes( @{$genes} ) ];

        # close file        
	    close $FH;
	    $f->unlock_open_handle( $FH );
        		
	}else{
	    if( defined( $geneCol )){
	        $self->{GENES} = $cTable->col_get($geneCol);
	    }
	}	

	bless( $self, $class );
	return $self;
}

# return a ref to a list of the cases
sub cases{
    my $self = shift;
    return $self->{CASES};  
}

# the MSKCC portal presents simplified case-IDs (barcodes) created by convertCaseID()
# return a ref to a list of the unique simplified cases
sub simplifiedCases{
    my $self = shift;

	my @simplifiedCases = map {convertCaseID( $_ )} @{$self->{CASES}};

    # remove dupes
	my %cases;
	my @uniqueCases;
	foreach my $c (@simplifiedCases){
		if( exists( $cases{$c})){

            # warn about dupes in profile matrices, but not in lists or methyl files, which we expect to have dupes
            my $properties = $fileProperties->{ $self->{TYPE} };
            if( $properties->{structure} eq 'profile' and
                 $self->{TYPE} ne '<CANCER>.methylation__humanmethylation27__jhu_usc_edu__Level_3__within_bioassay_data_set_function__data.data.txt' ){
            	warn "case $c duplicated in ", $self->getFilename(), "\n";
            }
		}else{
            $cases{$c}=1;
            push @uniqueCases, $c;
		}
	}

	return \@uniqueCases;
}

# return a list of all genes in the data
sub genes{
    my $self = shift;
    return $self->{GENES};  
}

# return the number of unique simplified cases. 
# E.g., TCGA-02-0001-01C-01R and TCGA-02-0001-01A-01R count as 1 case
sub numCases{
    my $self = shift;
    my @simplifiedCases = map {convertCaseID( $_ )} @{$self->{CASES}};
    return scalar( removeDupes( @simplifiedCases ) );  
}

# return the number of unique genes in the data
sub numGenes{
    my $self = shift;
    return scalar( removeDupes( @{$self->{GENES}}) );  
}

sub getFilename{
    my $self = shift;
    return $self->{FILENAME};  
}

use Scalar::Util;

# class functions
# given references to a set of FirehoseFileMetadata instances, return the intersection of all their case lists
# Converts case IDs with convertCaseID() prior to intersection
sub intersection_of_case_lists{
	my( @FirehoseFileMetadataInstances ) = @_;
	if( 0 == scalar( @FirehoseFileMetadataInstances ) ){
		return(); # empty set
	}
	
	unless( _typecheck_FirehoseFileMetadataInstances_elements( @_) ){
		return;
	}

    my $ffmo = shift @FirehoseFileMetadataInstances;

	my $s = Set::Scalar->new( map {convertCaseID( $_ )} @{ $ffmo->cases() } );
	foreach $ffmo (@FirehoseFileMetadataInstances){
		$s = $s->intersection( Set::Scalar->new( map {convertCaseID( $_ )} @{ $ffmo->cases() } ) );
	}

	my @toReturn = ();
	for my $case ($s->members) {
	  if (defined $case) {
		push(@toReturn, $case);
	  }
	}

	return @toReturn;
}

# given references to a set of FirehoseFileMetadata instances, return the union of all their class lists
# also converts case IDs with convertCaseID() prior to intersection
sub union_of_case_lists{
    my( @FirehoseFileMetadataInstances) = @_;

    if( 0 == scalar( @FirehoseFileMetadataInstances ) ){
        return(); # empty set
    }

    # typecheck
    unless( _typecheck_FirehoseFileMetadataInstances_elements( @_) ){
        return;
    }

    my $ffmo = shift @FirehoseFileMetadataInstances;

    my $s = Set::Scalar->new( map {convertCaseID( $_ )} @{ $ffmo->cases() } );
    foreach $ffmo (@FirehoseFileMetadataInstances){
        $s = $s->union( Set::Scalar->new( map {convertCaseID( $_ )} @{ $ffmo->cases() } ) );
    }

	my @toReturn = ();
	for my $case ($s->members) {
	  if (defined $case) {
		push(@toReturn, $case);
	  }
	}

	return @toReturn;
}

# typecheck @FirehoseFileMetadataInstances elements
sub _typecheck_FirehoseFileMetadataInstances_elements{
    map {if( ref($_) ne 'FirehoseFileMetadata'){
        warn "Some argument not a FirehoseFileMetadata";
        return;
    }} @_;
    return 1;
}

1;
