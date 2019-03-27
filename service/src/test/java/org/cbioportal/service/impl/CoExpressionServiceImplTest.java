package org.cbioportal.service.impl;

import org.cbioportal.model.CoExpression;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneMolecularAlteration;
import org.cbioportal.model.GeneMolecularData;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.MolecularDataRepository;
import org.cbioportal.persistence.SampleListRepository;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.MolecularDataService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.SampleService;
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
    private SampleListRepository sampleListRepository;
    @Mock
    private MolecularDataRepository molecularDataRepository;
    @Mock
    private MolecularProfileService molecularProfileService;
    @Mock
    private SampleService sampleService;
    
    @Test
    public void getCoExpressions() throws Exception {

        Mockito.when(sampleListRepository.getAllSampleIdsInSampleList(SAMPLE_LIST_ID))
            .thenReturn(Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3));

        List<GeneMolecularAlteration> molecularAlterations = createGeneAlterations();
        Mockito.when(molecularDataService.getMolecularAlterations(MOLECULAR_PROFILE_ID, null, "SUMMARY"))
            .thenReturn(molecularAlterations);

        List<Gene> genes = createGenes();

        Mockito.when(geneService.fetchGenes(Arrays.asList("2", "3", "4"), "ENTREZ_GENE_ID", "SUMMARY"))
            .thenReturn(genes);

        Mockito.when(molecularDataRepository.getCommaSeparatedSampleIdsOfMolecularProfile(MOLECULAR_PROFILE_ID))
            .thenReturn("1,2,3");

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setCancerStudyIdentifier(STUDY_ID);
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(molecularProfile);

        List<Sample> samples = new ArrayList<>();
        Sample sample1 = new Sample();
        sample1.setInternalId(1);
        samples.add(sample1);
        Sample sample2 = new Sample();
        sample2.setInternalId(2);
        samples.add(sample2);
        Sample sample3 = new Sample();
        sample3.setInternalId(3);
        samples.add(sample3);
        Mockito.when(sampleService.fetchSamples(Arrays.asList(STUDY_ID, STUDY_ID, STUDY_ID), 
            Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3), "ID")).thenReturn(samples);

        List<CoExpression> result = coExpressionService.getCoExpressions(MOLECULAR_PROFILE_ID,
            SAMPLE_LIST_ID, ENTREZ_GENE_ID_1, THRESHOLD);

        Assert.assertEquals(2, result.size());
        CoExpression coExpression1 = result.get(0);
        Assert.assertEquals((Integer) 2, coExpression1.getEntrezGeneId());
        Assert.assertEquals("HUGO2", coExpression1.getHugoGeneSymbol());
        Assert.assertEquals("CYTOBAND2", coExpression1.getCytoband());
        Assert.assertEquals(new BigDecimal("0.5"), coExpression1.getSpearmansCorrelation());
        Assert.assertEquals(new BigDecimal("0.6666666666666667"), coExpression1.getpValue());
        CoExpression coExpression2 = result.get(1);
        Assert.assertEquals((Integer) 3, coExpression2.getEntrezGeneId());
        Assert.assertEquals("HUGO3", coExpression2.getHugoGeneSymbol());
        Assert.assertEquals("CYTOBAND3", coExpression2.getCytoband());
        Assert.assertEquals(new BigDecimal("0.8660254037844386"), coExpression2.getSpearmansCorrelation());
        Assert.assertEquals(new BigDecimal("0.3333333333333333"), coExpression2.getpValue());
    }

    @Test
    public void fetchCoExpressions() throws Exception {

        List<GeneMolecularAlteration> molecularAlterations = createGeneAlterations();
        Mockito.when(molecularDataService.getMolecularAlterations(MOLECULAR_PROFILE_ID, null, "SUMMARY"))
            .thenReturn(molecularAlterations);

        List<Gene> genes = createGenes();

        Mockito.when(geneService.fetchGenes(Arrays.asList("2", "3", "4"), "ENTREZ_GENE_ID", "SUMMARY"))
            .thenReturn(genes);

        Mockito.when(molecularDataRepository.getCommaSeparatedSampleIdsOfMolecularProfile(MOLECULAR_PROFILE_ID))
            .thenReturn("1,2,3");

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setCancerStudyIdentifier(STUDY_ID);
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(molecularProfile);

        List<Sample> samples = new ArrayList<>();
        Sample sample1 = new Sample();
        sample1.setInternalId(1);
        samples.add(sample1);
        Sample sample2 = new Sample();
        sample2.setInternalId(2);
        samples.add(sample2);
        Sample sample3 = new Sample();
        sample3.setInternalId(3);
        samples.add(sample3);
        Mockito.when(sampleService.fetchSamples(Arrays.asList(STUDY_ID, STUDY_ID, STUDY_ID), 
            Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3), "ID")).thenReturn(samples);

        List<CoExpression> result = coExpressionService.fetchCoExpressions(MOLECULAR_PROFILE_ID,
            Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3), ENTREZ_GENE_ID_1, THRESHOLD);

        Assert.assertEquals(2, result.size());
        CoExpression coExpression1 = result.get(0);
        Assert.assertEquals((Integer) 2, coExpression1.getEntrezGeneId());
        Assert.assertEquals("HUGO2", coExpression1.getHugoGeneSymbol());
        Assert.assertEquals("CYTOBAND2", coExpression1.getCytoband());
        Assert.assertEquals(new BigDecimal("0.5"), coExpression1.getSpearmansCorrelation());
        Assert.assertEquals(new BigDecimal("0.6666666666666667"), coExpression1.getpValue());
        CoExpression coExpression2 = result.get(1);
        Assert.assertEquals((Integer) 3, coExpression2.getEntrezGeneId());
        Assert.assertEquals("HUGO3", coExpression2.getHugoGeneSymbol());
        Assert.assertEquals("CYTOBAND3", coExpression2.getCytoband());
        Assert.assertEquals(new BigDecimal("0.8660254037844386"), coExpression2.getSpearmansCorrelation());
        Assert.assertEquals(new BigDecimal("0.3333333333333333"), coExpression2.getpValue());
    }

    private List<GeneMolecularAlteration> createGeneAlterations() {

        List<GeneMolecularAlteration> molecularAlterations = new ArrayList<>();
        GeneMolecularAlteration geneMolecularAlteration1 = new GeneMolecularAlteration();
        geneMolecularAlteration1.setEntrezGeneId(1);
        geneMolecularAlteration1.setValues("2.1,3,3");
        molecularAlterations.add(geneMolecularAlteration1);
        GeneMolecularAlteration geneMolecularAlteration2 = new GeneMolecularAlteration();
        geneMolecularAlteration2.setEntrezGeneId(2);
        geneMolecularAlteration2.setValues("2,3,2");
        molecularAlterations.add(geneMolecularAlteration2);
        GeneMolecularAlteration geneMolecularAlteration3 = new GeneMolecularAlteration();
        geneMolecularAlteration3.setEntrezGeneId(3);
        geneMolecularAlteration3.setValues("1.1,5,3");
        molecularAlterations.add(geneMolecularAlteration3);
        GeneMolecularAlteration geneMolecularAlteration4 = new GeneMolecularAlteration();
        geneMolecularAlteration4.setEntrezGeneId(4);
        geneMolecularAlteration4.setValues("1,4,0");
        molecularAlterations.add(geneMolecularAlteration4);

        return molecularAlterations;
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