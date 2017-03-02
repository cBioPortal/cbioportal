package org.cbioportal.service.impl;

import org.cbioportal.model.ExpressionEnrichment;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneGeneticData;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.GeneticDataService;
import org.cbioportal.service.util.BenjaminiHochbergFDRCalculator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class ExpressionEnrichmentServiceImplTest extends BaseServiceImplTest {
    
    @InjectMocks
    private ExpressionEnrichmentServiceImpl expressionEnrichmentService;

    @Mock
    private GeneticDataService geneticDataService;
    @Mock
    private GeneService geneService;
    @Mock
    private BenjaminiHochbergFDRCalculator benjaminiHochbergFDRCalculator;
    
    @Test
    public void getExpressionEnrichments() throws Exception {

        List<String> alteredSampleIds = new ArrayList<>();
        alteredSampleIds.add("sample_id_1");
        alteredSampleIds.add("sample_id_2");
        List<String> unalteredSampleIds = new ArrayList<>();
        unalteredSampleIds.add("sample_id_3");
        unalteredSampleIds.add("sample_id_4");
        
        List<GeneGeneticData> alteredGeneGeneticDataList = new ArrayList<>();
        GeneGeneticData GeneGeneticData1 = new GeneGeneticData();
        GeneGeneticData1.setEntrezGeneId(2);
        GeneGeneticData1.setValue("2");
        alteredGeneGeneticDataList.add(GeneGeneticData1);
        GeneGeneticData GeneGeneticData2 = new GeneGeneticData();
        GeneGeneticData2.setEntrezGeneId(2);
        GeneGeneticData2.setValue("3");
        alteredGeneGeneticDataList.add(GeneGeneticData2);
        GeneGeneticData GeneGeneticData3 = new GeneGeneticData();
        GeneGeneticData3.setEntrezGeneId(3);
        GeneGeneticData3.setValue("1.1");
        alteredGeneGeneticDataList.add(GeneGeneticData3);
        GeneGeneticData GeneGeneticData4 = new GeneGeneticData();
        GeneGeneticData4.setEntrezGeneId(3);
        GeneGeneticData4.setValue("5");
        alteredGeneGeneticDataList.add(GeneGeneticData4);
        Mockito.when(geneticDataService.fetchGeneticData(GENETIC_PROFILE_ID, alteredSampleIds, null, "SUMMARY"))
            .thenReturn(alteredGeneGeneticDataList);

        List<GeneGeneticData> unalteredGeneGeneticDataList = new ArrayList<>();
        GeneGeneticData GeneGeneticData5 = new GeneGeneticData();
        GeneGeneticData5.setEntrezGeneId(2);
        GeneGeneticData5.setValue("2.1");
        unalteredGeneGeneticDataList.add(GeneGeneticData5);
        GeneGeneticData GeneGeneticData6 = new GeneGeneticData();
        GeneGeneticData6.setEntrezGeneId(2);
        GeneGeneticData6.setValue("3");
        unalteredGeneGeneticDataList.add(GeneGeneticData6);
        GeneGeneticData GeneGeneticData7 = new GeneGeneticData();
        GeneGeneticData7.setEntrezGeneId(3);
        GeneGeneticData7.setValue("2.3");
        unalteredGeneGeneticDataList.add(GeneGeneticData7);
        GeneGeneticData GeneGeneticData8 = new GeneGeneticData();
        GeneGeneticData8.setEntrezGeneId(3);
        GeneGeneticData8.setValue("3");
        unalteredGeneGeneticDataList.add(GeneGeneticData8);
        Mockito.when(geneticDataService.fetchGeneticData(GENETIC_PROFILE_ID, unalteredSampleIds, null, "SUMMARY"))
            .thenReturn(unalteredGeneGeneticDataList);

        List<Gene> genes = new ArrayList<>();
        Gene gene1 = new Gene();
        gene1.setEntrezGeneId(2);
        gene1.setHugoGeneSymbol("HUGO2");
        gene1.setCytoband("CYTOBAND2");
        genes.add(gene1);
        Gene gene2 = new Gene();
        gene2.setEntrezGeneId(3);
        gene2.setHugoGeneSymbol("HUGO3");
        gene2.setCytoband("CYTOBAND3");
        genes.add(gene2);

        Mockito.when(geneService.fetchGenes(Arrays.asList("2", "3"), "ENTREZ_GENE_ID", "SUMMARY")).thenReturn(genes);

        Mockito.when(benjaminiHochbergFDRCalculator.calculate(new double[]{0.8716148250471419, 0.9475795430163914}))
            .thenReturn(new double[]{0.6, 1});

        List<ExpressionEnrichment> result = expressionEnrichmentService.getExpressionEnrichments(GENETIC_PROFILE_ID, 
            alteredSampleIds, unalteredSampleIds);

        Assert.assertEquals(2, result.size());
        ExpressionEnrichment expressionEnrichment1 = result.get(0);
        Assert.assertEquals((Integer) 3, expressionEnrichment1.getEntrezGeneId());
        Assert.assertEquals("HUGO3", expressionEnrichment1.getHugoGeneSymbol());
        Assert.assertEquals("CYTOBAND3", expressionEnrichment1.getCytoband());
        Assert.assertEquals(new BigDecimal("3.05"), expressionEnrichment1.getMeanExpressionInAlteredGroup());
        Assert.assertEquals(new BigDecimal("2.65"), expressionEnrichment1.getMeanExpressionInUnalteredGroup());
        Assert.assertEquals(new BigDecimal("2.7577164466275352"), expressionEnrichment1.getStandardDeviationInAlteredGroup());
        Assert.assertEquals(new BigDecimal("0.4949747468305834"), expressionEnrichment1.getStandardDeviationInUnalteredGroup());
        Assert.assertEquals(new BigDecimal("0.8716148250471419"), expressionEnrichment1.getpValue());
        Assert.assertEquals(new BigDecimal("0.6"), expressionEnrichment1.getqValue());
        ExpressionEnrichment expressionEnrichment2 = result.get(1);
        Assert.assertEquals((Integer) 2, expressionEnrichment2.getEntrezGeneId());
        Assert.assertEquals("HUGO2", expressionEnrichment2.getHugoGeneSymbol());
        Assert.assertEquals("CYTOBAND2", expressionEnrichment2.getCytoband());
        Assert.assertEquals(new BigDecimal("2.5"), expressionEnrichment2.getMeanExpressionInAlteredGroup());
        Assert.assertEquals(new BigDecimal("2.55"), expressionEnrichment2.getMeanExpressionInUnalteredGroup());
        Assert.assertEquals(new BigDecimal("0.7071067811865476"), expressionEnrichment2.getStandardDeviationInAlteredGroup());
        Assert.assertEquals(new BigDecimal("0.6363961030678927"), expressionEnrichment2.getStandardDeviationInUnalteredGroup());
        Assert.assertEquals(new BigDecimal("0.9475795430163914"), expressionEnrichment2.getpValue());
        Assert.assertEquals(new BigDecimal("1.0"), expressionEnrichment2.getqValue());
    }
}
