package org.mskcc.cbio.importer.icgc.support;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import java.util.Map;
import scala.Tuple2;
import scala.Tuple3;

/**
 * Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */
/**
 * responsible for supplying a Map of data_clinical attributes (keys) and
 * appropriate transformation functions
 *
 * @author criscuof
 */
public class ClinicalTransformationMapSupplier implements Supplier<Map<String, 
        Tuple3<
           Function<Tuple2<String, Optional<String>>,String>,
           String,
           Optional<String>>> 
        >{

    private static final Optional<String> absent = Optional.absent();
   
    

    public ClinicalTransformationMapSupplier() {
       
    }

    @Override
    public Map<String, 
        Tuple3<Function<Tuple2<String, Optional<String>>,String>,String,Optional<String>>> get() {
        Map<String, 
        Tuple3<Function<Tuple2<String, Optional<String>>,String>,String,Optional<String>>> transformationMap = Maps.newTreeMap();
       
        transformationMap.put("01SAMPLE_ID", new Tuple3<>(copyAttribute,"icgc_specimen_id",absent)); //1
        transformationMap.put("02PROJECT_CODE",new Tuple3<>( copyAttribute,"project_code",absent)); //2    
        transformationMap.put("03GENDER", new Tuple3<>(copyAttribute,"donor_sex",absent)); //3
        transformationMap.put("04DISEASE_STATUS",new Tuple3<>( copyAttribute,"disease_status_last_followup",absent)); //4
        transformationMap.put("05VITAL_STATUS", new Tuple3<>(copyAttribute,"donor_vital_status",absent)); //5
        transformationMap.put("06RELAPSE_TYPE", new Tuple3<>(copyAttribute,"donor_relapse_type",absent)); //6
        transformationMap.put("07AGE_AT_DIAGNOSIS", new Tuple3<>(copyAttribute,"donor_age_at_diagnosis",absent)); //7
        transformationMap.put("08AGE_AT_ENROLLMENT", new Tuple3<>(copyAttribute,"donor_age_at_enrollment",absent)); //8
        transformationMap.put("09AGE_AT_LAST_FOLLOWUP",new Tuple3<>( copyAttribute,"donor_age_at_last_followup",absent)); //9
        transformationMap.put("11RELAPSE_INTERVAL",new Tuple3<>( copyAttribute,"donor_relapse_interval",absent)); //110
        transformationMap.put("12DIAGNOSIS_ICD10", new Tuple3<>(copyAttribute, "donor_diagnosis_icd10",absent)); //11
        transformationMap.put("13TUMOR_STAGE_AT_DIAGNOSIS",new Tuple3<>( copyAttribute,"donor_tumour_stage_at_diagnosis",absent)); //13
        transformationMap.put("14SURVIVAL_TIME", new Tuple3<>(copyAttribute,"donor_survival_time",absent)); //14
        transformationMap.put("15INTERVAL_OF_LAST_FOLLOWUP", new Tuple3<>(copyAttribute,"interval_of_last_followup",absent)); //15
        transformationMap.put("16SUBMITTED_SPECIMEN_ID", new Tuple3<>(copyAttribute,"submitted_speciment_id",absent)); //16
        transformationMap.put("17TUMOR_CONFIRMED", new Tuple3<>(copyAttribute,"tumour_confirmed",absent)); //17
        transformationMap.put("18TUMOR_HISTOLOGICAL_TYPE", new Tuple3<>(copyAttribute,"tumour_histological_type",absent)); //18
        transformationMap.put("19TUMOR_GRADING_SYSTEM",new Tuple3<>( copyAttribute,"tumour_grading_system",absent)); //19
        transformationMap.put("20TUMOR_GRADE", new Tuple3<>(copyAttribute,"tumour_grade",absent));   //20
        transformationMap.put("21TUMOR_STAGE", new Tuple3<>(copyAttribute,"tumour_stage_system",absent));   //21
        transformationMap.put("22TUMOR_STAGE_SYSTEM",new Tuple3<>( copyAttribute,"tumour_stage",absent));   //22
        
        
        return transformationMap;
    }

    Function<Tuple2<String, Optional<String>>, String> copyAttribute
            = new Function<Tuple2<String, Optional<String>>, String>() {

                @Override
                /*
                 simple copy of ICGC attribute to MAF file
                 */
                public String apply(Tuple2<String, Optional<String>> f) {
                    if (null != f._1) {
                        return f._1;
                    }
                    return "";

                }

            };
    /*
     function to provide an empty string for unsupported MAF columns
     */
    Function<Tuple2<String, Optional<String>>, String> unsupported
            = new Function<Tuple2<String, Optional<String>>, String>() {

                @Override
                public String apply(Tuple2<String, Optional<String>> f) {
                    return "";
                }

            };

    
    

}
