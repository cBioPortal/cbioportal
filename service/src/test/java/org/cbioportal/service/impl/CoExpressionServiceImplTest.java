package org.cbioportal.service.impl;

import org.cbioportal.model.CoExpression;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneGeneticData;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.GeneticDataService;
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
public class CoExpressionServiceImplTest extends BaseServiceImplTest {

    private static final double THRESHOLD = 0.3;
    
    @InjectMocks
    private CoExpressionServiceImpl coExpressionService;
    
    @Mock
    private GeneticDataService geneticDataService;
    @Mock
    private GeneService geneService;
    
    @Test
    public void getCoExpressions() throws Exception {

        List<GeneGeneticData> geneticDataList = createGeneGeneticData();
        Mockito.when(geneticDataService.getGeneticData(GENETIC_PROFILE_ID, SAMPLE_LIST_ID, null, "SUMMARY"))
            .thenReturn(geneticDataList);

        List<Gene> genes = createGenes();

        Mockito.when(geneService.fetchGenes(Arrays.asList("2", "3", "4"), "ENTREZ_GENE_ID", "SUMMARY"))
            .thenReturn(genes);

        List<CoExpression> result = coExpressionService.getCoExpressions(GENETIC_PROFILE_ID,
            SAMPLE_LIST_ID, ENTREZ_GENE_ID, THRESHOLD);

        Assert.assertEquals(2, result.size());
        CoExpression coExpression1 = result.get(0);
        Assert.assertEquals((Integer) 2, coExpression1.getEntrezGeneId());
        Assert.assertEquals("HUGO2", coExpression1.getHugoGeneSymbol());
        Assert.assertEquals("CYTOBAND2", coExpression1.getCytoband());
        Assert.assertEquals(new BigDecimal("0.49999999999999994"), coExpression1.getPearsonsCorrelation());
        Assert.assertEquals(new BigDecimal("0.5"), coExpression1.getSpearmansCorrelation());
        CoExpression coExpression2 = result.get(1);
        Assert.assertEquals((Integer) 3, coExpression2.getEntrezGeneId());
        Assert.assertEquals("HUGO3", coExpression2.getHugoGeneSymbol());
        Assert.assertEquals("CYTOBAND3", coExpression2.getCytoband());
        Assert.assertEquals(new BigDecimal("0.8585294073051386"), coExpression2.getPearsonsCorrelation());
        Assert.assertEquals(new BigDecimal("0.8660254037844386"), coExpression2.getSpearmansCorrelation());
    }

    @Test
    public void fetchCoExpressions() throws Exception {

        List<GeneGeneticData> geneticDataList = createGeneGeneticData();
        Mockito.when(geneticDataService.fetchGeneticData(GENETIC_PROFILE_ID, Arrays.asList(SAMPLE_ID1, SAMPLE_ID2), 
            null, "SUMMARY")).thenReturn(geneticDataList);

        List<Gene> genes = createGenes();

        Mockito.when(geneService.fetchGenes(Arrays.asList("2", "3", "4"), "ENTREZ_GENE_ID", "SUMMARY"))
            .thenReturn(genes);

        List<CoExpression> result = coExpressionService.fetchCoExpressions(GENETIC_PROFILE_ID,
            Arrays.asList(SAMPLE_ID1, SAMPLE_ID2), ENTREZ_GENE_ID, THRESHOLD);

        Assert.assertEquals(2, result.size());
        CoExpression coExpression1 = result.get(0);
        Assert.assertEquals((Integer) 2, coExpression1.getEntrezGeneId());
        Assert.assertEquals("HUGO2", coExpression1.getHugoGeneSymbol());
        Assert.assertEquals("CYTOBAND2", coExpression1.getCytoband());
        Assert.assertEquals(new BigDecimal("0.49999999999999994"), coExpression1.getPearsonsCorrelation());
        Assert.assertEquals(new BigDecimal("0.5"), coExpression1.getSpearmansCorrelation());
        CoExpression coExpression2 = result.get(1);
        Assert.assertEquals((Integer) 3, coExpression2.getEntrezGeneId());
        Assert.assertEquals("HUGO3", coExpression2.getHugoGeneSymbol());
        Assert.assertEquals("CYTOBAND3", coExpression2.getCytoband());
        Assert.assertEquals(new BigDecimal("0.8585294073051386"), coExpression2.getPearsonsCorrelation());
        Assert.assertEquals(new BigDecimal("0.8660254037844386"), coExpression2.getSpearmansCorrelation());
    }

    private List<GeneGeneticData> createGeneGeneticData() {
        List<GeneGeneticData> geneticDataList = new ArrayList<>();
        GeneGeneticData geneGeneticData1 = new GeneGeneticData();
        geneGeneticData1.setEntrezGeneId(ENTREZ_GENE_ID);
        geneGeneticData1.setValue("2.1");
        geneticDataList.add(geneGeneticData1);
        GeneGeneticData geneGeneticData2 = new GeneGeneticData();
        geneGeneticData2.setEntrezGeneId(ENTREZ_GENE_ID);
        geneGeneticData2.setValue("3");
        geneticDataList.add(geneGeneticData2);
        GeneGeneticData geneGeneticData3 = new GeneGeneticData();
        geneGeneticData3.setEntrezGeneId(ENTREZ_GENE_ID);
        geneGeneticData3.setValue("3");
        geneticDataList.add(geneGeneticData3);
        GeneGeneticData geneGeneticData4 = new GeneGeneticData();
        geneGeneticData4.setEntrezGeneId(2);
        geneGeneticData4.setValue("2");
        geneticDataList.add(geneGeneticData4);
        GeneGeneticData geneGeneticData5 = new GeneGeneticData();
        geneGeneticData5.setEntrezGeneId(2);
        geneGeneticData5.setValue("3");
        geneticDataList.add(geneGeneticData5);
        GeneGeneticData geneGeneticData6 = new GeneGeneticData();
        geneGeneticData6.setEntrezGeneId(2);
        geneGeneticData6.setValue("2");
        geneticDataList.add(geneGeneticData6);
        GeneGeneticData geneGeneticData7 = new GeneGeneticData();
        geneGeneticData7.setEntrezGeneId(3);
        geneGeneticData7.setValue("1.1");
        geneticDataList.add(geneGeneticData7);
        GeneGeneticData geneGeneticData8 = new GeneGeneticData();
        geneGeneticData8.setEntrezGeneId(3);
        geneGeneticData8.setValue("5");
        geneticDataList.add(geneGeneticData8);
        GeneGeneticData geneGeneticData9 = new GeneGeneticData();
        geneGeneticData9.setEntrezGeneId(3);
        geneGeneticData9.setValue("3");
        geneticDataList.add(geneGeneticData9);
        GeneGeneticData geneGeneticData10 = new GeneGeneticData();
        geneGeneticData10.setEntrezGeneId(4);
        geneGeneticData10.setValue("1");
        geneticDataList.add(geneGeneticData10);
        GeneGeneticData geneGeneticData11 = new GeneGeneticData();
        geneGeneticData11.setEntrezGeneId(4);
        geneGeneticData11.setValue("4");
        geneticDataList.add(geneGeneticData11);
        GeneGeneticData geneGeneticData12 = new GeneGeneticData();
        geneGeneticData12.setEntrezGeneId(4);
        geneGeneticData12.setValue("0");
        geneticDataList.add(geneGeneticData12);
        return geneticDataList;
    }

    private List<Gene> createGenes() {
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
        Gene gene3 = new Gene();
        gene3.setEntrezGeneId(4);
        gene3.setHugoGeneSymbol("HUGO4");
        gene3.setCytoband("CYTOBAND4");
        genes.add(gene3);
        return genes;
    }
}