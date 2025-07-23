package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.cbioportal.legacy.model.GeneMolecularAlteration;
import org.cbioportal.legacy.model.GenericAssayMolecularAlteration;
import org.cbioportal.legacy.model.GenesetMolecularAlteration;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.persistence.MolecularDataRepository;

public class VSAwareMolecularDataRepository implements MolecularDataRepository {

  private final VirtualizationService virtualizationService;
  private final MolecularDataRepository molecularDataRepository;

  public VSAwareMolecularDataRepository(
      VirtualizationService virtualizationService,
      MolecularDataRepository molecularDataRepository) {
    this.virtualizationService = virtualizationService;
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
    Map<String, Pair<String, Set<String>>> molecularProfileDefinition =
        virtualizationService.getVirtualMolecularProfileDefinition(molecularProfileIds);
    Map<String, List<String>> materialisedSampleIds =
        molecularDataRepository.stableSampleIdsOfMolecularProfilesMap(
            molecularProfileDefinition.values().stream()
                .map(Pair::getLeft)
                .collect(Collectors.toSet()));
    return molecularProfileDefinition.entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                  String materializedSampleId = entry.getValue().getLeft();
                  Set<String> limitToSampleIds = entry.getValue().getRight();
                  return materialisedSampleIds
                      .getOrDefault(materializedSampleId, List.of())
                      .stream()
                      .filter(
                          sampleId ->
                              limitToSampleIds == null || limitToSampleIds.contains(sampleId))
                      .collect(Collectors.toList());
                }));
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
    return virtualizationService.handleMolecularData(
        molecularProfileIds,
        GeneMolecularAlteration::getMolecularProfileId,
        (mpids) ->
            molecularDataRepository.getGeneMolecularAlterationsInMultipleMolecularProfiles(
                mpids, entrezGeneIds, projection),
        this::virtualizeGeneMolecularAlteration);
  }

  @Override
  public List<GenesetMolecularAlteration> getGenesetMolecularAlterations(
      String molecularProfileId, List<String> genesetIds, String projection) {
    return virtualizationService.handleMolecularData(
        molecularProfileId,
        (mpid) ->
            molecularDataRepository.getGenesetMolecularAlterations(mpid, genesetIds, projection),
        this::virtualizeGenesetMolecularAlteration);
  }

  @Override
  public List<GenericAssayMolecularAlteration> getGenericAssayMolecularAlterations(
      String molecularProfileId, List<String> stableIds, String projection) {
    return virtualizationService.handleMolecularData(
        molecularProfileId,
        (mpid) ->
            molecularDataRepository.getGenericAssayMolecularAlterations(
                mpid, stableIds, projection),
        this::virtualizeGenericAssayMolecularAlteration);
  }

  @Override
  public Iterable<GenericAssayMolecularAlteration> getGenericAssayMolecularAlterationsIterable(
      String molecularProfileId, List<String> stableIds, String projection) {
    return getGenericAssayMolecularAlterations(molecularProfileId, stableIds, projection);
  }

  private GeneMolecularAlteration virtualizeGeneMolecularAlteration(
      MolecularProfile molecularProfile, GeneMolecularAlteration geneMolecularAlteration) {
    GeneMolecularAlteration virtualGeneMolecularAlteration = new GeneMolecularAlteration();
    virtualGeneMolecularAlteration.setMolecularProfileId(molecularProfile.getStableId());
    virtualGeneMolecularAlteration.setEntrezGeneId(geneMolecularAlteration.getEntrezGeneId());
    virtualGeneMolecularAlteration.setGene(geneMolecularAlteration.getGene());
    virtualGeneMolecularAlteration.setValues(geneMolecularAlteration.getValues());
    return virtualGeneMolecularAlteration;
  }

  private GenericAssayMolecularAlteration virtualizeGenericAssayMolecularAlteration(
      MolecularProfile molecularProfile,
      GenericAssayMolecularAlteration genericAssayMolecularAlteration) {
    GenericAssayMolecularAlteration virtualGenericAssayMolecularAlteration =
        new GenericAssayMolecularAlteration();
    virtualGenericAssayMolecularAlteration.setMolecularProfileId(molecularProfile.getStableId());
    virtualGenericAssayMolecularAlteration.setValues(genericAssayMolecularAlteration.getValues());
    return virtualGenericAssayMolecularAlteration;
  }

  private GenesetMolecularAlteration virtualizeGenesetMolecularAlteration(
      MolecularProfile molecularProfile, GenesetMolecularAlteration genesetMolecularAlteration) {
    GenesetMolecularAlteration virtualGenesetMolecularAlteration = new GenesetMolecularAlteration();
    virtualGenesetMolecularAlteration.setGeneset(genesetMolecularAlteration.getGeneset());
    virtualGenesetMolecularAlteration.setGenesetId(genesetMolecularAlteration.getGenesetId());
    virtualGenesetMolecularAlteration.setValues(genesetMolecularAlteration.getValues());
    return virtualGenesetMolecularAlteration;
  }
}
