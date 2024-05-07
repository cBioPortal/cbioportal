package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.ResourceData;
import org.cbioportal.model.ResourceDefinition;
import org.cbioportal.model.ResourceType;
import org.cbioportal.persistence.mybatis.config.TestConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {ResourceDataMyBatisRepository.class, TestConfig.class})
public class ResourceDataMyBatisRepositoryTest {
    
    @Autowired
    private ResourceDataMyBatisRepository resourceDataMyBatisRepository;
    
    @Test
    public void getAllResourceDataOfSampleInStudyIdProjection() throws Exception {
        
        List<ResourceData> result = resourceDataMyBatisRepository.getAllResourceDataOfSampleInStudy("study_tcga_pub", null, null, "ID", null, null, null, null);

        Assert.assertEquals(7, result.size());
        ResourceData resourceData = result.get(0);
        Assert.assertEquals("TCGA-A1-A0SB-01", resourceData.getSampleId());
        Assert.assertEquals("TCGA-A1-A0SB", resourceData.getPatientId());
        Assert.assertEquals("HE", resourceData.getResourceId());
        Assert.assertNull(resourceData.getUrl());
        Assert.assertNull(resourceData.getResourceDefinition());
    }

    @Test
    public void getAllResourceDataOfSampleInStudySummaryProjection() throws Exception {

        List<ResourceData> result = resourceDataMyBatisRepository.getAllResourceDataOfSampleInStudy("study_tcga_pub", null, null, "SUMMARY", null, null, null, null);

        Assert.assertEquals(7, result.size());
        ResourceData resourceData = result.get(0);
        Assert.assertEquals("TCGA-A1-A0SB-01", resourceData.getSampleId());
        Assert.assertEquals("TCGA-A1-A0SB", resourceData.getPatientId());
        Assert.assertEquals("HE", resourceData.getResourceId());
        Assert.assertEquals("https://upload.wikimedia.org/wikipedia/commons/8/80/Breast_DCIS_histopathology_%281%29.jpg", resourceData.getUrl());
        Assert.assertNull(resourceData.getResourceDefinition());
    }
    
    @Test
    public void getAllResourceDataOfSampleInStudyDetailedProjection() throws Exception {

        List<ResourceData> result = resourceDataMyBatisRepository.getAllResourceDataOfSampleInStudy("study_tcga_pub", null, null, "DETAILED", null, null, null, null);

        Assert.assertEquals(7, result.size());
        ResourceData resourceData = result.get(0);
        Assert.assertEquals("TCGA-A1-A0SB-01", resourceData.getSampleId());
        Assert.assertEquals("TCGA-A1-A0SB", resourceData.getPatientId());
        Assert.assertEquals("HE", resourceData.getResourceId());
        Assert.assertEquals("https://upload.wikimedia.org/wikipedia/commons/8/80/Breast_DCIS_histopathology_%281%29.jpg", resourceData.getUrl());
        ResourceDefinition resourceDefinition = resourceData.getResourceDefinition();
        Assert.assertEquals("H&E Slide", resourceDefinition.getDisplayName());
        Assert.assertEquals("H&E Slide", resourceDefinition.getDescription());
        ResourceType resourceType = resourceDefinition.getResourceType();
        Assert.assertEquals(ResourceType.SAMPLE, resourceType);
        Assert.assertEquals(true, resourceDefinition.getOpenByDefault());
        Assert.assertEquals("1", resourceDefinition.getPriority());
        Assert.assertEquals("study_tcga_pub", resourceDefinition.getCancerStudyIdentifier());
    }
    
    @Test
    public void getAllResourceDataOfSampleInStudySummaryProjection1PageSize() throws Exception {
        
        List<ResourceData> result = resourceDataMyBatisRepository.getAllResourceDataOfSampleInStudy("study_tcga_pub", null, null, "SUMMARY", 1, 0, null, null);
        
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void getAllResourceDataOfSampleInStudySummaryProjectionSampleIdSort() throws Exception {

        List<ResourceData> result = resourceDataMyBatisRepository.getAllResourceDataOfSampleInStudy("study_tcga_pub", null, null, "SUMMARY", null, null, "sampleId", "DESC");

        Assert.assertEquals(7, result.size());
        Assert.assertEquals("TCGA-A1-A0SH-01", result.get(0).getSampleId());
        Assert.assertEquals("TCGA-A1-A0SG-01", result.get(1).getSampleId());
        Assert.assertEquals("TCGA-A1-A0SF-01", result.get(2).getSampleId());
        Assert.assertEquals("TCGA-A1-A0SE-01", result.get(3).getSampleId());
        Assert.assertEquals("TCGA-A1-A0SD-01", result.get(4).getSampleId());
        Assert.assertEquals("TCGA-A1-A0SB-02", result.get(5).getSampleId());
        Assert.assertEquals("TCGA-A1-A0SB-01", result.get(6).getSampleId());
    }
    
    @Test
    public void getAllResouceDataOfPatientInStudy() throws Exception {

        List<ResourceData> result = resourceDataMyBatisRepository.getAllResourceDataOfPatientInStudy("study_tcga_pub", "TCGA-A1-A0SB", null, "DETAILED", null, null, null, null);

        Assert.assertEquals(1, result.size());
        ResourceData resourceData = result.get(0);
        Assert.assertEquals("TCGA-A1-A0SB", resourceData.getPatientId());
        Assert.assertEquals("IDC_OHIF_V2", resourceData.getResourceId());
        Assert.assertEquals("https://upload.wikimedia.org/wikipedia/commons/thumb/e/ea/SADDLE_PE.JPG/721px-SADDLE_PE.JPG", resourceData.getUrl());
        ResourceDefinition resourceDefinition = resourceData.getResourceDefinition();
        Assert.assertEquals("CT Scan", resourceDefinition.getDisplayName());
        Assert.assertEquals("CT Scan", resourceDefinition.getDescription());
        ResourceType resourceType = resourceDefinition.getResourceType();
        Assert.assertEquals(ResourceType.PATIENT, resourceType);
        Assert.assertEquals(true, resourceDefinition.getOpenByDefault());
        Assert.assertEquals("1", resourceDefinition.getPriority());
        Assert.assertEquals("study_tcga_pub", resourceDefinition.getCancerStudyIdentifier());
    }
    
    @Test
    public void getAllResourceDataForStudy() throws Exception {
        
        List<ResourceData> result = resourceDataMyBatisRepository.getAllResourceDataForStudy("acc_tcga", null, "DETAILED", null, null, null, null);

        Assert.assertEquals(1, result.size());
        ResourceData resourceData = result.get(0);
        Assert.assertEquals("FIGURES", resourceData.getResourceId());
        Assert.assertEquals("https://upload.wikimedia.org/wikipedia/commons/thumb/5/56/Tumor_Mesothelioma2_legend.jpg/220px-Tumor_Mesothelioma2_legend.jpg", resourceData.getUrl());
        ResourceDefinition resourceDefinition = resourceData.getResourceDefinition();
        Assert.assertEquals("Figures", resourceDefinition.getDisplayName());
        Assert.assertEquals("Figures", resourceDefinition.getDescription());
        ResourceType resourceType = resourceDefinition.getResourceType();
        Assert.assertEquals(ResourceType.STUDY, resourceType);
        Assert.assertEquals(true, resourceDefinition.getOpenByDefault());
        Assert.assertEquals("1", resourceDefinition.getPriority());
        Assert.assertEquals("acc_tcga", resourceDefinition.getCancerStudyIdentifier());
    }
}
