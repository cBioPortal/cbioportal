package org.cbioportal.legacy.service.util;

import org.apache.commons.math3.util.Pair;
import org.cbioportal.legacy.model.AlterationCountBase;
import org.cbioportal.legacy.model.AlterationCountByGene;
import org.cbioportal.legacy.model.CopyNumberCountByGene;
import org.cbioportal.legacy.model.Gistic;
import org.cbioportal.legacy.model.GisticToGene;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.MutSig;

import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AlterationCountServiceUtil {

    private AlterationCountServiceUtil() {}

    private static final String WHOLE_EXOME_SEQUENCING = "WES";

    public static int computeTotalProfiledCount(boolean hasGenePanelData, int alterationsProfiledCount, int sampleProfileCountWithoutGenePanelData, int totalProfiledCount) {
        int profiledCount = hasGenePanelData ? alterationsProfiledCount + sampleProfileCountWithoutGenePanelData
            : sampleProfileCountWithoutGenePanelData;
        return profiledCount == 0 ? totalProfiledCount : profiledCount;
    }

    public static List<AlterationCountByGene> updateAlterationCountsWithMutSigQValue(
        List<AlterationCountByGene> alterationCountByGenes,
        Map<String, MutSig> mutSigs) {

        if (!mutSigs.isEmpty()) {
            alterationCountByGenes.parallelStream()
                .filter(alterationCount -> mutSigs.containsKey(alterationCount.getHugoGeneSymbol()))
                .forEach(alterationCount ->
                    alterationCount.setqValue(mutSigs.get(alterationCount.getHugoGeneSymbol()).getqValue())
                );
        }
        return alterationCountByGenes;
    }

    public static List<CopyNumberCountByGene> updateAlterationCountsWithCNASigQValue(
        List<CopyNumberCountByGene> alterationCountByGenes,
        Map<Pair<String, Integer>, Gistic> gisticMap) {

        if (!gisticMap.isEmpty()) {
            alterationCountByGenes.parallelStream()
                .filter(alterationCount -> gisticMap.containsKey(Pair.create(alterationCount.getHugoGeneSymbol(), alterationCount.getAlteration())))
                .forEach(alterationCount ->
                    alterationCount.setqValue(gisticMap.get(Pair.create(alterationCount.getHugoGeneSymbol(), alterationCount.getAlteration())).getqValue())
                );
        }
        return alterationCountByGenes;
    }

    public static List<MolecularProfile> getFirstMolecularProfileGroupedByStudy(List<MolecularProfile> molecularProfiles) {
        return molecularProfiles.stream()
            .collect(Collectors.toMap(
                MolecularProfile::getCancerStudyIdentifier,
                Function.identity(),
                (existing, replacement) -> existing  // Keep the first occurrence
            ))
            .values()
            .stream()
            .toList();
    }

    /**
     * Combines alteration counts by Hugo gene symbols. If multiple entries exist for the same 
     * gene symbol, their number of altered cases and total counts are summed up. Returns a 
     * list of unique AlterationCountByGene objects where each gene symbol is represented only once.
     *
     * This appears in the Data where Genes have similar Hugo Gene Symbols but different Entrez Ids
     *
     * @param alterationCounts List of AlterationCountByGene objects, potentially with duplicate gene symbols
     * @return List of AlterationCountByGene objects with unique gene symbols and combined counts
     */
    public static List<AlterationCountByGene> combineAlterationCountsWithConflictingHugoSymbols(List<AlterationCountByGene> alterationCounts) {
        Map<String, AlterationCountByGene> alterationCountByGeneMap = new HashMap<>();
        for (var alterationCount : alterationCounts) {
            if (alterationCountByGeneMap.containsKey(alterationCount.getHugoGeneSymbol())){
                AlterationCountByGene toUpdate = alterationCountByGeneMap.get(alterationCount.getHugoGeneSymbol());
                toUpdate.setNumberOfAlteredCases(toUpdate.getNumberOfAlteredCases() + alterationCount.getNumberOfAlteredCases());
                toUpdate.setTotalCount(toUpdate.getTotalCount() + alterationCount.getTotalCount());
            } else {
                alterationCountByGeneMap.put(alterationCount.getHugoGeneSymbol(), alterationCount);
            }
        }
        return alterationCountByGeneMap.values().stream().toList();
    }

    /**
     * Combines alteration counts by Hugo gene symbols. If multiple entries exist for the same 
     * gene symbol, their number of altered cases and total counts are summed up. Returns a 
     * list of unique AlterationCountByGene objects where each gene symbol is represented only once.
     *
     * This appears in the Data where Genes have similar Hugo Gene Symbols but different Entrez Ids.
     * This is a special case to handle Copy Number Mutations where the Alteration type should be a part of the key
     *
     * @param alterationCounts List of CopyNumberCountByGene objects, potentially with duplicate gene symbols
     * @return List of AlterationCountByGene objects with unique gene symbols and combined counts
     */
    public static List<CopyNumberCountByGene> combineCopyNumberCountsWithConflictingHugoSymbols(List<CopyNumberCountByGene> alterationCounts) {
        Map<Pair<String, Integer>, CopyNumberCountByGene> alterationCountByGeneMap = new HashMap<>();
        for (var alterationCount : alterationCounts) {
            var copyNumberKey = Pair.create(alterationCount.getHugoGeneSymbol(), alterationCount.getAlteration());
            if (alterationCountByGeneMap.containsKey(copyNumberKey)) {
                CopyNumberCountByGene toUpdate = alterationCountByGeneMap.get(copyNumberKey);
                toUpdate.setNumberOfAlteredCases(toUpdate.getNumberOfAlteredCases() + alterationCount.getNumberOfAlteredCases());
                toUpdate.setTotalCount(toUpdate.getTotalCount() + alterationCount.getTotalCount());
            } else {
                alterationCountByGeneMap.put(copyNumberKey, alterationCount);
            }
        }
        return alterationCountByGeneMap.values().stream().toList();
    }

    public static boolean hasGenePanelData(@NonNull Set<String> matchingGenePanelIds) {
        return matchingGenePanelIds.contains(WHOLE_EXOME_SEQUENCING)
            && matchingGenePanelIds.size() > 1 || !matchingGenePanelIds.contains(WHOLE_EXOME_SEQUENCING) && !matchingGenePanelIds.isEmpty();
    }

    public static void setupGisticMap(List<Gistic> gisticList, Map<Pair<String, Integer>, Gistic> gisticMap) {
        for (Gistic gistic : gisticList) {
            var amp = gistic.getAmp().booleanValue() ? 2 : -2;
            for (GisticToGene gene : gistic.getGenes()) {
                var key = Pair.create(gene.getHugoGeneSymbol(), amp);
                Gistic currentGistic = gisticMap.get(key);
                if (currentGistic == null || gistic.getqValue().compareTo(currentGistic.getqValue()) < 0) {
                    gisticMap.put(key, gistic);
                }
            }
        }
    }

    public static <S extends AlterationCountBase> void setupAlterationGeneCountsMap(
        List<S> studyAlterationCountByGenes,
        Map<String, S> totalResult) {

        studyAlterationCountByGenes.forEach(datum -> {
            String key = datum.getUniqueEventKey();
            if (totalResult.containsKey(key)) {
                S alterationCountByGene = totalResult.get(key);
                alterationCountByGene.setTotalCount(alterationCountByGene.getTotalCount() + datum.getTotalCount());
                alterationCountByGene.setNumberOfAlteredCases(alterationCountByGene.getNumberOfAlteredCases() + datum.getNumberOfAlteredCases());
                alterationCountByGene.setNumberOfProfiledCases(0); //Set number of cases to zero 
                Set<String> matchingGenePanelIds = new HashSet<>();
                if (!alterationCountByGene.getMatchingGenePanelIds().isEmpty()) {
                    matchingGenePanelIds.addAll(alterationCountByGene.getMatchingGenePanelIds());
                }
                if (!datum.getMatchingGenePanelIds().isEmpty()) {
                    matchingGenePanelIds.addAll(datum.getMatchingGenePanelIds());
                }
                alterationCountByGene.setMatchingGenePanelIds(matchingGenePanelIds);
                totalResult.put(key, alterationCountByGene);
            } else {
                datum.setNumberOfProfiledCases(0); //Ensure number of cases is initialized to zero
                totalResult.put(key, datum);
            }
        });
    }
    
    public static <S extends AlterationCountBase> void updateAlterationGeneCountsMap(
        Map<String, S> totalResult,  // the map of all genes with at least one mutation in the whole cohort
        Long profiledCasesCount) {  // the list of all cases in the study

        List<S>  allGene= new ArrayList<>(totalResult.values()); // get all genes with at least one mutation in the whole cohort
        allGene.forEach(datum -> { // for each gene in the whole cohort
            String key = datum.getUniqueEventKey(); // get the unique key of the gene
            S alterationCountByGene = totalResult.get(key);  // get the gene from the map
            alterationCountByGene.setNumberOfProfiledCases(profiledCasesCount.intValue()); // the update the number of profiled cases for each study
            totalResult.put(key, alterationCountByGene); // update the map
        });
    }


}
