package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.GeneticAlteration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class GeneticDataMyBatisRepositoryTest {
    
    @Autowired
    private GeneticDataMyBatisRepository geneticDataMyBatisRepository;
    
    @Test
    public void getCommaSeparatedSampleIdsOfGeneticProfile() throws Exception {

        String result = geneticDataMyBatisRepository
            .getCommaSeparatedSampleIdsOfGeneticProfile("study_tcga_pub_gistic");

        Assert.assertEquals("1,2,3,4,5,6,7,8,9,10,11,12,13,14,", result);
    }

    @Test
    public void getGeneticAlterations() throws Exception {

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(207);
        entrezGeneIds.add(208);
        
        List<GeneticAlteration> result = geneticDataMyBatisRepository.getGeneticAlterations("study_tcga_pub_gistic",
            entrezGeneIds, "SUMMARY");
        
        Assert.assertEquals(2, result.size());
        GeneticAlteration geneticAlteration1 = result.get(0);
        Assert.assertEquals((Integer) 207, geneticAlteration1.getEntrezGeneId());
        Assert.assertEquals("-0.4674,-0.6270,-1.2266,-1.2479,-1.2262,0.6962,-0.3338,-0.1264,0.7559,-1.1267,-0.5893," +
            "-1.1546,-1.0027,-1.3157,", geneticAlteration1.getValues());
        GeneticAlteration geneticAlteration2 = result.get(1);
        Assert.assertEquals((Integer) 208, geneticAlteration2.getEntrezGeneId());
        Assert.assertEquals("1.4146,-0.0662,-0.8585,-1.6576,-0.3552,-0.8306,0.8102,0.1146,0.3498,0.0349,0.4927," +
            "-0.8665,-0.4754,-0.7221,", geneticAlteration2.getValues());
    }
}