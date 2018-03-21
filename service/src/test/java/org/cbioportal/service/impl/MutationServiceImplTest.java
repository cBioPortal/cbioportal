package org.cbioportal.service.impl;

import org.cbioportal.model.*;
import org.cbioportal.model.meta.MutationMeta;
import org.cbioportal.persistence.MutationRepository;
import org.cbioportal.persistence.SampleListRepository;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
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
    private MolecularProfileService molecularProfileService;
    @Mock
    private ChromosomeCalculator chromosomeCalculator;
    @Mock
    private SampleListRepository sampleListRepository;
    @Mock
    private GenePanelService genePanelService;
    @Mock
    private OffsetCalculator offsetCalculator;
    
    @Test
    public void getMutationsInMolecularProfileBySampleListId() throws Exception {
        
        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED);
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(molecularProfile);

        List<Mutation> expectedMutationList = new ArrayList<>();
        Mutation mutation = new Mutation();
        Gene gene = new Gene();
        mutation.setGene(gene);
        expectedMutationList.add(mutation);

        Mockito.when(mutationRepository.getMutationsInMolecularProfileBySampleListId(MOLECULAR_PROFILE_ID, 
            SAMPLE_LIST_ID, Arrays.asList(ENTREZ_GENE_ID_1), null, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION))
            .thenReturn(expectedMutationList);
        
        Mockito.when(sampleListRepository.getAllSampleIdsInSampleList(SAMPLE_LIST_ID)).thenReturn(Arrays.asList(SAMPLE_ID1));
        Mockito.doAnswer(invocationOnMock -> {
            ((Gene) invocationOnMock.getArguments()[0]).setChromosome("19");
            return null;
        }).when(chromosomeCalculator).setChromosome(gene);
        
        List<Mutation> result = mutationService.getMutationsInMolecularProfileBySampleListId(MOLECULAR_PROFILE_ID, 
            SAMPLE_LIST_ID, Arrays.asList(ENTREZ_GENE_ID_1), null, null, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedMutationList, result);
        Assert.assertEquals("19", result.get(0).getGene().getChromosome());
    }

    @Test
    public void getMutationsInMolecularProfileBySampleListIdIncludeNonMutated() throws Exception {
        
        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED);
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(molecularProfile);

        List<Mutation> expectedMutationList = new ArrayList<>();
        Mutation mutation = new Mutation();
        mutation.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        mutation.setSampleId(SAMPLE_ID1);
        mutation.setEntrezGeneId(ENTREZ_GENE_ID_1);
        Gene gene = new Gene();
        mutation.setGene(gene);
        expectedMutationList.add(mutation);

        Mockito.when(mutationRepository.getMutationsInMolecularProfileBySampleListId(MOLECULAR_PROFILE_ID, 
            SAMPLE_LIST_ID, Arrays.asList(ENTREZ_GENE_ID_1, ENTREZ_GENE_ID_2), null, PROJECTION, PAGE_SIZE, 
            PAGE_NUMBER, SORT, DIRECTION)).thenReturn(expectedMutationList);
        
        Mockito.when(sampleListRepository.getAllSampleIdsInSampleList(SAMPLE_LIST_ID)).thenReturn(Arrays.asList(SAMPLE_ID1));
        Mockito.doAnswer(invocationOnMock -> {
            ((Gene) invocationOnMock.getArguments()[0]).setChromosome("19");
            return null;
        }).when(chromosomeCalculator).setChromosome(gene);

        List<GenePanelData> genePanelDataList = new ArrayList<>();
        GenePanelData genePanelData1 = new GenePanelData();
        genePanelData1.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        genePanelData1.setSampleId(SAMPLE_ID1);
        genePanelData1.setEntrezGeneId(ENTREZ_GENE_ID_1);
        genePanelData1.setSequenced(true);
        genePanelDataList.add(genePanelData1);
        GenePanelData genePanelData2 = new GenePanelData();
        genePanelData2.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        genePanelData2.setSampleId(SAMPLE_ID1);
        genePanelData2.setEntrezGeneId(ENTREZ_GENE_ID_2);
        genePanelData2.setSequenced(false);
        genePanelDataList.add(genePanelData2);
        GenePanelData genePanelData3 = new GenePanelData();
        genePanelData3.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        genePanelData3.setSampleId(SAMPLE_ID1);
        genePanelData3.setEntrezGeneId(ENTREZ_GENE_ID_3);
        genePanelData3.setSequenced(true);
        genePanelDataList.add(genePanelData3);

        Mockito.when(genePanelService.fetchGenePanelDataInMultipleMolecularProfiles(Arrays.asList(MOLECULAR_PROFILE_ID), 
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID_1, ENTREZ_GENE_ID_2))).thenReturn(genePanelDataList);
        
        Mockito.when(offsetCalculator.calculate(PAGE_SIZE, PAGE_NUMBER)).thenReturn(null);

        List<Mutation> result = mutationService.getMutationsInMolecularProfileBySampleListId(MOLECULAR_PROFILE_ID, 
            SAMPLE_LIST_ID, Arrays.asList(ENTREZ_GENE_ID_1, ENTREZ_GENE_ID_2), null, true, PROJECTION, PAGE_SIZE, 
            PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(3, result.size());
        Mutation mutation1 = result.get(0);
        Assert.assertEquals(ENTREZ_GENE_ID_1, mutation1.getEntrezGeneId());
        Assert.assertTrue(mutation1.getSequenced());
        Assert.assertFalse(mutation1.getWildType());
        Mutation mutation2 = result.get(1);
        Assert.assertEquals(ENTREZ_GENE_ID_2, mutation2.getEntrezGeneId());
        Assert.assertFalse(mutation2.getSequenced());
        Assert.assertFalse(mutation2.getWildType());
        Mutation mutation3 = result.get(2);
        Assert.assertEquals(ENTREZ_GENE_ID_3, mutation3.getEntrezGeneId());
        Assert.assertTrue(mutation3.getSequenced());
        Assert.assertTrue(mutation3.getWildType());
    }

    @Test(expected = MolecularProfileNotFoundException.class)
    public void getMutationsInMolecularProfileBySampleListIdMolecularProfileNotFound() throws Exception {

        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenThrow(
            new MolecularProfileNotFoundException(MOLECULAR_PROFILE_ID));
        mutationService.getMutationsInMolecularProfileBySampleListId(MOLECULAR_PROFILE_ID, SAMPLE_LIST_ID, 
            Arrays.asList(ENTREZ_GENE_ID_1), null, null, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);
    }

    @Test
    public void getMetaMutationsInMolecularProfileBySampleListId() throws Exception {

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED);
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(molecularProfile);
        
        MutationMeta expectedMutationMeta = new MutationMeta();
        Mockito.when(mutationRepository.getMetaMutationsInMolecularProfileBySampleListId(MOLECULAR_PROFILE_ID,
            SAMPLE_LIST_ID, Arrays.asList(ENTREZ_GENE_ID_1))).thenReturn(expectedMutationMeta);
        MutationMeta result = mutationService.getMetaMutationsInMolecularProfileBySampleListId(MOLECULAR_PROFILE_ID, 
            SAMPLE_LIST_ID, Arrays.asList(ENTREZ_GENE_ID_1), null);

        Assert.assertEquals(expectedMutationMeta, result);
    }

    @Test(expected = MolecularProfileNotFoundException.class)
    public void getMetaMutationsInMolecularProfileBySampleListIdMolecularProfileNotFound() throws Exception {
        
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenThrow(
            new MolecularProfileNotFoundException(MOLECULAR_PROFILE_ID));
        mutationService.getMetaMutationsInMolecularProfileBySampleListId(MOLECULAR_PROFILE_ID, SAMPLE_LIST_ID, 
            Arrays.asList(ENTREZ_GENE_ID_1), null);
    }

    @Test
    public void getMutationsInMultipleMolecularProfiles() throws Exception {

        List<Mutation> expectedMutationList = new ArrayList<>();
        Mutation mutation = new Mutation();
        Gene gene = new Gene();
        mutation.setGene(gene);
        expectedMutationList.add(mutation);

        Mockito.when(mutationRepository.getMutationsInMultipleMolecularProfiles(Arrays.asList(MOLECULAR_PROFILE_ID), 
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID_1), PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, 
            DIRECTION)).thenReturn(expectedMutationList);
        Mockito.doAnswer(invocationOnMock -> {
            ((Gene) invocationOnMock.getArguments()[0]).setChromosome("19");
            return null;
        }).when(chromosomeCalculator).setChromosome(gene);
        
        List<Mutation> result = mutationService.getMutationsInMultipleMolecularProfiles(
            Arrays.asList(MOLECULAR_PROFILE_ID), Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID_1), null, 
            PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedMutationList, result);
        Assert.assertEquals("19", result.get(0).getGene().getChromosome());
    }

    @Test
    public void getMetaMutationsInMultipleMolecularProfiles() throws Exception {

        MutationMeta expectedMutationMeta = new MutationMeta();
        Mockito.when(mutationRepository.getMetaMutationsInMultipleMolecularProfiles(Arrays.asList(MOLECULAR_PROFILE_ID),
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID_1))).thenReturn(expectedMutationMeta);
        MutationMeta result = mutationService.getMetaMutationsInMultipleMolecularProfiles(
            Arrays.asList(MOLECULAR_PROFILE_ID), Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID_1), null);

        Assert.assertEquals(expectedMutationMeta, result);
    }

    @Test
    public void fetchMutationsInMolecularProfile() throws Exception {

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED);
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(molecularProfile);

        List<Mutation> expectedMutationList = new ArrayList<>();
        Mutation mutation = new Mutation();
        Gene gene = new Gene();
        mutation.setGene(gene);
        expectedMutationList.add(mutation);

        Mockito.when(mutationRepository.fetchMutationsInMolecularProfile(MOLECULAR_PROFILE_ID,
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID_1), null, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, 
            DIRECTION)).thenReturn(expectedMutationList);
        Mockito.doAnswer(invocationOnMock -> {
            ((Gene) invocationOnMock.getArguments()[0]).setChromosome("19");
            return null;
        }).when(chromosomeCalculator).setChromosome(gene);

        List<Mutation> result = mutationService.fetchMutationsInMolecularProfile(MOLECULAR_PROFILE_ID, 
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID_1), null, null, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT,
            DIRECTION);

        Assert.assertEquals(expectedMutationList, result);
        Assert.assertEquals("19", result.get(0).getGene().getChromosome());
    }

    @Test(expected = MolecularProfileNotFoundException.class)
    public void fetchMutationsInMolecularProfileNotFound() throws Exception {
        
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenThrow(
            new MolecularProfileNotFoundException(MOLECULAR_PROFILE_ID));
        mutationService.fetchMutationsInMolecularProfile(MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1),
            Arrays.asList(ENTREZ_GENE_ID_1), null, null, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);
    }

    @Test
    public void fetchMetaMutationsInMolecularProfile() throws Exception {

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED);
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(molecularProfile);

        MutationMeta expectedMutationMeta = new MutationMeta();
        Mockito.when(mutationRepository.fetchMetaMutationsInMolecularProfile(MOLECULAR_PROFILE_ID, 
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID_1))).thenReturn(expectedMutationMeta);
        MutationMeta result = mutationService.fetchMetaMutationsInMolecularProfile(MOLECULAR_PROFILE_ID, 
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID_1), null);

        Assert.assertEquals(expectedMutationMeta, result);
    }

    @Test(expected = MolecularProfileNotFoundException.class)
    public void fetchMetaMutationsInMolecularProfileNotFound() throws Exception {
        
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenThrow(
            new MolecularProfileNotFoundException(MOLECULAR_PROFILE_ID));
        mutationService.fetchMetaMutationsInMolecularProfile(MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1),
            Arrays.asList(ENTREZ_GENE_ID_1), null);
    }

    @Test
    public void getSampleCountByEntrezGeneIds() throws Exception {

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED);
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(molecularProfile);

        List<MutationCountByGene> expectedMutationSampleCountByGeneList = new ArrayList<>();
        MutationCountByGene mutationSampleCountByGene = new MutationCountByGene();
        expectedMutationSampleCountByGeneList.add(mutationSampleCountByGene);
        
        Mockito.when(mutationRepository.getSampleCountByEntrezGeneIdsAndSampleIds(MOLECULAR_PROFILE_ID, null,
            Arrays.asList(ENTREZ_GENE_ID_1))).thenReturn(expectedMutationSampleCountByGeneList);
        
        List<MutationCountByGene> result = mutationService.getSampleCountByEntrezGeneIdsAndSampleIds(
            MOLECULAR_PROFILE_ID, null, Arrays.asList(ENTREZ_GENE_ID_1));
        
        Assert.assertEquals(expectedMutationSampleCountByGeneList, result);
    }

    @Test(expected = MolecularProfileNotFoundException.class)
    public void getSampleCountByEntrezGeneIdsMolecularProfileNotFound() throws Exception {
        
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenThrow(
            new MolecularProfileNotFoundException(MOLECULAR_PROFILE_ID));
        mutationService.getSampleCountByEntrezGeneIdsAndSampleIds(MOLECULAR_PROFILE_ID, null,
            Arrays.asList(ENTREZ_GENE_ID_1));
    }

    @Test
    public void getPatientCountByEntrezGeneIds() throws Exception {

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED);
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(molecularProfile);

        List<MutationCountByGene> expectedMutationPatientCountByGeneList = new ArrayList<>();
        MutationCountByGene mutationPatientCountByGene = new MutationCountByGene();
        expectedMutationPatientCountByGeneList.add(mutationPatientCountByGene);
        
        Mockito.when(mutationRepository.getPatientCountByEntrezGeneIdsAndSampleIds(MOLECULAR_PROFILE_ID, null,
            Arrays.asList(ENTREZ_GENE_ID_1))).thenReturn(expectedMutationPatientCountByGeneList);
        
        List<MutationCountByGene> result = mutationService.getPatientCountByEntrezGeneIdsAndSampleIds(
            MOLECULAR_PROFILE_ID, null, Arrays.asList(ENTREZ_GENE_ID_1));
        
        Assert.assertEquals(expectedMutationPatientCountByGeneList, result);
    }

    @Test(expected = MolecularProfileNotFoundException.class)
    public void getPatientCountByEntrezGeneIdsMolecularProfileNotFound() throws Exception {
        
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenThrow(
            new MolecularProfileNotFoundException(MOLECULAR_PROFILE_ID));
        mutationService.getPatientCountByEntrezGeneIdsAndSampleIds(MOLECULAR_PROFILE_ID, null,
            Arrays.asList(ENTREZ_GENE_ID_1));
    }
    
    @Test
    public void getMutationCountsInMolecularProfileBySampleListId() throws Exception {

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED);
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(molecularProfile);
        
        List<MutationCount> expectedMutationCountList = new ArrayList<>();
        MutationCount mutationCount = new MutationCount();
        expectedMutationCountList.add(mutationCount);
        
        Mockito.when(mutationRepository.getMutationCountsInMolecularProfileBySampleListId(MOLECULAR_PROFILE_ID, 
            SAMPLE_LIST_ID)).thenReturn(expectedMutationCountList);
        
        List<MutationCount> result = mutationService.getMutationCountsInMolecularProfileBySampleListId(
            MOLECULAR_PROFILE_ID, SAMPLE_LIST_ID);
        
        Assert.assertEquals(expectedMutationCountList, result);
    }

    @Test(expected = MolecularProfileNotFoundException.class)
    public void getMutationCountsInMolecularProfileBySampleListIdMolecularProfileNotFound() throws Exception {

        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenThrow(
            new MolecularProfileNotFoundException(MOLECULAR_PROFILE_ID));
        mutationService.getMutationCountsInMolecularProfileBySampleListId(MOLECULAR_PROFILE_ID, SAMPLE_LIST_ID);
    }

    @Test
    public void fetchMutationCountsInMolecularProfile() throws Exception {

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED);
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(molecularProfile);

        List<MutationCount> expectedMutationCountList = new ArrayList<>();
        MutationCount mutationCount = new MutationCount();
        expectedMutationCountList.add(mutationCount);

        Mockito.when(mutationRepository.fetchMutationCountsInMolecularProfile(MOLECULAR_PROFILE_ID,
            Arrays.asList(SAMPLE_LIST_ID))).thenReturn(expectedMutationCountList);

        List<MutationCount> result = mutationService.fetchMutationCountsInMolecularProfile(MOLECULAR_PROFILE_ID,
            Arrays.asList(SAMPLE_LIST_ID));

        Assert.assertEquals(expectedMutationCountList, result);
    }

    @Test(expected = MolecularProfileNotFoundException.class)
    public void fetchMutationCountsInMolecularProfileNotFound() throws Exception {
        
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenThrow(
            new MolecularProfileNotFoundException(MOLECULAR_PROFILE_ID));
        mutationService.fetchMutationCountsInMolecularProfile(MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_LIST_ID));
    }

    @Test
    public void fetchMutationCountsByPosition() throws Exception {

        MutationCountByPosition expectedMutationCountByPosition = new MutationCountByPosition();
        Mockito.when(mutationRepository.getMutationCountByPosition(ENTREZ_GENE_ID_1,
            PROTEIN_POS_START, PROTEIN_POS_END)).thenReturn(expectedMutationCountByPosition);
        
        List<MutationCountByPosition> result = mutationService.fetchMutationCountsByPosition(
            Arrays.asList(ENTREZ_GENE_ID_1), Arrays.asList(PROTEIN_POS_START), Arrays.asList(PROTEIN_POS_END));
        
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(expectedMutationCountByPosition, result.get(0));
    }
}