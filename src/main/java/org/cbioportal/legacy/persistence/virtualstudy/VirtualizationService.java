package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.VirtualStudy;

public class VirtualizationService {

  private final VirtualStudyService virtualStudyService;
  private final VSAwareMolecularProfileRepository molecularProfileRepository;

  public VirtualizationService(
      VirtualStudyService virtualStudyService,
      VSAwareMolecularProfileRepository molecularProfileRepository) {
    this.virtualStudyService = virtualStudyService;
    this.molecularProfileRepository = molecularProfileRepository;
  }

  public <T> List<T> handleMolecularData(
      String molecularProfileId,
      List<String> sampleIds,
      Function<T, String> getMolecularProfileId,
      Function<T, String> getSampleId,
      BiFunction<String, List<String>, List<T>> fetch,
      BiFunction<MolecularProfile, T, T> virtualize) {
    return handleMolecularData(
        repeatMolecularProfileId(molecularProfileId, sampleIds),
        sampleIds,
        getMolecularProfileId,
        getSampleId,
        (mpids, sids) -> {
          checkAllValuesTheSame(mpids);
          return fetch.apply(mpids.getFirst(), sids);
        },
        virtualize);
  }

  public <T> List<T> handleMolecularData(
      String molecularProfileId,
      Function<String, List<T>> fetch,
      BiFunction<MolecularProfile, T, T> virtualize) {
    MolecularProfile molecularProfile =
        molecularProfileRepository.getMolecularProfile(molecularProfileId);
    Optional<VirtualStudy> virtualStudyOptional =
        virtualStudyService.getVirtualStudyByIdIfExists(
            molecularProfile.getCancerStudyIdentifier());
    if (virtualStudyOptional.isEmpty()) {
      return fetch.apply(molecularProfileId);
    }
    return fetch
        .apply(
            calculateOriginalMolecularProfileId(
                molecularProfileId, virtualStudyOptional.get().getId()))
        .stream()
        .map(md -> virtualize.apply(molecularProfile, md))
        .toList();
  }

  // TODO has to be by substitution of the study id back with the original stable id
  private String calculateOriginalMolecularProfileId(
      String molecularProfileId, String virtualStudyId) {
    return molecularProfileId.replace(virtualStudyId + "_", "");
  }

  private static void checkAllValuesTheSame(List<String> molecularProfileIds) {
    int uniqueMolecularProfileIds = new HashSet<>(molecularProfileIds).size();
    if (uniqueMolecularProfileIds > 1) {
      throw new IllegalArgumentException(
          "Molecular profile ids must be the same for all sample ids");
    }
  }

  private static List<String> repeatMolecularProfileId(
      String molecularProfileId, List<String> sampleIds) {
    return sampleIds.stream().map(sampleId -> molecularProfileId).toList();
  }

  public <T> List<T> handleMolecularData(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      Function<T, String> getMolecularProfileId,
      Function<T, String> getSampleId,
      BiFunction<List<String>, List<String>, List<T>> fetch,
      BiFunction<MolecularProfile, T, T> virtualize) {
    TranslatedIdsInfo translatedIdsInfo = translateIds(molecularProfileIds, sampleIds);
    List<T> fetchedEntities =
        fetch.apply(
            translatedIdsInfo.idsLists().molecularProfile(),
            translatedIdsInfo.idsLists().sampleIds());
    ArrayList<T> result = new ArrayList<>();
    for (T molecularDataEntity : fetchedEntities) {
      String molecularProfileId = getMolecularProfileId.apply(molecularDataEntity);
      String sampleId = getSampleId.apply(molecularDataEntity);
      if (translatedIdsInfo.molecularProfileToCorrectSampleIds().containsKey(molecularProfileId)
          && translatedIdsInfo
              .molecularProfileToCorrectSampleIds()
              .get(molecularProfileId)
              .contains(sampleId)) {
        // this is a materialized structural variant
        result.add(molecularDataEntity);
      }
      if (translatedIdsInfo
          .materializedMolecularProfileToVirtualMolecularProfileIds()
          .containsKey(molecularProfileId)) {
        Set<String> virtualMolecularProfileIds =
            translatedIdsInfo
                .materializedMolecularProfileToVirtualMolecularProfileIds()
                .get(molecularProfileId);
        // this is a virtual structural variant
        for (String virtualMolecularProfileId : virtualMolecularProfileIds) {
          // we need to check if the sample id is in the correct sample ids for the virtual
          // molecular profile
          // this is a virtual structural variant
          if (translatedIdsInfo
                  .molecularProfileToCorrectSampleIds()
                  .containsKey(virtualMolecularProfileId)
              && translatedIdsInfo
                  .molecularProfileToCorrectSampleIds()
                  .get(virtualMolecularProfileId)
                  .contains(sampleId)) {
            MolecularProfile virtualMolecularProfile =
                translatedIdsInfo
                    .virtualMolecularProfilesByStableId()
                    .get(virtualMolecularProfileId);
            result.add(virtualize.apply(virtualMolecularProfile, molecularDataEntity));
          }
        }
      }
    }
    // TODO we might want to sort the result here in certain order if needed
    return result;
  }

  public Pair<String, List<String>> toMaterializedMolecularProfileIds(
      String molecularProfileId, List<String> sampleIds) {
    Pair<List<String>, List<String>> materializedMolecularProfileIds;
    if (sampleIds == null) {
      MolecularProfile molecularProfile =
          molecularProfileRepository.getMolecularProfile(molecularProfileId);
      Optional<VirtualStudy> virtualStudyOptional =
          virtualStudyService.getVirtualStudyByIdIfExists(
              molecularProfile.getCancerStudyIdentifier());
      if (virtualStudyOptional.isEmpty()) {
        return ImmutablePair.of(molecularProfileId, null);
      }
      // If the molecular profile is virtual, we need to return the stable id and all sample ids
      return ImmutablePair.of(
          // TODO it has to be calculated differently
          calculateOriginalMolecularProfileId(
              molecularProfile.getStableId(), molecularProfile.getCancerStudyIdentifier()),
          virtualStudyOptional.get().getData().getStudies().stream()
              .flatMap(vss -> vss.getSamples().stream())
              .collect(Collectors.toList()));
    } else {
      materializedMolecularProfileIds =
          toMaterializedMolecularProfileIds(
              repeatMolecularProfileId(molecularProfileId, sampleIds), sampleIds);
    }
    checkAllValuesTheSame(materializedMolecularProfileIds.getKey());
    return ImmutablePair.of(
        materializedMolecularProfileIds.getKey().getFirst(),
        materializedMolecularProfileIds.getValue());
  }

  public Pair<List<String>, List<String>> toMaterializedMolecularProfileIds(
      List<String> molecularProfileIds, List<String> sampleIds) {
    if (sampleIds == null) {
      throw new UnsupportedOperationException(
          "Sample ids cannot be null when translating molecular profile ids");
    }
    MolecularProfileSampleIds molecularProfileSampleIds =
        translateIds(molecularProfileIds, sampleIds).idsLists();
    return ImmutablePair.of(
        molecularProfileSampleIds.molecularProfile(), molecularProfileSampleIds.sampleIds());
  }

  private TranslatedIdsInfo translateIds(List<String> molecularProfileIds, List<String> sampleIds) {
    LinkedHashMap<String, LinkedHashSet<String>> molecularProfileToSampleIds =
        toMap(molecularProfileIds, sampleIds);
    Map<String, VirtualStudy> virtualStudiesById = getPublishedVirtualStudiesById();
    Map<String, MolecularProfile> molecularProfilesByStableId =
        getMolecularProfileById(molecularProfileToSampleIds.keySet());
    Map<String, MolecularProfile> virtualMolecularProfilesByStableId =
        molecularProfilesByStableId.entrySet().stream()
            .filter(
                entry ->
                    virtualStudiesById.containsKey(entry.getValue().getCancerStudyIdentifier()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    LinkedHashMap<String, LinkedHashSet<String>> molecularProfileToCorrectSampleIds =
        molecularProfileToSampleIds.sequencedEntrySet().stream()
            .map(
                entry -> {
                  String molecularProfileId = entry.getKey();
                  LinkedHashSet<String> sampleIdsSet = entry.getValue();
                  MolecularProfile molecularProfile =
                      virtualMolecularProfilesByStableId.get(molecularProfileId);
                  if (molecularProfile == null) {
                    return new AbstractMap.SimpleEntry<>(molecularProfileId, sampleIdsSet);
                  }
                  VirtualStudy virtualStudy =
                      virtualStudiesById.get(molecularProfile.getCancerStudyIdentifier());
                  Set<String> allVsSampleIds =
                      virtualStudy.getData().getStudies().stream()
                          .flatMap(vss -> vss.getSamples().stream())
                          .collect(Collectors.toSet());
                  return new AbstractMap.SimpleEntry<>(
                      molecularProfileId,
                      sampleIdsSet.stream()
                          .filter(allVsSampleIds::contains)
                          .collect(Collectors.toCollection(LinkedHashSet::new)));
                })
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (a, b) -> {
                      a.addAll(b);
                      return a;
                    },
                    LinkedHashMap::new));
    // TODO this way of calculating virtual molecular profile ids has to change
    Map<String, String> virtualMolecularProfileIdToMaterializedMolecularProfileId =
        virtualMolecularProfilesByStableId.entrySet().stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    e ->
                        calculateOriginalMolecularProfileId(
                            e.getKey(), e.getValue().getCancerStudyIdentifier())));
    Map<String, Set<String>> materializedMolecularProfileToVirtualMolecularProfileIds =
        virtualMolecularProfileIdToMaterializedMolecularProfileId.entrySet().stream()
            .collect(
                Collectors.groupingBy(
                    Map.Entry::getValue,
                    Collectors.mapping(Map.Entry::getKey, Collectors.toSet())));
    Map<String, Set<String>> molecularProfileIdToSampleIdToQuery =
        molecularProfileToCorrectSampleIds.entrySet().stream()
            .map(
                entry ->
                    new AbstractMap.SimpleEntry<>(
                        virtualMolecularProfileIdToMaterializedMolecularProfileId.getOrDefault(
                            entry.getKey(), entry.getKey()),
                        entry.getValue()))
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (a, b) -> {
                      a.addAll(b);
                      return a;
                    },
                    LinkedHashMap::new));
    MolecularProfileSampleIds idsLists = getIdsLists(molecularProfileIdToSampleIdToQuery);
    return new TranslatedIdsInfo(
        virtualMolecularProfilesByStableId,
        molecularProfileToCorrectSampleIds,
        materializedMolecularProfileToVirtualMolecularProfileIds,
        idsLists);
  }

  private record TranslatedIdsInfo(
      Map<String, MolecularProfile> virtualMolecularProfilesByStableId,
      LinkedHashMap<String, LinkedHashSet<String>> molecularProfileToCorrectSampleIds,
      Map<String, Set<String>> materializedMolecularProfileToVirtualMolecularProfileIds,
      MolecularProfileSampleIds idsLists) {}

  private static MolecularProfileSampleIds getIdsLists(
      Map<String, Set<String>> molecularProfileIdToSampleIdToQuery) {
    List<String> molecularProfileIdsToQuery = new ArrayList<>();
    List<String> sampleIdsToQuery = new ArrayList<>();
    for (Map.Entry<String, Set<String>> entry : molecularProfileIdToSampleIdToQuery.entrySet()) {
      String molecularProfileId = entry.getKey();
      Set<String> sampleIdsSet = entry.getValue();
      for (String sampleId : sampleIdsSet) {
        molecularProfileIdsToQuery.add(molecularProfileId);
        sampleIdsToQuery.add(sampleId);
      }
    }
    return new MolecularProfileSampleIds(molecularProfileIdsToQuery, sampleIdsToQuery);
  }

  private record MolecularProfileSampleIds(List<String> molecularProfile, List<String> sampleIds) {}

  private Map<String, MolecularProfile> getMolecularProfileById(Set<String> molecularProfileIds) {
    return molecularProfileRepository
        .getMolecularProfiles(molecularProfileIds, Projection.DETAILED.name())
        .stream()
        .collect(Collectors.toMap(MolecularProfile::getStableId, Function.identity()));
  }

  private Map<String, VirtualStudy> getPublishedVirtualStudiesById() {
    return virtualStudyService.getPublishedVirtualStudies().stream()
        .collect(Collectors.toMap(VirtualStudy::getId, Function.identity()));
  }

  private static LinkedHashMap<String, LinkedHashSet<String>> toMap(
      List<String> molecularProfileIds, List<String> sampleIds) {
    checkSizeMatches(molecularProfileIds, sampleIds);
    LinkedHashMap<String, LinkedHashSet<String>> molecularProfileToSampleIds =
        new LinkedHashMap<>();
    for (int i = 0; i < molecularProfileIds.size(); i++) {
      String molecularProfileId = molecularProfileIds.get(i);
      String sampleId = sampleIds.get(i);
      molecularProfileToSampleIds
          .computeIfAbsent(molecularProfileId, k -> new LinkedHashSet<>())
          .add(sampleId);
    }
    return molecularProfileToSampleIds;
  }

  private static void checkSizeMatches(List<String> molecularProfileIds, List<String> sampleIds) {
    if (molecularProfileIds.size() != sampleIds.size()) {
      throw new IllegalArgumentException(
          "Molecular profile ids and sample ids must have the same size");
    }
  }
}
