package org.cbioportal.service.impl;

import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneFilterQuery;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.GenomicDataCountItem;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCountByPosition;
import org.cbioportal.model.MutationEventType;
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
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;

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
            SAMPLE_LIST_ID, Arrays.asList(ENTREZ_GENE_ID_1), false, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION))
            .thenReturn(expectedMutationList);
        
        List<Mutation> result = mutationService.getMutationsInMolecularProfileBySampleListId(MOLECULAR_PROFILE_ID, 
            SAMPLE_LIST_ID, Arrays.asList(ENTREZ_GENE_ID_1), false, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedMutationList, result);
        Assert.assertEquals("19", result.get(0).getChr());
    }

    @Test(expected = MolecularProfileNotFoundException.class)
    public void getMutationsInMolecularProfileBySampleListIdMolecularProfileNotFound() throws Exception {

        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenThrow(
            new MolecularProfileNotFoundException(MOLECULAR_PROFILE_ID));
        mutationService.getMutationsInMolecularProfileBySampleListId(MOLECULAR_PROFILE_ID, SAMPLE_LIST_ID, 
            Arrays.asList(ENTREZ_GENE_ID_1), false, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);
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

        Mockito.when(mutationRepository.getMutationsInMultipleMolecularProfiles(anyList(),
            anyList(), anyList(), eq(PROJECTION), eq(PAGE_SIZE), eq(PAGE_NUMBER), eq(SORT),
            eq(DIRECTION))).thenReturn(expectedMutationList);
        
        List<Mutation> result = mutationService.getMutationsInMultipleMolecularProfiles(
            Arrays.asList(MOLECULAR_PROFILE_ID), Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID_1), PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedMutationList, result);
        Assert.assertEquals("19", result.get(0).getChr());
    }

    @Test
    public void getMutationsInMultipleMolecularProfilesByGeneQueries() throws Exception {

        List<Mutation> expectedMutationList = new ArrayList<>();
        Mutation mutation = new Mutation();
        Gene gene = new Gene();
        mutation.setGene(gene);
        mutation.setChr("19");
        expectedMutationList.add(mutation);

        GeneFilterQuery geneFilterQuery = mock(GeneFilterQuery.class);

        Mockito.when(mutationRepository.getMutationsInMultipleMolecularProfilesByGeneQueries(anyList(),
            anyList(), anyList(), eq(PROJECTION), eq(PAGE_SIZE), eq(PAGE_NUMBER), eq(SORT),
            eq(DIRECTION))).thenReturn(expectedMutationList);
        
        List<Mutation> result = mutationService.getMutationsInMultipleMolecularProfilesByGeneQueries(
            Arrays.asList(MOLECULAR_PROFILE_ID), Arrays.asList(SAMPLE_ID1), Arrays.asList(geneFilterQuery), PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

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
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID_1), false, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, 
            DIRECTION)).thenReturn(expectedMutationList);

        List<Mutation> result = mutationService.fetchMutationsInMolecularProfile(MOLECULAR_PROFILE_ID, 
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID_1), false, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT,
            DIRECTION);

        Assert.assertEquals(expectedMutationList, result);
        Assert.assertEquals("19", result.get(0).getChr());
    }

    @Test(expected = MolecularProfileNotFoundException.class)
    public void fetchMutationsInMolecularProfileNotFound() throws Exception {
        
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenThrow(
            new MolecularProfileNotFoundException(MOLECULAR_PROFILE_ID));
        mutationService.fetchMutationsInMolecularProfile(MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1),
            Arrays.asList(ENTREZ_GENE_ID_1), false, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);
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
    
    @Test
    public void  getMutationCountsByType() {
        GenomicDataCountItem expectedGenomicDataCountItem = new GenomicDataCountItem();
        expectedGenomicDataCountItem.setProfileType(PROFILE_TYPE_1);
        expectedGenomicDataCountItem.setHugoGeneSymbol(HUGO_GENE_SYMBOL_1);
        GenomicDataCount expectedGenomicDataCount = new GenomicDataCount();
        expectedGenomicDataCount.setLabel(MutationEventType.missense_mutation.getMutationType());
        expectedGenomicDataCount.setValue(MutationEventType.missense_mutation.getMutationType());
        expectedGenomicDataCount.setCount(2);
        expectedGenomicDataCount.setUniqueCount(1);
        expectedGenomicDataCountItem.setCounts(Collections.singletonList(expectedGenomicDataCount));

        Mockito.when(mutationRepository.getMutationCountsByType(
            Collections.singletonList(MOLECULAR_PROFILE_ID), Collections.singletonList(SAMPLE_ID1),
            Collections.singletonList(ENTREZ_GENE_ID_1), PROFILE_TYPE_1)).thenReturn(expectedGenomicDataCountItem);

        GenomicDataCountItem result = mutationService.getMutationCountsByType(
            Collections.singletonList(MOLECULAR_PROFILE_ID), Collections.singletonList(SAMPLE_ID1),
            Collections.singletonList(ENTREZ_GENE_ID_1), PROFILE_TYPE_1);

        Assert.assertEquals(expectedGenomicDataCountItem, result);
        Assert.assertEquals(1, result.getCounts().size());
    }
}