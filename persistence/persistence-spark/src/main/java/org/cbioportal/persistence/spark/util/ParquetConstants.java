package org.cbioportal.persistence.spark.util;

public class ParquetConstants {
    // Parquet file names
    public static final String DATA_MUTATIONS = "data_mutations*.txt.parquet";
    public static final String DATA_CLINICAL_SAMPLE = "data_clinical_sample.txt.parquet";
    public static final String CNA_SEG = "data_cna_hg19.seg.parquet";
    public static final String CASE_LIST_DIR = "/case_list/";
    public static final String GENE_PANEL_DIR = "/gene_panel/";
    // meta is assumed to have contain all meta_ files. Load with mergeSchema=true.
    public static final String META = "meta.parquet";
    public static final String GENE_MATRIX = "data_gene_matrix.parquet";
}
