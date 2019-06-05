package org.cbioportal.persistence.spark;

import org.cbioportal.model.*;
import org.cbioportal.persistence.PersistenceConstants;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testSparkContext.xml")
@Configurable
public class ClinicalDataSparkRepositoryTest {

    @Autowired
    private ClinicalDataSparkRepository clinicalDataSparkRepository;
    
    private static final List<String> STUDY_IDS = Arrays.asList("msk_impact_2017");
    private static final List<String> SAMPLE_ATTR_IDS = Arrays.asList("CANCER_TYPE",
        "CANCER_TYPE_DETAILED", "SAMPLE_TYPE", "MATCHED_STATUS", "METASTATIC_SITE", "ONCOTREE_CODE",
        "PRIMARY_SITE", "SAMPLE_CLASS", "SAMPLE_COLLECTION_SOURCE", "SPECIMEN_PRESERVATION_TYPE");
    private static final List<String> PATIENT_ATTR_IDS = Arrays.asList("SAMPLE_COUNT", "SEX", "OS_STATUS", "VITAL_STATUS", "SMOKING_HISTORY");
    
    @Test
    public void testFetchClinicalDataCountsSample() {

        List<ClinicalDataCount> res = clinicalDataSparkRepository
            .fetchClinicalDataCounts(STUDY_IDS, null, SAMPLE_ATTR_IDS, PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE);
        //METASTATIC_SITE 146 -> 152
        //PRIMARY_SITE 156 -> 159
        // MissingAttributeValues "Not available" + "Not Applicable" 6130 + 36 = 6166 NAs
        Assert.assertEquals(1102, res.size()); // 1111
    }

    @Test
    public void testFetchClinicalDataCountsPatient() {

        List<ClinicalDataCount> res = clinicalDataSparkRepository
            .fetchClinicalDataCounts(STUDY_IDS, null, PATIENT_ATTR_IDS, PersistenceConstants.PATIENT_CLINICAL_DATA_TYPE);
        Assert.assertEquals(17, res.size());
    }
    
    @Test
    public void testFetchClinicalData() {
        
        List<ClinicalData> res = clinicalDataSparkRepository
            .fetchClinicalData(STUDY_IDS, null, Arrays.asList("TUMOR_PURITY"), 
                PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE, "SUMMARY");

        Assert.assertEquals(10945, res.size());
    }
}
