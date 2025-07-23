package org.cbioportal.legacy.persistence.virtualstudy;

import static org.cbioportal.legacy.persistence.virtualstudy.VirtualisationUtils.calculateOriginalMolecularProfileId;
import static org.cbioportal.legacy.persistence.virtualstudy.VirtualisationUtils.checkSingleSourceStudy;
import static org.cbioportal.legacy.persistence.virtualstudy.VirtualisationUtils.toIdLists;
import static org.cbioportal.legacy.persistence.virtualstudy.VirtualisationUtils.toStudyScopedIds;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.MolecularProfileCaseIdentifier;
import org.cbioportal.legacy.model.StudyScopedId;
import org.cbioportal.legacy.persistence.SampleRepository;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.VirtualStudy;

public class VirtualizationService {

  private final VirtualStudyService virtualStudyService;
  private final VSAwareMolecularProfileRepository molecularProfileRepository;
  private final SampleRepository sampleRepository;

  public VirtualizationService(
      VirtualStudyService virtualStudyService,
      SampleRepository sampleRepository,
      VSAwareMolecularProfileRepository molecularProfileRepository) {
    this.virtualStudyService = virtualStudyService;
    this.sampleRepository = sampleRepository;
    this.molecularProfileRepository = molecularProfileRepository;
  }

  public List<VirtualStudy> getPublishedVirtualStudies() {
    return virtualStudyService.getPublishedVirtualStudies();
  }

  public List<VirtualStudy> getPublishedVirtualStudies(String keyword) {
    return virtualStudyService.getPublishedVirtualStudies(keyword);
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

  public Map<String, Pair<String, Set<String>>> getVirtualMolecularProfileDefinition(
      Set<String> molecularProfileIds) {
    Map<String, MolecularProfile> molecularProfileById =
        getMolecularProfileById(molecularProfileIds);
    Map<String, VirtualStudy> publishedVirtualStudiesById = getPublishedVirtualStudiesById();
    return molecularProfileById.entrySet().stream()
        .map(
            entry -> {
              String cancerStudyIdentifier = entry.getValue().getCancerStudyIdentifier();
              String molecularProfileId = entry.getKey();
              if (publishedVirtualStudiesById.containsKey(cancerStudyIdentifier)) {
                VirtualStudy virtualStudy = publishedVirtualStudiesById.get(cancerStudyIdentifier);
                checkSingleSourceStudy(virtualStudy);
                String materializeStudyId =
                    virtualStudy.getData().getStudies().iterator().next().getId();
                Set<String> sampleIds =
                    virtualStudy.getData().getStudies().stream()
                        .flatMap(vss -> vss.getSamples().stream())
                        .collect(Collectors.toSet());
                String originalMolecularProfileId =
                    calculateOriginalMolecularProfileId(
                        molecularProfileId, cancerStudyIdentifier, materializeStudyId);
                Pair<String, Set<String>> originalProfileIdAndSampleIdsPair =
                    ImmutablePair.of(originalMolecularProfileId, sampleIds);
                return Pair.of(molecularProfileId, originalProfileIdAndSampleIdsPair);
              }
              Pair<String, Set<String>> noVirtaualProfilePair =
                  ImmutablePair.of(molecularProfileId, null);
              return Pair.of(molecularProfileId, noVirtaualProfilePair);
            })
        .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
  }

  public <T> List<T> handleMolecularData(
      String molecularProfileId,
      Function<String, List<T>> fetch,
      BiFunction<MolecularProfile, T, T> virtualize) {

    return handleMolecularData(molecularProfileId, null, fetch, virtualize);
  }

  public <T> List<T> handleMolecularData(
      String molecularProfileId,
      Function<T, String> getSampleId,
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
    VirtualStudy virtualStudy = virtualStudyOptional.get();
    checkSingleSourceStudy(virtualStudy);
    String materializeStudyId = virtualStudy.getData().getStudies().iterator().next().getId();
    Set<String> sampleIds =
        virtualStudy.getData().getStudies().stream()
            .flatMap(vss -> vss.getSamples().stream())
            .collect(Collectors.toSet());
    return fetch
        .apply(
            calculateOriginalMolecularProfileId(
                molecularProfileId, virtualStudy.getId(), materializeStudyId))
        .stream()
        .filter(e -> getSampleId == null || sampleIds.contains(getSampleId.apply(e)))
        .map(md -> virtualize.apply(molecularProfile, md))
        .toList();
  }

  public <T> List<T> handleMolecularData(
      Set<String> molecularProfileIds,
      Function<T, String> getMolecularProfileId,
      Function<Set<String>, List<T>> fetch,
      BiFunction<MolecularProfile, T, T> virtualize) {
    Map<String, MolecularProfile> molecularProfileById =
        getMolecularProfileById(molecularProfileIds);
    Map<String, VirtualStudy> publishedVirtualStudiesById = getPublishedVirtualStudiesById();
    Map<String, Set<MolecularProfile>> molecularProfileIdsToFetch =
        molecularProfileIds.stream()
            .map(
                mpid -> {
                  MolecularProfile molecularProfile = molecularProfileById.get(mpid);
                  if (publishedVirtualStudiesById.containsKey(
                      molecularProfile.getCancerStudyIdentifier())) {
                    VirtualStudy virtualStudy =
                        publishedVirtualStudiesById.get(
                            molecularProfile.getCancerStudyIdentifier());
                    checkSingleSourceStudy(virtualStudy);
                    String materializeStudyId =
                        virtualStudy.getData().getStudies().iterator().next().getId();
                    return Pair.of(
                        calculateOriginalMolecularProfileId(
                            mpid, molecularProfile.getCancerStudyIdentifier(), materializeStudyId),
                        molecularProfile);
                  }
                  return Pair.of(mpid, molecularProfile);
                })
            .collect(
                Collectors.toMap(
                    Pair::getLeft,
                    pair -> Set.of(pair.getRight()),
                    (set1, set2) -> {
                      Set<MolecularProfile> merged = new HashSet<>(set1);
                      merged.addAll(set2);
                      return merged;
                    }));
    List<T> result = new ArrayList<>();
    List<T> fetchedEntities = fetch.apply(molecularProfileIdsToFetch.keySet());
    for (T t : fetchedEntities) {
      String molecularProfileId = getMolecularProfileId.apply(t);
      Set<MolecularProfile> profiles = molecularProfileIdsToFetch.get(molecularProfileId);
      for (MolecularProfile profile : profiles) {
        if (profile.getStableId().equals(molecularProfileId)) {
          // this is a materialized structural variant
          result.add(t);
        } else {
          result.add(virtualize.apply(profile, t));
        }
      }
    }
    return result;
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
      VirtualStudy virtualStudy = virtualStudyOptional.get();
      checkSingleSourceStudy(virtualStudy);
      String materializeStudyId = virtualStudy.getData().getStudies().iterator().next().getId();
      return ImmutablePair.of(
          calculateOriginalMolecularProfileId(
              molecularProfile.getStableId(),
              molecularProfile.getCancerStudyIdentifier(),
              materializeStudyId),
          virtualStudy.getData().getStudies().stream()
              .flatMap(vss -> vss.getSamples().stream())
              .toList());
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

  public Set<MolecularProfileCaseIdentifier> toMaterializedMolecularProfileIds(
      Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers) {
    List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifierList =
        new ArrayList<>(molecularProfileCaseIdentifiers);
    List<String> caseIds =
        molecularProfileCaseIdentifierList.stream()
            .map(MolecularProfileCaseIdentifier::getCaseId)
            .toList();
    List<String> molecularProfileIds =
        molecularProfileCaseIdentifierList.stream()
            .map(MolecularProfileCaseIdentifier::getMolecularProfileId)
            .toList();
    Pair<List<String>, List<String>> idsLists =
        toMaterializedMolecularProfileIds(molecularProfileIds, caseIds);
    List<String> materializedMolecularProfileIds = idsLists.getKey();
    List<String> materializedCaseIds = idsLists.getValue();
    Set<MolecularProfileCaseIdentifier> materializedMolecularProfileCaseIdentifiers =
        new HashSet<>();
    for (int i = 0; i < materializedMolecularProfileIds.size(); i++) {
      String materializedMolecularProfileId = materializedMolecularProfileIds.get(i);
      String materializedCaseId = materializedCaseIds.get(i);
      materializedMolecularProfileCaseIdentifiers.add(
          new MolecularProfileCaseIdentifier(materializedCaseId, materializedMolecularProfileId));
    }
    return materializedMolecularProfileCaseIdentifiers;
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
    Map<String, String> virtualMolecularProfileIdToMaterializedMolecularProfileId =
        virtualMolecularProfilesByStableId.entrySet().stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    e -> {
                      String molecularProfileId = e.getKey();
                      String vsCancerStudyIdentifier = e.getValue().getCancerStudyIdentifier();
                      VirtualStudy virtualStudy = virtualStudiesById.get(vsCancerStudyIdentifier);
                      checkSingleSourceStudy(virtualStudy);
                      String materializeStudyId =
                          virtualStudy.getData().getStudies().iterator().next().getId();
                      return calculateOriginalMolecularProfileId(
                          molecularProfileId, vsCancerStudyIdentifier, materializeStudyId);
                    }));
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

  /**
   * Returns a map of virtual study-sample pairs to materialized study-sample pairs. The keys are
   * StudySamplePair objects representing the virtual study-sample pairs, and the values are
   * StudySamplePair objects representing the corresponding materialized study-sample pairs.
   *
   * @return a map of virtual to materialized study-sample pairs
   */
  // TODO cahce
  private Map<StudyScopedId, StudyScopedId> getVirtualToMaterializedStudySamplePairs() {
    return virtualStudyService.getPublishedVirtualStudies().stream()
        .flatMap(
            vs ->
                vs.getData().getStudies().stream()
                    .flatMap(
                        virtualStudySamples ->
                            virtualStudySamples.getSamples().stream()
                                .map(
                                    s ->
                                        ImmutablePair.of(
                                            // TODO We might want to use LinkedHashMap<String,
                                            // LinkedHashSet<String>> data structure instead
                                            new StudyScopedId(vs.getId(), s),
                                            new StudyScopedId(virtualStudySamples.getId(), s)))))
        .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
  }

  public Pair<List<String>, List<String>> toMaterializedStudySampleIds(
      List<String> studyIds, List<String> sampleIds) {
    List<StudyScopedId> requestedIds = toStudyScopedIds(studyIds, sampleIds);
    Map<StudyScopedId, StudyScopedId> map = getVirtualToMaterializedStudySamplePairs();
    List<StudyScopedId> materializedIds =
        requestedIds.stream().map(map::get).filter(Objects::nonNull).distinct().toList();
    return toIdLists(materializedIds);
  }

  /** Returns a map of virtual study-patient pairs to materialized study-patient pairs. */
  // TODO cahce
  private Map<StudyScopedId, StudyScopedId> getVirtualToMaterializedStudyPatientPairs() {
    return virtualStudyService.getPublishedVirtualStudies().stream()
        .flatMap(
            vs -> {
              List<String> studyIds =
                  vs.getData().getStudies().stream()
                      .flatMap(vss -> vss.getSamples().stream().map(s -> vss.getId()))
                      .toList();
              List<String> sampleIds =
                  vs.getData().getStudies().stream()
                      .flatMap(vss -> vss.getSamples().stream())
                      .toList();
              return sampleRepository
                  .fetchSamples(studyIds, sampleIds, Projection.ID.name())
                  .stream()
                  .map(s -> new StudyScopedId(s.getCancerStudyIdentifier(), s.getPatientStableId()))
                  .distinct()
                  .map(
                      ssi ->
                          ImmutablePair.of(new StudyScopedId(vs.getId(), ssi.getStableId()), ssi));
            })
        .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
  }

  /**
   * Converts a list of virtual StudySamplePair objects into a map where the keys are materialised
   * StudySamplePair objects and values are sets of study IDs (virtual or regular).
   */
  private Map<StudyScopedId, Set<String>> toMaterializedStudySamplePairsMap(
      List<StudyScopedId> studyScopedIds) {
    Map<StudyScopedId, StudyScopedId> vsToMzMap = getVirtualToMaterializedStudySamplePairs();
    return studyScopedIds.stream()
        .map(ssp -> ImmutablePair.of(vsToMzMap.getOrDefault(ssp, ssp), ssp.getStudyStableId()))
        .collect(
            Collectors.toMap(
                Pair::getLeft,
                pair -> Set.of(pair.getRight()),
                (existing, replacement) -> {
                  existing.addAll(replacement);
                  return existing;
                }));
  }

  /**
   * Converts a list of virtual StudyScopedId objects into a map where the keys are materialised
   * StudyScopedId objects and values are sets of study IDs (virtual or regular).
   *
   * @param studyScopedIds
   * @return
   */
  private Map<StudyScopedId, Set<String>> toMaterializedStudyPatientPairsMap(
      List<StudyScopedId> studyScopedIds) {
    Map<StudyScopedId, StudyScopedId> vsToMzMap = getVirtualToMaterializedStudyPatientPairs();
    return studyScopedIds.stream()
        .map(ssp -> ImmutablePair.of(vsToMzMap.getOrDefault(ssp, ssp), ssp.getStudyStableId()))
        .collect(
            Collectors.toMap(
                Pair::getLeft,
                pair -> Set.of(pair.getRight()),
                (existing, replacement) -> {
                  existing.addAll(replacement);
                  return existing;
                }));
  }

  public <T> List<T> handleStudyPatientData(
      List<String> studyIds,
      List<String> patientIds,
      Function<T, String> getStudyId,
      Function<T, String> getPatientId,
      BiFunction<List<String>, List<String>, List<T>> fetch,
      BiFunction<String, T, T> virtualize) {
    return handleStudyPatientData2(
        toStudyScopedIds(studyIds, patientIds),
        getStudyId,
        getPatientId,
        studyScopedIds -> {
          Pair<List<String>, List<String>> idsListPair = toIdLists(studyScopedIds);
          List<String> materialisedStudyIds = idsListPair.getLeft();
          List<String> materialisedPatientIds = idsListPair.getRight();
          return fetch.apply(materialisedStudyIds, materialisedPatientIds);
        },
        virtualize);
  }

  public <T> List<T> handleStudyPatientData(
      String studyId,
      Function<T, String> getPatientId,
      Function<String, List<T>> fetch,
      BiFunction<String, T, T> virtualize) {
    Map<String, VirtualStudy> publishedVirtualStudiesById = getPublishedVirtualStudiesById();
    if (!publishedVirtualStudiesById.containsKey(studyId)) {
      // If the study is not virtual, we can just fetch the data and return as is
      return fetch.apply(studyId);
    }
    ArrayList<T> result = new ArrayList<>();
    VirtualStudy virtualStudy = publishedVirtualStudiesById.get(studyId);
    Map<StudyScopedId, StudyScopedId> vsToMzMap = getVirtualToMaterializedStudyPatientPairs();
    virtualStudy
        .getData()
        .getStudies()
        .forEach(
            vss -> {
              String materialisedStudyId = vss.getId();
              List<T> fetchedEntities = fetch.apply(materialisedStudyId);
              for (T entity : fetchedEntities) {
                String patientId = getPatientId.apply(entity);
                String virtualStudyId = virtualStudy.getId();
                if (patientId != null
                    && vsToMzMap.containsKey(new StudyScopedId(virtualStudyId, patientId))) {
                  // We need to virtualize the entity
                  result.add(virtualize.apply(virtualStudyId, entity));
                }
              }
            });
    return result;
  }

  public <T> List<T> handleStudyPatientData2(
      List<StudyScopedId> patientIds,
      Function<T, String> getStudyId,
      Function<T, String> getPatientId,
      Function<List<StudyScopedId>, List<T>> fetch,
      BiFunction<String, T, T> virtualize) {
    List<T> result = new ArrayList<>();
    Map<StudyScopedId, Set<String>> materialisedStudyPatientPairToStudyIds =
        toMaterializedStudyPatientPairsMap(patientIds);
    if (materialisedStudyPatientPairToStudyIds.isEmpty()) {
      return result; // No materialized study-patient pairs found
    }
    Map<String, VirtualStudy> virtualStudyIdToVirtualStudy = getPublishedVirtualStudiesById();
    for (T entity : fetch.apply(List.copyOf(materialisedStudyPatientPairToStudyIds.keySet()))) {
      Set<String> studyIds =
          materialisedStudyPatientPairToStudyIds.get(
              new StudyScopedId(getStudyId.apply(entity), getPatientId.apply(entity)));
      for (String studyId : studyIds) {
        if (virtualStudyIdToVirtualStudy.containsKey(studyId)) {
          result.add(virtualize.apply(studyId, entity));
        } else {
          result.add(entity);
        }
      }
    }
    return result;
  }

  public <T> List<T> handleStudySampleData(
      List<String> studyIds,
      List<String> sampleIds,
      Function<T, String> getStudyId,
      Function<T, String> getSampleId,
      BiFunction<List<String>, List<String>, List<T>> fetch,
      BiFunction<String, T, T> virtualize) {
    return handleStudySampleData2(
        toStudyScopedIds(studyIds, sampleIds),
        getStudyId,
        getSampleId,
        (studyScopedIds) -> {
          Pair<List<String>, List<String>> studyIdsAndSampleIds = toIdLists(studyScopedIds);
          return fetch.apply(studyIdsAndSampleIds.getLeft(), studyIdsAndSampleIds.getRight());
        },
        virtualize);
  }

  public <T> List<T> handleStudySampleData2(
      List<StudyScopedId> sampleIds,
      Function<T, String> getStudyId,
      Function<T, String> getSampleId,
      Function<List<StudyScopedId>, List<T>> fetch,
      BiFunction<String, T, T> virtualize) {
    List<T> resultSamples = new ArrayList<>();
    Map<StudyScopedId, Set<String>> materialisedStudySamplePairToStudyIds =
        toMaterializedStudySamplePairsMap(sampleIds);
    if (materialisedStudySamplePairToStudyIds.isEmpty()) {
      return resultSamples; // No materialized study-sample pairs found
    }
    Map<String, VirtualStudy> virtualStudyIdToVirtualStudy = getPublishedVirtualStudiesById();
    for (T entity : fetch.apply(List.copyOf(materialisedStudySamplePairToStudyIds.keySet()))) {
      Set<String> sampleForStudyIds =
          materialisedStudySamplePairToStudyIds.get(
              new StudyScopedId(getStudyId.apply(entity), getSampleId.apply(entity)));
      for (String studyId : sampleForStudyIds) {
        if (virtualStudyIdToVirtualStudy.containsKey(studyId)) {
          resultSamples.add(virtualize.apply(studyId, entity));
        } else {
          resultSamples.add(entity);
        }
      }
    }
    return resultSamples;
  }

  public <T> List<T> handleStudySampleData(
      List<String> studyIds,
      Function<T, String> getStudyId,
      Function<T, String> getSampleId,
      BiFunction<List<String>, List<String>, List<T>> fetch,
      BiFunction<String, T, T> virtualize) {
    List<VirtualStudy> allVirtualStudies = virtualStudyService.getPublishedVirtualStudies();
    Map<String, VirtualStudy> allVirtualStudyIds =
        allVirtualStudies.stream()
            .collect(Collectors.toMap(VirtualStudy::getId, virtualStudy -> virtualStudy));
    List<String> materializedStudyIds = new ArrayList<>();
    List<String> virtualStudyIds = new ArrayList<>();
    for (int i = 0; i < studyIds.size(); i++) {
      String studyId = studyIds.get(i);
      if (allVirtualStudyIds.containsKey(studyId)) {
        virtualStudyIds.add(studyId);
      } else {
        materializedStudyIds.add(studyId);
      }
    }
    List<T> resultSamples = new ArrayList<>();
    if (!materializedStudyIds.isEmpty()) {
      resultSamples.addAll(fetch.apply(materializedStudyIds, null));
    }
    if (!virtualStudyIds.isEmpty()) {
      LinkedHashSet<String> vMaterializedStudyIds = new LinkedHashSet<>();
      LinkedHashSet<String> vMaterializedSampleIds = new LinkedHashSet<>();
      Map<ImmutablePair<String, String>, LinkedHashSet<String>>
          virtualStudyIdsByMaterializedSamples = new HashMap<>();
      for (int i = 0; i < virtualStudyIds.size(); i++) {
        String virtualStudyId = virtualStudyIds.get(i);
        VirtualStudy virtualStudy = allVirtualStudyIds.get(virtualStudyId);
        virtualStudy.getData().getStudies().stream()
            .flatMap(vss -> vss.getSamples().stream().map(s -> new ImmutablePair<>(vss.getId(), s)))
            .forEach(
                pair -> {
                  vMaterializedStudyIds.add(pair.getLeft());
                  vMaterializedSampleIds.add(pair.getRight());
                  virtualStudyIdsByMaterializedSamples.computeIfAbsent(
                      pair, k -> new LinkedHashSet<>());
                  virtualStudyIdsByMaterializedSamples.get(pair).add(virtualStudyId);
                });
      }
      for (T enity :
          fetch.apply(
              vMaterializedStudyIds.stream().toList(), vMaterializedSampleIds.stream().toList())) {
        String sampleId = getSampleId.apply(enity);
        LinkedHashSet<String> sampleRequestingVirtualStudyIds =
            virtualStudyIdsByMaterializedSamples.get(
                ImmutablePair.of(getStudyId.apply(enity), sampleId));
        if (sampleRequestingVirtualStudyIds == null || sampleRequestingVirtualStudyIds.isEmpty()) {
          throw new IllegalStateException(
              "Virtual study IDs not found for materialized enity: " + sampleId);
        }
        sampleRequestingVirtualStudyIds.forEach(
            virtualStudyId -> resultSamples.add(virtualize.apply(virtualStudyId, enity)));
      }
    }
    return resultSamples;
  }
}
