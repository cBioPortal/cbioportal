package org.cbioportal.persistence.mybatis;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.cbioportal.model.VariantCount;
import org.cbioportal.persistence.mybatis.config.TestConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {VariantCountMyBatisRepository.class, TestConfig.class})
public class VariantCountMyBatisRepositoryTest {
    
    @Autowired
    private VariantCountMyBatisRepository variantCountMyBatisRepository;
    
    @Test
    public void fetchVariantCounts() throws Exception {

        List<VariantCount> result = variantCountMyBatisRepository.fetchVariantCounts("study_tcga_pub_mutations", 
            Arrays.asList(207, 207, 369), Arrays.asList("AKT1 truncating", null, "ARAF G1513 missense"));

        Assert.assertEquals(3, result.size());

        Optional<VariantCount> variantCountOptional =
            result.stream().filter(r -> r.getKeyword() == null).findAny();
        Assert.assertTrue(variantCountOptional.isPresent());
        VariantCount variantCount1 = variantCountOptional.get();

        Assert.assertEquals("study_tcga_pub_mutations", variantCount1.getMolecularProfileId());
        Assert.assertEquals((Integer) 207, variantCount1.getEntrezGeneId());
        Assert.assertEquals((Integer) 21, variantCount1.getNumberOfSamplesWithKeyword());
        Assert.assertEquals((Integer) 22, variantCount1.getNumberOfSamplesWithMutationInGene());

        variantCountOptional =
            result.stream().filter(r -> r.getKeyword().equals("AKT1 truncating")).findAny();
        Assert.assertTrue(variantCountOptional.isPresent());
        VariantCount variantCount2 = variantCountOptional.get();

        Assert.assertEquals("study_tcga_pub_mutations", variantCount2.getMolecularProfileId());
        Assert.assertEquals((Integer) 207, variantCount2.getEntrezGeneId());
        Assert.assertEquals((Integer) 54, variantCount2.getNumberOfSamplesWithKeyword());
        Assert.assertEquals((Integer) 64, variantCount2.getNumberOfSamplesWithMutationInGene());
    }
}
