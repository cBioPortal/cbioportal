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
    public void getMutationsInGeneticProfileBySampleListId() throws Exception {

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED);
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(molecularProfile);

        List<Mutation> expectedMutationList = new ArrayList<>();
        Mutation mutation = new Mutation();
        Gene gene = new Gene();
        mutation.setGene(gene);
        mutation.setChromosome(CHR_ID);
        expectedMutationList.add(mutation);

        Mockito.when(mutationRepository.getMutationsInMolecularProfileBySampleListId(MOLECULAR_PROFILE_ID, SAMPLE_LIST_ID,
            Arrays.asList(ENTREZ_GENE_ID), null, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION))
            .thenReturn(expectedMutationList);
        
        List<Mutation> result = mutationService.getMutationsInMolecularProfileBySampleListId(MOLECULAR_PROFILE_ID, 
            SAMPLE_LIST_ID, Arrays.asList(ENTREZ_GENE_ID), null, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedMutationList, result);
        Assert.assertEquals(CHR_ID,result.get(0).getChromosome());
    }

    @Test(expected = MolecularProfileNotFoundException.class)
    public void getMutationsInMolecularProfileBySampleListIdMolecularProfileNotFound() throws Exception {

        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenThrow(
            new MolecularProfileNotFoundException(MOLECULAR_PROFILE_ID));
        mutationService.getMutationsInMolecularProfileBySampleListId(MOLECULAR_PROFILE_ID, SAMPLE_LIST_ID, 
            Arrays.asList(ENTREZ_GENE_ID), null, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);
    }

    @Test
    public void getMetaMutationsInMolecularProfileBySampleListId() throws Exception {

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED);
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(molecularProfile);
        
        MutationMeta expectedMutationMeta = new MutationMeta();
        Mockito.when(mutationRepository.getMetaMutationsInMolecularProfileBySampleListId(MOLECULAR_PROFILE_ID,
            SAMPLE_LIST_ID, Arrays.asList(ENTREZ_GENE_ID))).thenReturn(expectedMutationMeta);
        MutationMeta result = mutationService.getMetaMutationsInMolecularProfileBySampleListId(MOLECULAR_PROFILE_ID,
            SAMPLE_LIST_ID, Arrays.asList(ENTREZ_GENE_ID));

        Assert.assertEquals(expectedMutationMeta, result);
    }

    @Test(expected = MolecularProfileNotFoundException.class)
    public void getMetaMutationsInMolecularProfileBySampleListIdMolecularProfileNotFound() throws Exception {

        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenThrow(
            new MolecularProfileNotFoundException(MOLECULAR_PROFILE_ID));
        mutationService.getMetaMutationsInMolecularProfileBySampleListId(MOLECULAR_PROFILE_ID, SAMPLE_LIST_ID,
            Arrays.asList(ENTREZ_GENE_ID));
    }

    @Test
    public void getMutationsInMultipleMolecularProfiles() throws Exception {

        List<Mutation> expectedMutationList = new ArrayList<>();
        Mutation mutation = new Mutation();
        Gene gene = new Gene();
        mutation.setGene(gene);
        mutation.setChromosome(CHR_ID);
        expectedMutationList.add(mutation);

        Mockito.when(mutationRepository.getMutationsInMultipleMolecularProfiles(Arrays.asList(MOLECULAR_PROFILE_ID),
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID), PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT,
            DIRECTION)).thenReturn(expectedMutationList);

        List<Mutation> result = mutationService.getMutationsInMultipleMolecularProfiles(
            Arrays.asList(MOLECULAR_PROFILE_ID), Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID), PROJECTION,
            PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedMutationList, result);
        Assert.assertEquals(CHR_ID, result.get(0).getChromosome());
    }

    @Test
    public void getMetaMutationsInMultipleMolecularProfiles() throws Exception {

        MutationMeta expectedMutationMeta = new MutationMeta();
        Mockito.when(mutationRepository.getMetaMutationsInMultipleMolecularProfiles(Arrays.asList(MOLECULAR_PROFILE_ID),
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID))).thenReturn(expectedMutationMeta);
        MutationMeta result = mutationService.getMetaMutationsInMultipleMolecularProfiles(
            Arrays.asList(MOLECULAR_PROFILE_ID), Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID));

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
        mutation.setChromosome(CHR_ID);
        expectedMutationList.add(mutation);

        Mockito.when(mutationRepository.fetchMutationsInMolecularProfile(MOLECULAR_PROFILE_ID,
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID), null, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT,
            DIRECTION)).thenReturn(expectedMutationList);

        List<Mutation> result = mutationService.fetchMutationsInMolecularProfile(MOLECULAR_PROFILE_ID,
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID), null, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT,
            DIRECTION);

        Assert.assertEquals(expectedMutationList, result);
        Assert.assertEquals(CHR_ID, result.get(0).getChromosome());
    }

    @Test(expected = MolecularProfileNotFoundException.class)
    public void fetchMutationsInMolecularProfileNotFound() throws Exception {

        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenThrow(
            new MolecularProfileNotFoundException(MOLECULAR_PROFILE_ID));
        mutationService.fetchMutationsInMolecularProfile(MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1),
            Arrays.asList(ENTREZ_GENE_ID), null, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);
    }

    @Test
    public void fetchMetaMutationsInMolecularProfile() throws Exception {

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED);
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(molecularProfile);

        MutationMeta expectedMutationMeta = new MutationMeta();
        Mockito.when(mutationRepository.fetchMetaMutationsInMolecularProfile(MOLECULAR_PROFILE_ID,
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID))).thenReturn(expectedMutationMeta);
        MutationMeta result = mutationService.fetchMetaMutationsInMolecularProfile(MOLECULAR_PROFILE_ID,
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID));

        Assert.assertEquals(expectedMutationMeta, result);
    }

    @Test(expected = MolecularProfileNotFoundException.class)
    public void fetchMetaMutationsInMolecularProfileNotFound() throws Exception {

        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenThrow(
            new MolecularProfileNotFoundException(MOLECULAR_PROFILE_ID));
        mutationService.fetchMetaMutationsInMolecularProfile(MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1),
            Arrays.asList(ENTREZ_GENE_ID));
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
            Arrays.asList(ENTREZ_GENE_ID))).thenReturn(expectedMutationSampleCountByGeneList);

        List<MutationCountByGene> result = mutationService.getSampleCountByEntrezGeneIdsAndSampleIds(
            MOLECULAR_PROFILE_ID, null, Arrays.asList(ENTREZ_GENE_ID));
        
        Assert.assertEquals(expectedMutationSampleCountByGeneList, result);
    }

    @Test(expected = MolecularProfileNotFoundException.class)
    public void getSampleCountByEntrezGeneIdsMolecularProfileNotFound() throws Exception {

        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenThrow(
            new MolecularProfileNotFoundException(MOLECULAR_PROFILE_ID));
        mutationService.getSampleCountByEntrezGeneIdsAndSampleIds(MOLECULAR_PROFILE_ID, null,
            Arrays.asList(ENTREZ_GENE_ID));
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
        Mockito.when(mutationRepository.getMutationCountByPosition(ENTREZ_GENE_ID,
            PROTEIN_POS_START, PROTEIN_POS_END)).thenReturn(expectedMutationCountByPosition);
        
        List<MutationCountByPosition> result = mutationService.fetchMutationCountsByPosition(
            Arrays.asList(ENTREZ_GENE_ID), Arrays.asList(PROTEIN_POS_START), Arrays.asList(PROTEIN_POS_END));
        
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(expectedMutationCountByPosition, result.get(0));
    }
}