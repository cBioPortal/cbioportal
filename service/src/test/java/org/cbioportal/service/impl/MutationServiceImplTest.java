package org.cbioportal.service.impl;

import org.cbioportal.model.*;
import org.cbioportal.model.meta.MutationMeta;
import org.cbioportal.persistence.MutationRepository;
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
        Mockito.doAnswer(invocationOnMock -> {
            ((Gene) invocationOnMock.getArguments()[0]).setChromosome("19");
            return null;
        }).when(chromosomeCalculator).setChromosome(gene);
        
        List<Mutation> result = mutationService.getMutationsInMolecularProfileBySampleListId(MOLECULAR_PROFILE_ID, 
            SAMPLE_LIST_ID, Arrays.asList(ENTREZ_GENE_ID_1), null, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedMutationList, result);
        Assert.assertEquals("19", result.get(0).getGene().getChromosome());
    }

    @Test(expected = MolecularProfileNotFoundException.class)
    public void getMutationsInMolecularProfileBySampleListIdMolecularProfileNotFound() throws Exception {

        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenThrow(
            new MolecularProfileNotFoundException(MOLECULAR_PROFILE_ID));
        mutationService.getMutationsInMolecularProfileBySampleListId(MOLECULAR_PROFILE_ID, SAMPLE_LIST_ID, 
            Arrays.asList(ENTREZ_GENE_ID_1), null, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);
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
            SAMPLE_LIST_ID, Arrays.asList(ENTREZ_GENE_ID_1));

        Assert.assertEquals(expectedMutationMeta, result);
    }

    @Test(expected = MolecularProfileNotFoundException.class)
    public void getMetaMutationsInMolecularProfileBySampleListIdMolecularProfileNotFound() throws Exception {
        
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenThrow(
            new MolecularProfileNotFoundException(MOLECULAR_PROFILE_ID));
        mutationService.getMetaMutationsInMolecularProfileBySampleListId(MOLECULAR_PROFILE_ID, SAMPLE_LIST_ID, 
            Arrays.asList(ENTREZ_GENE_ID_1));
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
            Arrays.asList(MOLECULAR_PROFILE_ID), Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID_1), PROJECTION, 
            PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedMutationList, result);
        Assert.assertEquals("19", result.get(0).getGene().getChromosome());
    }

    @Test
    public void getMetaMutationsInMultipleMolecularProfiles() throws Exception {

        MutationMeta expectedMutationMeta = new MutationMeta();
        Mockito.when(mutationRepository.getMetaMutationsInMultipleMolecularProfiles(Arrays.asList(MOLECULAR_PROFILE_ID),
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID_1))).thenReturn(expectedMutationMeta);
        MutationMeta result = mutationService.getMetaMutationsInMultipleMolecularProfiles(
            Arrays.asList(MOLECULAR_PROFILE_ID), Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID_1));

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
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID_1), null, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT,
            DIRECTION);

        Assert.assertEquals(expectedMutationList, result);
        Assert.assertEquals("19", result.get(0).getGene().getChromosome());
    }

    @Test(expected = MolecularProfileNotFoundException.class)
    public void fetchMutationsInMolecularProfileNotFound() throws Exception {
        
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenThrow(
            new MolecularProfileNotFoundException(MOLECULAR_PROFILE_ID));
        mutationService.fetchMutationsInMolecularProfile(MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1),
            Arrays.asList(ENTREZ_GENE_ID_1), null, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);
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
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID_1));

        Assert.assertEquals(expectedMutationMeta, result);
    }

    @Test(expected = MolecularProfileNotFoundException.class)
    public void fetchMetaMutationsInMolecularProfileNotFound() throws Exception {
        
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenThrow(
            new MolecularProfileNotFoundException(MOLECULAR_PROFILE_ID));
        mutationService.fetchMetaMutationsInMolecularProfile(MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1),
            Arrays.asList(ENTREZ_GENE_ID_1));
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