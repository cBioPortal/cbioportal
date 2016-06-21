package org.cbioportal.service.impl;

import junit.framework.Assert;
import org.cbioportal.model.Mutation;
import org.cbioportal.persistence.MutationRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class MutationServiceImplTest {

    @InjectMocks
    private MutationServiceImpl mutationService;

    @Mock
    private MutationRepository mutationRepository;

    @Test
    public void getMutationsDetailed() throws Exception {

        ArrayList<String> testGeneticProfileStableIds = new ArrayList<String>();
        testGeneticProfileStableIds.add("test_genetic_profile_stable_id");
        ArrayList<String> testHugoGeneSymbols = new ArrayList<String>();
        testHugoGeneSymbols.add("test_hugo_gene_symbol");
        ArrayList<String> testSampleStableIds = new ArrayList<String>();
        testSampleStableIds.add("test_sample_stable_id");
        String testSampleList = "test_sample_list";

        List<Mutation> expectedMutationList = new ArrayList<Mutation>();
        Mutation expectedMutation = new Mutation();
        expectedMutationList.add(expectedMutation);

        Mockito.when(mutationRepository.getMutationsDetailed(testGeneticProfileStableIds, testHugoGeneSymbols,
                testSampleStableIds, testSampleList)).thenReturn(expectedMutationList);

        List<Mutation> resultMutationList = mutationService.getMutationsDetailed(testGeneticProfileStableIds,
                testHugoGeneSymbols, testSampleStableIds, testSampleList);

        Assert.assertEquals(expectedMutationList, resultMutationList);
    }
}
