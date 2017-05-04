package org.cbioportal.service.impl;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.Gene;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationSampleCountByGene;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.util.BenjaminiHochbergFDRCalculator;
import org.cbioportal.service.util.FisherExactTestCalculator;
import org.cbioportal.service.util.LogRatioCalculator;
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
public class MutationEnrichmentServiceImplTest extends BaseServiceImplTest {
    
    @InjectMocks
    private MutationEnrichmentServiceImpl mutationEnrichmentService;

    @Mock
    private MutationService mutationService;
    @Mock
    private FisherExactTestCalculator fisherExactTestCalculator;
    @Mock
    private LogRatioCalculator logRatioCalculator;
    @Mock
    private BenjaminiHochbergFDRCalculator benjaminiHochbergFDRCalculator;
    @Mock
    private GeneService geneService;
    
    @Test
    public void getMutationEnrichments() throws Exception {
        
        List<String> alteredSampleIds = new ArrayList<>();
        alteredSampleIds.add("sample_id_1");
        alteredSampleIds.add("sample_id_2");
        List<String> unalteredSampleIds = new ArrayList<>();
        unalteredSampleIds.add("sample_id_3");
        unalteredSampleIds.add("sample_id_4");
        List<String> allSampleIds = new ArrayList<>(alteredSampleIds);
        allSampleIds.addAll(unalteredSampleIds);

        List<MutationSampleCountByGene> mutationSampleCountByGeneList = new ArrayList<>();
        MutationSampleCountByGene mutationSampleCountByGene1 = new MutationSampleCountByGene();
        mutationSampleCountByGene1.setEntrezGeneId(ENTREZ_GENE_ID);
        mutationSampleCountByGene1.setSampleCount(2);
        mutationSampleCountByGeneList.add(mutationSampleCountByGene1);
        MutationSampleCountByGene mutationSampleCountByGene2 = new MutationSampleCountByGene();
        mutationSampleCountByGene2.setEntrezGeneId(2);
        mutationSampleCountByGene2.setSampleCount(3);
        mutationSampleCountByGeneList.add(mutationSampleCountByGene2);
        MutationSampleCountByGene mutationSampleCountByGene3 = new MutationSampleCountByGene();
        mutationSampleCountByGene3.setEntrezGeneId(3);
        mutationSampleCountByGene3.setSampleCount(2);
        mutationSampleCountByGeneList.add(mutationSampleCountByGene3);
        
        Mockito.when(mutationService.getSampleCountByEntrezGeneIdsAndSampleIds(GENETIC_PROFILE_ID, allSampleIds, null))
            .thenReturn(mutationSampleCountByGeneList);
        
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
        
        List<Mutation> mutations = new ArrayList<>();
        Mutation mutation1 = new Mutation();
        mutation1.setEntrezGeneId(2);
        mutation1.setSampleId(1);
        mutations.add(mutation1);
        Mutation mutation2 = new Mutation();
        mutation2.setEntrezGeneId(2);
        mutation2.setSampleId(1);
        mutations.add(mutation2);
        Mutation mutation3 = new Mutation();
        mutation3.setEntrezGeneId(3);
        mutation3.setSampleId(1);
        mutations.add(mutation3);
        Mutation mutation4 = new Mutation();
        mutation4.setEntrezGeneId(3);
        mutation4.setSampleId(2);
        mutations.add(mutation4);
        
        Mockito.when(mutationService.fetchMutationsInGeneticProfile(GENETIC_PROFILE_ID, alteredSampleIds, null, "ID", 
            null, null, null, null)).thenReturn(mutations);
        
        Mockito.when(logRatioCalculator.getLogRatio(0.5, 1.0)).thenReturn(-1.0);
        Mockito.when(logRatioCalculator.getLogRatio(1.0, 0.0)).thenReturn(Double.POSITIVE_INFINITY);
        Mockito.when(fisherExactTestCalculator.getCumlativePValue(0, 2, 1, 1)).thenReturn(1.0);
        Mockito.when(fisherExactTestCalculator.getCumlativePValue(2, 0, 0, 2)).thenReturn(0.3);
        Mockito.when(benjaminiHochbergFDRCalculator.calculate(new double[]{0.3, 1})).thenReturn(new double[]{0.6, 1});
        
        List<AlterationEnrichment> result = mutationEnrichmentService.getMutationEnrichments(GENETIC_PROFILE_ID, 
            alteredSampleIds, unalteredSampleIds, Arrays.asList(ENTREZ_GENE_ID));

        Assert.assertEquals(2, result.size());
        AlterationEnrichment alterationEnrichment1 = result.get(0);
        Assert.assertEquals((Integer) 3, alterationEnrichment1.getEntrezGeneId());
        Assert.assertEquals("HUGO3", alterationEnrichment1.getHugoGeneSymbol());
        Assert.assertEquals("CYTOBAND3", alterationEnrichment1.getCytoband());
        Assert.assertEquals((Integer) 2, alterationEnrichment1.getNumberOfSamplesInAlteredGroup());
        Assert.assertEquals((Integer) 0, alterationEnrichment1.getNumberOfSamplesInUnalteredGroup());
        Assert.assertEquals("Infinity", alterationEnrichment1.getLogRatio());
        Assert.assertEquals(new BigDecimal("0.3"), alterationEnrichment1.getpValue());
        Assert.assertEquals(new BigDecimal("0.6"), alterationEnrichment1.getqValue());
        AlterationEnrichment alterationEnrichment2 = result.get(1);
        Assert.assertEquals((Integer) 2, alterationEnrichment2.getEntrezGeneId());
        Assert.assertEquals("HUGO2", alterationEnrichment2.getHugoGeneSymbol());
        Assert.assertEquals("CYTOBAND2", alterationEnrichment2.getCytoband());
        Assert.assertEquals((Integer) 1, alterationEnrichment2.getNumberOfSamplesInAlteredGroup());
        Assert.assertEquals((Integer) 2, alterationEnrichment2.getNumberOfSamplesInUnalteredGroup());
        Assert.assertEquals("-1.0", alterationEnrichment2.getLogRatio());
        Assert.assertEquals(new BigDecimal("1.0"), alterationEnrichment2.getpValue());
        Assert.assertEquals(new BigDecimal("1.0"), alterationEnrichment2.getqValue());
    }
}
