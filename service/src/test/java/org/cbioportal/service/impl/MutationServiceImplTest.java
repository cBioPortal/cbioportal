package org.cbioportal.service.impl;

import org.cbioportal.model.Gene;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.meta.BaseMeta;
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
    public void getMutationsInGeneticProfile() throws Exception {

        List<Mutation> expectedMutationList = new ArrayList<>();
        Mutation mutation = new Mutation();
        Gene gene = new Gene();
        mutation.setGene(gene);
        expectedMutationList.add(mutation);

        Mockito.when(mutationRepository.getMutationsInGeneticProfile(GENETIC_PROFILE_ID, SAMPLE_ID, PROJECTION, 
            PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION)).thenReturn(expectedMutationList);
        Mockito.doAnswer(invocationOnMock -> {
            ((Gene) invocationOnMock.getArguments()[0]).setChromosome("19");
            return null;
        }).when(chromosomeCalculator).setChromosome(gene);
        
        List<Mutation> result = mutationService.getMutationsInGeneticProfile(GENETIC_PROFILE_ID, SAMPLE_ID, PROJECTION,
            PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedMutationList, result);
        Assert.assertEquals("19", result.get(0).getGene().getChromosome());
    }

    @Test
    public void getMetaMutationsInGeneticProfile() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(mutationRepository.getMetaMutationsInGeneticProfile(GENETIC_PROFILE_ID, SAMPLE_ID))
            .thenReturn(expectedBaseMeta);
        BaseMeta result = mutationService.getMetaMutationsInGeneticProfile(GENETIC_PROFILE_ID, SAMPLE_ID);

        Assert.assertEquals(expectedBaseMeta, result);
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

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(mutationRepository.fetchMetaMutationsInGeneticProfile(GENETIC_PROFILE_ID, 
            Arrays.asList(SAMPLE_ID))).thenReturn(expectedBaseMeta);
        BaseMeta result = mutationService.fetchMetaMutationsInGeneticProfile(GENETIC_PROFILE_ID, 
            Arrays.asList(SAMPLE_ID));

        Assert.assertEquals(expectedBaseMeta, result);
    }
}