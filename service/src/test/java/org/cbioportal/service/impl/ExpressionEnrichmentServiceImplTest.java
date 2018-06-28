package org.cbioportal.service.impl;

import org.cbioportal.model.ExpressionEnrichment;
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
public class ExpressionEnrichmentServiceImplTest extends BaseServiceImplTest {
    
    @InjectMocks
    private ExpressionEnrichmentServiceImpl expressionEnrichmentService;

    @Mock
    private MolecularDataService molecularDataService;
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
        
        List<GeneMolecularData> alteredGeneMolecularDataList = new ArrayList<>();
        GeneMolecularData geneMolecularData1 = new GeneMolecularData();
        geneMolecularData1.setEntrezGeneId(2);
        geneMolecularData1.setValue("2");
        alteredGeneMolecularDataList.add(geneMolecularData1);
        GeneMolecularData geneMolecularData2 = new GeneMolecularData();
        geneMolecularData2.setEntrezGeneId(2);
        geneMolecularData2.setValue("3");
        alteredGeneMolecularDataList.add(geneMolecularData2);
        GeneMolecularData geneMolecularData3 = new GeneMolecularData();
        geneMolecularData3.setEntrezGeneId(3);
        geneMolecularData3.setValue("1.1");
        alteredGeneMolecularDataList.add(geneMolecularData3);
        GeneMolecularData geneMolecularData4 = new GeneMolecularData();
        geneMolecularData4.setEntrezGeneId(3);
        geneMolecularData4.setValue("5");
        alteredGeneMolecularDataList.add(geneMolecularData4);
        Mockito.when(molecularDataService.fetchMolecularData(MOLECULAR_PROFILE_ID, alteredSampleIds, null, "SUMMARY"))
            .thenReturn(alteredGeneMolecularDataList);

        List<GeneMolecularData> unalteredGeneMolecularDataList = new ArrayList<>();
        GeneMolecularData geneMolecularData5 = new GeneMolecularData();
        geneMolecularData5.setEntrezGeneId(2);
        geneMolecularData5.setValue("2.1");
        unalteredGeneMolecularDataList.add(geneMolecularData5);
        GeneMolecularData geneMolecularData6 = new GeneMolecularData();
        geneMolecularData6.setEntrezGeneId(2);
        geneMolecularData6.setValue("3");
        unalteredGeneMolecularDataList.add(geneMolecularData6);
        GeneMolecularData geneMolecularData7 = new GeneMolecularData();
        geneMolecularData7.setEntrezGeneId(3);
        geneMolecularData7.setValue("2.3");
        unalteredGeneMolecularDataList.add(geneMolecularData7);
        GeneMolecularData geneMolecularData8 = new GeneMolecularData();
        geneMolecularData8.setEntrezGeneId(3);
        geneMolecularData8.setValue("3");
        unalteredGeneMolecularDataList.add(geneMolecularData8);
        Mockito.when(molecularDataService.fetchMolecularData(MOLECULAR_PROFILE_ID, unalteredSampleIds, null, "SUMMARY"))
            .thenReturn(unalteredGeneMolecularDataList);

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

        List<ExpressionEnrichment> result = expressionEnrichmentService.getExpressionEnrichments(MOLECULAR_PROFILE_ID, 
            alteredSampleIds, unalteredSampleIds, Arrays.asList(ENTREZ_GENE_ID_1), "SAMPLE");

        Assert.assertEquals(2, result.size());
        ExpressionEnrichment expressionEnrichment1 = result.get(0);
        Assert.assertEquals((Integer) 3, expressionEnrichment1.getEntrezGeneId());
        Assert.assertEquals("HUGO3", expressionEnrichment1.getHugoGeneSymbol());
        Assert.assertEquals("CYTOBAND3", expressionEnrichment1.getCytoband());
        Assert.assertEquals(new BigDecimal("3.05"), expressionEnrichment1.getMeanExpressionInAlteredGroup());
        Assert.assertEquals(new BigDecimal("2.65"), expressionEnrichment1.getMeanExpressionInUnalteredGroup());
        Assert.assertEquals(new BigDecimal("2.7577164466275352"), 
            expressionEnrichment1.getStandardDeviationInAlteredGroup());
        Assert.assertEquals(new BigDecimal("0.4949747468305834"), 
            expressionEnrichment1.getStandardDeviationInUnalteredGroup());
        Assert.assertEquals(new BigDecimal("0.8716148250471419"), expressionEnrichment1.getpValue());
        Assert.assertEquals(new BigDecimal("0.6"), expressionEnrichment1.getqValue());
        ExpressionEnrichment expressionEnrichment2 = result.get(1);
        Assert.assertEquals((Integer) 2, expressionEnrichment2.getEntrezGeneId());
        Assert.assertEquals("HUGO2", expressionEnrichment2.getHugoGeneSymbol());
        Assert.assertEquals("CYTOBAND2", expressionEnrichment2.getCytoband());
        Assert.assertEquals(new BigDecimal("2.5"), expressionEnrichment2.getMeanExpressionInAlteredGroup());
        Assert.assertEquals(new BigDecimal("2.55"), expressionEnrichment2.getMeanExpressionInUnalteredGroup());
        Assert.assertEquals(new BigDecimal("0.7071067811865476"), 
            expressionEnrichment2.getStandardDeviationInAlteredGroup());
        Assert.assertEquals(new BigDecimal("0.6363961030678927"), 
            expressionEnrichment2.getStandardDeviationInUnalteredGroup());
        Assert.assertEquals(new BigDecimal("0.9475795430163914"), expressionEnrichment2.getpValue());
        Assert.assertEquals(new BigDecimal("1.0"), expressionEnrichment2.getqValue());
    }
}
