package org.cbioportal.persistence.spark.util;

public class ParquetConstants {
    // Parquet directory names
    public static final String CASE_LIST_DIR = "/case_lists/";
    public static final String STUDIES_DIR = "/studies/";
    // Parquet file names
    public static final String DATA_MUTATIONS = "mutations";
    public static final String CLINICAL_SAMPLE = "clinical_sample";
    public static final String CLINICAL_PATIENT = "clinical_patient";
    public static final String CNA_SEG = "data_cna*.seg";
    public static final String META = "meta";
    
}