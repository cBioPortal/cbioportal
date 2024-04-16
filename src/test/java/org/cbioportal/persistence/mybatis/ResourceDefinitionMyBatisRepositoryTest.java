package org.cbioportal.persistence.mybatis;

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
@SpringBootTest(classes = {ResourceDefinitionMyBatisRepository.class, TestConfig.class})
public class ResourceDefinitionMyBatisRepositoryTest {
    
    @Autowired
    private ResourceDefinitionMyBatisRepository resourceDefinitionMyBatisRepository;
    
    @Test
    public void getResourceDefinition() throws Exception {
        
        ResourceDefinition result = resourceDefinitionMyBatisRepository.getResourceDefinition("study_tcga_pub", "HE");

        Assert.assertEquals("H&E Slide", result.getDisplayName());
        Assert.assertEquals("H&E Slide", result.getDescription());
        Assert.assertEquals(ResourceType.SAMPLE, result.getResourceType());
        Assert.assertEquals("1", result.getPriority());
        Assert.assertEquals(true, result.getOpenByDefault());
    }
    
    @Test
    public void fetchResourceDefinitionsIdProjection() throws Exception {

        List<ResourceDefinition> result = resourceDefinitionMyBatisRepository.fetchResourceDefinitions(List.of("study_tcga_pub"), "ID", null, null, null, null);

        Assert.assertEquals(2, result.size());
        ResourceDefinition resourceDefinition = result.get(0);
        Assert.assertEquals("HE", resourceDefinition.getResourceId());
        Assert.assertEquals("H&E Slide", resourceDefinition.getDisplayName());
        Assert.assertNull(resourceDefinition.getDescription());
        Assert.assertNull(resourceDefinition.getResourceType());
        Assert.assertNull(resourceDefinition.getPriority());
        Assert.assertNull(resourceDefinition.getOpenByDefault());
    }

    @Test
    public void fetchResourceDefinitionsSummaryProjection() throws Exception {

        List<ResourceDefinition> result = resourceDefinitionMyBatisRepository.fetchResourceDefinitions(List.of("study_tcga_pub"), "SUMMARY", null, null, null, null);

        Assert.assertEquals(2, result.size());
        ResourceDefinition resourceDefinition = result.get(0);
        Assert.assertEquals("HE", resourceDefinition.getResourceId());
        Assert.assertEquals("H&E Slide", resourceDefinition.getDisplayName());
        Assert.assertEquals("H&E Slide", resourceDefinition.getDescription());
        Assert.assertEquals(ResourceType.SAMPLE, resourceDefinition.getResourceType());
        Assert.assertEquals("1", resourceDefinition.getPriority());
        Assert.assertEquals(true, resourceDefinition.getOpenByDefault());
    }

    @Test
    public void fetchResourceDefinitionsDetailedProjection() throws Exception {

        List<ResourceDefinition> result = resourceDefinitionMyBatisRepository.fetchResourceDefinitions(List.of("study_tcga_pub"), "DETAILED", null, null, null, null);

        Assert.assertEquals(2, result.size());
        ResourceDefinition resourceDefinition = result.get(0);
        Assert.assertEquals("HE", resourceDefinition.getResourceId());
        Assert.assertEquals("H&E Slide", resourceDefinition.getDisplayName());
        Assert.assertEquals("H&E Slide", resourceDefinition.getDescription());
        Assert.assertEquals(ResourceType.SAMPLE, resourceDefinition.getResourceType());
        Assert.assertEquals("1", resourceDefinition.getPriority());
        Assert.assertEquals(true, resourceDefinition.getOpenByDefault());
    }
}
