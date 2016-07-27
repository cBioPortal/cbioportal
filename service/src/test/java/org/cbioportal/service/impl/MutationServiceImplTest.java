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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class MutationServiceImplTest {

    @InjectMocks
    private MutationServiceImpl mutationService;

    @Mock
    private MutationRepository mutationRepository;
    @Mock
    private MutationMatrixCalculator mutationMatrixCalculator;
    @Mock
    private MutationCountCalculator mutationCountCalculator;
    @Mock
    private SmgCalculator smgCalculator;

    @Test
    public void getMutationsDetailed() throws Exception {

        List<String> testGeneticProfileStableIds = new ArrayList<>();
        testGeneticProfileStableIds.add("test_genetic_profile_stable_id");
        List<String> testHugoGeneSymbols = new ArrayList<>();
        testHugoGeneSymbols.add("test_hugo_gene_symbol");
        List<String> testSampleStableIds = new ArrayList<>();
        testSampleStableIds.add("test_sample_stable_id");
        String testSampleList = "test_sample_list";

        List<Mutation> expectedMutationList = new ArrayList<>();
        Mutation expectedMutation = new Mutation();
        expectedMutationList.add(expectedMutation);

        Mockito.when(mutationRepository.getMutationsDetailed(testGeneticProfileStableIds, testHugoGeneSymbols,
                testSampleStableIds, testSampleList)).thenReturn(expectedMutationList);

        List<Mutation> resultMutationList = mutationService.getMutationsDetailed(testGeneticProfileStableIds,
                testHugoGeneSymbols, testSampleStableIds, testSampleList);

        Assert.assertEquals(expectedMutationList, resultMutationList);
    }

    @Test
    public void getMutationMatrix() throws Exception {

        List<String> testSampleStableIds = new ArrayList<>();
        testSampleStableIds.add("test_sample_stable_id");
        String mutationGeneticProfileStableId = "test_mutation_genetic_profile_stable_id";
        String mrnaGeneticProfileStableId = "test_mrna_genetic_profile_stable_id";
        String cnaGeneticProfileStableId = "test_cna_genetic_profile_stable_id";
        String drugType = "test_drug_type";

        Map<String,List> expectedResult = new HashMap<>();
        expectedResult.put("test", new ArrayList());

        Mockito.when(mutationMatrixCalculator.calculate(testSampleStableIds, mutationGeneticProfileStableId,
                mrnaGeneticProfileStableId, cnaGeneticProfileStableId, drugType)).thenReturn(expectedResult);

        Map<String,List> result = mutationService.getMutationMatrix(testSampleStableIds,
                mutationGeneticProfileStableId, mrnaGeneticProfileStableId, cnaGeneticProfileStableId, drugType);

        Assert.assertEquals(expectedResult, result);
    }

    @Test
    public void getMutationCount() throws Exception {

        List<String> testSampleStableIds = new ArrayList<>();
        testSampleStableIds.add("test_sample_stable_id");
        String mutationGeneticProfileStableId = "test_mutation_genetic_profile_stable_id";

        Map<String, Integer>  expectedResult = new HashMap<>();
        expectedResult.put("test", 1);

        Mockito.when(mutationCountCalculator.calculate(mutationGeneticProfileStableId,
                testSampleStableIds)).thenReturn(expectedResult);

        Map<String, Integer> result = mutationService.getMutationCount(mutationGeneticProfileStableId,
                testSampleStableIds);

        Assert.assertEquals(expectedResult, result);
    }

    @Test
    public void getSmg() throws Exception {

        String mutationGeneticProfileStableId = "test_mutation_genetic_profile_stable_id";

        List<Map<String, Object>> expectedResult = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        expectedResult.add(map);

        Mockito.when(smgCalculator.calculate(mutationGeneticProfileStableId)).thenReturn(expectedResult);

        List<Map<String, Object>> result = mutationService.getSmg(mutationGeneticProfileStableId);

        Assert.assertEquals(expectedResult, result);
    }
}
