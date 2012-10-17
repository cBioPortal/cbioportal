package CaseIDs;
require Exporter;
@ISA = qw(Exporter);
@EXPORT = qw(convertCaseID convert_case_ID_headers tumorCaseID matchedNormalCaseID 
    normalTissueCaseID recurrentTumorCaseID tcgaHeaderPattern getCaseCount );

use File::Spec;
use strict;
use warnings;
use Data::Dumper;

# SUMMARY
# Match and convert case IDs (CaseIDs.pm)
# Identify case ID (aka sample bar code) patterns
# Convert TCGA case IDs to MSK id format
# Issue: New TCGA case format being deployed
# Testing: Full coverage

# pattern constants, so they're not duplicated
# todo: convert these to lists of IDs and pattern transformations for each category
my $tumorPatternStandard = '^(TCGA-\w\w-\w\w\w\w)\-01[A-Q].*$'; # A-Q recognizes all sample sequences up to R for recurrent
my $tumorPatternJunky = '.*(\w\w\-\w\d\d\w)(-|-D-|-DN-|_DN-)Tumor$'; # junky Jan 2011 pattern
my $tumorPatternOvMAF = '^(TCGA-\w\w-\w\w\w\w)\-01$'; # ov MAF pattern, at least in Jan 2011, e.g., TCGA-13-1481-01
my $tumorPatternMelMAF = '^(TCGA-\w\w-\w\w\w\w)\-06$'; # melanoma MAF pattern
my $truncatedCaseID = '^(TCGA-\w\w-\w\w\w\w)$'; # a truncated case ID, as appeared in Dec 2010 maf files, we assume these are tumor IDs

my $normalBloodPatternStandard = '^(TCGA-\w\w-\w\w\w\w)\-10'; # normal blood sample, correct, as truncated in MAF files 
my $normalBloodPatternJunky = '.*(\w\w\-\d\d\d\d)-Normal$'; # bad Jan 2011 pattern, in some MAF files; e.g., BCM-READ-AF-2689-Normal

my $normalTissueCaseID = '^(TCGA-\w\w-\w\w\w\w\-11)';

my $recurrentTumorCaseID = '^(TCGA-\w\w-\w\w\w\w\-02)|^(TCGA-\w\w-\w\w\w\w\-01R)';

# mskcc studies
my $tumorPatternProstateMSKCC = '^(PCA\d\d\d\d)$';
my $tumorPatternSarcomaMSKCC = '^(PT\w+)$';
my $tumorPatternBladderMSKCC = '^(BL\w+)$';
my $tumorPatternBreastScand = '^(BC\w+)$';

# convert case ID to MSKCC format
# truncate case IDs to TCGA-xx-xxxx
# except leave sample type on recurrent tumors and normal tissue 
# originals are TCGA-xx-xxxx-xxx-xxx-xxxx-xx
sub convertCaseID{
    # Official info on CaseIDs (aka Barcodes) from the NCI Wiki 
    # https://wiki.nci.nih.gov/display/TCGA/A+-+Aliquot+Barcode+Values
    # 
    # The main parts of the barcode are TCGA-xx-xxxx-xxx-xxx-xxxx-xx
    # (1)-(2)-(3)-(4)(5)-(6)(7)-(8)-(9)
    # 
    #  (1) Project = The project the ID is associated with - currently only TCGA
    #  (2) Collection Center = BCR sample collection site (e.g. 02=MD Anderson - each site will get a different ID for each tumor type)
    #  (3) Patient = Patient ID
    #  (4) Sample Type = e.g. solid tumor (01) or normal blood (10)
    #  (5) Sample Sequence = The number of samples from the same patient for a sample type (e.g. 'A' is first sample)
    #  (6) Portion Sequence = The number of portions from a sample
    #  (7) Portion Analyte = The type of analyte (D=DNA, R=RNA, T=total RNA, W=whole genome amplified)
    #  (8) Plate ID = The plate that an aliquot or a portion was placed on
    #  (9) Data Generating Center ID = The center that a plate was sent to
    # 
    # The different sample types for (4) are:
    # 01  solid tumor
    # 02  recurrent tumor (originally 01R, but this terminology was retired)
    # 10  normal blood (matched normal used for copy-number and sequencing)
    # 11  normal tissue (not always matched to a cancer patient, used for mRNA, microRNA, methylation) 
    # 12  buccal smear
    # 20  cell line 
    my( $caseID ) = @_;
    my $rv;
    
    # should not see normal blood
#     
#     if( $caseID =~ /$normalBloodPatternJunky/ ){ 
#         $rv = 'TCGA-' . $1 . '-11';
#     }

#    if( $caseID =~ /$normalBloodPatternStandard/ ){ 
#        $rv = $1;
#    }

    if( $caseID =~ /$tumorPatternStandard/ ){ 
        $rv = $1;
    }

    if( $caseID =~ /$tumorPatternJunky/ ){ 
        $rv = 'TCGA-' . $1;
    }

    if( $caseID =~ /$truncatedCaseID/ ){ 
        $rv = $1;
    }
    
    if( $caseID =~ /$tumorPatternOvMAF/ ){ 
        $rv = $1;
    }

    if( $caseID =~ /$tumorPatternMelMAF/ ){ 
        $rv = $1;
    }

    if( $caseID =~ /$tumorPatternProstateMSKCC/ ){ 
        $rv = $1;
    }

    if( $caseID =~ /$tumorPatternSarcomaMSKCC/ ){ 
        $rv = $1;
    }

    if( $caseID =~ /$tumorPatternBladderMSKCC/ ){ 
        $rv = $1;
    }

    if( $caseID =~ /$tumorPatternBreastScand/ ){ 
        $rv = $1;
    }

    if( $caseID =~ /$normalTissueCaseID/ ){ 
        $rv = $1;
    }

    if( $caseID =~ /$recurrentTumorCaseID/ ){ 
    	if( defined( $1 )){
            $rv = $1;
    	}else{
    		if( defined( $2 )){
                $rv = $2;
    		}
    	}
    }

    if( defined( $rv ) ){
        # print "convertCaseID $caseID -> $rv\n";
        return $rv;
    }else{
        warn "Unrecognized sample ID: $caseID.\n";
        return undef;
    }
}

# return true if given a tumor case ID
sub tumorCaseID{
    my( $caseID ) = @_;
    return( 
        $caseID =~ /$tumorPatternJunky/ || 
        $caseID =~ /$tumorPatternStandard/ || 
        $caseID =~ /$truncatedCaseID/ ||
        $caseID =~ /$tumorPatternOvMAF/ ||
        $caseID =~ /$tumorPatternMelMAF/ ||
		$caseID =~ /$tumorPatternProstateMSKCC/ ||
		$caseID =~ /$tumorPatternSarcomaMSKCC/  ||
		$caseID =~ /$tumorPatternBladderMSKCC/  ||
		$caseID =~ /$tumorPatternBreastScand/); 
}

# sub to identify normal blood case-IDs
sub matchedNormalCaseID{
    my( $caseID ) = @_;
    return( $caseID =~ /$normalBloodPatternStandard/ || 
        $caseID =~ /$normalBloodPatternJunky/ );
}

# return true if given a normal tissue case-ID
sub normalTissueCaseID{
    my( $caseID ) = @_;
    return( $caseID =~ /$normalTissueCaseID/ ); 
}

# return true if given a recurrent Tumor case ID
sub recurrentTumorCaseID{
    my( $caseID ) = @_;
    return( $caseID =~ /$recurrentTumorCaseID/ ); 
}

# convert case-ID headers, deleting columns with duplicate converted names
sub convert_case_ID_headers{
    my( $filename, $cTable ) = @_;
    
    # for error output
    my($volume,$directories,$file) = File::Spec->splitpath( $filename );
    
    foreach my $f ( @{$cTable->fieldlist()} ){

        if( tumorCaseID( $f ) ){
		  my $convertedCaseID = convertCaseID($f);
            if( defined( $cTable->{ $convertedCaseID } ) && ($f ne $convertedCaseID) ){
                # delete column
                warn "Deleting column with duplicate case $f in $file.\n";
                $cTable->col_delete( $f );      
            }else{
            	# print "renaming $f to ", convertCaseID( $f ), "\n";
                $cTable->col_rename( $f => convertCaseID( $f ) );
            }
        }
    }
}

# returns numbers of cases in cTable
sub getCaseCount{
  my( $cTable ) =  @_;

  my $toReturn = 0;
  foreach my $f ( @{$cTable->fieldlist()} ){
	if( tumorCaseID( $f ) ){
	  $toReturn++;
	}
  }

  return $toReturn;
}

1;
