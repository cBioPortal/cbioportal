package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.List;
import org.cbioportal.legacy.model.GenericAssayAdditionalProperty;
import org.cbioportal.legacy.model.meta.GenericAssayMeta;
import org.cbioportal.legacy.persistence.GenericAssayRepository;

// TODO implmeent
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
    return List.of();
  }

  @Override
  public List<GenericAssayAdditionalProperty> getGenericAssayAdditionalproperties(
      List<String> stableIds) {
    return List.of();
  }

  @Override
  public List<String> getGenericAssayStableIdsByMolecularIds(List<String> molecularProfileIds) {
    return List.of();
  }
}
