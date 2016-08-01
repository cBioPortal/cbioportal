package org.cbioportal.service.impl;

import junit.framework.Assert;
import org.cbioportal.model.Mutation;
import org.cbioportal.persistence.MutationRepository;
import org.cbioportal.persistence.dto.MutatedGeneSampleCount;
import org.cbioportal.service.impl.util.MutSigUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.model.converter.MutationModelConverter;
import org.mskcc.cbio.portal.util.AlterationUtil;
import org.mskcc.cbio.portal.util.InternalIdUtil;
import org.mskcc.cbio.portal.util.OncokbHotspotUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DaoGeneticProfile.class, DaoCancerStudy.class, DaoGeneOptimized.class,
        DaoSample.class, DaoDrugInteraction.class, DaoGeneticAlteration.class, InternalIdUtil.class,
        OncokbHotspotUtil.class, DaoCosmicData.class, DaoSangerCensus.class})
@SuppressStaticInitializationFor("org.mskcc.cbio.portal.dao.DaoCancerStudy")
public class MutationMatrixCalculatorTest {

    @InjectMocks
    private MutationMatrixCalculator mutationMatrixCalculator;

    @Mock
    private MutationRepository mutationRepository;
    @Mock
    private MutationModelConverter mutationModelConverter;
    @Mock
    private MutSigUtil mutSigUtil;
    @Mock
    private AlterationUtil alterationUtil;
    @Mock
    private DaoGeneOptimized daoGeneOptimized;
    @Mock
    private DaoDrugInteraction daoDrugInteraction;
    @Mock
    private DaoGeneticAlteration daoGeneticAlteration;
    @Mock
    private DaoSangerCensus daoSangerCensus;

    @Test
    public void calculate() throws Exception {

        PowerMockito.mockStatic(DaoGeneticProfile.class);
        PowerMockito.mockStatic(DaoCancerStudy.class);
        PowerMockito.mockStatic(DaoGeneOptimized.class);
        PowerMockito.mockStatic(DaoSample.class);
        PowerMockito.mockStatic(DaoDrugInteraction.class);
        PowerMockito.mockStatic(DaoGeneticAlteration.class);
        PowerMockito.mockStatic(InternalIdUtil.class);
        PowerMockito.mockStatic(OncokbHotspotUtil.class);
        PowerMockito.mockStatic(DaoCosmicData.class);
        PowerMockito.mockStatic(DaoSangerCensus.class);

        int mutationGeneticProfileId = 12;
        int cnaGeneticProfileId = 15;
        int cancerStudyId = 345;
        int sampleId = 134;
        long entrezGeneId1 = 235;
        long entrezGeneId2 = 236;
        String hugoGeneSymbol1 = "GENE1";
        String hugoGeneSymbol2 = "GENE2";
        long mutationEventId1 = 34;
        long mutationEventId2 = 44;
        String drugId1 = "32432";
        String drugId2 = "21321";
        int mutatedGeneCount1 = 3;
        int mutatedGeneCount2 = 4;
        String mutationType1 = "test_mutation_type1";
        String mutationType2 = "test_mutation_type2";
        int proteinPosStart1 = 23434;
        int proteinPosEnd1 = 23456;
        int proteinPosStart2 = 12345;
        int proteinPosEnd2 = 12346;
        String refAllele1 = "test_ref_allele1";
        String refAllele2 = "test_ref_allele2";
        String impactScore1 = "impact_score1";
        String impactScore2 = "impact_score2";
        String linkXVar1 = "link_xvar1";
        String linkXVar2 = "link_xvar2";
        String linkPdb1 = "link_pdb1";
        String linkPdb2 = "link_pdb2";
        String linkMsa1 = "link_msa1";
        String linkMsa2 = "link_msa2";
        long startPos1 = 234;
        long endPos1 = 235;
        long startPos2 = 345;
        long endPos2 = 349;
        int altCount1 = 3;
        int refCount1 = 2;
        int altCount2 = 6;
        int refCount2 = 8;
        String validationStatus1 = "test_validation1";
        String validationStatus2 = "test_validation2";
        String proteinChange1 = "test_protein_change1";
        String proteinChange2 = "test_protein_change2";
        String chr1 = "chr1";
        String chr2 = "chr2";
        String mutationStatus1 = "test_mutation_status1";
        String mutationStatus2 = "test_mutation_status2";

        List<String> sampleStableIds = new ArrayList<>();
        sampleStableIds.add("test_sample_stable_id");
        String mutationGeneticProfileStableId = "test_mutation_genetic_profile_stable_id";
        String mrnaGeneticProfileStableId = "test_mrna_genetic_profile_stable_id";
        String cnaGeneticProfileStableId = "test_cna_genetic_profile_stable_id";
        String drugType = "test_drug_type";

        Mutation mutation1 = new Mutation();
        Mutation mutation2 = new Mutation();
        List<Mutation> mutations = new ArrayList<>();
        mutations.add(mutation1);
        mutations.add(mutation2);

        ExtendedMutation extendedMutation1 = new ExtendedMutation();
        extendedMutation1.setMutationEventId(mutationEventId1);
        extendedMutation1.setMutationType(mutationType1);
        extendedMutation1.setSampleId(sampleId);
        extendedMutation1.setOncotatorProteinPosStart(proteinPosStart1);
        extendedMutation1.setOncotatorProteinPosEnd(proteinPosEnd1);
        extendedMutation1.setReferenceAllele(refAllele1);
        extendedMutation1.setFunctionalImpactScore(impactScore1);
        extendedMutation1.setLinkXVar(linkXVar1);
        extendedMutation1.setLinkPdb(linkPdb1);
        extendedMutation1.setLinkMsa(linkMsa1);
        extendedMutation1.setStartPosition(startPos1);
        extendedMutation1.setEndPosition(endPos1);
        extendedMutation1.setTumorAltCount(altCount1);
        extendedMutation1.setTumorRefCount(refCount1);
        extendedMutation1.setValidationStatus(validationStatus1);
        extendedMutation1.setProteinChange(proteinChange1);
        extendedMutation1.setChr(chr1);
        extendedMutation1.setMutationStatus(mutationStatus1);

        ExtendedMutation extendedMutation2 = new ExtendedMutation();
        extendedMutation2.setMutationEventId(mutationEventId2);
        extendedMutation2.setMutationType(mutationType2);
        extendedMutation2.setSampleId(sampleId);
        extendedMutation2.setOncotatorProteinPosStart(proteinPosStart2);
        extendedMutation2.setOncotatorProteinPosEnd(proteinPosEnd2);
        extendedMutation2.setReferenceAllele(refAllele2);
        extendedMutation2.setFunctionalImpactScore(impactScore2);
        extendedMutation2.setLinkXVar(linkXVar2);
        extendedMutation2.setLinkPdb(linkPdb2);
        extendedMutation2.setLinkMsa(linkMsa2);
        extendedMutation2.setStartPosition(startPos2);
        extendedMutation2.setEndPosition(endPos2);
        extendedMutation2.setTumorAltCount(altCount2);
        extendedMutation2.setTumorRefCount(refCount2);
        extendedMutation2.setValidationStatus(validationStatus2);
        extendedMutation2.setProteinChange(proteinChange2);
        extendedMutation2.setChr(chr2);
        extendedMutation2.setMutationStatus(mutationStatus2);

        List<ExtendedMutation> extendedMutations = new ArrayList<>();
        extendedMutations.add(extendedMutation1);
        extendedMutations.add(extendedMutation2);

        GeneticProfile mutationGeneticProfile = new GeneticProfile();
        mutationGeneticProfile.setStableId(mutationGeneticProfileStableId);
        mutationGeneticProfile.setGeneticProfileId(mutationGeneticProfileId);
        mutationGeneticProfile.setCancerStudyId(cancerStudyId);
        Mockito.when(DaoGeneticProfile.getGeneticProfileByStableId(mutationGeneticProfileStableId))
                .thenReturn(mutationGeneticProfile);

        CancerStudy cancerStudy = new CancerStudy();
        cancerStudy.setInternalId(cancerStudyId);
        Mockito.when(DaoCancerStudy.getCancerStudyByInternalId(cancerStudyId)).thenReturn(cancerStudy);

        List<Integer> sampleIds = new ArrayList<>();
        sampleIds.add(sampleId);
        Mockito.when(InternalIdUtil.getInternalSampleIds(cancerStudyId, sampleStableIds)).thenReturn(sampleIds);

        Sample sample = new Sample();
        sample.setStableId(sampleStableIds.get(0));
        sample.setInternalId(sampleId);
        Mockito.when(DaoSample.getSampleByCancerStudyAndSampleId(cancerStudyId, sampleStableIds.get(0)))
                .thenReturn(sample);

        Mockito.when(mutationRepository.getMutations(sampleIds, mutationGeneticProfileId)).thenReturn(mutations);
        Mockito.when(mutationModelConverter.convert(mutations)).thenReturn(extendedMutations);

        List<Integer> mutationEventIds = new ArrayList<>();
        mutationEventIds.add((int) mutationEventId1);
        mutationEventIds.add((int) mutationEventId2);
        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add((int) entrezGeneId1);
        entrezGeneIds.add((int) entrezGeneId2);
        Mockito.when(mutationRepository.getGenesOfMutations(mutationEventIds)).thenReturn(entrezGeneIds);

        Mockito.when(DaoDrugInteraction.getInstance()).thenReturn(daoDrugInteraction);
        Mockito.when(daoDrugInteraction.getMoreTargets(entrezGeneId1, MutationMatrixCalculator.MUT))
                .thenReturn(new HashSet<Long>());
        Mockito.when(daoDrugInteraction.getMoreTargets(entrezGeneId2, MutationMatrixCalculator.MUT))
                .thenReturn(new HashSet<Long>());
        Mockito.doNothing().when(alterationUtil).addEventGenes(Mockito.anyMap(), Mockito.anyLong(), Mockito.anySet());
        Set<Long> entrezGeneIdSet = new HashSet<>();
        entrezGeneIdSet.add(entrezGeneId1);
        entrezGeneIdSet.add(entrezGeneId2);
        Map<Long, List<String>> drugs = new HashMap<>();
        List<String> drugIds1 = new ArrayList<>();
        drugIds1.add(drugId1);
        drugs.put(entrezGeneId1, drugIds1);
        List<String> drugIds2 = new ArrayList<>();
        drugIds2.add(drugId2);
        drugs.put(entrezGeneId2, drugIds2);
        Mockito.when(daoDrugInteraction.getDrugs(entrezGeneIdSet, false, true)).thenReturn(drugs);

        Mockito.doNothing().when(alterationUtil).addDrugs(Mockito.anyMap(), Mockito.anyMap(), Mockito.anyMap());

        Mockito.when(mutationRepository.getGenesOfMutations(mutationEventIds)).thenReturn(entrezGeneIds);

        MutatedGeneSampleCount mutatedGeneSampleCount1 = new MutatedGeneSampleCount();
        mutatedGeneSampleCount1.setEntrezGeneId((int) entrezGeneId1);
        mutatedGeneSampleCount1.setCount(mutatedGeneCount1);
        MutatedGeneSampleCount mutatedGeneSampleCount2 = new MutatedGeneSampleCount();
        mutatedGeneSampleCount2.setEntrezGeneId((int) entrezGeneId2);
        mutatedGeneSampleCount2.setCount(mutatedGeneCount2);
        List<MutatedGeneSampleCount> mutatedGeneSampleCounts = new ArrayList<>();
        Mockito.when(mutationRepository.countSamplesWithMutatedGenes(mutationGeneticProfileId, entrezGeneIds))
                .thenReturn(mutatedGeneSampleCounts);

        Map<Long, Integer> mutatedGeneSampleMap = new HashMap<>();
        mutatedGeneSampleMap.put(entrezGeneId1, mutatedGeneCount1);
        mutatedGeneSampleMap.put(entrezGeneId2, mutatedGeneCount2);
        Mockito.when(mutationModelConverter.convertMutatedGeneSampleCountToMap(mutatedGeneSampleCounts))
                .thenReturn(mutatedGeneSampleMap);

        CanonicalGene canonicalGene1 = new CanonicalGene(entrezGeneId1, hugoGeneSymbol1);
        CanonicalGene canonicalGene2 = new CanonicalGene(entrezGeneId2, hugoGeneSymbol2);
        extendedMutation1.setGene(canonicalGene1);
        extendedMutation2.setGene(canonicalGene2);
        Mockito.when(DaoGeneOptimized.getInstance()).thenReturn(daoGeneOptimized);
        Mockito.when(daoGeneOptimized.getGene(entrezGeneId1)).thenReturn(canonicalGene1);
        Mockito.when(daoGeneOptimized.getGene(entrezGeneId2)).thenReturn(canonicalGene2);

        Mockito.when(DaoGeneticAlteration.getInstance()).thenReturn(daoGeneticAlteration);

        GeneticProfile cnaGeneticProfile = new GeneticProfile();
        cnaGeneticProfile.setStableId(cnaGeneticProfileStableId);
        cnaGeneticProfile.setGeneticProfileId(cnaGeneticProfileId);
        cnaGeneticProfile.setCancerStudyId(cancerStudyId);
        Mockito.when(DaoGeneticProfile.getGeneticProfileByStableId(cnaGeneticProfileStableId))
                .thenReturn(cnaGeneticProfile);

        Mockito.when(DaoSample.getSampleById(sampleId)).thenReturn(sample);

        Mockito.when(OncokbHotspotUtil.getOncokbHotspot(hugoGeneSymbol1, proteinChange1)).thenReturn(true);
        Mockito.when(OncokbHotspotUtil.getOncokbHotspot(hugoGeneSymbol2, proteinChange2)).thenReturn(false);

        Mockito.when(daoGeneOptimized.isCbioCancerGene(canonicalGene1)).thenReturn(false);
        Mockito.when(daoGeneOptimized.isCbioCancerGene(canonicalGene2)).thenReturn(true);
        Mockito.when(DaoSangerCensus.getInstance()).thenReturn(daoSangerCensus);

        Map<String, List> result = mutationMatrixCalculator.calculate(sampleStableIds,
                mutationGeneticProfileStableId, mrnaGeneticProfileStableId, cnaGeneticProfileStableId, drugType);

        Assert.assertEquals(33, result.size());
        List<Long> idList = result.get("id");
        Assert.assertEquals(mutationEventId1, idList.get(0).longValue());
        Assert.assertEquals(mutationEventId2, idList.get(1).longValue());
        List<List<String>> caseIdsList = result.get("caseIds");
        Assert.assertEquals(sampleStableIds.get(0), caseIdsList.get(0).get(0));
        Assert.assertEquals(sampleStableIds.get(0), caseIdsList.get(1).get(0));
        List<String> chrList = result.get("chr");
        Assert.assertEquals(chr1, chrList.get(0));
        Assert.assertEquals(chr2, chrList.get(1));
        List<Long> startList = result.get("start");
        Assert.assertEquals(startPos1, startList.get(0).longValue());
        Assert.assertEquals(startPos2, startList.get(1).longValue());
        List<Long> endList = result.get("end");
        Assert.assertEquals(endPos1, endList.get(0).longValue());
        Assert.assertEquals(endPos2, endList.get(1).longValue());
        List<Long> entrezList = result.get("entrez");
        Assert.assertEquals(entrezGeneId1, entrezList.get(0).longValue());
        Assert.assertEquals(entrezGeneId2, entrezList.get(1).longValue());
        List<String> hugoList = result.get("gene");
        Assert.assertEquals(hugoGeneSymbol1, hugoList.get(0));
        Assert.assertEquals(hugoGeneSymbol2, hugoList.get(1));
        List<Integer> proteinStartList = result.get("protein-start");
        Assert.assertEquals(proteinPosStart1, proteinStartList.get(0).intValue());
        Assert.assertEquals(proteinPosStart2, proteinStartList.get(1).intValue());
        List<Integer> proteinEndList = result.get("protein-end");
        Assert.assertEquals(proteinPosEnd1, proteinEndList.get(0).intValue());
        Assert.assertEquals(proteinPosEnd2, proteinEndList.get(1).intValue());
        List<String> proteinChangeList = result.get("aa");
        Assert.assertEquals(proteinChange1, proteinChangeList.get(0));
        Assert.assertEquals(proteinChange2, proteinChangeList.get(1));
        List<String> refAlleleList = result.get("ref");
        Assert.assertEquals(refAllele1, refAlleleList.get(0));
        Assert.assertEquals(refAllele2, refAlleleList.get(1));
        List<String> typeList = result.get("type");
        Assert.assertEquals(mutationType1, typeList.get(0));
        Assert.assertEquals(mutationType2, typeList.get(1));
        List<String> mutationStatusList = result.get("status");
        Assert.assertEquals(mutationStatus1, mutationStatusList.get(0));
        Assert.assertEquals(mutationStatus2, mutationStatusList.get(1));
        List<Map<String, Integer>> altCountList = result.get("alt-count");
        Assert.assertEquals(altCount1, altCountList.get(0).get(sampleStableIds.get(0)).intValue());
        Assert.assertEquals(altCount2, altCountList.get(1).get(sampleStableIds.get(0)).intValue());
        List<Map<String, Integer>> refCountList = result.get("ref-count");
        Assert.assertEquals(refCount1, refCountList.get(0).get(sampleStableIds.get(0)).intValue());
        Assert.assertEquals(refCount2, refCountList.get(1).get(sampleStableIds.get(0)).intValue());
        List<String> validationList = result.get("validation");
        Assert.assertEquals(validationStatus1, validationList.get(0));
        Assert.assertEquals(validationStatus2, validationList.get(1));
        List<Boolean> isHotspotList = result.get("is-hotspot");
        Assert.assertTrue(isHotspotList.get(0));
        Assert.assertFalse(isHotspotList.get(1));
        List<Integer> geneMutRateList = result.get("genemutrate");
        Assert.assertEquals(mutatedGeneCount1, geneMutRateList.get(0).intValue());
        Assert.assertEquals(mutatedGeneCount2, geneMutRateList.get(1).intValue());
        List<Boolean> isCancerGeneList = result.get("cancer-gene");
        Assert.assertFalse(isCancerGeneList.get(0));
        Assert.assertTrue(isCancerGeneList.get(1));
        List<Set<String>> drugList = result.get("drug");
        Assert.assertEquals(1, drugList.get(0).size());
        Assert.assertTrue(drugList.get(0).contains(drugId1));
        Assert.assertEquals(1, drugList.get(1).size());
        Assert.assertTrue(drugList.get(1).contains(drugId2));
        List<Map<String, String>> maList = result.get("ma");
        Assert.assertEquals(impactScore1, maList.get(0).get("score"));
        Assert.assertEquals(linkXVar1, maList.get(0).get("xvia"));
        Assert.assertEquals(linkPdb1, maList.get(0).get("pdb"));
        Assert.assertEquals(linkMsa1, maList.get(0).get("msa"));
        Assert.assertEquals(impactScore2, maList.get(1).get("score"));
        Assert.assertEquals(linkXVar2, maList.get(1).get("xvia"));
        Assert.assertEquals(linkPdb2, maList.get(1).get("pdb"));
        Assert.assertEquals(linkMsa2, maList.get(1).get("msa"));
    }
}