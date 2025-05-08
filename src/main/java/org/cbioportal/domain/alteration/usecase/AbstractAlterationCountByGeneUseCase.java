package org.cbioportal.domain.alteration.usecase;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.cbioportal.domain.alteration.repository.AlterationRepository;
import org.cbioportal.domain.generic_assay.usecase.GetFilteredMolecularProfilesByAlterationType;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.AlterationCountByGene;
import org.cbioportal.legacy.model.AlterationType;
import org.cbioportal.legacy.model.MolecularProfile;
import org.springframework.lang.NonNull;

abstract class AbstractAlterationCountByGeneUseCase {

  private static final String WHOLE_EXOME_SEQUENCING = "WES";

  private final AlterationRepository alterationRepository;
  private final GetFilteredMolecularProfilesByAlterationType
      getFilteredMolecularProfilesByAlterationType;

  AbstractAlterationCountByGeneUseCase(
      AlterationRepository alterationRepository,
      GetFilteredMolecularProfilesByAlterationType getFilteredMolecularProfilesByAlterationType) {
    this.alterationRepository = alterationRepository;
    this.getFilteredMolecularProfilesByAlterationType =
        getFilteredMolecularProfilesByAlterationType;
  }

  /**
   * Populates alteration counts with profile data, including the total profiled count and matching
   * gene panel IDs.
   *
   * @param alterationCounts List of alteration counts to enrich.
   * @param studyViewFilterContext Context containing filter criteria.
   * @param alterationType Type of alteration (e.g., mutation, CNA, structural variant).
   * @param <T> The type of alteration count.
   * @return List of enriched alteration counts.
   */
  <T extends AlterationCountByGene> List<T> populateAlterationCounts(
      @NonNull List<T> alterationCounts,
      @NonNull StudyViewFilterContext studyViewFilterContext,
      @NonNull AlterationType alterationType) {
    final var firstMolecularProfileForEachStudy =
        getFirstMolecularProfileGroupedByStudy(studyViewFilterContext, alterationType);
    final int totalProfiledCount =
        alterationRepository.getTotalProfiledCountsByAlterationType(
            studyViewFilterContext, alterationType.toString());
    var profiledCountsMap =
        alterationRepository.getTotalProfiledCounts(
            studyViewFilterContext, alterationType.toString(), firstMolecularProfileForEachStudy);
    final var matchingGenePanelIdsMap =
        alterationRepository.getMatchingGenePanelIds(
            studyViewFilterContext, alterationType.toString());
    final int sampleProfileCountWithoutGenePanelData =
        alterationRepository.getSampleProfileCountWithoutPanelData(
            studyViewFilterContext, alterationType.toString());

    alterationCounts.parallelStream()
        .forEach(
            alterationCountByGene -> {
              String hugoGeneSymbol = alterationCountByGene.getHugoGeneSymbol();
              Set<String> matchingGenePanelIds =
                  matchingGenePanelIdsMap.get(hugoGeneSymbol) != null
                      ? matchingGenePanelIdsMap.get(hugoGeneSymbol)
                      : Collections.emptySet();

              int alterationTotalProfiledCount =
                  computeTotalProfiledCount(
                      hasGenePanelData(matchingGenePanelIds),
                      profiledCountsMap.getOrDefault(hugoGeneSymbol, 0),
                      sampleProfileCountWithoutGenePanelData,
                      totalProfiledCount);

              alterationCountByGene.setNumberOfProfiledCases(alterationTotalProfiledCount);

              alterationCountByGene.setMatchingGenePanelIds(matchingGenePanelIds);
            });
    return alterationCounts;
  }

  private boolean hasGenePanelData(@NonNull Set<String> matchingGenePanelIds) {
    return matchingGenePanelIds.contains(WHOLE_EXOME_SEQUENCING) && matchingGenePanelIds.size() > 1
        || !matchingGenePanelIds.contains(WHOLE_EXOME_SEQUENCING)
            && !matchingGenePanelIds.isEmpty();
  }

  private int computeTotalProfiledCount(
      boolean hasGenePanelData,
      int alterationsProfiledCount,
      int sampleProfileCountWithoutGenePanelData,
      int totalProfiledCount) {
    int profiledCount =
        hasGenePanelData
            ? alterationsProfiledCount + sampleProfileCountWithoutGenePanelData
            : sampleProfileCountWithoutGenePanelData;
    return profiledCount == 0 ? totalProfiledCount : profiledCount;
  }

  /**
   * Retrieves the first molecular profile for each study based on the alteration type.
   *
   * @param studyViewFilterContext Context containing filter criteria.
   * @param alterationType Type of alteration (e.g., mutation, CNA, structural variant).
   * @return List of MolecularProfile objects representing the first profile for each study.
   */
  private List<MolecularProfile> getFirstMolecularProfileGroupedByStudy(
      StudyViewFilterContext studyViewFilterContext, AlterationType alterationType) {
    final var molecularProfiles =
        getFilteredMolecularProfilesByAlterationType.execute(
            studyViewFilterContext, alterationType.toString());
    return getFirstMolecularProfileGroupedByStudy(molecularProfiles);
  }

  private List<MolecularProfile> getFirstMolecularProfileGroupedByStudy(
      List<MolecularProfile> molecularProfiles) {
    return molecularProfiles.stream()
        .collect(
            Collectors.toMap(
                MolecularProfile::getCancerStudyIdentifier,
                Function.identity(),
                (existing, replacement) -> existing // Keep the first occurrence
                ))
        .values()
        .stream()
        .toList();
  }
}
