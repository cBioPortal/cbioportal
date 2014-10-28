/*
 *  Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 * 
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 *  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 *  documentation provided hereunder is on an "as is" basis, and
 *  Memorial Sloan-Kettering Cancer Center 
 *  has no obligations to provide maintenance, support,
 *  updates, enhancements or modifications.  In no event shall
 *  Memorial Sloan-Kettering Cancer Center
 *  be liable to any party for direct, indirect, special,
 *  incidental or consequential damages, including lost profits, arising
 *  out of the use of this software and its documentation, even if
 *  Memorial Sloan-Kettering Cancer Center 
 *  has been advised of the possibility of such damage.
 */
package org.mskcc.cbio.importer.dmp.util;

/**
 * a collection of literal values used throughout the DMP importer application
 * @author criscuof
 */
public interface DMPCommonNames {
    public static final String REPORT_TYPE_CNV_INTRAGENIC = "CnvIntragenicVariant";
    public static final String REPORT_TYPE_CNV = "CnvVariant";
    public static final String REPORT_TYPE_METADATA = "Metadata";
    public static final String REPORT_TYPE_SNP_EXONIC = "SnpExonic";
    public static final String REPORT_TYPE_SNP_SILENT = "SnpSilent";
    public static final String REPORT_TYPE_TUMOR_TYPE = "TumorType";
    
    public static final String REPORT_TYPE_MUTATIONS  = "mutations";
    public static final String REPORT_TYPE_CNA  = "cna";   
    public static final String REPORT_TYPE_CLINICAL  = "clinical";  
    
    public static final String CENTER_MSKCC = "MSKCC";
    public static final String DEFAULT_BUILD_NUMBER ="37";
    public static final String DEFAULT_STRAND ="+";
    public static final String DEFAULT_SAMPLE_TYPE ="Tumor";
    
    public static final String SAMPLE_ID_COLUMN_NAME = "Tumor_Sample_Barcode";
    public static final String SEGMENT_ID_COLUMN_NAME = "ID";
    
    public static final String DMP_MUTATIONS_FILENAME = "data_mutations_exteneded.txt";
    public static final String DMP_CNV_FILENAME = "data_CNA.txt";
    public static final String DMP_CLINICAL_FILENAME = "data_clinical.txt";
    
    public static final Integer DMP_DATA_STATUS_NEW = 1;
    public static final Integer DMP_DATA_STATUS_RETRIEVAL = 11;
   
    
    public static final String IS_METASTASTIC_SITE = "1";
  
    
}
