package org.cbioportal.domain.alteration.usecase;

import org.cbioportal.domain.alteration.repository.AlterationRepository;
import org.cbioportal.domain.generic_assay.usecase.GetFilteredMolecularProfilesByAlterationType;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.AlterationCountByGene;
import org.cbioportal.legacy.model.AlterationType;
import org.cbioportal.legacy.model.MolecularProfile;
import org.springframework.lang.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

abstract class AbstractAlterationCountByGeneUseCase {

    private static final String WHOLE_EXOME_SEQUENCING = "WES";

    private final AlterationRepository alterationRepository;
    private final GetFilteredMolecularProfilesByAlterationType getFilteredMolecularProfilesByAlterationType;

    AbstractAlterationCountByGeneUseCase(AlterationRepository alterationRepository, GetFilteredMolecularProfilesByAlterationType getFilteredMolecularProfilesByAlterationType){
        this.alterationRepository = alterationRepository;
        this.getFilteredMolecularProfilesByAlterationType = getFilteredMolecularProfilesByAlterationType;
    }

    /**
     * Populates alteration counts with profile data, including the total profiled count and matching gene panel IDs.
     *
     * @param alterationCounts       List of alteration counts to enrich.
     * @param studyViewFilterContext Context containing filter criteria.
     * @param alterationType         Type of alteration (e.g., mutation, CNA, structural variant).
     * @param <T>                    The type of alteration count.
     * @return List of enriched alteration counts.
     */
    <T extends AlterationCountByGene> List<T> populateAlterationCounts(@NonNull List<T> alterationCounts,
                                                                       @NonNull StudyViewFilterContext studyViewFilterContext,
                                                                       @NonNull AlterationType alterationType) {
        final var firstMolecularProfileForEachStudy = getFirstMolecularProfileGroupedByStudy(studyViewFilterContext,
                alterationType);
        // Get sample profiled count by study
        final Map<String, Integer> studyIdToSampleProfiledCount = alterationRepository.getTotalProfiledCountsByAlterationType(studyViewFilterContext,
                alterationType.toString());
        // Get profiled counts by gene and study
        var profiledCountsByStudy = alterationRepository.getTotalProfiledCounts(studyViewFilterContext, alterationType.toString(),
                firstMolecularProfileForEachStudy);
        // Get matching gene panel IDs
        final var matchingGenePanelIdsMap = alterationRepository.getMatchingGenePanelIds(studyViewFilterContext,
                alterationType.toString());
        // Get WES profiled count by study
        final Map<String, Integer> studyIdToWESProfiledCount =
                alterationRepository.getSampleProfileCountWithoutPanelData(studyViewFilterContext, alterationType.toString());

        alterationCounts.parallelStream()
                .forEach(alterationCountByGene -> {
                    String hugoGeneSymbol = alterationCountByGene.getHugoGeneSymbol();
                    Set<String> matchingGenePanelIds = matchingGenePanelIdsMap.get(hugoGeneSymbol) != null ?
                            matchingGenePanelIdsMap.get(hugoGeneSymbol) : Collections.emptySet();
    
                    // Get studies where this gene has profiled data
                    Map<String, Integer> geneProfiledCountByStudy = profiledCountsByStudy.getOrDefault(hugoGeneSymbol, Collections.emptyMap());
                    // Get studies where this gene has alterations
                    Set<String> alteredInStudyIds = alterationCountByGene.getAlteredInStudyIds();
                    // Combine: all studies where this gene is altered or profiled (in gene panels or WES)
                    Set<String> relevantStudyIds = new HashSet<>(alteredInStudyIds);
                    relevantStudyIds.addAll(geneProfiledCountByStudy.keySet());
                    relevantStudyIds.addAll(studyIdToWESProfiledCount.keySet());
                    
                    // Only calculate WES and sample profiled counts for relevant studies
                    Map<String, Integer> relevantWESProfiledCount = new HashMap<>();
                    Map<String, Integer> relevantSampleProfiledCount = new HashMap<>();
                    for (String studyId : relevantStudyIds) {
                        if (studyIdToWESProfiledCount.containsKey(studyId)) {
                            relevantWESProfiledCount.put(studyId, studyIdToWESProfiledCount.get(studyId));
                        }
                        if (studyIdToSampleProfiledCount.containsKey(studyId)) {
                            relevantSampleProfiledCount.put(studyId, studyIdToSampleProfiledCount.get(studyId));
                        }
                    }

                    // Calculate panel-based non-WES profiled count from profiledCountsMap 
                    int alterationsProfiledCount = geneProfiledCountByStudy.values().stream()
                        .mapToInt(Integer::intValue)
                        .sum();
    
                    // Compute the total profiled count
                    int alterationTotalProfiledCount = computeTotalProfiledCount(
                        hasGenePanelData(matchingGenePanelIds),
                        alterationsProfiledCount,
                        relevantWESProfiledCount,
                        relevantSampleProfiledCount);

                    alterationCountByGene.setNumberOfProfiledCases(alterationTotalProfiledCount);
                    alterationCountByGene.setMatchingGenePanelIds(matchingGenePanelIds);

                });
        return alterationCounts;
    }

    /**
     * Determines if a gene has associated gene panel data
     *
     * @param matchingGenePanelIds Set of gene panel IDs associated with the gene
     * @return true if gene has panel data, false otherwise
     */
    private boolean hasGenePanelData(@NonNull Set<String> matchingGenePanelIds) {
        return matchingGenePanelIds.contains(WHOLE_EXOME_SEQUENCING)
                && matchingGenePanelIds.size() > 1 || !matchingGenePanelIds.contains(WHOLE_EXOME_SEQUENCING) && !matchingGenePanelIds.isEmpty();
    }

    /**
     * Computes the total profiled count for a gene based on gene panel data availability
     *
     * @param hasGenePanelData Whether the gene has panel data
     * @param alterationsProfiledCount Profile counts from non-WES gene panels
     * @param studyIdToWESProfiledCount Map of study IDs to WES profiled counts
     * @param studyIdToSampleProfiledCount Map of study IDs to sample profiled counts
     * @return Total profiled count for the gene
     */
    private int computeTotalProfiledCount(boolean hasGenePanelData, int alterationsProfiledCount,
                                          Map<String, Integer> studyIdToWESProfiledCount, Map<String, Integer> studyIdToSampleProfiledCount) {
        if (hasGenePanelData) {
            // For genes with panel data, use panel count + WES count
            return alterationsProfiledCount + studyIdToWESProfiledCount.values().stream().mapToInt(Integer::intValue).sum();
        } else {
            // For genes without panel data, use WES count or sample count if WES is 0
            Map<String, Integer> updatedWESProfiledCount = new HashMap<>(studyIdToWESProfiledCount);
            for (Map.Entry<String, Integer> entry : studyIdToSampleProfiledCount.entrySet()) {
                String studyId = entry.getKey();
                if (updatedWESProfiledCount.getOrDefault(studyId, 0) == 0) {
                    updatedWESProfiledCount.put(studyId, studyIdToSampleProfiledCount.getOrDefault(studyId, 0));
                }
            }
            return updatedWESProfiledCount.values().stream().mapToInt(Integer::intValue).sum();
        }
    }

    /**
     * Retrieves the first molecular profile for each study based on the alteration type.
     *
     * @param studyViewFilterContext Context containing filter criteria.
     * @param alterationType Type of alteration (e.g., mutation, CNA, structural variant).
     * @return List of MolecularProfile objects representing the first profile for each study.
     */
    private List<MolecularProfile> getFirstMolecularProfileGroupedByStudy(StudyViewFilterContext studyViewFilterContext, AlterationType alterationType) {
        final var molecularProfiles =
                getFilteredMolecularProfilesByAlterationType.execute(studyViewFilterContext, alterationType.toString());
        return getFirstMolecularProfileGroupedByStudy(molecularProfiles);
    }

    private List<MolecularProfile> getFirstMolecularProfileGroupedByStudy(List<MolecularProfile> molecularProfiles) {
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

}
