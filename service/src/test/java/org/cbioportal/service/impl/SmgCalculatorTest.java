package org.cbioportal.service.impl;

import junit.framework.Assert;
import org.cbioportal.persistence.MutationRepository;
import org.cbioportal.persistence.dto.SignificantlyMutatedGene;
import org.cbioportal.service.impl.util.MutSigUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.converter.MutationModelConverter;
import org.mskcc.cbio.portal.util.InternalIdUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DaoGeneticProfile.class, DaoGeneOptimized.class, InternalIdUtil.class})
public class SmgCalculatorTest {

    @InjectMocks
    private SmgCalculator smgCalculator;

    @Mock
    private MutationRepository mutationRepository;
    @Mock
    private MutationModelConverter mutationModelConverter;
    @Mock
    private MutSigUtil mutSigUtil;
    @Mock
    private DaoGeneOptimized daoGeneOptimized;

    @Test
    public void calculate() throws Exception {

        PowerMockito.mockStatic(DaoGeneticProfile.class);
        PowerMockito.mockStatic(DaoGeneOptimized.class);
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
        long entrezGeneId = 235;
        String hugoGeneSymbol = "GENE";
        int count = 3;
        String cytoband = "test_cytoband";
        int length = 5;
        double qValue = 3.2;

        GeneticProfile geneticProfile = new GeneticProfile();
        geneticProfile.setGeneticProfileId(mutationGeneticProfileId);
        geneticProfile.setCancerStudyId(cancerStudyId);
        Mockito.when(DaoGeneticProfile.getGeneticProfileByStableId(mutationGeneticProfileStableId))
                .thenReturn(geneticProfile);

        List<Integer> sampleIds = new ArrayList<>();
        sampleIds.add(sampleId1);
        sampleIds.add(sampleId2);
        Mockito.when(InternalIdUtil.getInternalSampleIds(cancerStudyId, sampleStableIds)).thenReturn(sampleIds);

        List<SignificantlyMutatedGene> significantlyMutatedGenes = new ArrayList<>();
        Mockito.when(mutationRepository.getSignificantlyMutatedGenes(mutationGeneticProfileId, null, sampleIds,
                SmgCalculator.DEFAULT_THERSHOLD_RECURRENCE, SmgCalculator.DEFAULT_THERSHOLD_NUM_SMGS))
                .thenReturn(significantlyMutatedGenes);

        Map<String, String> value = new HashMap<>();
        value.put("caseIds", Integer.toString(sampleId1) + "," + Integer.toString(sampleId2));
        value.put("count", Integer.toString(count));
        Map<Long, Map<String, String>> significantlyMutatedGeneMap = new HashMap<>();
        significantlyMutatedGeneMap.put(entrezGeneId, value);
        Mockito.when(mutationModelConverter.convertSignificantlyMutatedGeneToMap(significantlyMutatedGenes))
                .thenReturn(significantlyMutatedGeneMap);

        Mockito.when(DaoGeneOptimized.getInstance()).thenReturn(daoGeneOptimized);
        CanonicalGene canonicalGene = new CanonicalGene(entrezGeneId, hugoGeneSymbol);
        canonicalGene.setCytoband(cytoband);
        canonicalGene.setLength(length);
        Set<CanonicalGene> canonicalGeneSet = new HashSet<>();
        canonicalGeneSet.add(canonicalGene);
        Mockito.when(daoGeneOptimized.getCbioCancerGenes()).thenReturn(canonicalGeneSet);
        Mockito.when(daoGeneOptimized.getGene(entrezGeneId)).thenReturn(canonicalGene);

        Set<Long> entrezGeneIdSet = new HashSet<>();
        entrezGeneIdSet.add(entrezGeneId);
        Mockito.when(daoGeneOptimized.getEntrezGeneIds(canonicalGeneSet)).thenReturn(entrezGeneIdSet);

        List<Integer> intEntrezGeneIds = new ArrayList<>();
        intEntrezGeneIds.add((int) entrezGeneId);
        Mockito.when(mutationRepository.getSignificantlyMutatedGenes(mutationGeneticProfileId, intEntrezGeneIds,
                sampleIds, -1, -1)).thenReturn(significantlyMutatedGenes);

        Map<Long,Double> mutSig = new HashMap<>();
        mutSig.put(entrezGeneId, qValue);
        Mockito.when(mutSigUtil.getMutSig(cancerStudyId)).thenReturn(mutSig);

        Mockito.when(InternalIdUtil.getStableSampleIds(sampleIds)).thenReturn(sampleStableIds);

        List<Map<String, Object>> result = smgCalculator.calculate(mutationGeneticProfileStableId, sampleStableIds);

        Assert.assertEquals(1, result.size());
        Map<String, Object> resultValue = result.get(0);
        Assert.assertEquals(hugoGeneSymbol, resultValue.get("gene_symbol"));
        Assert.assertEquals(cytoband, resultValue.get("cytoband"));
        Assert.assertEquals(length, resultValue.get("length"));
        Assert.assertEquals(count, resultValue.get("num_muts"));
        Assert.assertEquals(qValue, resultValue.get("qval"));
        Assert.assertEquals(sampleStableId1, ((List) resultValue.get("caseIds")).get(0));
        Assert.assertEquals(sampleStableId2, ((List) resultValue.get("caseIds")).get(1));

    }

}