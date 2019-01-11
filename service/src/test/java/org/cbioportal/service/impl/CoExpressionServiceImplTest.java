package org.cbioportal.service.impl;

import org.cbioportal.model.CoExpression;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneMolecularData;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.MolecularDataService;
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
public class CoExpressionServiceImplTest extends BaseServiceImplTest {

    private static final double THRESHOLD = 0.3;
    
    @InjectMocks
    private CoExpressionServiceImpl coExpressionService;
    
    @Mock
    private MolecularDataService molecularDataService;
    @Mock
    private GeneService geneService;
    @Mock
    private BenjaminiHochbergFDRCalculator benjaminiHochbergFDRCalculator;
    
    @Test
    public void getCoExpressions() throws Exception {

        List<GeneMolecularData> molecularDataList = createGeneMolecularData();
        Mockito.when(molecularDataService.getMolecularData(MOLECULAR_PROFILE_ID, SAMPLE_LIST_ID, null, "SUMMARY"))
            .thenReturn(molecularDataList);

        List<Gene> genes = createGenes();

        Mockito.when(geneService.fetchGenes(Arrays.asList("2", "3", "4"), "ENTREZ_GENE_ID", "SUMMARY"))
            .thenReturn(genes);

        Mockito.when(benjaminiHochbergFDRCalculator.calculate(new double[]{0.3333333333333333, 0.6666666666666667}))
            .thenReturn(new double[]{0.6, 1});

        List<CoExpression> result = coExpressionService.getCoExpressions(MOLECULAR_PROFILE_ID,
            SAMPLE_LIST_ID, ENTREZ_GENE_ID_1, THRESHOLD);

        Assert.assertEquals(2, result.size());
        CoExpression coExpression1 = result.get(0);
        Assert.assertEquals((Integer) 3, coExpression1.getEntrezGeneId());
        Assert.assertEquals("HUGO3", coExpression1.getHugoGeneSymbol());
        Assert.assertEquals("CYTOBAND3", coExpression1.getCytoband());
        Assert.assertEquals(new BigDecimal("0.8660254037844386"), coExpression1.getSpearmansCorrelation());
        Assert.assertEquals(new BigDecimal("0.3333333333333333"), coExpression1.getpValue());
        Assert.assertEquals(new BigDecimal("0.6"), coExpression1.getqValue());
        CoExpression coExpression2 = result.get(1);
        Assert.assertEquals((Integer) 2, coExpression2.getEntrezGeneId());
        Assert.assertEquals("HUGO2", coExpression2.getHugoGeneSymbol());
        Assert.assertEquals("CYTOBAND2", coExpression2.getCytoband());
        Assert.assertEquals(new BigDecimal("0.5"), coExpression2.getSpearmansCorrelation());
        Assert.assertEquals(new BigDecimal("0.6666666666666667"), coExpression2.getpValue());
        Assert.assertEquals(new BigDecimal("1.0"), coExpression2.getqValue());
    }

    @Test
    public void fetchCoExpressions() throws Exception {

        List<GeneMolecularData> molecularDataList = createGeneMolecularData();
        Mockito.when(molecularDataService.fetchMolecularData(MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1, SAMPLE_ID2), 
            null, "SUMMARY")).thenReturn(molecularDataList);

        List<Gene> genes = createGenes();

        Mockito.when(geneService.fetchGenes(Arrays.asList("2", "3", "4"), "ENTREZ_GENE_ID", "SUMMARY"))
            .thenReturn(genes);

        Mockito.when(benjaminiHochbergFDRCalculator.calculate(new double[]{0.3333333333333333, 0.6666666666666667}))
            .thenReturn(new double[]{0.6, 1});

        List<CoExpression> result = coExpressionService.fetchCoExpressions(MOLECULAR_PROFILE_ID,
            Arrays.asList(SAMPLE_ID1, SAMPLE_ID2), ENTREZ_GENE_ID_1, THRESHOLD);

        Assert.assertEquals(2, result.size());
        CoExpression coExpression1 = result.get(0);
        Assert.assertEquals((Integer) 3, coExpression1.getEntrezGeneId());
        Assert.assertEquals("HUGO3", coExpression1.getHugoGeneSymbol());
        Assert.assertEquals("CYTOBAND3", coExpression1.getCytoband());
        Assert.assertEquals(new BigDecimal("0.8660254037844386"), coExpression1.getSpearmansCorrelation());
        Assert.assertEquals(new BigDecimal("0.3333333333333333"), coExpression1.getpValue());
        Assert.assertEquals(new BigDecimal("0.6"), coExpression1.getqValue());
        CoExpression coExpression2 = result.get(1);
        Assert.assertEquals((Integer) 2, coExpression2.getEntrezGeneId());
        Assert.assertEquals("HUGO2", coExpression2.getHugoGeneSymbol());
        Assert.assertEquals("CYTOBAND2", coExpression2.getCytoband());
        Assert.assertEquals(new BigDecimal("0.5"), coExpression2.getSpearmansCorrelation());
        Assert.assertEquals(new BigDecimal("0.6666666666666667"), coExpression2.getpValue());
        Assert.assertEquals(new BigDecimal("1.0"), coExpression2.getqValue());
    }

    private List<GeneMolecularData> createGeneMolecularData() {
        List<GeneMolecularData> molecularDataList = new ArrayList<>();
        GeneMolecularData geneMolecularData1 = new GeneMolecularData();
        geneMolecularData1.setEntrezGeneId(ENTREZ_GENE_ID_1);
        geneMolecularData1.setValue("2.1");
        molecularDataList.add(geneMolecularData1);
        GeneMolecularData geneMolecularData2 = new GeneMolecularData();
        geneMolecularData2.setEntrezGeneId(ENTREZ_GENE_ID_1);
        geneMolecularData2.setValue("3");
        molecularDataList.add(geneMolecularData2);
        GeneMolecularData geneMolecularData3 = new GeneMolecularData();
        geneMolecularData3.setEntrezGeneId(ENTREZ_GENE_ID_1);
        geneMolecularData3.setValue("3");
        molecularDataList.add(geneMolecularData3);
        GeneMolecularData geneMolecularData4 = new GeneMolecularData();
        geneMolecularData4.setEntrezGeneId(2);
        geneMolecularData4.setValue("2");
        molecularDataList.add(geneMolecularData4);
        GeneMolecularData geneMolecularData5 = new GeneMolecularData();
        geneMolecularData5.setEntrezGeneId(2);
        geneMolecularData5.setValue("3");
        molecularDataList.add(geneMolecularData5);
        GeneMolecularData geneMolecularData6 = new GeneMolecularData();
        geneMolecularData6.setEntrezGeneId(2);
        geneMolecularData6.setValue("2");
        molecularDataList.add(geneMolecularData6);
        GeneMolecularData geneMolecularData7 = new GeneMolecularData();
        geneMolecularData7.setEntrezGeneId(3);
        geneMolecularData7.setValue("1.1");
        molecularDataList.add(geneMolecularData7);
        GeneMolecularData geneMolecularData8 = new GeneMolecularData();
        geneMolecularData8.setEntrezGeneId(3);
        geneMolecularData8.setValue("5");
        molecularDataList.add(geneMolecularData8);
        GeneMolecularData geneMolecularData9 = new GeneMolecularData();
        geneMolecularData9.setEntrezGeneId(3);
        geneMolecularData9.setValue("3");
        molecularDataList.add(geneMolecularData9);
        GeneMolecularData geneMolecularData10 = new GeneMolecularData();
        geneMolecularData10.setEntrezGeneId(4);
        geneMolecularData10.setValue("1");
        molecularDataList.add(geneMolecularData10);
        GeneMolecularData geneMolecularData11 = new GeneMolecularData();
        geneMolecularData11.setEntrezGeneId(4);
        geneMolecularData11.setValue("4");
        molecularDataList.add(geneMolecularData11);
        GeneMolecularData geneMolecularData12 = new GeneMolecularData();
        geneMolecularData12.setEntrezGeneId(4);
        geneMolecularData12.setValue("0");
        molecularDataList.add(geneMolecularData12);
        return molecularDataList;
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