package org.cbioportal.service.impl;

import org.cbioportal.model.*;
import org.cbioportal.model.meta.MutationMeta;
import org.cbioportal.persistence.MutationRepository;
import org.cbioportal.service.GeneticProfileService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
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
    private GeneticProfileService geneticProfileService;
    @Mock
    private ChromosomeCalculator chromosomeCalculator;
    
    @Test
    public void getMutationsInGeneticProfileBySampleListId() throws Exception {
        
        GeneticProfile geneticProfile = new GeneticProfile();
        geneticProfile.setGeneticAlterationType(GeneticProfile.GeneticAlterationType.MUTATION_EXTENDED);
        Mockito.when(geneticProfileService.getGeneticProfile(GENETIC_PROFILE_ID)).thenReturn(geneticProfile);

        List<Mutation> expectedMutationList = new ArrayList<>();
        Mutation mutation = new Mutation();
        Gene gene = new Gene();
        mutation.setGene(gene);
        expectedMutationList.add(mutation);

        Mockito.when(mutationRepository.getMutationsInGeneticProfileBySampleListId(GENETIC_PROFILE_ID, SAMPLE_LIST_ID,
            Arrays.asList(ENTREZ_GENE_ID), null, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION))
            .thenReturn(expectedMutationList);
        Mockito.doAnswer(invocationOnMock -> {
            ((Gene) invocationOnMock.getArguments()[0]).setChromosome("19");
            return null;
        }).when(chromosomeCalculator).setChromosome(gene);
        
        List<Mutation> result = mutationService.getMutationsInGeneticProfileBySampleListId(GENETIC_PROFILE_ID, 
            SAMPLE_LIST_ID, Arrays.asList(ENTREZ_GENE_ID), null, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedMutationList, result);
        Assert.assertEquals("19", result.get(0).getGene().getChromosome());
    }

    @Test(expected = GeneticProfileNotFoundException.class)
    public void getMutationsInGeneticProfileBySampleListIdGeneticProfileNotFound() throws Exception {

        Mockito.when(geneticProfileService.getGeneticProfile(GENETIC_PROFILE_ID)).thenThrow(
            new GeneticProfileNotFoundException(GENETIC_PROFILE_ID));
        mutationService.getMutationsInGeneticProfileBySampleListId(GENETIC_PROFILE_ID, SAMPLE_LIST_ID, 
            Arrays.asList(ENTREZ_GENE_ID), null, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);
    }

    @Test
    public void getMetaMutationsInGeneticProfileBySampleListId() throws Exception {

        GeneticProfile geneticProfile = new GeneticProfile();
        geneticProfile.setGeneticAlterationType(GeneticProfile.GeneticAlterationType.MUTATION_EXTENDED);
        Mockito.when(geneticProfileService.getGeneticProfile(GENETIC_PROFILE_ID)).thenReturn(geneticProfile);
        
        MutationMeta expectedMutationMeta = new MutationMeta();
        Mockito.when(mutationRepository.getMetaMutationsInGeneticProfileBySampleListId(GENETIC_PROFILE_ID,
            SAMPLE_LIST_ID, Arrays.asList(ENTREZ_GENE_ID))).thenReturn(expectedMutationMeta);
        MutationMeta result = mutationService.getMetaMutationsInGeneticProfileBySampleListId(GENETIC_PROFILE_ID, 
            SAMPLE_LIST_ID, Arrays.asList(ENTREZ_GENE_ID));

        Assert.assertEquals(expectedMutationMeta, result);
    }

    @Test(expected = GeneticProfileNotFoundException.class)
    public void getMetaMutationsInGeneticProfileBySampleListIdGeneticProfileNotFound() throws Exception {
        
        Mockito.when(geneticProfileService.getGeneticProfile(GENETIC_PROFILE_ID)).thenThrow(
            new GeneticProfileNotFoundException(GENETIC_PROFILE_ID));
        mutationService.getMetaMutationsInGeneticProfileBySampleListId(GENETIC_PROFILE_ID, SAMPLE_LIST_ID, 
            Arrays.asList(ENTREZ_GENE_ID));
    }

    @Test
    public void getMutationsInMultipleGeneticProfiles() throws Exception {

        List<Mutation> expectedMutationList = new ArrayList<>();
        Mutation mutation = new Mutation();
        Gene gene = new Gene();
        mutation.setGene(gene);
        expectedMutationList.add(mutation);

        Mockito.when(mutationRepository.getMutationsInMultipleGeneticProfiles(Arrays.asList(GENETIC_PROFILE_ID), 
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID), PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, 
            DIRECTION)).thenReturn(expectedMutationList);
        Mockito.doAnswer(invocationOnMock -> {
            ((Gene) invocationOnMock.getArguments()[0]).setChromosome("19");
            return null;
        }).when(chromosomeCalculator).setChromosome(gene);
        
        List<Mutation> result = mutationService.getMutationsInMultipleGeneticProfiles(Arrays.asList(GENETIC_PROFILE_ID),
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID), PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT,
            DIRECTION);

        Assert.assertEquals(expectedMutationList, result);
        Assert.assertEquals("19", result.get(0).getGene().getChromosome());
    }

    @Test
    public void getMetaMutationsInMultipleGeneticProfiles() throws Exception {

        MutationMeta expectedMutationMeta = new MutationMeta();
        Mockito.when(mutationRepository.getMetaMutationsInMultipleGeneticProfiles(Arrays.asList(GENETIC_PROFILE_ID),
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID))).thenReturn(expectedMutationMeta);
        MutationMeta result = mutationService.getMetaMutationsInMultipleGeneticProfiles(
            Arrays.asList(GENETIC_PROFILE_ID), Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID));

        Assert.assertEquals(expectedMutationMeta, result);
    }

    @Test
    public void fetchMutationsInGeneticProfile() throws Exception {

        GeneticProfile geneticProfile = new GeneticProfile();
        geneticProfile.setGeneticAlterationType(GeneticProfile.GeneticAlterationType.MUTATION_EXTENDED);
        Mockito.when(geneticProfileService.getGeneticProfile(GENETIC_PROFILE_ID)).thenReturn(geneticProfile);

        List<Mutation> expectedMutationList = new ArrayList<>();
        Mutation mutation = new Mutation();
        Gene gene = new Gene();
        mutation.setGene(gene);
        expectedMutationList.add(mutation);

        Mockito.when(mutationRepository.fetchMutationsInGeneticProfile(GENETIC_PROFILE_ID, Arrays.asList(SAMPLE_ID1),
            Arrays.asList(ENTREZ_GENE_ID), null, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION))
            .thenReturn(expectedMutationList);
        Mockito.doAnswer(invocationOnMock -> {
            ((Gene) invocationOnMock.getArguments()[0]).setChromosome("19");
            return null;
        }).when(chromosomeCalculator).setChromosome(gene);

        List<Mutation> result = mutationService.fetchMutationsInGeneticProfile(GENETIC_PROFILE_ID, 
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID), null, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT,
            DIRECTION);

        Assert.assertEquals(expectedMutationList, result);
        Assert.assertEquals("19", result.get(0).getGene().getChromosome());
    }

    @Test(expected = GeneticProfileNotFoundException.class)
    public void fetchMutationsInGeneticProfileNotFound() throws Exception {
        
        Mockito.when(geneticProfileService.getGeneticProfile(GENETIC_PROFILE_ID)).thenThrow(
            new GeneticProfileNotFoundException(GENETIC_PROFILE_ID));
        mutationService.fetchMutationsInGeneticProfile(GENETIC_PROFILE_ID, Arrays.asList(SAMPLE_ID1),
            Arrays.asList(ENTREZ_GENE_ID), null, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);
    }

    @Test
    public void fetchMetaMutationsInGeneticProfile() throws Exception {

        GeneticProfile geneticProfile = new GeneticProfile();
        geneticProfile.setGeneticAlterationType(GeneticProfile.GeneticAlterationType.MUTATION_EXTENDED);
        Mockito.when(geneticProfileService.getGeneticProfile(GENETIC_PROFILE_ID)).thenReturn(geneticProfile);

        MutationMeta expectedMutationMeta = new MutationMeta();
        Mockito.when(mutationRepository.fetchMetaMutationsInGeneticProfile(GENETIC_PROFILE_ID, 
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID))).thenReturn(expectedMutationMeta);
        MutationMeta result = mutationService.fetchMetaMutationsInGeneticProfile(GENETIC_PROFILE_ID, 
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID));

        Assert.assertEquals(expectedMutationMeta, result);
    }

    @Test(expected = GeneticProfileNotFoundException.class)
    public void fetchMetaMutationsInGeneticProfileNotFound() throws Exception {
        
        Mockito.when(geneticProfileService.getGeneticProfile(GENETIC_PROFILE_ID)).thenThrow(
            new GeneticProfileNotFoundException(GENETIC_PROFILE_ID));
        mutationService.fetchMetaMutationsInGeneticProfile(GENETIC_PROFILE_ID, Arrays.asList(SAMPLE_ID1),
            Arrays.asList(ENTREZ_GENE_ID));
    }

    @Test
    public void getSampleCountByEntrezGeneIds() throws Exception {

        GeneticProfile geneticProfile = new GeneticProfile();
        geneticProfile.setGeneticAlterationType(GeneticProfile.GeneticAlterationType.MUTATION_EXTENDED);
        Mockito.when(geneticProfileService.getGeneticProfile(GENETIC_PROFILE_ID)).thenReturn(geneticProfile);

        List<MutationSampleCountByGene> expectedMutationSampleCountByGeneList = new ArrayList<>();
        MutationSampleCountByGene mutationSampleCountByGene = new MutationSampleCountByGene();
        expectedMutationSampleCountByGeneList.add(mutationSampleCountByGene);
        
        Mockito.when(mutationRepository.getSampleCountByEntrezGeneIdsAndSampleIds(GENETIC_PROFILE_ID, null,
            Arrays.asList(ENTREZ_GENE_ID))).thenReturn(expectedMutationSampleCountByGeneList);
        
        List<MutationSampleCountByGene> result = mutationService.getSampleCountByEntrezGeneIdsAndSampleIds(
            GENETIC_PROFILE_ID, null, Arrays.asList(ENTREZ_GENE_ID));
        
        Assert.assertEquals(expectedMutationSampleCountByGeneList, result);
    }

    @Test(expected = GeneticProfileNotFoundException.class)
    public void getSampleCountByEntrezGeneIdsGeneticProfileNotFound() throws Exception {
        
        Mockito.when(geneticProfileService.getGeneticProfile(GENETIC_PROFILE_ID)).thenThrow(
            new GeneticProfileNotFoundException(GENETIC_PROFILE_ID));
        mutationService.getSampleCountByEntrezGeneIdsAndSampleIds(GENETIC_PROFILE_ID, null,
            Arrays.asList(ENTREZ_GENE_ID));
    }

    @Test
    public void getSampleCountByKeywords() throws Exception {

        GeneticProfile geneticProfile = new GeneticProfile();
        geneticProfile.setGeneticAlterationType(GeneticProfile.GeneticAlterationType.MUTATION_EXTENDED);
        Mockito.when(geneticProfileService.getGeneticProfile(GENETIC_PROFILE_ID)).thenReturn(geneticProfile);

        List<MutationSampleCountByKeyword> expectedmutationSampleCountByKeywordList = new ArrayList<>();
        MutationSampleCountByKeyword mutationSampleCountByKeyword = new MutationSampleCountByKeyword();
        expectedmutationSampleCountByKeywordList.add(mutationSampleCountByKeyword);

        Mockito.when(mutationRepository.getSampleCountByKeywords(GENETIC_PROFILE_ID,
            Arrays.asList(KEYWORD))).thenReturn(expectedmutationSampleCountByKeywordList);

        List<MutationSampleCountByKeyword> result = mutationService.getSampleCountByKeywords(GENETIC_PROFILE_ID,
            Arrays.asList(KEYWORD));

        Assert.assertEquals(expectedmutationSampleCountByKeywordList, result);
    }

    @Test(expected = GeneticProfileNotFoundException.class)
    public void getSampleCountByKeywordsGeneticProfileNotFound() throws Exception {
        
        Mockito.when(geneticProfileService.getGeneticProfile(GENETIC_PROFILE_ID)).thenThrow(
            new GeneticProfileNotFoundException(GENETIC_PROFILE_ID));
        mutationService.getSampleCountByKeywords(GENETIC_PROFILE_ID, Arrays.asList(KEYWORD));
    }

    @Test
    public void getMutationCountsInGeneticProfileBySampleListId() throws Exception {

        GeneticProfile geneticProfile = new GeneticProfile();
        geneticProfile.setGeneticAlterationType(GeneticProfile.GeneticAlterationType.MUTATION_EXTENDED);
        Mockito.when(geneticProfileService.getGeneticProfile(GENETIC_PROFILE_ID)).thenReturn(geneticProfile);
        
        List<MutationCount> expectedMutationCountList = new ArrayList<>();
        MutationCount mutationCount = new MutationCount();
        expectedMutationCountList.add(mutationCount);
        
        Mockito.when(mutationRepository.getMutationCountsInGeneticProfileBySampleListId(GENETIC_PROFILE_ID, 
            SAMPLE_LIST_ID)).thenReturn(expectedMutationCountList);
        
        List<MutationCount> result = mutationService.getMutationCountsInGeneticProfileBySampleListId(GENETIC_PROFILE_ID, 
            SAMPLE_LIST_ID);
        
        Assert.assertEquals(expectedMutationCountList, result);
    }

    @Test(expected = GeneticProfileNotFoundException.class)
    public void getMutationCountsInGeneticProfileBySampleListIdGeneticProfileNotFound() throws Exception {

        Mockito.when(geneticProfileService.getGeneticProfile(GENETIC_PROFILE_ID)).thenThrow(
            new GeneticProfileNotFoundException(GENETIC_PROFILE_ID));
        mutationService.getMutationCountsInGeneticProfileBySampleListId(GENETIC_PROFILE_ID, SAMPLE_LIST_ID);
    }

    @Test
    public void fetchMutationCountsInGeneticProfile() throws Exception {

        GeneticProfile geneticProfile = new GeneticProfile();
        geneticProfile.setGeneticAlterationType(GeneticProfile.GeneticAlterationType.MUTATION_EXTENDED);
        Mockito.when(geneticProfileService.getGeneticProfile(GENETIC_PROFILE_ID)).thenReturn(geneticProfile);

        List<MutationCount> expectedMutationCountList = new ArrayList<>();
        MutationCount mutationCount = new MutationCount();
        expectedMutationCountList.add(mutationCount);

        Mockito.when(mutationRepository.fetchMutationCountsInGeneticProfile(GENETIC_PROFILE_ID,
            Arrays.asList(SAMPLE_LIST_ID))).thenReturn(expectedMutationCountList);

        List<MutationCount> result = mutationService.fetchMutationCountsInGeneticProfile(GENETIC_PROFILE_ID,
            Arrays.asList(SAMPLE_LIST_ID));

        Assert.assertEquals(expectedMutationCountList, result);
    }

    @Test(expected = GeneticProfileNotFoundException.class)
    public void fetchMutationCountsInGeneticProfileNotFound() throws Exception {
        
        Mockito.when(geneticProfileService.getGeneticProfile(GENETIC_PROFILE_ID)).thenThrow(
            new GeneticProfileNotFoundException(GENETIC_PROFILE_ID));
        mutationService.fetchMutationCountsInGeneticProfile(GENETIC_PROFILE_ID, Arrays.asList(SAMPLE_LIST_ID));
    }

    @Test
    public void fetchMutationCountsByPosition() throws Exception {

        MutationCountByPosition expectedMutationCountByPosition = new MutationCountByPosition();
        Mockito.when(mutationRepository.getMutationCountByPosition(ENTREZ_GENE_ID,
            PROTEIN_POS_START, PROTEIN_POS_END)).thenReturn(expectedMutationCountByPosition);
        
        List<MutationCountByPosition> result = mutationService.fetchMutationCountsByPosition(
            Arrays.asList(ENTREZ_GENE_ID), Arrays.asList(PROTEIN_POS_START), Arrays.asList(PROTEIN_POS_END));
        
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(expectedMutationCountByPosition, result.get(0));
    }
}