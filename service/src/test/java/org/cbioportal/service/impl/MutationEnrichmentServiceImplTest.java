package org.cbioportal.service.impl;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationSampleCountByGene;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.util.AlterationEnrichmentUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class MutationEnrichmentServiceImplTest extends BaseServiceImplTest {
    
    @InjectMocks
    private MutationEnrichmentServiceImpl mutationEnrichmentService;

    @Mock
    private MutationService mutationService;
    @Mock
    private AlterationEnrichmentUtil alterationEnrichmentUtil;
    
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
        Mockito.when(mutationService.getSampleCountByEntrezGeneIdsAndSampleIds(GENETIC_PROFILE_ID, allSampleIds, null))
            .thenReturn(mutationSampleCountByGeneList);
        
        List<Mutation> mutations = new ArrayList<>();
        Mockito.when(mutationService.fetchMutationsInGeneticProfile(GENETIC_PROFILE_ID, alteredSampleIds, null, null, 
            "ID", null, null, null, null)).thenReturn(mutations);
        
        List<AlterationEnrichment> expectedAlterationEnrichments = new ArrayList<>(); 
        Mockito.when(alterationEnrichmentUtil.createAlterationEnrichments(2, 2, mutationSampleCountByGeneList, 
            mutations)).thenReturn(expectedAlterationEnrichments);
        
        List<AlterationEnrichment> result = mutationEnrichmentService.getMutationEnrichments(GENETIC_PROFILE_ID, 
            alteredSampleIds, unalteredSampleIds);

        Assert.assertEquals(result, expectedAlterationEnrichments);
    }
}
