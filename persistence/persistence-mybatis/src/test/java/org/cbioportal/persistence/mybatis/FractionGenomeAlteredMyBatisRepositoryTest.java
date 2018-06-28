package org.cbioportal.persistence.mybatis;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.cbioportal.model.FractionGenomeAltered;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class FractionGenomeAlteredMyBatisRepositoryTest {

    @Autowired
    private FractionGenomeAlteredMyBatisRepository fractionGenomeAlteredMyBatisRepository;

    @Test
    public void getFractionGenomeAltered() throws Exception {

        List<FractionGenomeAltered> result = fractionGenomeAlteredMyBatisRepository.getFractionGenomeAltered(
            "study_tcga_pub", "study_tcga_pub_all");
        
        Assert.assertEquals(3, result.size());
        FractionGenomeAltered fractionGenomeAltered = result.get(0);
        Assert.assertEquals("study_tcga_pub", fractionGenomeAltered.getStudyId());
        Assert.assertEquals("TCGA-A1-A0SB-01", fractionGenomeAltered.getSampleId());
        Assert.assertEquals("TCGA-A1-A0SB", fractionGenomeAltered.getPatientId());
        Assert.assertEquals(new BigDecimal("0.2345"), fractionGenomeAltered.getValue());
    }

    @Test
    public void fetchFractionGenomeAltered() throws Exception {

        List<FractionGenomeAltered> result = fractionGenomeAlteredMyBatisRepository.fetchFractionGenomeAltered(
            "study_tcga_pub", Arrays.asList("TCGA-A1-A0SB-01", "TCGA-A1-A0SD-01"));
        
        Assert.assertEquals(2, result.size());
        FractionGenomeAltered fractionGenomeAltered = result.get(0);
        Assert.assertEquals("study_tcga_pub", fractionGenomeAltered.getStudyId());
        Assert.assertEquals("TCGA-A1-A0SB-01", fractionGenomeAltered.getSampleId());
        Assert.assertEquals("TCGA-A1-A0SB", fractionGenomeAltered.getPatientId());
        Assert.assertEquals(new BigDecimal("0.2345"), fractionGenomeAltered.getValue());
    }
}
