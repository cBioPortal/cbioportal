package org.cbioportal.persistence.spark.util;

public class ParquetConstants {
    // Parquet file names
    public static final String DATA_MUTATIONS = "data_mutations*.txt.parquet";
    public static final String CLINICAL_SAMPLE = "data_clinical_sample.txt.parquet";
    public static final String CLINICAL_PATIENT = "data_clinical_patient.txt.parquet";
    public static final String CNA_SEG = "data_cna_hg19.seg.parquet";
}
