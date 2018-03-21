package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.meta.BaseMeta;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class ClinicalAttributeMyBatisRepositoryTest {

    @Autowired
    private ClinicalAttributeMyBatisRepository clinicalAttributeMyBatisRepository;

    @Test
    public void getAllClinicalAttributesIdProjection() throws Exception {

        List<ClinicalAttribute> result = clinicalAttributeMyBatisRepository.getAllClinicalAttributes("ID", null, null,
                null, null);

        Assert.assertEquals(28, result.size());
        ClinicalAttribute clinicalAttribute = result.get(0);
        Assert.assertEquals("DAYS_TO_COLLECTION", clinicalAttribute.getAttrId());
        Assert.assertEquals("study_tcga_pub", clinicalAttribute.getCancerStudyIdentifier());
    }

    @Test
    public void getAllClinicalAttributesSummaryProjection() throws Exception {

        List<ClinicalAttribute> result = clinicalAttributeMyBatisRepository.getAllClinicalAttributes("SUMMARY", null,
                null, null, null);

        Assert.assertEquals(28, result.size());
        ClinicalAttribute clinicalAttribute = result.get(0);
        Assert.assertEquals("RETROSPECTIVE_COLLECTION", clinicalAttribute.getAttrId());
        Assert.assertEquals("study_tcga_pub", clinicalAttribute.getCancerStudyIdentifier());
        Assert.assertEquals((Integer) 1, clinicalAttribute.getCancerStudyId());
        Assert.assertEquals("STRING", clinicalAttribute.getDatatype());
        Assert.assertEquals("Text indicator for the time frame of tissue procurement,indicating that the tissue was " +
                "obtained and stored prior to the initiation of the project.", clinicalAttribute.getDescription());
        Assert.assertEquals("Tissue Retrospective Collection Indicator", clinicalAttribute.getDisplayName());
        Assert.assertEquals("1", clinicalAttribute.getPriority());
        Assert.assertEquals(true, clinicalAttribute.getPatientAttribute());
    }

    @Test
    public void getAllClinicalAttributesDetailedProjection() throws Exception {

        List<ClinicalAttribute> result = clinicalAttributeMyBatisRepository.getAllClinicalAttributes("DETAILED", null,
                null, null, null);

        Assert.assertEquals(28, result.size());
        ClinicalAttribute clinicalAttribute = result.get(0);
        Assert.assertEquals("RETROSPECTIVE_COLLECTION", clinicalAttribute.getAttrId());
        Assert.assertEquals("study_tcga_pub", clinicalAttribute.getCancerStudyIdentifier());
        Assert.assertEquals((Integer) 1, clinicalAttribute.getCancerStudyId());
        Assert.assertEquals("STRING", clinicalAttribute.getDatatype());
        Assert.assertEquals("Text indicator for the time frame of tissue procurement,indicating that the tissue was " +
                "obtained and stored prior to the initiation of the project.", clinicalAttribute.getDescription());
        Assert.assertEquals("Tissue Retrospective Collection Indicator", clinicalAttribute.getDisplayName());
        Assert.assertEquals("1", clinicalAttribute.getPriority());
        Assert.assertEquals(true, clinicalAttribute.getPatientAttribute());
    }

    @Test
    public void getAllClinicalAttributesSummaryProjection1PageSize() throws Exception {

        List<ClinicalAttribute> result = clinicalAttributeMyBatisRepository.getAllClinicalAttributes("SUMMARY", 1, 0,
                null, null);

        Assert.assertEquals(1, result.size());
    }

    @Test
    public void getAllClinicalAttributesSummaryProjectionDisplayNameSort() throws Exception {

        List<ClinicalAttribute> result = clinicalAttributeMyBatisRepository.getAllClinicalAttributes("SUMMARY", null,
                null, "displayName", "ASC");

        Assert.assertEquals(28, result.size());
        Assert.assertEquals("Days to Sample Collection.", result.get(0).getDisplayName());
        Assert.assertEquals("Days to Sample Collection.", result.get(1).getDisplayName());
        Assert.assertEquals("Disease Free (Months)", result.get(2).getDisplayName());
        Assert.assertEquals("Disease Free (Months)", result.get(3).getDisplayName());
        Assert.assertEquals("Disease Free Status", result.get(4).getDisplayName());
        Assert.assertEquals("Disease Free Status", result.get(5).getDisplayName());
    }

    @Test
    public void getMetaClinicalAttributes() throws Exception {

        BaseMeta result = clinicalAttributeMyBatisRepository.getMetaClinicalAttributes();

        Assert.assertEquals((Integer) 28, result.getTotalCount());
    }

    @Test
    public void getClinicalAttributeNullResult() throws Exception {

        ClinicalAttribute result = clinicalAttributeMyBatisRepository.getClinicalAttribute("study_tcga_pub", 
            "invalid_clinical_attribute");

        Assert.assertNull(result);
    }

    @Test
    public void getClinicalAttribute() throws Exception {

        ClinicalAttribute result = clinicalAttributeMyBatisRepository.getClinicalAttribute("study_tcga_pub", 
            "RETROSPECTIVE_COLLECTION");
        
        Assert.assertEquals("RETROSPECTIVE_COLLECTION", result.getAttrId());
        Assert.assertEquals("study_tcga_pub", result.getCancerStudyIdentifier());
        Assert.assertEquals((Integer) 1, result.getCancerStudyId());
        Assert.assertEquals("STRING", result.getDatatype());
        Assert.assertEquals("Text indicator for the time frame of tissue procurement,indicating that the tissue was " +
            "obtained and stored prior to the initiation of the project.", result.getDescription());
        Assert.assertEquals("Tissue Retrospective Collection Indicator", result.getDisplayName());
        Assert.assertEquals("1", result.getPriority());
        Assert.assertEquals(true, result.getPatientAttribute());
    }

    @Test
    public void getAllClinicalAttributesInStudySummaryProjection() throws Exception {

        List<ClinicalAttribute> result = clinicalAttributeMyBatisRepository.getAllClinicalAttributesInStudy(
            "study_tcga_pub", "SUMMARY", null, null, null, null);

        Assert.assertEquals(14, result.size());
        ClinicalAttribute clinicalAttribute = result.get(0);
        Assert.assertEquals("RETROSPECTIVE_COLLECTION", clinicalAttribute.getAttrId());
        Assert.assertEquals("study_tcga_pub", clinicalAttribute.getCancerStudyIdentifier());
        Assert.assertEquals((Integer) 1, clinicalAttribute.getCancerStudyId());
        Assert.assertEquals("STRING", clinicalAttribute.getDatatype());
        Assert.assertEquals("Text indicator for the time frame of tissue procurement,indicating that the tissue was " +
            "obtained and stored prior to the initiation of the project.", clinicalAttribute.getDescription());
        Assert.assertEquals("Tissue Retrospective Collection Indicator", clinicalAttribute.getDisplayName());
        Assert.assertEquals("1", clinicalAttribute.getPriority());
        Assert.assertEquals(true, clinicalAttribute.getPatientAttribute());
    }

    @Test
    public void getMetaClinicalAttributesInStudy() throws Exception {

        BaseMeta result = clinicalAttributeMyBatisRepository.getMetaClinicalAttributesInStudy("study_tcga_pub");

        Assert.assertEquals((Integer) 14, result.getTotalCount());

    }

    @Test
    public void getAllClinicalAttributesInStudyWithStatsCheckCount() throws Exception {

        List<String> studyId = new ArrayList<>();
        studyId.add("study_tcga_pub");
        studyId.add("study_tcga_pub");
        List<String> sampleIds = new ArrayList<String>();
        sampleIds.add("TCGA-A1-A0SB-01");
        sampleIds.add("TCGA-A1-A0SD-01");
        List<ClinicalAttribute> result = clinicalAttributeMyBatisRepository
                .getAllClinicalAttributesInStudiesBySampleIds(studyId, sampleIds, "DETAILED", null, null);

        Assert.assertEquals(14, result.size());
        ClinicalAttribute clinicalAttributeWithCountSample = result.get(0);
        Assert.assertEquals("DAYS_TO_COLLECTION", clinicalAttributeWithCountSample.getAttrId());
        Assert.assertEquals("study_tcga_pub", clinicalAttributeWithCountSample.getCancerStudyIdentifier());
        Assert.assertEquals((Integer) 1, clinicalAttributeWithCountSample.getCancerStudyId());
        Assert.assertEquals("STRING", clinicalAttributeWithCountSample.getDatatype());
        Assert.assertEquals("Days to sample collection.", clinicalAttributeWithCountSample.getDescription());
        Assert.assertEquals("Days to Sample Collection.", clinicalAttributeWithCountSample.getDisplayName());
        Assert.assertEquals("1", clinicalAttributeWithCountSample.getPriority());
        Assert.assertEquals(false, clinicalAttributeWithCountSample.getPatientAttribute());
        Assert.assertEquals((Integer) 1, clinicalAttributeWithCountSample.getCount());
        Assert.assertEquals(14, result.size());
        ClinicalAttribute clinicalAttributeWithCountPatient = result.get(12);
        Assert.assertEquals("RETROSPECTIVE_COLLECTION", clinicalAttributeWithCountPatient.getAttrId());
        Assert.assertEquals("study_tcga_pub", clinicalAttributeWithCountPatient.getCancerStudyIdentifier());
        Assert.assertEquals((Integer) 1, clinicalAttributeWithCountPatient.getCancerStudyId());
        Assert.assertEquals("STRING", clinicalAttributeWithCountPatient.getDatatype());
        Assert.assertEquals(
                "Text indicator for the time frame of tissue procurement,indicating that the tissue was obtained and stored prior to the initiation of the project.",
                clinicalAttributeWithCountPatient.getDescription());
        Assert.assertEquals("Tissue Retrospective Collection Indicator",
                clinicalAttributeWithCountPatient.getDisplayName());
        Assert.assertEquals("1", clinicalAttributeWithCountPatient.getPriority());
        Assert.assertEquals(true, clinicalAttributeWithCountPatient.getPatientAttribute());
        Assert.assertEquals((Integer) 1, clinicalAttributeWithCountPatient.getCount());

    }

    @Test
    public void getAllClinicalAttributesInStudyWithStatsCheckCountWhenEmptyColumn() throws Exception {

        List<String> studyId = new ArrayList<>();
        studyId.add("study_tcga_pub");
        studyId.add("study_tcga_pub");
        List<String> sampleIds = new ArrayList<String>();
        sampleIds.add("TCGA-A1-A0SB-01");
        sampleIds.add("TCGA-A1-A0SD-01");
        List<ClinicalAttribute> result = clinicalAttributeMyBatisRepository
                .getAllClinicalAttributesInStudiesBySampleIds(studyId, sampleIds, "DETAILED", null, null);

        Assert.assertEquals(14, result.size());
        ClinicalAttribute clinicalAttributeWithCount = result.get(1);
        Assert.assertEquals("DFS_MONTHS", clinicalAttributeWithCount.getAttrId());
        Assert.assertEquals("study_tcga_pub", clinicalAttributeWithCount.getCancerStudyIdentifier());
        Assert.assertEquals((Integer) 1, clinicalAttributeWithCount.getCancerStudyId());
        Assert.assertEquals("NUMBER", clinicalAttributeWithCount.getDatatype());
        Assert.assertEquals("Disease free (months) since initial treatment.",
                clinicalAttributeWithCount.getDescription());
        Assert.assertEquals("Disease Free (Months)", clinicalAttributeWithCount.getDisplayName());
        Assert.assertEquals("1", clinicalAttributeWithCount.getPriority());
        Assert.assertEquals(true, clinicalAttributeWithCount.getPatientAttribute());
        Assert.assertEquals((Integer) 0, clinicalAttributeWithCount.getCount());
    }

    @Test
    public void fetchClinicalAttributes() throws Exception {

        List<ClinicalAttribute> result = clinicalAttributeMyBatisRepository.fetchClinicalAttributes(
            Arrays.asList("acc_tcga", "study_tcga_pub"), "SUMMARY");

        Assert.assertEquals(28, result.size());
        ClinicalAttribute clinicalAttribute = result.get(0);
        Assert.assertEquals("RETROSPECTIVE_COLLECTION", clinicalAttribute.getAttrId());
        Assert.assertEquals("acc_tcga", clinicalAttribute.getCancerStudyIdentifier());
        Assert.assertEquals((Integer) 2, clinicalAttribute.getCancerStudyId());
        Assert.assertEquals("STRING", clinicalAttribute.getDatatype());
        Assert.assertEquals("Text indicator for the time frame of tissue procurement,indicating that the tissue was " +
            "obtained and stored prior to the initiation of the project.", clinicalAttribute.getDescription());
        Assert.assertEquals("Tissue Retrospective Collection Indicator", clinicalAttribute.getDisplayName());
        Assert.assertEquals("1", clinicalAttribute.getPriority());
        Assert.assertEquals(true, clinicalAttribute.getPatientAttribute());
    }

    public void fetchMetaClinicalAttributes() throws Exception {

        BaseMeta result = clinicalAttributeMyBatisRepository.fetchMetaClinicalAttributes(Arrays.asList("acc_tcga", 
            "study_tcga_pub"));

        Assert.assertEquals((Integer) 28, result.getTotalCount());
    }
}
