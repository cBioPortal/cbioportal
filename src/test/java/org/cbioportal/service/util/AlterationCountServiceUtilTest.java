package org.cbioportal.service.util;

import org.apache.commons.math3.util.Pair;
import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.AlterationCountByStructuralVariant;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.Gistic;
import org.cbioportal.model.GisticToGene;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MutSig;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class AlterationCountServiceUtilTest {

    @Test
    public void testComputeTotalProfiledCount() {
        // Test with gene panel data
        boolean hasGenePanelData = true;
        int alterationsProfiledCount = 5;
        int sampleProfileCountWithoutGenePanelData = 10;
        int totalProfiledCount = 20;

        int result = AlterationCountServiceUtil.computeTotalProfiledCount(
            hasGenePanelData, alterationsProfiledCount, sampleProfileCountWithoutGenePanelData, totalProfiledCount
        );
        assertEquals(15, result);

        // Test without gene panel data
        hasGenePanelData = false;
        result = AlterationCountServiceUtil.computeTotalProfiledCount(
            hasGenePanelData, alterationsProfiledCount, sampleProfileCountWithoutGenePanelData, totalProfiledCount
        );
        assertEquals(10, result);

        // Test with zero profiled count
        hasGenePanelData = true;
        alterationsProfiledCount = 0;
        sampleProfileCountWithoutGenePanelData = 0;
        result = AlterationCountServiceUtil.computeTotalProfiledCount(
            hasGenePanelData, alterationsProfiledCount, sampleProfileCountWithoutGenePanelData, totalProfiledCount
        );
        assertEquals(20, result);
    }


    @Test
    public void testUpdateAlterationCountsWithMutSigQValue() {
        AlterationCountByGene count1 = new AlterationCountByGene();
        count1.setHugoGeneSymbol("hugo1");

        AlterationCountByGene count2 = new AlterationCountByGene();
        count2.setHugoGeneSymbol("hugo2");

        List<AlterationCountByGene> counts = Arrays.asList(count1, count2);

        MutSig mutSig1 = new MutSig();
        mutSig1.setHugoGeneSymbol("hugo1");
        mutSig1.setqValue(BigDecimal.valueOf(0.01));

        Map<String, MutSig> mutSigs = new HashMap<>();
        mutSigs.put("hugo1", mutSig1);

        List<AlterationCountByGene> result = AlterationCountServiceUtil.updateAlterationCountsWithMutSigQValue(counts, mutSigs);

        assertEquals(BigDecimal.valueOf(0.01), result.get(0).getqValue());
        assertEquals(null, result.get(1).getqValue());
    }


    @Test
    public void testUpdateAlterationCountsWithCNASigQValue() {
        CopyNumberCountByGene count1 = new CopyNumberCountByGene();
        count1.setHugoGeneSymbol("hugo1");
        count1.setAlteration(2);

        CopyNumberCountByGene count2 = new CopyNumberCountByGene();
        count2.setHugoGeneSymbol("hugo2");
        count2.setAlteration(-2);

        List<CopyNumberCountByGene> counts = Arrays.asList(count1, count2);

        Gistic gistic1 = new Gistic();
        gistic1.setqValue(BigDecimal.valueOf(0.01));

        Map<Pair<String, Integer>, Gistic> gisticMap = new HashMap<>();
        gisticMap.put(Pair.create("hugo1", 2), gistic1);

        List<CopyNumberCountByGene> result = AlterationCountServiceUtil.updateAlterationCountsWithCNASigQValue(counts, gisticMap);

        assertEquals(BigDecimal.valueOf(0.01), result.get(0).getqValue());
        assertEquals(null, result.get(1).getqValue());
    }


    @Test
    public void testGetFirstMolecularProfileGroupedByStudy() {
        MolecularProfile profile1 = new MolecularProfile();
        profile1.setCancerStudyIdentifier("study1");
        profile1.setMolecularProfileId(1);

        MolecularProfile profile2 = new MolecularProfile();
        profile2.setCancerStudyIdentifier("study1");
        profile2.setMolecularProfileId(2);

        MolecularProfile profile3 = new MolecularProfile();
        profile3.setCancerStudyIdentifier("study2");
        profile3.setMolecularProfileId(3);

        List<MolecularProfile> profiles = Arrays.asList(profile1, profile2, profile3);

        List<MolecularProfile> result = AlterationCountServiceUtil.getFirstMolecularProfileGroupedByStudy(profiles);

        assertEquals(2, result.size());

        MolecularProfile resultProfile1 = result.stream()
            .filter(profile -> profile.getCancerStudyIdentifier().equals("study1"))
            .findFirst()
            .orElse(null);
        assertEquals(Integer.valueOf(1), resultProfile1.getMolecularProfileId());

        MolecularProfile resultProfile2 = result.stream()
            .filter(profile -> profile.getCancerStudyIdentifier().equals("study2"))
            .findFirst()
            .orElse(null);
        assertEquals(Integer.valueOf(3), resultProfile2.getMolecularProfileId());
    }


    @Test
    public void testCombineAlterationCountsWithConflictingHugoSymbols() {
        AlterationCountByGene count1 = new AlterationCountByGene();
        count1.setHugoGeneSymbol("hugo1");
        count1.setNumberOfAlteredCases(5);
        count1.setTotalCount(10);

        AlterationCountByGene count2 = new AlterationCountByGene();
        count2.setHugoGeneSymbol("hugo1");
        count2.setNumberOfAlteredCases(3);
        count2.setTotalCount(6);

        AlterationCountByGene count3 = new AlterationCountByGene();
        count3.setHugoGeneSymbol("hugo2");
        count3.setNumberOfAlteredCases(2);
        count3.setTotalCount(4);

        List<AlterationCountByGene> counts = Arrays.asList(count1, count2, count3);

        List<AlterationCountByGene> combinedCounts = AlterationCountServiceUtil.combineAlterationCountsWithConflictingHugoSymbols(counts);

        assertEquals(2, combinedCounts.size());

        AlterationCountByGene combinedCount1 = combinedCounts.stream()
            .filter(count -> count.getHugoGeneSymbol().equals("hugo1"))
            .findFirst()
            .orElse(null);
        assertEquals(8, combinedCount1.getNumberOfAlteredCases().intValue());
        assertEquals(16, combinedCount1.getTotalCount().intValue());

        AlterationCountByGene combinedCount2 = combinedCounts.stream()
            .filter(count -> count.getHugoGeneSymbol().equals("hugo2"))
            .findFirst()
            .orElse(null);
        assertEquals(2, combinedCount2.getNumberOfAlteredCases().intValue());
        assertEquals(4, combinedCount2.getTotalCount().intValue());
    }


    @Test
    public void testCombineCopyNumberCountsWithConflictingHugoSymbols() {
        CopyNumberCountByGene count1 = new CopyNumberCountByGene();
        count1.setHugoGeneSymbol("hugo1");
        count1.setAlteration(1);
        count1.setNumberOfAlteredCases(5);
        count1.setTotalCount(10);

        CopyNumberCountByGene count2 = new CopyNumberCountByGene();
        count2.setHugoGeneSymbol("hugo1");
        count2.setAlteration(1);
        count2.setNumberOfAlteredCases(3);
        count2.setTotalCount(6);

        CopyNumberCountByGene count3 = new CopyNumberCountByGene();
        count3.setHugoGeneSymbol("hugo2");
        count3.setAlteration(2);
        count3.setNumberOfAlteredCases(2);
        count3.setTotalCount(4);

        List<CopyNumberCountByGene> counts = Arrays.asList(count1, count2, count3);

        List<CopyNumberCountByGene> result = AlterationCountServiceUtil.combineCopyNumberCountsWithConflictingHugoSymbols(counts);

        assertEquals(2, result.size());

        CopyNumberCountByGene combinedCount1 = result.stream().filter(c -> c.getHugoGeneSymbol().equals("hugo1")).findFirst().orElse(null);
        assertEquals(8, combinedCount1.getNumberOfAlteredCases().intValue());
        assertEquals(16, combinedCount1.getTotalCount().intValue());

        CopyNumberCountByGene combinedCount2 = result.stream().filter(c -> c.getHugoGeneSymbol().equals("hugo2")).findFirst().orElse(null);
        assertEquals(2, combinedCount2.getNumberOfAlteredCases().intValue());
        assertEquals(4, combinedCount2.getTotalCount().intValue());
    }


    @Test
    public void testHasGenePanelData() {
        // Test with Whole Exome Sequencing and other panels
        Set<String> genePanelIds1 = Set.of("WES", "panel1");
        assertTrue(AlterationCountServiceUtil.hasGenePanelData(genePanelIds1));

        // Test with only Whole Exome Sequencing
        Set<String> genePanelIds2 = Set.of("WES");
        assertFalse(AlterationCountServiceUtil.hasGenePanelData(genePanelIds2));

        // Test with other panels
        Set<String> genePanelIds3 = Set.of("panel1", "panel2");
        assertTrue(AlterationCountServiceUtil.hasGenePanelData(genePanelIds3));

        // Test with empty set
        Set<String> genePanelIds4 = Set.of();
        assertFalse(AlterationCountServiceUtil.hasGenePanelData(genePanelIds4));
    }


    @Test
    public void testSetupGisticMap() {
        // Single Gistic with amplification
        Gistic gistic1 = new Gistic();
        gistic1.setAmp(true);
        gistic1.setqValue(BigDecimal.valueOf(0.01));
        GisticToGene gene1 = new GisticToGene();
        gene1.setHugoGeneSymbol("hugo1");
        gistic1.setGenes(List.of(gene1));

        // Single Gistic without amplification
        Gistic gistic2 = new Gistic();
        gistic2.setAmp(false);
        gistic2.setqValue(BigDecimal.valueOf(0.02));
        GisticToGene gene2 = new GisticToGene();
        gene2.setHugoGeneSymbol("hugo2");
        gistic2.setGenes(List.of(gene2));

        // Multiple Gistics for the same gene with different qValues
        Gistic gistic3 = new Gistic();
        gistic3.setAmp(true);
        gistic3.setqValue(BigDecimal.valueOf(0.03));
        GisticToGene gene3 = new GisticToGene();
        gene3.setHugoGeneSymbol("hugo1");
        gistic3.setGenes(List.of(gene3));

        List<Gistic> gisticList = List.of(gistic1, gistic2, gistic3);
        Map<Pair<String, Integer>, Gistic> gisticMap = new HashMap<>();

        AlterationCountServiceUtil.setupGisticMap(gisticList, gisticMap);

        assertEquals(2, gisticMap.size());
        assertTrue(gisticMap.containsKey(Pair.create("hugo1", 2)));
        assertTrue(gisticMap.containsKey(Pair.create("hugo2", -2)));
        assertEquals(gistic1, gisticMap.get(Pair.create("hugo1", 2))); // gistic1 should be chosen over gistic3 due to lower qValue
        assertEquals(gistic2, gisticMap.get(Pair.create("hugo2", -2)));
    }


    @Test
    public void testSetupAlterationGeneCountsMapWithAlterationCountByGene() {
        AlterationCountByGene datum1 = new AlterationCountByGene();
        datum1.setHugoGeneSymbol("hugo1");
        datum1.setTotalCount(1);
        datum1.setNumberOfAlteredCases(1);
        datum1.setNumberOfProfiledCases(1);
        datum1.setMatchingGenePanelIds(new HashSet<>(Arrays.asList("panel1")));

        AlterationCountByGene datum2 = new AlterationCountByGene();
        datum2.setHugoGeneSymbol("hugo1");
        datum2.setTotalCount(2);
        datum2.setNumberOfAlteredCases(2);
        datum2.setNumberOfProfiledCases(2);
        datum2.setMatchingGenePanelIds(new HashSet<>(Arrays.asList("panel2")));

        AlterationCountByGene datum3 = new AlterationCountByGene();
        datum3.setHugoGeneSymbol("hugo2");
        datum3.setTotalCount(3);
        datum3.setNumberOfAlteredCases(3);
        datum3.setNumberOfProfiledCases(3);
        datum3.setMatchingGenePanelIds(new HashSet<>(Arrays.asList("panel3")));

        List<AlterationCountByGene> studyAlterationCountByGenes = Arrays.asList(datum1, datum2, datum3);
        Map<String, AlterationCountByGene> totalResult = new HashMap<>();

        AlterationCountServiceUtil.setupAlterationGeneCountsMap(studyAlterationCountByGenes, totalResult);

        assertEquals(2, totalResult.size());
        assertTrue(totalResult.containsKey("hugo1"));
        assertTrue(totalResult.containsKey("hugo2"));

        AlterationCountByGene result1 = totalResult.get("hugo1");
        assertEquals(3L, (long) result1.getTotalCount());
        assertEquals(3L, (long) result1.getNumberOfAlteredCases());
        assertEquals(3L, (long) result1.getNumberOfProfiledCases());
        assertTrue(result1.getMatchingGenePanelIds().contains("panel1"));
        assertTrue(result1.getMatchingGenePanelIds().contains("panel2"));

        AlterationCountByGene result2 = totalResult.get("hugo2");
        assertEquals(3L, (long) result2.getTotalCount());
        assertEquals(3L, (long) result2.getNumberOfAlteredCases());
        assertEquals(3L, (long) result2.getNumberOfProfiledCases());
        assertTrue(result2.getMatchingGenePanelIds().contains("panel3"));
    }

    @Test
    public void testSetupAlterationGeneCountsMapWithAlterationCountByStructuralVariant() {
        AlterationCountByStructuralVariant datum1 = new AlterationCountByStructuralVariant();
        datum1.setGene1HugoGeneSymbol("hugo1");
        datum1.setGene2HugoGeneSymbol("hugo2");
        datum1.setTotalCount(1);
        datum1.setNumberOfAlteredCases(1);
        datum1.setNumberOfProfiledCases(1);
        datum1.setMatchingGenePanelIds(new HashSet<>(Arrays.asList("panel1")));

        AlterationCountByStructuralVariant datum2 = new AlterationCountByStructuralVariant();
        datum2.setGene1HugoGeneSymbol("hugo1");
        datum2.setGene2HugoGeneSymbol("hugo2");
        datum2.setTotalCount(2);
        datum2.setNumberOfAlteredCases(2);
        datum2.setNumberOfProfiledCases(2);
        datum2.setMatchingGenePanelIds(new HashSet<>(Arrays.asList("panel2")));

        AlterationCountByStructuralVariant datum3 = new AlterationCountByStructuralVariant();
        datum3.setGene1HugoGeneSymbol("hugo3");
        datum3.setGene2HugoGeneSymbol("hugo4");
        datum3.setTotalCount(3);
        datum3.setNumberOfAlteredCases(3);
        datum3.setNumberOfProfiledCases(3);
        datum3.setMatchingGenePanelIds(new HashSet<>(Arrays.asList("panel3")));

        List<AlterationCountByStructuralVariant> studyAlterationCountByGenes = Arrays.asList(datum1, datum2, datum3);
        Map<String, AlterationCountByStructuralVariant> totalResult = new HashMap<>();

        AlterationCountServiceUtil.setupAlterationGeneCountsMap(studyAlterationCountByGenes, totalResult);

        assertEquals(2, totalResult.size());
        assertTrue(totalResult.containsKey("hugo1::hugo2"));
        assertTrue(totalResult.containsKey("hugo3::hugo4"));

        AlterationCountByStructuralVariant result1 = totalResult.get("hugo1::hugo2");
        assertEquals(3L, (long) result1.getTotalCount());
        assertEquals(3L, (long) result1.getNumberOfAlteredCases());
        assertEquals(3L, (long) result1.getNumberOfProfiledCases());
        assertTrue(result1.getMatchingGenePanelIds().contains("panel1"));
        assertTrue(result1.getMatchingGenePanelIds().contains("panel2"));

        AlterationCountByStructuralVariant result2 = totalResult.get("hugo3::hugo4");
        assertEquals(3L, (long) result2.getTotalCount());
        assertEquals(3L, (long) result2.getNumberOfAlteredCases());
        assertEquals(3L, (long) result2.getNumberOfProfiledCases());
        assertTrue(result2.getMatchingGenePanelIds().contains("panel3"));
    }

    @Test
    public void testSetupAlterationGeneCountsMapWithCopyNumberCountByGene() {
        CopyNumberCountByGene datum1 = new CopyNumberCountByGene();
        datum1.setEntrezGeneId(1);
        datum1.setAlteration(2);
        datum1.setTotalCount(1);
        datum1.setNumberOfAlteredCases(1);
        datum1.setNumberOfProfiledCases(1);
        datum1.setMatchingGenePanelIds(new HashSet<>(Arrays.asList("panel1")));

        CopyNumberCountByGene datum2 = new CopyNumberCountByGene();
        datum2.setEntrezGeneId(1);
        datum2.setAlteration(2);
        datum2.setTotalCount(2);
        datum2.setNumberOfAlteredCases(2);
        datum2.setNumberOfProfiledCases(2);
        datum2.setMatchingGenePanelIds(new HashSet<>(Arrays.asList("panel2")));

        CopyNumberCountByGene datum3 = new CopyNumberCountByGene();
        datum3.setEntrezGeneId(2);
        datum3.setAlteration(-2);
        datum3.setTotalCount(3);
        datum3.setNumberOfAlteredCases(3);
        datum3.setNumberOfProfiledCases(3);
        datum3.setMatchingGenePanelIds(new HashSet<>(Arrays.asList("panel3")));

        List<CopyNumberCountByGene> studyAlterationCountByGenes = Arrays.asList(datum1, datum2, datum3);
        Map<String, CopyNumberCountByGene> totalResult = new HashMap<>();

        AlterationCountServiceUtil.setupAlterationGeneCountsMap(studyAlterationCountByGenes, totalResult);

        assertEquals(2, totalResult.size());
        assertTrue(totalResult.containsKey("12"));
        assertTrue(totalResult.containsKey("2-2"));

        CopyNumberCountByGene result1 = totalResult.get("12");
        assertEquals(3L, (long) result1.getTotalCount());
        assertEquals(3L, (long) result1.getNumberOfAlteredCases());
        assertEquals(3L, (long) result1.getNumberOfProfiledCases());
        assertTrue(result1.getMatchingGenePanelIds().contains("panel1"));
        assertTrue(result1.getMatchingGenePanelIds().contains("panel2"));

        CopyNumberCountByGene result2 = totalResult.get("2-2");
        assertEquals(3L, (long) result2.getTotalCount());
        assertEquals(3L, (long) result2.getNumberOfAlteredCases());
        assertEquals(3L, (long) result2.getNumberOfProfiledCases());
        assertTrue(result2.getMatchingGenePanelIds().contains("panel3"));
    }


}