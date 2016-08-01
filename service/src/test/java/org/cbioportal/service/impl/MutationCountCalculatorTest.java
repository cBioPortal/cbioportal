package org.cbioportal.service.impl;

import junit.framework.Assert;
import org.cbioportal.model.MutationCount;
import org.cbioportal.persistence.MutationRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.model.converter.MutationModelConverter;
import org.mskcc.cbio.portal.util.InternalIdUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DaoGeneticProfile.class, DaoSample.class, InternalIdUtil.class})
public class MutationCountCalculatorTest {

    @InjectMocks
    private MutationCountCalculator mutationCountCalculator;

    @Mock
    private MutationRepository mutationRepository;
    @Mock
    private MutationModelConverter mutationModelConverter;

    @Test
    public void calculate() throws Exception {

        PowerMockito.mockStatic(DaoGeneticProfile.class);
        PowerMockito.mockStatic(DaoSample.class);
        PowerMockito.mockStatic(InternalIdUtil.class);

        String mutationGeneticProfileStableId = "mutation_genetic_profile_stable_id";
        int mutationGeneticProfileId = 3;
        int cancerStudyId = 5;
        String sampleStableId1 = "sample_stable_id_1";
        int sampleId1 = 12;
        String sampleStableId2 = "sample_stable_id_2";
        int sampleId2 = 15;
        List<String> sampleStableIds = new ArrayList<>();
        sampleStableIds.add(sampleStableId1);
        sampleStableIds.add(sampleStableId2);
        int mutationCount1 = 1;
        int mutationCount2 = 2;

        GeneticProfile geneticProfile = new GeneticProfile();
        geneticProfile.setGeneticProfileId(mutationGeneticProfileId);
        geneticProfile.setCancerStudyId(cancerStudyId);
        Mockito.when(DaoGeneticProfile.getGeneticProfileByStableId(mutationGeneticProfileStableId))
                .thenReturn(geneticProfile);

        List<Integer> sampleIds = new ArrayList<>();
        sampleIds.add(sampleId1);
        sampleIds.add(sampleId2);
        Mockito.when(InternalIdUtil.getInternalNonNormalSampleIds(cancerStudyId, sampleStableIds))
                .thenReturn(sampleIds);

        List<MutationCount> mutationCounts = new ArrayList<>();
        Mockito.when(mutationRepository.countMutationEvents(mutationGeneticProfileId, sampleIds))
                .thenReturn(mutationCounts);

        Map<Integer, Integer> mutationCountMap = new HashMap<>();
        mutationCountMap.put(sampleId1, mutationCount1);
        mutationCountMap.put(sampleId2, mutationCount2);
        Mockito.when(mutationModelConverter.convertMutationCountToMap(mutationCounts)).thenReturn(mutationCountMap);

        Sample sample1 = new Sample();
        sample1.setInternalId(sampleId1);
        sample1.setStableId(sampleStableId1);
        Sample sample2 = new Sample();
        sample2.setInternalId(sampleId2);
        sample2.setStableId(sampleStableId2);
        Mockito.when(DaoSample.getSampleById(sampleId1)).thenReturn(sample1);
        Mockito.when(DaoSample.getSampleById(sampleId2)).thenReturn(sample2);

        Map<String, Integer> result = mutationCountCalculator.calculate(mutationGeneticProfileStableId,
                sampleStableIds);

        Assert.assertEquals(2, result.size());
        Assert.assertEquals(mutationCount1, result.get(sampleStableId1).intValue());
        Assert.assertEquals(mutationCount2, result.get(sampleStableId2).intValue());
    }

}