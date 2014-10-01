/*
 * A collection of constant vlaues reference by classes throughout the application
 */
package org.mskcc.cbio.importer.foundation.support;

/**
 *
 * @author criscuof
 */
public interface CommonNames {
    
    
    public static final String[]  MUTATIONS_REPORT_HEADINGS = {"Hugo_Symbol", "Center", "NCBI_Build","Chromosome","Start_Position",	
            "End_Position","Strand","Variant_Classification", "Reference_Allele","Tumor_Seq_Allele1","Tumor_Seq_Allele2","Tumor_Sample_Barcode",
            "Validation_Status","Mutation_Status","Amino_Acid_Change","Transcript","t_ref_count","t_alt_count"
        
    };
    public static final String[] CLINICAL_DATA_HEADINGS = { "SAMPLE_ID","GENDER",   "FMI_CASE_ID", "PIPELINE_VER",
        						"TUMOR_NUCLEI_PERCENT", "MEDIAN_COV", "COV>100X", "ERROR_PERCENT" };
    public static final String[] FUSION_DATA_HEADINGS = {"Hugo_Symbol","Entrez_Gene_Id","Center","Tumor_Sample_Barcode","Fusion","DNA support","RNA support","Method","Frame"};

    public static final String CNA_AMPLIFICATION = "amplification";
    public static final String CNA_LOSS = "loss";
    public static final String CENTER_FOUNDATION = "foundation";
    public static final String DEFAULT_FUSION ="fusion";
    public static final String DEFAULT_DNA_SUPPORT ="yes";
    public static final String DEFAULT_RNA_SUPPORT ="unknown";
    public static final String DEFAULT_TUMOR_SAMPLE_BARCODE ="unknown";
    public static final String DEFAULT_VALIDATION_STATUS ="unknown";
    public static final String DEFAULT_FUSION_METHOD = "NA";
    public static final String OUT_OF_FRAME = "out of frame";
    public static final String IN_FRAME = "in-frame";
    public static final String UNKNOWN = "unknown";
    public static final String BUILD = "37"; 
    public static final String CHR_PREFIX = "chr";
    public static final String MINUS_STRAND = "-";
    public static final String PLUS_STRAND = "+";
    // metric names
    public static final String METRIC_ERROR = "Error";
    public static final String METRIC_COVERAGE_GT_100 ="Coverage >100X";
    public static final String METRIC_MEDIAN_COVERAGE = "Median coverage";
    public static final String METRIC_TUMOR_NUCLEI_PERCENT = "Tumor Nuclei Percent";
    
    public static final String FOUNDATION_DATA_SOURCE_NAME  = "foundation";
    

    public static final String DEFAULT_FOUNDATION_OUTPUT_BASE = "/tmp/foundation";

    // standard header column names
    public static final String HUGO_SYMBOL = "Hugo_Symbol";
    public static final String ENTREZ_GENE_ID = "Entrez_Gene_Id";
    public static final String NCBI_BUILD = "NCBI_Build";
    public static final String CHROMOSOME = "Chromosome";
    public static final String START_POSITION = "Start_Position";
    public static final String END_POSITION = "End_Position";
    public static final String STRAND = "Strand";
    public static final String VARIANT_CLASSIFICATION = "Variant_Classification";
    public static final String VARIANT_TYPE = "Variant_Type";
    public static final String REFERENCE_ALLELE = "Reference_Allele";
    public static final String TUMOR_SEQ_ALLELE_1 = "Tumor_Seq_Allele1";
    public static final String TUMOR_SEQ_ALLELE_2 = "Tumor_Seq_Allele2";
    public static final String DBSNP_RS = "dbSNP_RS";
    public static final String DBSNP_VAL_STATUS = "dbSNP_Val_Status";
    public static final String TUMOR_SAMPLE_BARCODE = "Tumor_Sample_Barcode";
    public static final String MATCHED_NORM_SAMPLE_BARCODE = "Matched_Norm_Sample_Barcode";
    public static final String MATCH_NORM_SEQ_ALLELE1 = "Match_Norm_Seq_Allele1";
    public static final String MATCH_NORM_SEQ_ALLELE2 = "Match_Norm_Seq_Allele2";
    public static final String TUMOR_VALIDATION_ALLELE1 = "Tumor_Validation_Allele1";
    public static final String TUMOR_VALIDATION_ALLELE2 = "Tumor_Validation_Allele2";
    public static final String MATCH_NORM_VALIDATION_ALLELE1 = "Match_Norm_Validation_Allele1";
    public static final String MATCH_NORM_VALIDATION_ALLELE2 = "Match_Norm_Validation_Allele2";
    public static final String VERIFICATION_STATUS = "Verification_Status";
    public static final String VALIDATION_STATUS = "Validation_Status";
    public static final String MUTATION_STATUS = "Mutation_Status";
    public static final String SEQUENCING_PHASE = "Sequencing_Phase";
    public static final String SEQUENCE_SOURCE = "Sequence_Source";
    public static final String VALIDATION_METHOD = "Validation_Method";
    public static final String SCORE = "Score";
    public static final String BAM_FILE = "BAM_File";
    public static final String SEQUENCER = "Sequencer";
    public static final String T_REF_COUNT = "t_ref_count";
    public static final String T_ALT_COUNT = "t_alt_count";
    public static final String AMINO_ACID_CHANGE_MANNUAL = "Amino_Acid_Change";
    public static final String TRANSCRIPT = "Transcript";
}
