#!/usr/bin/perl

use strict;
use warnings;
use File::Spec;
use File::Util;
use Data::Dumper;
use File::Temp qw/ tempfile /;

# root
my $rootDir = File::Spec->catfile( File::Spec->updir(), File::Spec->curdir(), qw( data ) );
# $rootDir = '/tmp/data';

my $fileUtil = File::Util->new();
my $cancers = File::Spec->catfile( $rootDir, qw( cancers.txt ) );
my @cancersFile = $fileUtil->load_file( $cancers, '--as-lines');
my $cancerTypes = {};
foreach my $cancer (@cancersFile){
	my( $ctype, $desc) = split( /:/, $cancer );
    $ctype =~ s/^\s+//;
    $ctype =~ s/\s+$//;
    $cancerTypes->{$ctype} = 1;
}

# find files
# for files of form (cancer_name/cancer_name).txt:
my @allFiles = $fileUtil->list_dir( $rootDir, '--recurse', '--files-only' );
my @studyFiles;
foreach my $f (@allFiles){
	if( $f =~ m|/(\w+).txt$| ){		
	    if(     $f =~ m|$1/$1.txt$| ){
	        push @studyFiles, $f;
	    }
	}
}

# cancer_type_id: BC => type_of_cancer: \1
# cancer_type_id: BC => cancer_study_identifier: \1_portal 
# check that cancer_name is in cancers
my $cnMap = {
	'BC' => 'brca',
	'Sarc' => 'sarc', 
	'ova' => 'ov',
	'pca' => 'PRAD'
};
foreach my $sf (@studyFiles){
	print "$sf\n";
    my $studyFile = $fileUtil->load_file( $sf );
    my $t = $studyFile;
    if( $studyFile =~ /cancer_type_id\s*:\s*(\w+)/ ){
    	my $ct = toCT( $1 );
    	my $stu_id = toCT( $1 ) . '_portal';
    	$studyFile =~ s/cancer_type_id\s*:\s*(\w+)/type_of_cancer: $ct\ncancer_study_identifier: $stu_id/;
        # print "changing:\n$t\nto:\n$studyFile\n";
        print "changing: '$sf' => type_of_cancer: $ct, cancer_study_identifier: $stu_id\n";
    }
    $fileUtil->write_file('file' => $sf, 'content' => $studyFile ); 
}

# cancer_type_id: BC => cancer_study_identifier: \1_portal 
foreach my $fn (@allFiles){
	open my $fh, "<", "$fn" or die $!;
    my ($tempFH, $tempFile) = tempfile();

    # file
    while( my $line = <$fh> ){
    	my $t = $line;
    	if( $line =~ /cancer_type_id\s*:\s*(\w+)/ ){
            my $stu_id = toCT( $1 ) . '_portal';
	        $line =~ s/cancer_type_id\s*:\s*(\w+)/cancer_study_identifier: $stu_id/;
	        print "changing: '$fn' => cancer_study_identifier: $stu_id\n";
#	        print "changing:\n$t\nto:\n$line\n";
    	}
    	print $tempFH $line;
    }

    close $fh;
    close $tempFH;
    system( "mv $tempFile $fn");
}

sub toCT{
	my( $ct ) = @_;
	if( $cnMap->{$ct} ){
	    $ct = $cnMap->{$ct};
	}
	$ct = uc( $ct );
	unless( exists( $cancerTypes->{$ct} ) ) {
	    warn "$ct not in cancer types\n";
	}
	return $ct;
}

## CHANGES:
# ../data/breast/breast.txt
# changing: '../data/breast/breast.txt' => type_of_cancer: BRCA, cancer_study_identifier: BRCA_portal
# ../data/gbm/gbm.txt
# changing: '../data/gbm/gbm.txt' => type_of_cancer: GBM, cancer_study_identifier: GBM_portal
# ../data/ovarian/ovarian.txt
# changing: '../data/ovarian/ovarian.txt' => type_of_cancer: OV, cancer_study_identifier: OV_portal
# ../data/prostate/prostate.txt
# changing: '../data/prostate/prostate.txt' => type_of_cancer: PRAD, cancer_study_identifier: PRAD_portal
# ../data/sarcoma/sarcoma.txt
# changing: '../data/sarcoma/sarcoma.txt' => type_of_cancer: SARC, cancer_study_identifier: SARC_portal
# changing: '../data/breast/case_lists/cases_DBCG.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/case_lists/cases_DBCG_mRNA.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/case_lists/cases_DCIS.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/case_lists/cases_DCIS_ERneg.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/case_lists/cases_DCIS_ERpos.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/case_lists/cases_FW.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/case_lists/cases_FW_mRNA.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/case_lists/cases_IDC_T1.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/case_lists/cases_IDC_T1_ERneg.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/case_lists/cases_IDC_T1_ERpos.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/case_lists/cases_IDC_T2.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/case_lists/cases_IDC_T2_ERneg.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/case_lists/cases_IDC_T2_ERpos.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/case_lists/cases_IDC_T3.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/case_lists/cases_IDC_T3_ERneg.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/case_lists/cases_IDC_T3_ERpos.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/case_lists/cases_MDG.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/case_lists/cases_MDG_mRNA.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/case_lists/cases_MicMa.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/case_lists/cases_MicMa_mRNA.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/case_lists/cases_Normal.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/case_lists/cases_PIK3CA_Sequenced.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/case_lists/cases_ULL.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/case_lists/cases_ULL_mRNA.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/case_lists/cases_all.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/case_lists/cases_tumors.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/meta_aCGH.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/meta_mRNA_DBCG.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/meta_mRNA_DBCG_Z.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/meta_mRNA_FW_MDG.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/meta_mRNA_MicMa.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/meta_mRNA_ULL.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/breast/meta_mutations.txt' => cancer_study_identifier: BRCA_portal
# changing: '../data/gbm/case_lists/cases_all.txt' => cancer_study_identifier: GBM_portal
# changing: '../data/gbm/case_lists/cases_all_three.txt' => cancer_study_identifier: GBM_portal
# changing: '../data/gbm/case_lists/cases_expression_classical.txt' => cancer_study_identifier: GBM_portal
# changing: '../data/gbm/case_lists/cases_expression_mesenchymal.txt' => cancer_study_identifier: GBM_portal
# changing: '../data/gbm/case_lists/cases_expression_neural.txt' => cancer_study_identifier: GBM_portal
# changing: '../data/gbm/case_lists/cases_expression_proneural.txt' => cancer_study_identifier: GBM_portal
# changing: '../data/gbm/case_lists/cases_seq_manuscript.txt' => cancer_study_identifier: GBM_portal
# changing: '../data/gbm/case_lists/cases_seq_no_hypermut.txt' => cancer_study_identifier: GBM_portal
# changing: '../data/gbm/case_lists/cases_seq_not_treated.txt' => cancer_study_identifier: GBM_portal
# changing: '../data/gbm/case_lists/cases_seq_treated.txt' => cancer_study_identifier: GBM_portal
# changing: '../data/gbm/data_clinical.txt' => cancer_study_identifier: GBM_portal
# changing: '../data/gbm/meta_CNA_RAE.txt' => cancer_study_identifier: GBM_portal
# changing: '../data/gbm/meta_CNA_consensus.txt' => cancer_study_identifier: GBM_portal
# changing: '../data/gbm/meta_mRNA.txt' => cancer_study_identifier: GBM_portal
# changing: '../data/gbm/meta_mRNA_ZbyDiploidTumors.txt' => cancer_study_identifier: GBM_portal
# changing: '../data/gbm/meta_miRNA.txt' => cancer_study_identifier: GBM_portal
# changing: '../data/gbm/meta_mutations.txt' => cancer_study_identifier: GBM_portal
# changing: '../data/gbm/meta_mutations_MAF.txt' => cancer_study_identifier: GBM_portal
# changing: '../data/ovarian/case_lists/cases_CGH.txt' => cancer_study_identifier: OV_portal
# changing: '../data/ovarian/case_lists/cases_all.txt' => cancer_study_identifier: OV_portal
# changing: '../data/ovarian/case_lists/cases_complete.txt' => cancer_study_identifier: OV_portal
# changing: '../data/ovarian/case_lists/cases_expression_cluster1.txt' => cancer_study_identifier: OV_portal
# changing: '../data/ovarian/case_lists/cases_expression_cluster2.txt' => cancer_study_identifier: OV_portal
# changing: '../data/ovarian/case_lists/cases_expression_cluster3.txt' => cancer_study_identifier: OV_portal
# changing: '../data/ovarian/case_lists/cases_expression_cluster4.txt' => cancer_study_identifier: OV_portal
# changing: '../data/ovarian/case_lists/cases_mRNA.txt' => cancer_study_identifier: OV_portal
# changing: '../data/ovarian/case_lists/cases_methylation_cluster1.txt' => cancer_study_identifier: OV_portal
# changing: '../data/ovarian/case_lists/cases_methylation_cluster2.txt' => cancer_study_identifier: OV_portal
# changing: '../data/ovarian/case_lists/cases_methylation_cluster3.txt' => cancer_study_identifier: OV_portal
# changing: '../data/ovarian/case_lists/cases_methylation_cluster4.txt' => cancer_study_identifier: OV_portal
# changing: '../data/ovarian/case_lists/cases_normal_mRNA.txt' => cancer_study_identifier: OV_portal
# changing: '../data/ovarian/case_lists/cases_protein.txt' => cancer_study_identifier: OV_portal
# changing: '../data/ovarian/case_lists/cases_sequenced.txt' => cancer_study_identifier: OV_portal
# changing: '../data/ovarian/meta_CNA.txt' => cancer_study_identifier: OV_portal
# changing: '../data/ovarian/meta_CNA_RAE.txt' => cancer_study_identifier: OV_portal
# changing: '../data/ovarian/meta_brca1_binary_methylation.txt' => cancer_study_identifier: OV_portal
# changing: '../data/ovarian/meta_mRNA_median.txt' => cancer_study_identifier: OV_portal
# changing: '../data/ovarian/meta_mRNA_median_Zscores.txt' => cancer_study_identifier: OV_portal
# changing: '../data/ovarian/meta_mRNA_unified.txt' => cancer_study_identifier: OV_portal
# changing: '../data/ovarian/meta_mRNA_unified_Zscores.txt' => cancer_study_identifier: OV_portal
# changing: '../data/ovarian/meta_methylation.txt' => cancer_study_identifier: OV_portal
# changing: '../data/ovarian/meta_miRNA.txt' => cancer_study_identifier: OV_portal
# changing: '../data/ovarian/meta_miRNA_median_Zscores.txt' => cancer_study_identifier: OV_portal
# changing: '../data/ovarian/meta_mutations.txt' => cancer_study_identifier: OV_portal
# changing: '../data/ovarian/meta_mutations_BRCA.txt' => cancer_study_identifier: OV_portal
# changing: '../data/ovarian/meta_mutations_extended.txt' => cancer_study_identifier: OV_portal
# changing: '../data/ovarian/meta_protein.txt' => cancer_study_identifier: OV_portal
# changing: '../data/prostate/case_lists/cases_all_list.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/case_lists/cases_all_three.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/case_lists/cases_cell_lines.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/case_lists/cases_cluster1.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/case_lists/cases_cluster2.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/case_lists/cases_cluster3.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/case_lists/cases_cluster4.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/case_lists/cases_cluster5.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/case_lists/cases_cluster6.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/case_lists/cases_copy_number_nonflat.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/case_lists/cases_flat.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/case_lists/cases_mRNA.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/case_lists/cases_mRNA_mets.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/case_lists/cases_mRNA_primaries.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/case_lists/cases_mRNA_tumors.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/case_lists/cases_metastatic.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/case_lists/cases_metastatic_castrate.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/case_lists/cases_metastatic_non-castrate.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/case_lists/cases_miRNA.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/case_lists/cases_normal_mRNA.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/case_lists/cases_primary.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/case_lists/cases_sequenced_all.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/case_lists/cases_sequenced_w_normal.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/case_lists/cases_tumors.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/meta_CNA.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/meta_mRNA.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/meta_mRNA_ZbyNorm.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/meta_mRNA_outliers.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/meta_miRNA.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/meta_miRNA_ZbyNorm.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/meta_miRNA_outliers.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/prostate/meta_mutations.txt' => cancer_study_identifier: PRAD_portal
# changing: '../data/sarcoma/case_lists/cases_DDLPS.txt' => cancer_study_identifier: SARC_portal
# changing: '../data/sarcoma/case_lists/cases_GIST.txt' => cancer_study_identifier: SARC_portal
# changing: '../data/sarcoma/case_lists/cases_LMS.txt' => cancer_study_identifier: SARC_portal
# changing: '../data/sarcoma/case_lists/cases_MFH.txt' => cancer_study_identifier: SARC_portal
# changing: '../data/sarcoma/case_lists/cases_MYXOID.txt' => cancer_study_identifier: SARC_portal
# changing: '../data/sarcoma/case_lists/cases_PLEO.txt' => cancer_study_identifier: SARC_portal
# changing: '../data/sarcoma/case_lists/cases_SYNOVIAL.txt' => cancer_study_identifier: SARC_portal
# changing: '../data/sarcoma/case_lists/cases_all.txt' => cancer_study_identifier: SARC_portal
# changing: '../data/sarcoma/case_lists/cases_all_tumors.txt' => cancer_study_identifier: SARC_portal
# changing: '../data/sarcoma/case_lists/cases_normal_fat.txt' => cancer_study_identifier: SARC_portal
# changing: '../data/sarcoma/meta_CNA.txt' => cancer_study_identifier: SARC_portal
# changing: '../data/sarcoma/meta_mrna.txt' => cancer_study_identifier: SARC_portal
# changing: '../data/sarcoma/meta_mrna_ZbyNormals.txt' => cancer_study_identifier: SARC_portal
# changing: '../data/sarcoma/meta_mrna_outlier.txt' => cancer_study_identifier: SARC_portal
# changing: '../data/sarcoma/meta_mutations.txt' => cancer_study_identifier: SARC_portal
