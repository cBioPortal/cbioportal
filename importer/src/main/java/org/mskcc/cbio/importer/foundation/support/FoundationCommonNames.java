/*
 * A collection of constant vlaues reference by classes throughout the application
 */
package org.mskcc.cbio.importer.foundation.support;

/**
 *
 * @author criscuof
 */
public interface FoundationCommonNames {
   
    public static final String MUTATION_REPORT_TYPE = "mutationReport";
    public static final String CNA_REPORT_TYPE = "cnaReport";
    public static final String CLINICAL_REPORT_TYPE = "clinicalReport";
    public static final String FUSION_REPORT_TYPE = "fusionReport";

    public static final String[] MUTATIONS_REPORT_HEADINGS = { "Hugo_Symbol", "Entrez_Gene_Id", "Center",
            "NCBI_Build", "Chromosome", "Start_Position","End_Position", "Strand", "Variant_Classification", "Variant_Type",
            "Reference_Allele", "Tumor_Seq_Allele1", "Tumor_Seq_Allele2", "dbSNP_RS", "dbSNP_Val_Status",
            "Tumor_Sample_Barcode", "Matched_Norm_Sample_Barcode", "Match_Norm_Seq_Allele1", "Match_Norm_Seq_Allele2",
            "Tumor_Validation_Allele1", "Tumor_Validation_Allele2", "Match_Norm_Validation_Allele1",
            "Match_Norm_Validation_Allele2", "Verification_Status", "Validation_Status", "Mutation_Status",
            "Sequencing_Phase", "Sequence_Source", "Validation_Method", "Score", "BAM_File", "Sequencer",
              "Tumor_Sample_UUID" , "Matched_Norm_Sample_UUID","t_ref_count","t_alt_count"};


    public static final String[] CLINICAL_DATA_HEADINGS = { "SAMPLE_ID","GENDER",   "FMI_CASE_ID", "PIPELINE_VER",
        						"TUMOR_NUCLEI_PERCENT", "MEDIAN_COV", "COV>100X", "ERROR_PERCENT" };
    public static final String[] FUSION_DATA_HEADINGS = {"Hugo_Symbol","Entrez_Gene_Id","Center","Tumor_Sample_Barcode","Fusion","DNA support","RNA support","Method","Frame"};

    public static final String CNA_AMPLIFICATION = "amplification";

    public static final String CNA_LOSS = "loss";

    public static final String CENTER_FOUNDATION = "foundation";
    public static final String DEFAULT_FUSION ="fusion";
    public static final String DEFAULT_DNA_SUPPORT ="yes";
    public static final String DEFAULT_RNA_SUPPORT ="unknown";
    public static final String DEFAULT_MUTATION_STATUS ="unknown";
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
    

}
