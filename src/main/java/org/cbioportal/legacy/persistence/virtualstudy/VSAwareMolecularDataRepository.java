package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.cbioportal.legacy.model.GeneMolecularAlteration;
import org.cbioportal.legacy.model.GenericAssayMolecularAlteration;
import org.cbioportal.legacy.model.GenesetMolecularAlteration;
import org.cbioportal.legacy.persistence.MolecularDataRepository;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.web.parameter.VirtualStudy;
import org.cbioportal.legacy.web.parameter.VirtualStudySamples;

public class VSAwareMolecularDataRepository implements MolecularDataRepository {

  private final VirtualStudyService virtualStudyService;
  private final MolecularDataRepository molecularDataRepository;

  public VSAwareMolecularDataRepository(
      VirtualStudyService virtualStudyService, MolecularDataRepository molecularDataRepository) {
    this.virtualStudyService = virtualStudyService;
    this.molecularDataRepository = molecularDataRepository;
  }

  @Override
  public List<String> getStableSampleIdsOfMolecularProfile(String molecularProfileId) {
    return stableSampleIdsOfMolecularProfilesMap(Set.of(molecularProfileId))
        .get(molecularProfileId);
  }

  @Override
  public Map<String, List<String>> stableSampleIdsOfMolecularProfilesMap(
      Set<String> molecularProfileIds) {
    Map<String, Pair<String, String>> map =
        virtualStudyService.toMolecularProfileInfo(molecularProfileIds);
    if (map == null || map.isEmpty()) {
      return molecularDataRepository.stableSampleIdsOfMolecularProfilesMap(molecularProfileIds);
    }
    Map<String, List<String>> result = new LinkedHashMap<>();
    Set<String> materializedMolecularProfileIds =
        molecularProfileIds.stream()
            .filter(mpid -> !map.containsKey(mpid))
            .collect(Collectors.toSet());
    if (!materializedMolecularProfileIds.isEmpty()) {
      result.putAll(
          molecularDataRepository.stableSampleIdsOfMolecularProfilesMap(
              materializedMolecularProfileIds));
    }
    for (Map.Entry<String, Pair<String, String>> entry : map.entrySet()) {
      String virtualMolecularProfileId = entry.getKey();
      Pair<String, String> pair = entry.getValue();
      String virtualStudyId = pair.getLeft();
      Optional<VirtualStudy> virtualStudyOptional =
          virtualStudyService.getVirtualStudyByIdIfExists(virtualStudyId);
      if (virtualStudyOptional.isEmpty()) {
        continue; // Skip if the virtual study does not exist
      }
      String materializedMolecularProfileId = pair.getRight();
      Optional<VirtualStudySamples> virtualStudySamplesOptional =
          virtualStudyOptional.get().getData().getStudies().stream()
              .filter(vss -> materializedMolecularProfileId.startsWith(vss.getId() + "_"))
              .findFirst();
      if (virtualStudySamplesOptional.isEmpty()) {
        continue; // Skip if the virtual study samples do not exist
      }
      Set<String> stableSampleIds = virtualStudySamplesOptional.get().getSamples();
      List<String> vsStableSampleIds =
          molecularDataRepository
              .getStableSampleIdsOfMolecularProfile(materializedMolecularProfileId)
              .stream()
              .filter(stableSampleIds::contains)
              .toList();
      result.put(virtualMolecularProfileId, vsStableSampleIds);
    }
    return result;
  }

  @Override
  public List<GeneMolecularAlteration> getGeneMolecularAlterations(
      String molecularProfileId, List<Integer> entrezGeneIds, String projection) {
    return getGeneMolecularAlterationsInMultipleMolecularProfiles(
        Set.of(molecularProfileId), entrezGeneIds, projection);
  }

  @Override
  public Iterable<GeneMolecularAlteration> getGeneMolecularAlterationsIterable(
      String molecularProfileId, List<Integer> entrezGeneIds, String projection) {
    return getGeneMolecularAlterationsInMultipleMolecularProfiles(
        Set.of(molecularProfileId), entrezGeneIds, projection);
  }

  @Override
  public Iterable<GeneMolecularAlteration> getGeneMolecularAlterationsIterableFast(
      String molecularProfileId) {
    return getGeneMolecularAlterationsIterable(molecularProfileId, null, "SUMMARY");
  }

  @Override
  public List<GeneMolecularAlteration> getGeneMolecularAlterationsInMultipleMolecularProfiles(
      Set<String> molecularProfileIds, List<Integer> entrezGeneIds, String projection) {
    Map<String, Pair<String, String>> map =
        virtualStudyService.toMolecularProfileInfo(molecularProfileIds);
    List<GeneMolecularAlteration> result = new ArrayList<>();
    for (String molecularProfileId : molecularProfileIds) {
      // TODO improve performance by using a single query for all materialized profiles
      if (map.containsKey(molecularProfileId)) {
        Pair<String, String> pair = map.get(molecularProfileId);
        String materializedMolecularProfileId = pair.getRight();
        List<GeneMolecularAlteration> alterations =
            molecularDataRepository.getGeneMolecularAlterationsInMultipleMolecularProfiles(
                Set.of(materializedMolecularProfileId), entrezGeneIds, projection);
        for (GeneMolecularAlteration alteration : alterations) {
          alteration.setMolecularProfileId(molecularProfileId);
          result.add(alteration);
        }
      } else {
        List<GeneMolecularAlteration> alterations =
            molecularDataRepository.getGeneMolecularAlterationsInMultipleMolecularProfiles(
                Set.of(molecularProfileId), entrezGeneIds, projection);
        result.addAll(alterations);
      }
    }
    return result;
  }

  @Override
  public List<GenesetMolecularAlteration> getGenesetMolecularAlterations(
      String molecularProfileId, List<String> genesetIds, String projection) {
    Map<String, Pair<String, String>> map =
        virtualStudyService.toMolecularProfileInfo(Set.of(molecularProfileId));
    if (map == null || map.isEmpty()) {
      return molecularDataRepository.getGenesetMolecularAlterations(
          molecularProfileId, genesetIds, projection);
    }
    Pair<String, String> pair = map.get(molecularProfileId);
    String materializedMolecularProfileId = pair.getRight();
    return molecularDataRepository.getGenesetMolecularAlterations(
        materializedMolecularProfileId, genesetIds, projection);
  }

  @Override
  public List<GenericAssayMolecularAlteration> getGenericAssayMolecularAlterations(
      String molecularProfileId, List<String> stableIds, String projection) {
    Map<String, Pair<String, String>> map =
        virtualStudyService.toMolecularProfileInfo(Set.of(molecularProfileId));
    if (map == null || map.isEmpty()) {
      return molecularDataRepository.getGenericAssayMolecularAlterations(
          molecularProfileId, stableIds, projection);
    }
    Pair<String, String> pair = map.get(molecularProfileId);
    String materializedMolecularProfileId = pair.getRight();
    return molecularDataRepository
        .getGenericAssayMolecularAlterations(materializedMolecularProfileId, stableIds, projection)
        .stream()
        .map(
            gama -> {
              gama.setMolecularProfileId(molecularProfileId);
              return gama;
            })
        .toList();
  }

  @Override
  public Iterable<GenericAssayMolecularAlteration> getGenericAssayMolecularAlterationsIterable(
      String molecularProfileId, List<String> stableIds, String projection) {
    return getGenericAssayMolecularAlterations(molecularProfileId, stableIds, projection);
  }
}
