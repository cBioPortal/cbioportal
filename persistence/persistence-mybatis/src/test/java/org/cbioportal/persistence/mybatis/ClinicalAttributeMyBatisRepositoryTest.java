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
}