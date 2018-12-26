package org.cbioportal.service.util;

import org.cbioportal.model.Alteration;
import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.Gene;
import org.cbioportal.service.GeneService;
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
public class AlterationEnrichmentUtilTest {

    @InjectMocks
    private AlterationEnrichmentUtil alterationEnrichmentUtil;
    
    @Mock
    private FisherExactTestCalculator fisherExactTestCalculator;
    @Mock
    private LogRatioCalculator logRatioCalculator;
    @Mock
    private GeneService geneService;

    @Test
    public void createAlterationEnrichments() throws Exception {
        
        List<AlterationCountByGene> alterationSampleCountByGenes = new ArrayList<>();
        AlterationCountByGene alterationSampleCountByGene1 = new AlterationCountByGene();
        alterationSampleCountByGene1.setEntrezGeneId(2);
        alterationSampleCountByGene1.setCountByEntity(3);
        alterationSampleCountByGenes.add(alterationSampleCountByGene1);
        AlterationCountByGene alterationSampleCountByGene2 = new AlterationCountByGene();
        alterationSampleCountByGene2.setEntrezGeneId(3);
        alterationSampleCountByGene2.setCountByEntity(2);
        alterationSampleCountByGenes.add(alterationSampleCountByGene2);

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

        List<Alteration> alterations = new ArrayList<>();
        Alteration alteration1 = new Alteration();
        alteration1.setEntrezGeneId(2);
        alteration1.setSampleId("1");
        alterations.add(alteration1);
        Alteration alteration2 = new Alteration();
        alteration2.setEntrezGeneId(2);
        alteration2.setSampleId("1");
        alterations.add(alteration2);
        Alteration alteration3 = new Alteration();
        alteration3.setEntrezGeneId(3);
        alteration3.setSampleId("1");
        alterations.add(alteration3);
        Alteration alteration4 = new Alteration();
        alteration4.setEntrezGeneId(3);
        alteration4.setSampleId("2");
        alterations.add(alteration4);

        Mockito.when(logRatioCalculator.getLogRatio(0.5, 1.0)).thenReturn(-1.0);
        Mockito.when(logRatioCalculator.getLogRatio(1.0, 0.0)).thenReturn(Double.POSITIVE_INFINITY);
        Mockito.when(fisherExactTestCalculator.getCumulativePValue(0, 2, 1, 1)).thenReturn(1.0);
        Mockito.when(fisherExactTestCalculator.getCumulativePValue(2, 0, 0, 2)).thenReturn(0.3);

        List<AlterationEnrichment> result = alterationEnrichmentUtil.createAlterationEnrichments(2, 2, 
            alterationSampleCountByGenes, alterations, "SAMPLE");

        Assert.assertEquals(2, result.size());
        AlterationEnrichment alterationEnrichment1 = result.get(0);
        Assert.assertEquals((Integer) 2, alterationEnrichment1.getEntrezGeneId());
        Assert.assertEquals("HUGO2", alterationEnrichment1.getHugoGeneSymbol());
        Assert.assertEquals("CYTOBAND2", alterationEnrichment1.getCytoband());
        Assert.assertEquals((Integer) 1, alterationEnrichment1.getAlteredCount());
        Assert.assertEquals((Integer) 2, alterationEnrichment1.getUnalteredCount());
        Assert.assertEquals("-1.0", alterationEnrichment1.getLogRatio());
        Assert.assertEquals(new BigDecimal("1.0"), alterationEnrichment1.getpValue());
        AlterationEnrichment alterationEnrichment2 = result.get(1);
        Assert.assertEquals((Integer) 3, alterationEnrichment2.getEntrezGeneId());
        Assert.assertEquals("HUGO3", alterationEnrichment2.getHugoGeneSymbol());
        Assert.assertEquals("CYTOBAND3", alterationEnrichment2.getCytoband());
        Assert.assertEquals((Integer) 2, alterationEnrichment2.getAlteredCount());
        Assert.assertEquals((Integer) 0, alterationEnrichment2.getUnalteredCount());
        Assert.assertEquals("Infinity", alterationEnrichment2.getLogRatio());
        Assert.assertEquals(new BigDecimal("0.3"), alterationEnrichment2.getpValue());
    }
}
