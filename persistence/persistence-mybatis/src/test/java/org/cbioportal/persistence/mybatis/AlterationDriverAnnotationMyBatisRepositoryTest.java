package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.AlterationDriverAnnotation;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class AlterationDriverAnnotationMyBatisRepositoryTest {

    @Autowired
    private AlterationDriverAnnotationMyBatisRepository alterationDriverAnnotationMyBatisRepository;

    @Test
    public void getAlterationDriverAnnotationsCna() {
        List<String> molecularProfileIds = Arrays.asList("study_tcga_pub_gistic");
        List<AlterationDriverAnnotation> annotations = alterationDriverAnnotationMyBatisRepository.getAlterationDriverAnnotations(molecularProfileIds);
        Assert.assertEquals(2, annotations.size());
    }

    @Test
    public void getAlterationDriverAnnotationsMutations() {
        List<String> molecularProfileIds = Arrays.asList("study_tcga_pub_mutations");
        List<AlterationDriverAnnotation> annotations = alterationDriverAnnotationMyBatisRepository.getAlterationDriverAnnotations(molecularProfileIds);
        Assert.assertEquals(8, annotations.size());
    }

    @Test
    public void getAlterationDriverAnnotationsAll() {
        List<String> molecularProfileIds = Arrays.asList("study_tcga_pub_gistic", "study_tcga_pub_mutations");
        List<AlterationDriverAnnotation> annotations = alterationDriverAnnotationMyBatisRepository.getAlterationDriverAnnotations(molecularProfileIds);
        Assert.assertEquals(10, annotations.size());
    }

    @Test
    public void getAlterationDriverAnnotationsNull() {
        List<AlterationDriverAnnotation> annotations = alterationDriverAnnotationMyBatisRepository.getAlterationDriverAnnotations(null);
        Assert.assertEquals(0, annotations.size());
    }
    
}