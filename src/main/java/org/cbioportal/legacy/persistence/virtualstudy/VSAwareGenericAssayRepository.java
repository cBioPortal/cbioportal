package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.cbioportal.legacy.model.GenericAssayAdditionalProperty;
import org.cbioportal.legacy.model.meta.GenericAssayMeta;
import org.cbioportal.legacy.persistence.GenericAssayRepository;

public class VSAwareGenericAssayRepository implements GenericAssayRepository {

  private final VirtualizationService virtualizationService;
  private final GenericAssayRepository genericAssayRepository;

  public VSAwareGenericAssayRepository(
      VirtualizationService virtualizationService, GenericAssayRepository genericAssayRepository) {
    this.virtualizationService = virtualizationService;
    this.genericAssayRepository = genericAssayRepository;
  }

  @Override
  public List<GenericAssayMeta> getGenericAssayMeta(List<String> stableIds) {
    return genericAssayRepository.getGenericAssayMeta(stableIds);
  }

  @Override
  public List<GenericAssayAdditionalProperty> getGenericAssayAdditionalproperties(
      List<String> stableIds) {
    return genericAssayRepository.getGenericAssayAdditionalproperties(stableIds);
  }

  @Override
  public List<String> getGenericAssayStableIdsByMolecularIds(List<String> molecularProfileIds) {
    Map<String, Pair<String, Set<String>>> molecularProfileDefinitions =
        virtualizationService.getVirtualMolecularProfileDefinition(
            new HashSet<>(molecularProfileIds));
    List<String> materializedMolecularProfileIds =
        molecularProfileDefinitions.values().stream().map(Pair::getLeft).toList();
    return genericAssayRepository.getGenericAssayStableIdsByMolecularIds(
        materializedMolecularProfileIds);
  }
}
