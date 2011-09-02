package GeneIdentifiers;
require Exporter;
@ISA = qw(Exporter);
@EXPORT = qw(new getGeneID loadGeneList hasGeneID hasGeneName ); 

use strict;
use warnings;
use File::Util;
use Data::Dumper;

# SUMMARY
# Map gene symbols to EntrŽz ids (GeneIdentifiers.pm)
# Uses local copy of NCBI's gene_info.gz, with columns 'GeneID Symbol LocusTag Synonyms'
# To maximize accuracy and minimize unmapped genes, use all fields
# Issues: Better comments
# Testing: Full coverage

sub new {
    unless( scalar(@_) == 2 ){
        warn "GeneIdentifiers::new() takes a filename";
        return;
    }
    
    my $class = shift;
    my $self  = {
        FILENAME     => shift,
        DEBUG       => 1,
        # build two hashes GeneIDtoGeneSymbol and GeneSymboltoGeneID, inverses of each other
        GeneIDtoGeneSymbol     => {},
        GeneSymboltoGeneID     => {},
        GeneLists              => {},
    };
        
    bless( $self, $class );
    
    unless( $self->_loadGeneMaps( $self->{FILENAME} ) ){
    	return;
    }
    return $self;
}
    
# gene mapping table from ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/gene_info.gz
# read human_gene_info, gene_info filtered on human genes, tax_id == 9606
# Format: tax_id GeneID Symbol LocusTag Synonyms dbXrefs 
sub _loadGeneMaps{
    my( $self, $geneFile ) = @_;
    
    if( $self->{DEBUG} ){
        print "Loading genes from $geneFile.\n";
    }
    my $fileUtil = File::Util->new();
    unless( -r  $geneFile ){
    	warn "Cannot read gene file: '$geneFile'";
    	return;
    }
    my @genes = $fileUtil->load_file( $geneFile, '--as-lines' );
    
    # maps of gene ID and gene symbols
    my $GeneIDtoGeneSymbol = $self->{GeneIDtoGeneSymbol}; 
    my $GeneSymboltoGeneID = $self->{GeneSymboltoGeneID};
        
    my $dupeIDs= 0;
    my %badSymbols;

    foreach my $g (@genes){
        my( $tax_id, $GeneID, $Symbol) = split( "\t", $g);
        # double-check taxonomy tax_id = 9606  == human
        if( $tax_id ne '9606' ){
            die "Non-human genes in $geneFile";
        }
        
        if( $GeneID < 0){
            warn "might be mir: $g\n";
        }

        # check for duplicate IDs and Symbols
        if( exists( $GeneIDtoGeneSymbol->{$GeneID} )){
        	die "GeneID $GeneID is not unique";
            $dupeIDs++;
            next;
        }
        if( exists( $GeneSymboltoGeneID->{$Symbol} ) && $GeneSymboltoGeneID->{$Symbol} ne $GeneID ){
            warn "Not using gene symbol $Symbol because it maps to duplicate gene IDs: ", 
                $GeneSymboltoGeneID->{$Symbol}, " and $GeneID.\n";
            delete( $GeneSymboltoGeneID->{$Symbol} );
            $badSymbols{ $Symbol } = 1;                
            next;
        }
        if( exists( $badSymbols{ $Symbol } )){
        	warn "More than 2 distinct mappings for $Symbol.\n";
        	next;
        }
        $GeneIDtoGeneSymbol->{$GeneID} = $Symbol;
        $GeneSymboltoGeneID->{$Symbol} = $GeneID;
    }

    my $n = scalar( keys %{$GeneSymboltoGeneID} );
    if( scalar( keys %badSymbols ) ){
        warn "Ignoring ", scalar( keys %badSymbols ), " duplicate Gene Symbol(s) in $geneFile.\n";
    }
    print "Loaded ", $n, " unique Gene symbol to Gene ID mappings from $geneFile.\n";
    
    # add unique symbols -- those that appear only once -- in Synonyms and LocusTag columns to the mappings
    # first find duplicated Synonyms and LocusTags
    
    # '-' is the blank symbol
    my $blank = '-';
    my $SynonymCounts = {};
    my $LocusTagCounts = {};
    foreach my $g (@genes){
        my( $tax_id, $GeneID, $Symbol, $LocusTag, $Synonyms ) = split( "\t", $g);
        
        if( $Synonyms ne $blank ){
	        my @Synonyms = split( "\\|", $Synonyms );
	        foreach my $s (@Synonyms){
	            if( exists( $SynonymCounts->{$s} )){
	                $SynonymCounts->{$s}++;
	            }else{
	                $SynonymCounts->{$s} = 1;
	            }
	        }
        }
        if( $LocusTag ne $blank ){
	        if( exists( $LocusTagCounts->{$LocusTag} )){
	            $LocusTagCounts->{$LocusTag}++;
	        }else{
	            $LocusTagCounts->{$LocusTag} = 1;
	        }
        }
    }
    
    my $SynonymsMappings;
    my $locusTagMappings;
    # now add only the unique ones
    foreach my $g (@genes){
        my( $tax_id, $GeneID, $Symbol, $LocusTag, $Synonyms ) = split( "\t", $g);
        my @Synonyms = split( "\\|", $Synonyms );
        foreach my $Symbol (@Synonyms){
        	# dont replace existing mappings
            if( $Synonyms ne $blank && $SynonymCounts->{$Symbol} == 1 && 
                !exists( $GeneSymboltoGeneID->{$Symbol} )){
            	#  print "Adding Symbol: $Symbol\n";
            	$SynonymsMappings++;
                $GeneIDtoGeneSymbol->{$GeneID} = $Symbol;
                $GeneSymboltoGeneID->{ $Symbol} = $GeneID;
            }
        }

        if( defined( $LocusTag ) && $LocusTag ne $blank ){
			if( $LocusTagCounts->{$LocusTag} == 1 && !exists( $GeneSymboltoGeneID->{$LocusTag} )){
                # print "Adding LocusTag: $LocusTag\n";
                $locusTagMappings++;
			    $GeneIDtoGeneSymbol->{$GeneID} = $LocusTag;
			    $GeneSymboltoGeneID->{ $LocusTag} = $GeneID;
			}
        }
    }
    print "Loaded $SynonymsMappings unique Gene synonym and $locusTagMappings unique LocusTag mappings from $geneFile.\n";
}

# given a gene ID and/or a gene symbol 
# try to return a useful gene ID
# except when ID < 0, indicating a mir, when the symbol is returned
# otherwise return undef

# process all gene lookups through this logic:
# some files contains protein coding genes and miRNAs
# since miRNA geneIDs are negative, we keep geneIDs for protein coding genes and symbols for miRNAs
# if ( both GeneID and GeneSymbol are provided ) {
#     if ( 0 <= GeneID ) {
#         validateGeneID;
#     } else {
#         use GeneSymbol;
#     }
# }
# if ( just GeneID is provided ) {
#     validateGeneID;
# }
# if ( just GeneSymbol is provided ) {
#     if ( gene_info maps GeneSymbol to GeneID ) {
#         use gene_info mapping;
#     } else {
#         ignore data, return undef;
#     }
# }
# 
# validateGeneID:
# if ( GeneID < 0 or GeneID is in gene_info ) {
#     use GeneID;
# } else {
#     if ( GeneSymbol maps to a GeneID ) {
#         use gene_info mapping;
#     } else {
#         ignore data, return undef;
#     }
# }
sub getGeneID{
    my( $self, $symbol, $ID ) = @_;
    if( defined($symbol) && defined($ID) ){
    	if( 0 <= $ID ){
    		return $self->_validateGeneID( $symbol, $ID  );
    	}else{
            return $symbol;
    	}
    }
    if( defined($ID) ){
    	return $self->_validateGeneID( $symbol, $ID  );
    }
    if( defined($symbol) ){
    	return $self->{GeneSymboltoGeneID}->{$symbol};  
    }
    warn "Neither Gene Symbol nor GeneID are defined.";
    return undef;
}

# given a gene ID and perhaps a gene symbol return good gene ID
# try to return a valid gene ID, even in the situation when the ID doesn't map to a symbol
# otherwise return undef
sub _validateGeneID{
    my( $self, $symbol, $ID ) = @_;
    my $GeneIDtoGeneSymbol = $self->{GeneIDtoGeneSymbol}; 
    my $GeneSymboltoGeneID = $self->{GeneSymboltoGeneID};
    unless( defined( $ID )){
        warn "GeneID not defined.";
        return undef;
    }
	if( $ID < 0 || exists( $GeneIDtoGeneSymbol->{$ID} ) ){
		return $ID;
	}else{
		
		# if ID is blank (empty string) or 0, try to recover it from Symbol
		# e.g., # A couple dozen of the rows in OV.maf.annotated have blank or 0 for the Gene ID
		if( defined( $symbol ) && exists( $GeneSymboltoGeneID->{$symbol} ) ){
			# print "recover ID from $symbol: ", $GeneSymboltoGeneID->{$symbol}, "\n"; 
			return( $GeneSymboltoGeneID->{$symbol} )
		}else{
			# print "cannot recover ID from $symbol\n"; 
			return undef;
		}
	}
}

# given a file containing gene names and a list name, load the gene list
# then hasGeneID( name, geneID ) will indicate whether the gene list called name contains the gene
# and similarly for hasGeneName( name, geneName ) 
sub loadGeneList{
	my( $self, $geneFile, $listname ) = @_;

    my $fileUtil = File::Util->new();
    unless( -r  $geneFile ){
    	my($package, $filename, $line, $subr)= caller(0);
        warn "$package\:\:$subr: Cannot read gene file: '$geneFile'.\n";
        return undef;
    }
    my @genes = $fileUtil->load_file( $geneFile, '--as-lines' );

    # for this gene list, create a hash of gene IDs
    $self->{GeneLists}->{$listname} = {};
    my $thisList = $self->{GeneLists}->{$listname};
    foreach my $gene (@genes){
    	if( $self->getGeneID( $gene ) ){
    		$thisList->{ $self->getGeneID( $gene ) } = 1;
    	}
    }
    print "loaded '$listname' with ", scalar( keys( %{$thisList}) ), " genes.\n";
}

# hasGeneList( name ) indicates whether gene list name exists
sub hasGeneList{
    my( $self, $listname ) = @_;
    if( exists( $self->{GeneLists}->{$listname} ) ){
    	return 1;
    }else{
    	return undef;
    }
}

# hasGeneID( name, geneID ) indicates whether gene list name contains a gene with geneID
sub hasGeneID{
    my( $self, $listname, $geneID ) = @_;
    return $self->_hasGeneIDorName( $listname, $geneID );
}

# hasGeneName( name, geneName ) indicates whether gene list name contains a gene with geneName
sub hasGeneName{
    my( $self, $name, $geneName ) = @_;
    return $self->_hasGeneIDorName( $name, undef, $geneName );
}

sub _hasGeneIDorName{
    my( $self, $listname, $geneID, $geneName ) = @_;
    unless( defined($listname) ){
        return undef;
    }
    unless( exists( $self->{GeneLists}->{$listname} )){
        return undef;
    }
    my $thisList = $self->{GeneLists}->{$listname};

    if( defined( $geneID ) ){
        return $thisList->{$geneID};
    }
    if( defined( $geneName ) ){
    	if( $self->getGeneID( $geneName ) ){
            return $thisList->{  $self->getGeneID( $geneName ) };
    	}
    }
    return undef;
}

1;