package CreateDataFiles;

require Exporter;
@ISA = qw(Exporter);
@EXPORT = qw( create_data_CNA create_data_mRNA_median create_data_miRNA 
    create_data_mutations create_data_clinical create_data_mutation_white_list create_data_mRNA_median_Zscores ); 

use strict;
use warnings;
use File::Spec;
use File::Util;
use File::Temp qw/ tempfile /;
use Data::Dumper;
use Data::CTable;

use CaseIDs;
use Utilities;
use LoadCGDSdata;
use FirehoseFileMetadata;
use FirehoseEnv;

# todo: put comments in POD

# SUMMARY
# Create CGDS files (CreateDataFiles.pm)
# Set of subroutines, each takes input files and produces a CGDS file
# Issues: Some inconveniences in CTable
# CTable variable name fix not in CPAN
# Testing: Full coverage, except as noted

# Subroutines to create CGDS data files;
# each of these subroutines transforms one or more Firehose input file(s) to a single CGDS input file
# each sub is named create_<CGDS_file>, where CGDS_file indicates the name of the CGDS file being created
# each sub takes the arguments: firehose input file(s), open cTable object loaded from the firehose input file, name of CGDS input file 

# only instance data is a geneMap 
sub new {
	my( $class, $geneMap ) = @_;
    unless( defined($class) && defined($geneMap) && ref($geneMap) eq 'GeneIdentifiers' ){
        my($package, undef, undef, $subr)= caller(0);
        warn "$package\:\:$subr takes a GeneIdentifiers instance.\n";
        return undef;
    }
    
    my $self  = {
        GENEMAP => $geneMap,
    };
    bless( $self, $class );
    return $self;
}    

#########
# set of subroutines, each of which takes Firehose file(s) and makes a CGDS input file
# almost all subs take just one firehose file; only current exception is create_data_mRNA_median_Zscores()

# create data_CNA.txt
# source tarball: gdac.broadinstitute.org_<cancer>.Gistic2.Level_4.<date><version>.tar.gz
# source file: all_thresholded.by_genes.txt
# data transformation:
# Convert case ID
# drop columns 1 & 3
# Use Locus ID (aka Gene ID)
sub create_data_CNA{
    my( $self, $globalHash, $firehoseFile, $data, $CGDSfile ) = oneToOne( @_ );
    
    unless( $self->_check_create_inputs( $firehoseFile, $data, $CGDSfile ) ){
        return undef;
    }

    $self->mapDataToGeneID( $firehoseFile, $data, 'Gene Symbol', 'Locus ID' );
 
    # drop cols
    $data->col_delete( "Gene Symbol" );
    $data->col_delete( "Cytoband" );
    
    # convert case-ID headers
    convert_case_ID_headers( $firehoseFile, $data );

    # write CGDS file
    $data->write( $CGDSfile );
}

# sub to create data_expression_median.txt
# only called if both mRNA and miRNA are available; these must be combined into a single file
#
# data transformation:
# for both:
# Convert case ID
# Ignore row 2
# for mRNA:
# Convert 'Hybridization REF' to Gene_ID
# then, combine columns
sub create_data_both_mRNA_miRNA{    
    my( $self, $globalHash, $firehoseFiles, $cTables, $CGDSfile, $codeForCGDS ) = @_;    
    print "in create_data_both_mRNA_miRNA\n";

    # 1: check input files
    for( my $i=0; $i<2; $i++){
        unless( $self->_check_create_inputs( $firehoseFiles->[$i], $cTables->[$i] ) ){
            return undef;
        }
    }
    
    # 2: clean up both files
    for( my $i=0; $i<2; $i++){
        # convert case-ID headers
        convert_case_ID_headers( $firehoseFiles->[$i], $cTables->[$i] );

        # Ignore 2nd row; rows count from 0, and header doesn't count, so CTable's row 0
        $cTables->[$i]->row_delete( 0 );      

    }

    # the mRNA and miRNA expression files are the first two entries in $firehoseFiles, respectively
    my $FirehoseMRNA_File = shift @{$firehoseFiles};
    my $firehoseMiRNA_File = shift @{$firehoseFiles};
    
    # Similarly for cTables
    my $mRNA_File_Ctable = shift @{$cTables};
    my $miRNA_File_Ctable = shift @{$cTables};

    # combine columns
    $mRNA_File_Ctable->append( $miRNA_File_Ctable );

    # todo: fix this: cannot convert geneIDs, because geneMap doesn't have miRNAs; S/B OK with importProfileData
    #    $self->mapDataToGeneID( $FirehoseMRNA_File, $mRNA_File_Ctable, 'Hybridization REF', 'Gene_ID' );
	$mRNA_File_Ctable->col_rename( 'Hybridization REF' => 'Gene' );    
	
    # write CGDS file
    $mRNA_File_Ctable->write( $CGDSfile );
}

# create data_expression_median.txt for mRNA
# source tarball: gdac.broadinstitute.org_<cancer>.GDAC_median_mRNA_Expression.Level_4.<date>.<version>.tar.gz 
# source file: <cancer>.medianexp.txt
# data transformation:
# Convert case ID
# Ignore row 2
# Convert 'Hybridization REF' to Gene_ID
sub create_data_mRNA_median{
    my( $self, $globalHash, $firehoseFile, $data, $CGDSfile ) = oneToOne( @_ );;
    
    unless( $self->_check_create_inputs( $firehoseFile, $data, $CGDSfile ) ){
        return undef;
    }

    # convert case-ID headers
    convert_case_ID_headers( $firehoseFile, $data );

    # Ignore row 2; rows count from 0, and header row doesn't count
    $data->row_delete( 0 );
    
    # create Gene_ID column
    $data->col('Gene_ID');
    
    # convert geneIDs
    $self->mapDataToGeneID( $firehoseFile, $data, 'Hybridization REF', 'Gene_ID' );

    # rename 'Hybridization REF' column to Gene
    # todo: change Zscores calculation and importProfile so we can create a Gene column
    my @cols = ( 'Gene_ID', grep {tumorCaseID($_)} @{$data->fieldlist()} ); # call to sub that identifies tumors case IDs
    $data->fieldlist_set( \@cols );
    
    # write CGDS file
    $data->write( $CGDSfile );
}

# sub to create data_expression_median.txt for miRNA
# find latest version of tarball
# source tarball: gdac.broadinstitute.org_<cancer>.Merge_mirna__h_mirna_8x15kv2__unc_edu__Level_3__unc_DWD_Batch_adjusted__data.Level_3.<date>.<version>.tar.gz
# source file: <cancer>.mirna__h_mirna_8x15k__unc_edu__Level_3__unc_DWD_Batch_adjusted__data.data.txt
# data transformation:
# Convert case ID
# Ignore row 2
# leave IDs alone
sub create_data_miRNA{
    my( $self, $globalHash, $firehoseFile, $data, $CGDSfile ) = oneToOne( @_ );;

    unless( $self->_check_create_inputs( $firehoseFile, $data, $CGDSfile ) ){
        return undef;
    }
    
    # convert case-ID headers
    convert_case_ID_headers( $firehoseFile, $data );
    
    # has only miRNA ids, so leave GeneIDs alone
    
    # Ignore 2nd row; rows count from 0, and header doesn't count, so CTable's row 0
    $data->row_delete( 0 );      

    # write CGDS file
    $data->write( $CGDSfile );
}

# create data_mutations_extended.txt
# source tarball: gdac.broadinstitute.org_<cancer>.MutationAssessor.Level_4.<date>.<version>.tar.gz 
# source file: <CANCER>.maf.annotated
# data transformation:
# if these are available, map them:
# MA:FImpact <- MA_Func.Impact
# MA:link.var <- MA_VAR
# MA:link.MSA <- MA_MSA
# MA:link.PDB <- MA_PDB
# discard bad links (like http://mutationassessor.org[sent]) from MA:link.MSA 
# if MA:FImpact, MA:link.var, MA:link.MSA or MA:link.PDB is empty or '--' convert to 'NA'
# MA:variant <- if( MA:link.MSA is available use that (or the renamed MA_MSA), else use amino_acid_change_WU or AAChange or
# Protein_Change or amino_acid_change )
# map MA:FImpact by { high => H, medium => 'M', low => 'L', neutral => 'N' }
sub create_data_mutations_extended{
    my( $self, $globalHash, $firehoseFile, $data, $CGDSfile ) = oneToOne( @_ );;
    
    unless( $self->_check_create_inputs( $firehoseFile, $data, $CGDSfile ) ){
        return undef;
    }
    
    # map these to standard column names
    my %fieldMap = ( 
        'MA_Func.Impact'    =>  'MA:FImpact',
        'MA_VAR'            =>  'MA:link.var',
        'MA_MSA'            =>  'MA:link.MSA', 
        'MA_PDB'            =>  'MA:link.PDB' );

    for my $keyValue (keys %fieldMap){

        # if destination column does not exist and key column exists, 
        # then move key column to destination
        if( !exists( $data->{ $fieldMap{$keyValue} } ) and
           exists( $data->{ $keyValue })){
               $data->col_rename( $keyValue => $fieldMap{$keyValue} ) # covered in testMapToStandardNames
        }
            
        # if neither exists then report error
        unless( exists( $data->{ $fieldMap{$keyValue} } ) or 
            exists( $data->{ $keyValue } ) ){
                warn "Error: missing column in maf file: neither ". $fieldMap{$keyValue} . 
                " nor " . $keyValue . " is present.\n";  # covered in testNoImpactCol
        }
    }

    # if MA:FImpact, MA:link.var MA:link.MSA or MA:link.PDB is empty or '--' convert to 'NA'
    $data->calc( sub{
        package main;
        no strict 'vars';
        no strict 'refs';
        
        for my $destinationColumn (values %fieldMap){
            if( exists( $data->{$destinationColumn} ) ){ # if the column exists
                if( !defined( $$destinationColumn ) ||
                    $$destinationColumn eq '' || $$destinationColumn eq '--' ){
                    $$destinationColumn = 'NA'; # covered in testMapToStandardNames
                }  
            }
        }

    }, undef, existingCols( $data, values %fieldMap ) );
    
    # discard bad links (like http://mutationassessor.org[sent]) from MA:link.MSA 
    # good ones match the RE var=(.+)$
    # covered in testAAChangeSimple
    if( $data->col_exists( 'MA:link.MSA' )){
	    $data->calc( sub{
	        package main;
	        no strict 'vars';
	        no strict 'refs';
            if( defined( ${'MA:link.MSA'} ) ){
            	unless( ${'MA:link.MSA'} =~ m|var=(.+)$| ){
            		${'MA:link.MSA'} = 'NA';
            	}
            }
	        
	    }, undef, [ 'MA:link.MSA' ] );
    }

    # get amino_acid_change 
    # MA:variant <- if( MA:link.MSA is available use that (or the renamed MA_MSA), else use amino_acid_change_WU or AAChange or
    # Protein_Change or amino_acid_change)

    # if an MA:variant col exists, delete it
    if( exists( $data->{'MA:variant'} ) ){
        $data->col_delete( 'MA:variant' );
    }
    # create an empty one
    $data->col( 'MA:variant' );
    
	$data->calc( sub{
	    package main;
        no strict 'vars';
        no strict 'refs';
	    ${MA:variant} = ''; 

        # all covered in testAAChangeSimple
	    if( defined( ${'MA:link.MSA'} ) && ${'MA:link.MSA'} =~ m|var=(.+)$| ){
            ${MA:variant} = $1; 
	    }
        # amino_acid_change_WU examples: p.R219H, e7-2, R219H, p.811_812EE>D*, missense, NULL
        elsif(defined($amino_acid_change_WU)) {
          if ($amino_acid_change_WU =~ m/^NULL$/ ){
            return;
          }
          if( $amino_acid_change_WU =~ m/^p\.(.+)$/ ){
            ${MA:variant} = $1; 
            return;
          }
          ${MA:variant} = $amino_acid_change_WU;
        }
        # AAChange examples: p.R219H, e7-2, R219H, p.811_812EE>D*, missense, NULL
        elsif(defined($AAChange)) {
          if ($AAChange =~ m/^NULL$/ ){
            return;
          }
          if( $AAChange =~ m/^p\.(.+)$/ ){
            ${MA:variant} = $1; 
            return;
          }
          ${MA:variant} = $AAChange;
        }
        # Protein_Change examples: p.R219H, e7-2, R219H, p.811_812EE>D*, missense, NULL
        elsif( defined( $Protein_Change ) ){
          if( $Protein_Change =~ m/^NULL$/ ){
            return;
          }
          if( $Protein_Change =~ m/^p\.(.+)$/ ){
            ${MA:variant} = $1; 
            return;
          }
          ${MA:variant} = $Protein_Change;  
        }
        # amino_acid_change examples: p.R219H, e7-2, R219H, p.811_812EE>D*, missense, NULL
        elsif(defined($amino_acid_change)) {
          if ($amino_acid_change =~ m/^NULL$/ ){
            return;
          }
          if( $amino_acid_change =~ m/^p\.(.+)$/ ){
            ${MA:variant} = $1; 
            return;
          }
          ${MA:variant} = $amino_acid_change;
        }
	}, undef, existingCols( $data, qw( MA:link.MSA MA:variant amino_acid_change_WU AAChange Protein_Change amino_acid_change ) ) );

    # map MA:FImpact by { high => H, medium => 'M', low => 'L', neutral => 'N' }
    my %impactMap = ( high => 'H', medium => 'M', low => 'L', neutral => 'N' );
    my $impactField = "MA:FImpact";
    $data->calc( sub{
        package main;
        no strict 'vars';
        no strict 'refs';
        
		if( defined( $$impactField ) ){
			if( exists( $impactMap{ $$impactField } )){
				$$impactField = $impactMap{ $$impactField };   # covered in testFImpactCol
			}
		}
        
    }, undef, existingCols( $data, $impactField ) ); # speed up calc by listing needed field

	# todo: HIGH: probably should map the Hugo_Symbol Entrez_Gene_Id to get better IDs
	# MAP this to MSKCC abbreviations: Tumor_Sample_Barcode    
    unless( $data->col_exists( 'Tumor_Sample_Barcode' )){
    	warn "Error: missing Tumor_Sample_Barcode column in maf file.\n";  # covered in testNoTumorName
    	return;
    	
    }else{
	    $data->calc( sub{
	        $main::Tumor_Sample_Barcode = convertCaseID( $main::Tumor_Sample_Barcode );
	    }, undef, ['Tumor_Sample_Barcode'] );
    }
	
    # subselect and reorder columns 
    $data->fieldlist_set( [ qw( Hugo_Symbol Entrez_Gene_Id Center Tumor_Sample_Barcode  
        Verification_Status Validation_Status Mutation_Status 
         Sequencer Chromosome Start_position End_position Variant_Classification
        MA:variant MA:FImpact MA:link.var MA:link.MSA MA:link.PDB  ) ] );

    my $ffm = FirehoseFileMetadata->new( '<CANCER>.maf.annotated', $firehoseFile, $data );    
#    print $ffm->numCases(), " unique case(s) in $firehoseFile:\n";
    $globalHash->{NUM_SEQUENCED_CASES} = $ffm->numCases();

    # write CGDS file
    $data->write( $CGDSfile );
}


# sub to create data_methylation.txt
# Present the beta value for the most anti-correlated probe on a gene.
# 
# input firehose files:
# Correlate_Methylation_vs_mRNA_<CANCER>_matrix.txt
# <CANCER>.methylation__humanmethylation27__jhu_usc_edu__Level_3__within_bioassay_data_set_function__data.data.txt
#
# data transformation:
#    For each gene select row with smallest Corr_Spearman in Correlate_Methylation_vs_mRNA_<CANCER>_matrix.txt
#    Join selected rows from Correlate_Methylation_vs_mRNA... with <CANCER>.methylation__humanmethylation... on probe (Hybridization REF)
# Output matrix beta_value( caseID, gene [symbol/Entrez gene ID] ) from joined table
sub create_data_methylation{
    my( $self, $globalHash, $firehoseFiles, $cTables, $CGDSfile, undef, $additionalArgs ) = @_;    
    
    ############
    # 1: check input files
    for( my $i=0; $i<2; $i++){
	    unless( $self->_check_create_inputs( $firehoseFiles->[$i], $cTables->[$i] ) ){
	        return undef;
	    }
    }

    # the Correlate_Methylation_vs_mRNA and <CANCER>.methylation__humanmethylation27... files are the first two entries in $firehoseFiles
    my $Firehose_Correlate_Methylation_vs_mRNA_File = shift @{$firehoseFiles};
    my $Firehose_methylation__humanmethylation27_File = shift @{$firehoseFiles};
    
    # Similarly for cTables
    my $Correlate_Methylation_vs_mRNA_Ctable = shift @{$cTables};

    # for debugging, identify the unique genes
    my $humanmethylation27_Ctable = shift @{$cTables};
    my $ffm = FirehoseFileMetadata->new( '<CANCER>.methylation__humanmethylation27__jhu_usc_edu__Level_3__within_bioassay_data_set_function__data.data.txt',
        "$Firehose_methylation__humanmethylation27_File", $humanmethylation27_Ctable );
    # print "methylation genes: ", $ffm->numGenes(), " unique genes in $Firehose_methylation__humanmethylation27_File:\n";

    ############
    # 2: get probe with lowest Spearman correlation for each gene in $Correlate_Methylation_vs_mRNA_Ctable
    # this works whether or not $Correlate_Methylation_vs_mRNA_Ctable is sorted by correlation value
    my $genes = {}; # hash: gene -> [ probe, correlation value ]
    $Correlate_Methylation_vs_mRNA_Ctable->calc( sub{
        package main;
        no strict 'vars';
        # print "$Meth_Probe\t$Gene\t$Corr_Spearman\n";
        if( exists( $genes->{$Gene} ) ){
            if( $Corr_Spearman < $genes->{$Gene}->[1] ){
                # print $Corr_Spearman . " < " .  $genes->{$Gene}->[1] . "\n";
                $genes->{$Gene} = [ $Meth_Probe, $Corr_Spearman ];
            }
            
        }else{
            $genes->{$Gene} = [ $Meth_Probe, $Corr_Spearman ];
        }
    }, undef, existingCols( $Correlate_Methylation_vs_mRNA_Ctable,
        qw( Meth_Probe Gene Corr_Spearman ) ) ); # speed up calc by listing needed fields 

    # print "methylation genes: ", scalar(keys %{$genes}), " unique lowest correlation genes from \$Correlate_Methylation_vs_mRNA_Ctable: $Firehose_Correlate_Methylation_vs_mRNA_File\n";
    print "create_data_methylation: of ", $ffm->numGenes(), " with measured methylation, only ", scalar(keys %{$genes}), " have correlations.\n";

    # determine set of probes; each should appear in only one gene
    my $probes={};
    foreach my $k (keys %{$genes}){
    	my $r = $genes->{$k};
        if( exists( $probes->{ $r->[0] } ) ){
            warn $r->[0] . " duplicated in multiple genes.\n";
        }
        $probes->{ $r->[0] } = $k;
    }
    
    ############
    # 3: remove extraneous columns from methylation__humanmethylation27_File
    # 
    # cannot use original cTable for $Firehose_methylation__humanmethylation27_File because it contains
    # multiple columns with the same name, in particular 4 columns (Beta_Value  Gene_Symbol Chromosome  Genomic_Coordinate)
    # for each case.
    # so, count the # of columns in $Firehose_methylation__humanmethylation27_File, and copy just
    # columns 1, 2, 6, 10, ..., to a new file and process those columns
    my($f) = File::Util->new();
    my $FIREHOSE_METHYL_FH = $f->open_handle('file' => $Firehose_methylation__humanmethylation27_File, 
        'mode' => 'read' );
    my $line = <$FIREHOSE_METHYL_FH>;
    my @fields = split( /\t/, $line );
    my $n = scalar @fields;
    # print "\$Firehose_methylation__humanmethylation27_File has $n columns.\n";
    $f->unlock_open_handle( $FIREHOSE_METHYL_FH );
    # check for 4 columns per case
    unless( ($n-1)%4 == 0 ){
        my($package, undef, undef, $subr)= caller(0);
        warn "$package\:\:$subr: '$Firehose_methylation__humanmethylation27_File' does not have 4 columns per case.\n";
        return;
    }
    my $cols = '1';
    for( my $c = 2; $c < $n; $c += 4 ){
    	$cols .= ",$c";
    }
    # todo: tmp cleanup: remove this and other temp files
	my ($fh, $tempFile) = tempfile();
	close $fh;
    my $cmd = "cut -f $cols " . File::Spec->rel2abs(  $Firehose_methylation__humanmethylation27_File ) . " > $tempFile" ;
    unless( system($cmd ) == 0 ){
        	warn "Error: system '$cmd' failed: $?";
        	return;
        }
        
    ############
    # 4: create and clean up $methylation__humanmethylation27_Ctable
    my $methylation__humanmethylation27_Ctable = Data::CTable->new( { _CacheOnRead   => 0 }, $tempFile );
    # remove row containing "Composite Element REF  Beta_Value ..."; rows count from 0, and header row doesn't count
    $methylation__humanmethylation27_Ctable->row_delete( 0 );
    # convert case IDs to MSKCC abbreviation
    convert_case_ID_headers( $Firehose_methylation__humanmethylation27_File, $methylation__humanmethylation27_Ctable );
    
    ############
    # 5: write rows in $methylation__humanmethylation27_Ctable with lowest correlated probes to methylation data file
    # select rows
    $methylation__humanmethylation27_Ctable->select_all();
    # if probe has the gene lowest correlation file (i.e., is in the $probes array ) then select its methylation row
    # if a gene is NOT in the correlation file, then select its first ?? methylation row
    $methylation__humanmethylation27_Ctable->select( 'Hybridization REF' => sub { exists( $probes->{$_} )} );
    
    # add gene column
    $methylation__humanmethylation27_Ctable->col('Gene');
    # add Entrez Gene ID
    $methylation__humanmethylation27_Ctable->col('Entrez_Gene_Id');
    # set column order
    my @cols = ( 'Gene', 'Entrez_Gene_Id', grep {tumorCaseID($_)} @{$methylation__humanmethylation27_Ctable->fieldlist()} ); 
    $methylation__humanmethylation27_Ctable->fieldlist_set( \@cols );

    # instantiate Gene and Entrez_Gene_Id columns
    $methylation__humanmethylation27_Ctable->calc( sub{
        package main;
        no strict 'vars';
        no strict 'refs';
        $Gene = $probes->{ ${'Hybridization REF'} };
        $Entrez_Gene_Id = $self->{GENEMAP}->getGeneID( $Gene ); 
        unless( defined( $Entrez_Gene_Id )){
        	$Entrez_Gene_Id = 'NA';
        }
    }, undef, existingCols( $methylation__humanmethylation27_Ctable, 
        ( 'Hybridization REF', 'Entrez_Gene_Id', 'Gene' ) ) );

    # remove Hybridization_REF column
    $methylation__humanmethylation27_Ctable->col_delete( 'Hybridization_REF' );

    # write CGDS file with methylation( Gene, caseID )
    $methylation__humanmethylation27_Ctable->write( $CGDSfile );    
}

# TODO: DISCARD?
# create data_mutations.txt
# source tarball: gdac.broadinstitute.org_<cancer>.MutationAssessor.Level_4.<date>.<version>.tar.gz 
# source file: <CANCER>.maf.annotated
# data transformation:
# keep column 2, Entrez_Gene_Id
# keep column 16 (P), Tumor_Sample_Barcode
# get the 'var' value in the "Xvar_MSA" or "MA_MSA" column for PROT_STRING, and for PROT_STRING_SHORT
# keep column 9 (I), Variant_Classification, and strip off "_Mutation"
# convert case IDs to MSKCC abbreviation
sub create_data_mutations{
    my( $self, $globalHash, $firehoseFile, $data, $CGDSfile ) = oneToOne( @_ );;
    
    die( "Error: Replaced by data_mutations_extended");
    return;
    
    unless( $self->_check_create_inputs( $firehoseFile, $data, $CGDSfile ) ){
    	return undef;
    }

    # make output table (actually, could do this without creating new table, by selecting output columns)
    # todo: do this without creating new table, which will make filtering easier
    my $CGDStable = Data::CTable->new( { _FDelimiter => "\t" });

    # transfer Entrez_Gene_Id and Tumor_Sample_Barcode columns
    $CGDStable->col_set('Entrez_Gene_Id', $data->col('Entrez_Gene_Id') );
    $CGDStable->col_set('Tumor_Sample', $data->col('Tumor_Sample_Barcode') );
    
    # get the amino acid change notation value for PROT_STRING and PROT_STRING_SHORT
    # e.g., R1028P in mutationassessor.org/?cm=msa&ty=f&p=BAI2_HUMAN&rb=929&re=1198&var=R1028P
    # most centers put this in the "Xvar_MSA" column, but Broad code calls this column MA_MSA
    
    my @possibleCols = qw( Xvar_MSA MA_MSA );
    my $mutColumn;
    foreach (@possibleCols){
        if( $data->col_exists($_) ){
            $mutColumn = $_;
        }
    }
    unless( defined( $mutColumn ) ){
        warn "No column for mutation amino acid change.\n";
        return undef;
    }
    
    # do calc on the right column; could do this with eval, but not worth the trouble, unless it gets bigger
    my($m, $n) = ( 0, 0 );
    if( $mutColumn eq 'MA_MSA'){
        $data->calc( sub{
            package main;
            no strict 'vars';
            $m++;
            if( defined( $MA_MSA )){
                $MA_MSA =~ m|var=(.+)$|;
                if( defined( $1 ) ){
                    $MA_MSA = $1;
                }
            }else{
                $n++;
            }
        });
        
    }
    
    if( $mutColumn eq 'Xvar_MSA'){
        $data->calc( sub{
            package main;
            no strict 'vars';
            $m++;
            if( defined( $Xvar_MSA )){
                $Xvar_MSA =~ m|var=(.+)$|;
                if( defined( $1 ) ){
                    $Xvar_MSA = $1;
                }
            }else{
                $n++;
            }
        });
        
    }   

    warn "$n undefined values for $mutColumn, out of $m.\n";
    $CGDStable->col_set('PROT_STRING', $data->col( $mutColumn ) );
    $CGDStable->col_set('PROT_STRING_SHORT', $data->col( $mutColumn ) );
    
    # keep Variant_Classification column, striping off "_Mutation"
    $data->calc( sub{
    	package main; # CTable says: Variables in $Sub are in the "main" package. So you should set $Sub to use pacakge "main" in case the rest of your code is not in "main".
        no strict 'vars';
        $Variant_Classification =~ s|_Mutation||; # ignore Perl "Name "main::Variant_Classification" used only once" warning
    }); 
    
    $CGDStable->col_set('CALLED_CLASSIFICATION', $data->col('Variant_Classification') );    

    # convert case IDs to MSKCC abbreviation
    $CGDStable->calc( sub{
        my $ts = $main::Tumor_Sample;
        $main::Tumor_Sample = convertCaseID( $main::Tumor_Sample );
        # print "converting $ts -> $Tumor_Sample\n";
    });

    # reorder columns to Entrez_Gene_Id    Tumor_Sample    PROT_STRING PROT_STRING_SHORT   CALLED_CLASSIFICATION
    $CGDStable->fieldlist_set( [ qw( Entrez_Gene_Id    Tumor_Sample    PROT_STRING PROT_STRING_SHORT   CALLED_CLASSIFICATION ) ] );
    
    # write CGDS file
    $CGDStable->write( $CGDSfile );    
}

# create_data_clinical
# does NOT create a cgds file;
# rather checks on num of cases with good years to birth field
sub create_data_clinical{
    my( $self, $globalHash, $firehoseFile, $data, $CGDSfile ) = oneToOne( @_ );;    
    print "create_data_clinical not implemented.\n";

    unless( $self->_check_create_inputs( $firehoseFile, $data, $CGDSfile ) ){
        return undef;
    }
    # count number of non-NA values in 'yearstobirth' row
    # 'Hybridization REF' is name of first column
    $data->select( Hybridization_REF => sub { $_ =~ /yearstobirth/ } ); ## Del nonmatching recs from sel.
    my $numValues = 0;
    foreach my $f (@{ $data->fieldlist() }){
    	if( $data->col_get( $f )->[0] =~ /\d+/ ){
    		$numValues++;
    	}
    }
    print $numValues . " numeric values in yearstobirth in $firehoseFile\n";
}

# obtain per-cancer gene 'whitelist' from mutation significance file
# since the output file ($CGDSfile), currently called significantlyMutatedSomaticGenesWhitelist.txt, is not a data_* file, 
# (see $Utilities::dataFilePrefix) it won't have a meta file created, and won't be loaded into the cgds dbms
#
# make list of genes that satisfy
# if q and n available then qval <= threshold OR pct*numCases <= nval 
# if only n available, then use 50 most frequent genes 
# TODO: report stats to evaluate this
sub create_mutation_white_list{
    my( $self, $globalHash, $firehoseFile, $data, $CGDSfile, undef, $additionalArgs ) = oneToOne( @_ );

    unless( $self->_check_create_inputs( $firehoseFile, $data, $CGDSfile ) ){
        return undef;
    }
    
    my @args = split( /,/, $additionalArgs );
    print "\$additionalArgs $additionalArgs\n";
    if( scalar(@args) != 2 ){
    	warn "create_mutation_white_list: Incorrect number of \$additionalArgs: '$additionalArgs'.";
    	return;
    }
    
    my $qThreshold = $args[0];                          # q-value threshold
    my $nPctThreshold = $args[1];                       # pct of cases with the gene mutated threshold

    my $nCases;  # number of sequenced cases
    unless( exists( $globalHash->{NUM_SEQUENCED_CASES} ) && 
        defined( $globalHash->{NUM_SEQUENCED_CASES} ) && 1 < $globalHash->{NUM_SEQUENCED_CASES} ){
        	warn "CreateDataFiles::create_mutation_white_list: number of sequenced cases not set.\n";
        	return;
    }
    $nCases = $globalHash->{NUM_SEQUENCED_CASES};
    
    print "Creating mutation white list from $firehoseFile with parameters:" .
        "\n\tq-value threshold: $qThreshold" .
        "\n\t% of cases with the gene mutated threshold: $nPctThreshold" .
        "\n\tnum of cases: $nCases\n";
    
    # ignore q if it isn't available, which occurs if maf hasn't been run through sig genes computation
    if( $data->col_exists( 'q' ) ){
        $data->select_none();
	    # include rows with q <= $qThreshold, while handling funny q-value format like <6.29e-08
	    $data->add( q => sub{ $_ =~ s/^<//; $_ <= $qThreshold });

	    # include rows with n >= $nThreshold
	    $data->add( n => sub{ $_ >= $nPctThreshold*$nCases/100 });
    }else{
    	
    	# just pick most frequent genes, up to 50 genes
    	my $maxGenes = 50;
    	my $rows = $data->length_get();
    	if( $maxGenes < $rows ){
    		$data->length_set( $maxGenes );
    	}
    }
    
    # output just Hugo gene symbol column
    $data->fieldlist_set( [ ( 'gene' ) ] );

    # write CGDS file; don't write header row
    $data->write(  _FileName => $CGDSfile, _HeaderRow     => 0 );
}

# todo: high: unit test

# sub to create data_mRNA_median_Zscores.txt
# source tarball and file same as for data_mRNA_median.txt
# data transformation:
# Giovanni's Z-score program
# inputs CNA and median expression profile files
# outputs z-Score expression file
# special, as calls java program to compute output file
sub create_data_mRNA_median_Zscores{
    my( $self, $globalHash, $firehoseFiles, $cTables, $CGDSfile, $codeForCGDS ) = @_;    

    # the CNA and median expression files are the first two entries in $firehoseFiles
    my $FirehoseGistic_File = shift @{$firehoseFiles};
    my $FirehoseMRNA_File = shift @{$firehoseFiles};
    
    # Similarly for cTables
    my $Gistic_FileCtable = shift @{$cTables};
    my $MRNA_FileCtable = shift @{$cTables};
    
    ###########
    # preprocess gene ids so that cgds finds as many zScore genes as possible 
    # for each input file, read file, map, write to temp file
    # give temp files to ComputeZScoreUnit
    my $tmpDir = File::Spec->tmpdir();

    # 1) combine Hugo_Symbol and Entrez_Gene_Id in all_thresholded.by_genes.txt into a 'best' gene ID
    # todo: make a "real" temp file; avoid concurency collisions
    # todo: tmp cleanup: remove this and other temp files
    my $tmpFirehoseGistic_File = File::Spec->catfile( $tmpDir, 'tmp_all_thresholded.by_genes.txt' );
    $self->create_data_CNA( $globalHash, [ $FirehoseGistic_File ], [ $Gistic_FileCtable ], $tmpFirehoseGistic_File );

    # 2) map Hugo_Symbol in <CANCER>.medianexp.txt into a 'best' gene ID 
    # todo: make a "real" temp file; avoid concurency collisions
    my $tmpFirehoseMRNA_File = File::Spec->catfile( $tmpDir, 'tmp_CANCER.transcriptome__agilentg4502a_07_3__unc_edu__Level_3__unc_lowess_normalization_gene_level__data.data.txt' );
    $self->create_data_mRNA_median( $globalHash, [ $FirehoseMRNA_File ], [ $MRNA_FileCtable ], $tmpFirehoseMRNA_File );

    my $cmdLineCP = set_up_classpath( $codeForCGDS );
    my $files = join( ' ', ( $tmpFirehoseGistic_File, $tmpFirehoseMRNA_File, $CGDSfile ) );

    # run the zScore java program
    runSystem( "$JAVA_HOME/bin/java -Xmx3000M -cp $cmdLineCP org.mskcc.cgds.scripts.ComputeZScoreUnit " . $files );
}

# create <CANCER>.seg
# source tarball: gdac.broadinstitute.org_<CANCER>.CopyNumber_Preprocess.Level_4.<date><version>
# source file: <CANCER>.Use_Me_Level_3__segmented_cna__seg.tsv
# data transformation:
# None.  Simply rename the file
sub create_igv_seg {
    my( $self, $globalHash, $firehoseFile, $data, $CGDSfile ) = oneToOne( @_ );
	$data->write($CGDSfile);
}

# Given a Gene symbol column, and a gene ID column (which might contain data), 
# obtain GeneID for all entries in a column in a CTable.
#
# Drop -- by de-selecting -- rows for which a gene ID cannot be found.  
# Use both gene symbol and gene ID to get best available ID.
# For example, since miRNA geneIDs are negative, we keep geneIDs for protein coding genes and symbols for miRNAs
# uses GeneIdentifiers->getGeneID()
# TODO: HIGH: TEST THESE VARIOUS EXECUTION PATHS AND ERRORS
sub mapDataToGeneID{
    my( $self, $firehoseFile, $data, $nameOfGeneSymbolColumn, $nameOfGeneIDColumn ) = @_;

    # for error output
    my($volume,$directories,$file) = File::Spec->splitpath( $firehoseFile );
    my @dirs = File::Spec->splitdir( $directories );
    pop @dirs;
    my $FirehoseDirAndFile = "$file in " . pop @dirs;
    
    my $Gene_Symbols;
    if( defined( $nameOfGeneSymbolColumn ) ){
        $Gene_Symbols = $data->col( $nameOfGeneSymbolColumn ); 
        unless( defined( $Gene_Symbols ) ){
            die "mapDataToGeneID: \$nameOfGeneSymbolColumn not a column header in $FirehoseDirAndFile";
        }
    }
    if( !defined( $nameOfGeneIDColumn ) ){
        die "mapDataToGeneID: \$nameOfGeneIDColumn must be set";
    }
    my $Gene_IDs = $data->col($nameOfGeneIDColumn); 

    # assumes only 1 header row
    # todo: HIGH: avoid or verify this assumption
    my $rowOffset = 2; # add 2 to get original row# in source data file 
    my $rows = $data->all();
    my $removed = 0;
    foreach my $rowNum (@{$rows}){
        if( defined( $Gene_Symbols ) && !defined( $Gene_Symbols->[$rowNum] )){
            warn "'$nameOfGeneSymbolColumn' not defined for row " . ($rowNum+$rowOffset) . " in $FirehoseDirAndFile.\n";
            next;
        }

        my $GeneID = $self->{GENEMAP}->getGeneID( defined( $Gene_Symbols ) ? $Gene_Symbols->[$rowNum] : undef,
            $Gene_IDs->[$rowNum] ); 
            
        my $inputGeneSym = defined( $Gene_Symbols ) && defined( $Gene_Symbols->[$rowNum] ) ? $Gene_Symbols->[$rowNum] : 'undefined' ;
        my $inputGeneID = defined( $Gene_IDs ) && defined( $Gene_IDs->[$rowNum] ) ? $Gene_IDs->[$rowNum] : 'undefined' ;
    
        if( defined( $GeneID )){
            # print "getGeneID( $inputGeneSym, $inputGeneID ) maps to $GeneID.\n";
            $Gene_IDs->[$rowNum] = $GeneID;
        }else{
            # note: CANNOT delete row in the loop
            # warn "Removing row $rowNum because no Gene ID is available.\n";
            $removed++;
        }
    }
    $data->select_all();
    my $m = scalar( @{ $data->selection() } );
    # select rows for which GeneID column is defined
    $data->select( $nameOfGeneIDColumn => sub { defined( $_ ) } );

    if( $removed ){
        print "$removed out of $m records in $FirehoseDirAndFile missing valid Gene IDs.\n";
    }else{
        print "All $m records in $FirehoseDirAndFile contain valid Gene IDs.\n";
    }
}

# check that $firehoseFile and $data are initialized
sub _check_create_inputs{
    my( $self, $firehoseFile, $data ) = @_;
    my($package, undef, undef, $subr)= caller(1);
    unless( -r $firehoseFile ){
        warn "$package\:\:$subr: '$firehoseFile' not readable.\n";
        return undef;
    }
    unless( defined( $data ) ){
        warn "$package\:\:$subr: CTable not initialized.\n";
        return undef;
    }
    return 1;
}

# get input params for a data file creation sub that reads only one Firehose input file
sub oneToOne{
    my( $self, $globalHash, $firehoseFiles, $dataHandles, $CGDSfile, $CGDScodeDir, $additionalArgs ) = @_;

    return( $self, $globalHash, $firehoseFiles->[0], $dataHandles->[0], $CGDSfile, $CGDScodeDir, $additionalArgs );
}

1;
