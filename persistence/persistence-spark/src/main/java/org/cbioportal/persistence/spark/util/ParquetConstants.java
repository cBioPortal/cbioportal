package org.cbioportal.persistence.spark.util;

public class ParquetConstants {

    // Parquet folder names
    public static final String CASE_LIST_DIR = "/case_lists/";
    public static final String STUDIES_DIR = "/studies/";
    public static final String GENE_PANEL_DIR = "/gene_panels/";
    // Parquet file names
    public static final String DATA_MUTATIONS = "mutations";
    public static final String DATA_CNA = "data_cna";
    public static final String CLINICAL_SAMPLE = "clinical_sample";
    public static final String CNA_SEG = "data_cna_*.seg";
    public static final String META = "meta";
    public static final String CLINICAL_PATIENT = "clinical_patient";
    public static final String GISTIC_GENES = "gistic_genes";
}
