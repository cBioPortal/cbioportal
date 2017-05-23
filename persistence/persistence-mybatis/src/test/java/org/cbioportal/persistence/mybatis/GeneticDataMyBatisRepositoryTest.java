package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.GeneGeneticAlteration;
import org.cbioportal.model.GenesetGeneticAlteration;
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
    public void getGeneGeneticAlterations() throws Exception {

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(207);
        entrezGeneIds.add(208);
        
        List<GeneGeneticAlteration> result = geneticDataMyBatisRepository.getGeneGeneticAlterations("study_tcga_pub_gistic",
            entrezGeneIds, "SUMMARY");
        
        Assert.assertEquals(2, result.size());
        GeneGeneticAlteration geneticAlteration1 = result.get(0);
        Assert.assertEquals((Integer) 207, geneticAlteration1.getEntrezGeneId());
        String[] expected = {"-0.4674","-0.6270","-1.2266","-1.2479","-1.2262","0.6962","-0.3338","-0.1264","0.7559","-1.1267","-0.5893",
        "-1.1546","-1.0027","-1.3157"};
        Assert.assertArrayEquals(expected, geneticAlteration1.getSplitValues());
        GeneGeneticAlteration geneticAlteration2 = result.get(1);
        Assert.assertEquals((Integer) 208, geneticAlteration2.getEntrezGeneId());
        String[] expected2 = {"1.4146","-0.0662","-0.8585","-1.6576","-0.3552","-0.8306","0.8102","0.1146","0.3498","0.0349","0.4927",
                "-0.8665","-0.4754","-0.7221"};
        Assert.assertArrayEquals(expected2, geneticAlteration2.getSplitValues());
    }

    @Test
    public void getGenesetGeneticAlterations() {

        String genesetId1 = "HINATA_NFKB_MATRIX";
        String genesetId2 = "MORF_ATRX";
        List<String> genesetIds = new ArrayList<>();
        genesetIds.add(genesetId1);
        genesetIds.add(genesetId2);

        List<GenesetGeneticAlteration> result = geneticDataMyBatisRepository.getGenesetGeneticAlterations("study_tcga_pub_gsva_scores",
                genesetIds, "SUMMARY");

        //expect 2 items, one for each geneset:
        Assert.assertEquals(2, result.size());
        GenesetGeneticAlteration geneticAlteration1 = result.get(0);
        Assert.assertEquals(genesetId1, geneticAlteration1.getGenesetId());
        String[] expected = {"1.0106","-0.0662","-0.8585","-1.6576","-0.3552","-0.8306","0.8102","0.1106","0.3098","0.0309","0.0927",
                "-0.8665","-0.0750","-0.7221"};
        Assert.assertArrayEquals(expected, geneticAlteration1.getSplitValues());
        GenesetGeneticAlteration geneticAlteration2 = result.get(1);
        Assert.assertEquals(genesetId2, geneticAlteration2.getGenesetId());
        String[] expected2 = {"-0.0670","-0.6270","-1.2266","-1.2079","-1.2262","0.6962","-0.3338","-0.1260","0.7559","-1.1267","-0.5893",
                "-1.1506","-1.0027","-1.3157"};
        Assert.assertArrayEquals(expected2, geneticAlteration2.getSplitValues());
    }


}