package org.cbioportal.service.impl;

import org.cbioportal.model.Gene;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCountByPosition;
import org.cbioportal.model.meta.MutationMeta;
import org.cbioportal.persistence.MutationRepository;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MutationServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private MutationServiceImpl mutationService;
    
    @Mock
    private MutationRepository mutationRepository;
    @Mock
    private MolecularProfileService molecularProfileService;
    
    @Test
    public void getMutationsInMolecularProfileBySampleListId() throws Exception {
        
        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED);
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(molecularProfile);

        List<Mutation> expectedMutationList = new ArrayList<>();
        Mutation mutation = new Mutation();
        Gene gene = new Gene();
        mutation.setGene(gene);
        mutation.setChr("19");
        expectedMutationList.add(mutation);

        Mockito.when(mutationRepository.getMutationsInMolecularProfileBySampleListId(MOLECULAR_PROFILE_ID, 
            SAMPLE_LIST_ID, Arrays.asList(ENTREZ_GENE_ID_1), null, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION))
            .thenReturn(expectedMutationList);
        
        List<Mutation> result = mutationService.getMutationsInMolecularProfileBySampleListId(MOLECULAR_PROFILE_ID, 
            SAMPLE_LIST_ID, Arrays.asList(ENTREZ_GENE_ID_1), null, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedMutationList, result);
        Assert.assertEquals("19", result.get(0).getChr());
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
        mutation.setChr("19");
        expectedMutationList.add(mutation);

        Mockito.when(mutationRepository.getMutationsInMultipleMolecularProfiles(Arrays.asList(MOLECULAR_PROFILE_ID), 
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID_1), PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, 
            DIRECTION)).thenReturn(expectedMutationList);
        
        List<Mutation> result = mutationService.getMutationsInMultipleMolecularProfiles(
            Arrays.asList(MOLECULAR_PROFILE_ID), Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID_1), PROJECTION, 
            PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedMutationList, result);
        Assert.assertEquals("19", result.get(0).getChr());
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
        mutation.setChr("19");
        expectedMutationList.add(mutation);

        Mockito.when(mutationRepository.fetchMutationsInMolecularProfile(MOLECULAR_PROFILE_ID,
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID_1), null, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, 
            DIRECTION)).thenReturn(expectedMutationList);

        List<Mutation> result = mutationService.fetchMutationsInMolecularProfile(MOLECULAR_PROFILE_ID, 
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID_1), null, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT,
            DIRECTION);

        Assert.assertEquals(expectedMutationList, result);
        Assert.assertEquals("19", result.get(0).getChr());
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