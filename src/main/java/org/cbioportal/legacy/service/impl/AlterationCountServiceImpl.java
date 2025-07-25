package org.cbioportal.legacy.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.math3.util.Pair;
import org.cbioportal.legacy.model.AlterationCountBase;
import org.cbioportal.legacy.model.AlterationCountByGene;
import org.cbioportal.legacy.model.AlterationCountByStructuralVariant;
import org.cbioportal.legacy.model.AlterationFilter;
import org.cbioportal.legacy.model.CopyNumberCountByGene;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.MolecularProfileCaseIdentifier;
import org.cbioportal.legacy.model.util.Select;
import org.cbioportal.legacy.persistence.AlterationRepository;
import org.cbioportal.legacy.persistence.MolecularProfileRepository;
import org.cbioportal.legacy.service.AlterationCountService;
import org.cbioportal.legacy.service.util.AlterationEnrichmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlterationCountServiceImpl implements AlterationCountService {

  private final AlterationRepository alterationRepository;
  private final AlterationEnrichmentUtil<AlterationCountByGene> alterationEnrichmentUtil;
  private final AlterationEnrichmentUtil<CopyNumberCountByGene> alterationEnrichmentUtilCna;
  private final AlterationEnrichmentUtil<AlterationCountByStructuralVariant>
      alterationEnrichmentUtilStructVar;
  private final MolecularProfileRepository molecularProfileRepository;

  @Autowired
  public AlterationCountServiceImpl(
      AlterationRepository alterationRepository,
      AlterationEnrichmentUtil<AlterationCountByGene> alterationEnrichmentUtil,
      AlterationEnrichmentUtil<CopyNumberCountByGene> alterationEnrichmentUtilCna,
      AlterationEnrichmentUtil<AlterationCountByStructuralVariant>
          alterationEnrichmentUtilStructVar,
      MolecularProfileRepository molecularProfileRepository) {
    this.alterationRepository = alterationRepository;
    this.alterationEnrichmentUtil = alterationEnrichmentUtil;
    this.alterationEnrichmentUtilCna = alterationEnrichmentUtilCna;
    this.alterationEnrichmentUtilStructVar = alterationEnrichmentUtilStructVar;
    this.molecularProfileRepository = molecularProfileRepository;
  }

  @Override
  public Pair<List<AlterationCountByGene>, Long> getSampleAlterationGeneCounts(
      List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      Select<Integer> entrezGeneIds,
      boolean includeFrequency,
      boolean includeMissingAlterationsFromGenePanel,
      AlterationFilter alterationFilter) {

    Function<List<MolecularProfileCaseIdentifier>, List<AlterationCountByGene>> dataFetcher =
        profileCaseIdentifiers ->
            alterationRepository.getSampleAlterationGeneCounts(
                new TreeSet<>(profileCaseIdentifiers), entrezGeneIds, alterationFilter);

    BiFunction<List<MolecularProfileCaseIdentifier>, List<AlterationCountByGene>, Long>
        includeFrequencyFunction =
            (a, b) ->
                alterationEnrichmentUtil.includeFrequencyForSamples(
                    a, b, includeMissingAlterationsFromGenePanel);

    return getAlterationGeneCounts(
        molecularProfileCaseIdentifiers, includeFrequency, dataFetcher, includeFrequencyFunction);
  }

  @Override
  public Pair<List<AlterationCountByGene>, Long> getPatientAlterationGeneCounts(
      List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      Select<Integer> entrezGeneIds,
      boolean includeFrequency,
      boolean includeMissingAlterationsFromGenePanel,
      AlterationFilter alterationFilter) {

    Function<List<MolecularProfileCaseIdentifier>, List<AlterationCountByGene>> dataFetcher =
        profileCaseIdentifiers ->
            alterationRepository.getPatientAlterationGeneCounts(
                new TreeSet<>(profileCaseIdentifiers), entrezGeneIds, alterationFilter);

    BiFunction<List<MolecularProfileCaseIdentifier>, List<AlterationCountByGene>, Long>
        includeFrequencyFunction =
            (a, b) ->
                alterationEnrichmentUtil.includeFrequencyForPatients(
                    a, b, includeMissingAlterationsFromGenePanel);

    return getAlterationGeneCounts(
        molecularProfileCaseIdentifiers, includeFrequency, dataFetcher, includeFrequencyFunction);
  }

  @Override
  public Pair<List<AlterationCountByGene>, Long> getSampleMutationGeneCounts(
      List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      Select<Integer> entrezGeneIds,
      boolean includeFrequency,
      boolean includeMissingAlterationsFromGenePanel,
      AlterationFilter alterationFilter) {
    return getSampleAlterationGeneCounts(
        molecularProfileCaseIdentifiers,
        entrezGeneIds,
        includeFrequency,
        includeMissingAlterationsFromGenePanel,
        alterationFilter);
  }

  @Override
  public Pair<List<AlterationCountByGene>, Long> getPatientMutationGeneCounts(
      List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      Select<Integer> entrezGeneIds,
      boolean includeFrequency,
      boolean includeMissingAlterationsFromGenePanel,
      AlterationFilter alterationFilter) {
    return getPatientAlterationGeneCounts(
        molecularProfileCaseIdentifiers,
        entrezGeneIds,
        includeFrequency,
        includeMissingAlterationsFromGenePanel,
        alterationFilter);
  }

  @Override
  public Pair<List<AlterationCountByGene>, Long> getSampleStructuralVariantGeneCounts(
      List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      Select<Integer> entrezGeneIds,
      boolean includeFrequency,
      boolean includeMissingAlterationsFromGenePanel,
      AlterationFilter alterationFilter) {
    return getSampleAlterationGeneCounts(
        molecularProfileCaseIdentifiers,
        entrezGeneIds,
        includeFrequency,
        includeMissingAlterationsFromGenePanel,
        alterationFilter);
  }

  @Override
  public Pair<List<AlterationCountByGene>, Long> getPatientStructuralVariantGeneCounts(
      List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      Select<Integer> entrezGeneIds,
      boolean includeFrequency,
      boolean includeMissingAlterationsFromGenePanel,
      AlterationFilter alterationFilter) {
    return getPatientAlterationGeneCounts(
        molecularProfileCaseIdentifiers,
        entrezGeneIds,
        includeFrequency,
        includeMissingAlterationsFromGenePanel,
        alterationFilter);
  }

  @Override
  public Pair<List<AlterationCountByStructuralVariant>, Long> getSampleStructuralVariantCounts(
      List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      boolean includeFrequency,
      boolean includeMissingAlterationsFromGenePanel,
      AlterationFilter alterationFilter) {

    Function<List<MolecularProfileCaseIdentifier>, List<AlterationCountByStructuralVariant>>
        dataFetcher =
            profileCaseIdentifiers ->
                alterationRepository.getSampleStructuralVariantCounts(
                    new TreeSet<>(profileCaseIdentifiers), alterationFilter);

    BiFunction<List<MolecularProfileCaseIdentifier>, List<AlterationCountByStructuralVariant>, Long>
        includeFrequencyFunction =
            (a, b) ->
                alterationEnrichmentUtilStructVar.includeFrequencyForSamples(
                    a, b, includeMissingAlterationsFromGenePanel);

    return getAlterationGeneCounts(
        molecularProfileCaseIdentifiers, includeFrequency, dataFetcher, includeFrequencyFunction);
  }

  @Override
  public Pair<List<CopyNumberCountByGene>, Long> getSampleCnaGeneCounts(
      List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      Select<Integer> entrezGeneIds,
      boolean includeFrequency,
      boolean includeMissingAlterationsFromGenePanel,
      AlterationFilter alterationFilter) {

    Function<List<MolecularProfileCaseIdentifier>, List<CopyNumberCountByGene>> dataFetcher =
        profileCaseIdentifiers ->
            alterationRepository.getSampleCnaGeneCounts(
                new TreeSet<>(profileCaseIdentifiers), entrezGeneIds, alterationFilter);

    BiFunction<List<MolecularProfileCaseIdentifier>, List<CopyNumberCountByGene>, Long>
        includeFrequencyFunction =
            (a, b) ->
                alterationEnrichmentUtilCna.includeFrequencyForSamples(
                    a, b, includeMissingAlterationsFromGenePanel);

    return getAlterationGeneCounts(
        molecularProfileCaseIdentifiers, includeFrequency, dataFetcher, includeFrequencyFunction);
  }

  @Override
  public Pair<List<CopyNumberCountByGene>, Long> getPatientCnaGeneCounts(
      List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      Select<Integer> entrezGeneIds,
      boolean includeFrequency,
      boolean includeMissingAlterationsFromGenePanel,
      AlterationFilter alterationFilter) {

    Function<List<MolecularProfileCaseIdentifier>, List<CopyNumberCountByGene>> dataFetcher =
        profileCaseIdentifiers ->
            alterationRepository.getPatientCnaGeneCounts(
                new TreeSet<>(profileCaseIdentifiers), entrezGeneIds, alterationFilter);

    BiFunction<List<MolecularProfileCaseIdentifier>, List<CopyNumberCountByGene>, Long>
        includeFrequencyFunction =
            (a, b) ->
                alterationEnrichmentUtilCna.includeFrequencyForPatients(
                    a, b, includeMissingAlterationsFromGenePanel);

    return getAlterationGeneCounts(
        molecularProfileCaseIdentifiers, includeFrequency, dataFetcher, includeFrequencyFunction);
  }

  /**
   * Retrieves gene alteration counts and calculates frequency if requested.
   *
   * @param <S> The specific type extending AlterationCountBase
   * @param molecularProfileCaseIdentifiers List of molecular profile identifiers
   * @param includeFrequency Whether to include frequency calculation
   * @param dataFetcher Function to fetch alteration count data
   * @param includeFrequencyFunction Function needed for frequency calculation
   * @return A pair containing the list of alteration counts and the total profiled cases count
   */
  private <S extends AlterationCountBase> Pair<List<S>, Long> getAlterationGeneCounts(
      List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
      boolean includeFrequency,
      Function<List<MolecularProfileCaseIdentifier>, List<S>> dataFetcher,
      BiFunction<List<MolecularProfileCaseIdentifier>, List<S>, Long> includeFrequencyFunction) {

    if (molecularProfileCaseIdentifiers.isEmpty()) {
      return new Pair<>(Collections.emptyList(), 0L);
    }

    // Get mapping from molecular profile IDs to study IDs
    Map<String, String> molecularProfileIdStudyIdMap =
        molecularProfileRepository
            .getMolecularProfiles(
                molecularProfileCaseIdentifiers.stream()
                    .map(MolecularProfileCaseIdentifier::getMolecularProfileId)
                    .collect(Collectors.toSet()),
                "SUMMARY")
            .stream()
            .collect(
                Collectors.toMap(
                    MolecularProfile::getStableId, MolecularProfile::getCancerStudyIdentifier));

    // Group by study ID and fetch alteration count data
    List<S> allAlterationCountByGenes =
        molecularProfileCaseIdentifiers.stream()
            .collect(
                Collectors.groupingBy(
                    identifier ->
                        molecularProfileIdStudyIdMap.get(identifier.getMolecularProfileId())))
            .values()
            .stream()
            .flatMap(group -> dataFetcher.apply(group).stream())
            .toList();

    // Merge alteration count data
    List<S> mergedAlterationCounts = mergeAlterationCounts(allAlterationCountByGenes);

    // Calculate frequency if requested
    long profiledCasesCount = 0L;
    if (includeFrequency) {
      profiledCasesCount =
          includeFrequencyFunction.apply(molecularProfileCaseIdentifiers, mergedAlterationCounts);
    }

    return new Pair<>(mergedAlterationCounts, profiledCasesCount);
  }

  /**
   * Merges alteration counts across different studies for the same gene/event.
   *
   * @param <S> The specific type extending AlterationCountBase
   * @param alterationCountByGenes List of alteration counts to be merged
   * @return A list of merged alteration counts
   */
  private <S extends AlterationCountBase> List<S> mergeAlterationCounts(
      List<S> alterationCountByGenes) {
    Map<String, S> mergedAlterationCountsMap = new HashMap<>();

    for (S datum : alterationCountByGenes) {
      String key = datum.getUniqueEventKey();

      if (mergedAlterationCountsMap.containsKey(key)) {
        // If already seen, sum up relevant raw counts
        S existing = mergedAlterationCountsMap.get(key);
        existing.setTotalCount(existing.getTotalCount() + datum.getTotalCount());
        existing.setNumberOfAlteredCases(
            existing.getNumberOfAlteredCases() + datum.getNumberOfAlteredCases());
        // NOTE: numberOfProfiledCases is intentionally not merged here
      } else {
        // First occurrence of this key, add to map
        mergedAlterationCountsMap.put(key, datum);
      }
    }

    return new ArrayList<>(mergedAlterationCountsMap.values());
  }
}
