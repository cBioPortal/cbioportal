package org.cbioportal.service.impl;

import org.cbioportal.model.*;
import org.cbioportal.model.meta.MutationMeta;
import org.cbioportal.persistence.MutationRepository;
import org.cbioportal.service.util.ChromosomeCalculator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class MutationServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private MutationServiceImpl mutationService;
    
    @Mock
    private MutationRepository mutationRepository;
    @Mock
    private ChromosomeCalculator chromosomeCalculator;
    
    @Test
    public void getMutationsInGeneticProfileBySampleListId() throws Exception {

        List<Mutation> expectedMutationList = new ArrayList<>();
        Mutation mutation = new Mutation();
        Gene gene = new Gene();
        mutation.setGene(gene);
        expectedMutationList.add(mutation);

        Mockito.when(mutationRepository.getMutationsInGeneticProfileBySampleListId(GENETIC_PROFILE_ID, SAMPLE_ID, 
            PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION)).thenReturn(expectedMutationList);
        Mockito.doAnswer(invocationOnMock -> {
            ((Gene) invocationOnMock.getArguments()[0]).setChromosome("19");
            return null;
        }).when(chromosomeCalculator).setChromosome(gene);
        
        List<Mutation> result = mutationService.getMutationsInGeneticProfileBySampleListId(GENETIC_PROFILE_ID, 
            SAMPLE_ID, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedMutationList, result);
        Assert.assertEquals("19", result.get(0).getGene().getChromosome());
    }

    @Test
    public void getMetaMutationsInGeneticProfileBySampleListId() throws Exception {

        MutationMeta expectedMutationMeta = new MutationMeta();
        Mockito.when(mutationRepository.getMetaMutationsInGeneticProfileBySampleListId(GENETIC_PROFILE_ID, SAMPLE_ID))
            .thenReturn(expectedMutationMeta);
        MutationMeta result = mutationService.getMetaMutationsInGeneticProfileBySampleListId(GENETIC_PROFILE_ID, 
            SAMPLE_ID);

        Assert.assertEquals(expectedMutationMeta, result);
    }

    @Test
    public void fetchMutationsInGeneticProfile() throws Exception {

        List<Mutation> expectedMutationList = new ArrayList<>();
        Mutation mutation = new Mutation();
        Gene gene = new Gene();
        mutation.setGene(gene);
        expectedMutationList.add(mutation);

        Mockito.when(mutationRepository.fetchMutationsInGeneticProfile(GENETIC_PROFILE_ID, Arrays.asList(SAMPLE_ID), 
            PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION)).thenReturn(expectedMutationList);
        Mockito.doAnswer(invocationOnMock -> {
            ((Gene) invocationOnMock.getArguments()[0]).setChromosome("19");
            return null;
        }).when(chromosomeCalculator).setChromosome(gene);

        List<Mutation> result = mutationService.fetchMutationsInGeneticProfile(GENETIC_PROFILE_ID, 
            Arrays.asList(SAMPLE_ID), PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedMutationList, result);
        Assert.assertEquals("19", result.get(0).getGene().getChromosome());
    }

    @Test
    public void fetchMetaMutationsInGeneticProfile() throws Exception {

        MutationMeta expectedMutationMeta = new MutationMeta();
        Mockito.when(mutationRepository.fetchMetaMutationsInGeneticProfile(GENETIC_PROFILE_ID, 
            Arrays.asList(SAMPLE_ID))).thenReturn(expectedMutationMeta);
        MutationMeta result = mutationService.fetchMetaMutationsInGeneticProfile(GENETIC_PROFILE_ID, 
            Arrays.asList(SAMPLE_ID));

        Assert.assertEquals(expectedMutationMeta, result);
    }

    @Test
    public void getSampleCountByEntrezGeneIds() throws Exception {

        List<MutationSampleCountByGene> expectedMutationSampleCountByGeneList = new ArrayList<>();
        MutationSampleCountByGene mutationSampleCountByGene = new MutationSampleCountByGene();
        expectedMutationSampleCountByGeneList.add(mutationSampleCountByGene);
        
        Mockito.when(mutationRepository.getSampleCountByEntrezGeneIds(GENETIC_PROFILE_ID, 
            Arrays.asList(ENTREZ_GENE_ID))).thenReturn(expectedMutationSampleCountByGeneList);
        
        List<MutationSampleCountByGene> result = mutationService.getSampleCountByEntrezGeneIds(GENETIC_PROFILE_ID,
            Arrays.asList(ENTREZ_GENE_ID));
        
        Assert.assertEquals(expectedMutationSampleCountByGeneList, result);
    }

    @Test
    public void getSampleCountByKeywords() throws Exception {

        List<MutationSampleCountByKeyword> expectedmutationSampleCountByKeywordList = new ArrayList<>();
        MutationSampleCountByKeyword mutationSampleCountByKeyword = new MutationSampleCountByKeyword();
        expectedmutationSampleCountByKeywordList.add(mutationSampleCountByKeyword);

        Mockito.when(mutationRepository.getSampleCountByKeywords(GENETIC_PROFILE_ID,
            Arrays.asList(KEYWORD))).thenReturn(expectedmutationSampleCountByKeywordList);

        List<MutationSampleCountByKeyword> result = mutationService.getSampleCountByKeywords(GENETIC_PROFILE_ID,
            Arrays.asList(KEYWORD));

        Assert.assertEquals(expectedmutationSampleCountByKeywordList, result);
    }

    @Test
    public void getMutationCountsInGeneticProfileBySampleListId() throws Exception {
        
        List<MutationCount> expectedMutationCountList = new ArrayList<>();
        MutationCount mutationCount = new MutationCount();
        expectedMutationCountList.add(mutationCount);
        
        Mockito.when(mutationRepository.getMutationCountsInGeneticProfileBySampleListId(GENETIC_PROFILE_ID, 
            SAMPLE_LIST_ID)).thenReturn(expectedMutationCountList);
        
        List<MutationCount> result = mutationService.getMutationCountsInGeneticProfileBySampleListId(GENETIC_PROFILE_ID, 
            SAMPLE_LIST_ID);
        
        Assert.assertEquals(expectedMutationCountList, result);
    }

    @Test
    public void fetchMutationCountsInGeneticProfile() throws Exception {

        List<MutationCount> expectedMutationCountList = new ArrayList<>();
        MutationCount mutationCount = new MutationCount();
        expectedMutationCountList.add(mutationCount);

        Mockito.when(mutationRepository.fetchMutationCountsInGeneticProfile(GENETIC_PROFILE_ID,
            Arrays.asList(SAMPLE_LIST_ID))).thenReturn(expectedMutationCountList);

        List<MutationCount> result = mutationService.fetchMutationCountsInGeneticProfile(GENETIC_PROFILE_ID,
            Arrays.asList(SAMPLE_LIST_ID));

        Assert.assertEquals(expectedMutationCountList, result);
    }
}