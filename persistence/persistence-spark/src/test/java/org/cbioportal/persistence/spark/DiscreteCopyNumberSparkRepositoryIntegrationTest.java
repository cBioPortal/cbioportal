package org.cbioportal.persistence.spark;

import org.apache.spark.sql.*;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.persistence.spark.util.ParquetLoader;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=SparkTestConfiguration.class)
@TestPropertySource("/testPortal.properties")
@Configurable
public class DiscreteCopyNumberSparkRepositoryIntegrationTest {
    
    @Autowired
    private SparkSession spark;

    @Autowired
    private ParquetLoader parquetLoader;
    
    @Autowired
    private DiscreteCopyNumberSparkRepository discreteCopyNumberSparkRepository;
    
    /* test data_CNA
    Hugo_Symbol     P-0006360-T01-IM5       P-0004897-T01-IM5       P-0005012-T01-IM5
    A1BG    -2      0       -1
    A2M     0       0       1
    A2MP1   2       -1      0
     */
    @Test
    public void getSampleCountInMultipleMolecularProfilesSampleIds() {

        List<CopyNumberCountByGene> cncGenes = discreteCopyNumberSparkRepository.getSampleCountInMultipleMolecularProfiles(
            Arrays.asList("msk_impact_2017_cna"), Arrays.asList("P-0006360-T01-IM5"), null,
            Arrays.asList(-2,2));

        Assert.assertEquals(2, cncGenes.size());
        CopyNumberCountByGene cncg1 = cncGenes.get(0);
        Assert.assertTrue(3 == cncg1.getEntrezGeneId());
        Assert.assertEquals("A2MP1", cncg1.getHugoGeneSymbol());
        Assert.assertEquals("12p13.31", cncg1.getCytoband());
        Assert.assertTrue(2 == cncg1.getAlteration());
        Assert.assertTrue(1 == cncg1.getNumberOfAlteredCases());
    }
    
    @Test
    public void getSampleCountInMultipleMolecularProfiles() {
        
        List<CopyNumberCountByGene> cncGenes = discreteCopyNumberSparkRepository.getSampleCountInMultipleMolecularProfiles(
            Arrays.asList("msk_impact_2017_cna"), null, Arrays.asList(1,3),
            Arrays.asList(-2, 2));
        
        Assert.assertEquals(2, cncGenes.size());
        CopyNumberCountByGene cncg = cncGenes.get(0);
        Assert.assertTrue(3 == cncg.getEntrezGeneId());
        Assert.assertEquals("A2MP1", cncg.getHugoGeneSymbol());
        Assert.assertEquals("12p13.31", cncg.getCytoband());
        Assert.assertTrue(2 == cncg.getAlteration());
        Assert.assertTrue(1 == cncg.getNumberOfAlteredCases());
        CopyNumberCountByGene cncg1 = cncGenes.get(1);
        Assert.assertTrue(1 == cncg1.getEntrezGeneId());
        Assert.assertEquals("A1BG", cncg1.getHugoGeneSymbol());
        Assert.assertEquals("19q13.43", cncg1.getCytoband());
        Assert.assertTrue(-2 == cncg1.getAlteration());
        Assert.assertTrue(1 == cncg1.getNumberOfAlteredCases());
        
    }
}