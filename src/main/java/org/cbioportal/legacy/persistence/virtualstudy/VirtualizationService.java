package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
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
      List<String> molecularProfileIds,
      List<String> sampleIds,
      Function<T, String> getMolecularProfileId,
      Function<T, String> getSampleId,
      BiFunction<List<String>, List<String>, List<T>> fetch,
      BiFunction<MolecularProfile, T, T> virtualize) {
    if (molecularProfileIds.size() != sampleIds.size()) {
      throw new IllegalArgumentException(
          "Molecular profile ids and sample ids must have the same size");
    }
    LinkedHashMap<String, LinkedHashSet<String>> molecularProfileToSampleIds =
        new LinkedHashMap<>();
    for (int i = 0; i < molecularProfileIds.size(); i++) {
      String molecularProfileId = molecularProfileIds.get(i);
      String sampleId = sampleIds.get(i);
      molecularProfileToSampleIds
          .computeIfAbsent(molecularProfileId, k -> new LinkedHashSet<>())
          .add(sampleId);
    }
    Map<String, VirtualStudy> virtualStudiesById =
        virtualStudyService.getPublishedVirtualStudies().stream()
            .collect(Collectors.toMap(VirtualStudy::getId, Function.identity()));
    Map<String, MolecularProfile> molecularProfilesByStableId =
        molecularProfileRepository
            .getMolecularProfiles(molecularProfileToSampleIds.keySet(), Projection.DETAILED.name())
            .stream()
            .collect(Collectors.toMap(MolecularProfile::getStableId, Function.identity()));
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
                    e -> e.getKey().replace(e.getValue().getCancerStudyIdentifier() + "_", "")));
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
    List<T> fetchedEntities = fetch.apply(molecularProfileIdsToQuery, sampleIdsToQuery);
    ArrayList<T> result = new ArrayList<>();
    for (T molecularDataEntity : fetchedEntities) {
      String molecularProfileId = getMolecularProfileId.apply(molecularDataEntity);
      String sampleId = getSampleId.apply(molecularDataEntity);
      if (molecularProfileToCorrectSampleIds.containsKey(molecularProfileId)
          && molecularProfileToCorrectSampleIds.get(molecularProfileId).contains(sampleId)) {
        // this is a materialized structural variant
        result.add(molecularDataEntity);
      }
      if (materializedMolecularProfileToVirtualMolecularProfileIds.containsKey(
          molecularProfileId)) {
        Set<String> virtualMolecularProfileIds =
            materializedMolecularProfileToVirtualMolecularProfileIds.get(molecularProfileId);
        // this is a virtual structural variant
        for (String virtualMolecularProfileId : virtualMolecularProfileIds) {
          // we need to check if the sample id is in the correct sample ids for the virtual
          // molecular profile
          // this is a virtual structural variant
          if (molecularProfileToCorrectSampleIds.containsKey(virtualMolecularProfileId)
              && molecularProfileToCorrectSampleIds
                  .get(virtualMolecularProfileId)
                  .contains(sampleId)) {
            MolecularProfile virtualMolecularProfile =
                virtualMolecularProfilesByStableId.get(virtualMolecularProfileId);
            result.add(virtualize.apply(virtualMolecularProfile, molecularDataEntity));
          }
        }
      }
    }
    // TODO we might want to sort the result here in certain order if needed
    return result;
  }
}
