package org.mskcc.cbio.importer.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 * <p/>
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.
 * <p/>
 * Created by criscuof on 1/14/15.
 */
public interface MetadataCommonNames {

    public static final String Worksheet_TumorTypes = "tumor_types";
    public static final String Worksheet_TumorTypesOncotree = "tumor_types_oncotree";
    public static final String Worksheet_OncotreeSrc = "oncotree_src";
    public static final String Workseet_OncotreeProperties = "oncotree_properties";
    public static final String Worksheet_TCGATumorTypes ="tcga_tumor_types";
    public static final String Worksheet_Oncotree = "oncotree";
    public static final String Worksheet_Datatypes = "datatypes";
    public static final String Worksheet_NormalDatatypes = "normal_datatypes";
    public static final String Worksheet_CancerStudies= "cancer_studies";
    public static final String Worksheet_CaseIdFilters = "case_id_filters";
    public static final String Worksheet_DataSources = "data_sources";
    public static final String Worksheet_ReferenceData = "reference_data";
    public static final String Worksheet_CaseLists = "case_lists";
    public static final String Worksheet_ClinicalAttributesNamespace = "clinical_attributes_namespace";
    public static final String Worksheet_ClinicalAttributes = "clinical_attributes";
    public static final String Worksheet_Portals = "portals";
    public static final String Worksheet_Foundation = "foundation";
    public static final String Worksheet_ICGC = "icgc";



    public static final ImmutableMap<String,String> idColumnMap = new ImmutableMap.Builder<String, String>()
            .put(Worksheet_TumorTypes,"tumortype")
            .put(Worksheet_TumorTypesOncotree,"tumortype")
            .put(Worksheet_OncotreeSrc,"")
            .put(Workseet_OncotreeProperties,"")
            .put(Worksheet_TCGATumorTypes,"")
            .put(Worksheet_Oncotree,"")
            .put(Worksheet_CancerStudies,"cancerstudies")
            .build();






}
