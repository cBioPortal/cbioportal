package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.List;
import org.cbioportal.legacy.model.GenericAssayAdditionalProperty;
import org.cbioportal.legacy.model.meta.GenericAssayMeta;
import org.cbioportal.legacy.persistence.GenericAssayRepository;
import org.cbioportal.legacy.service.VirtualStudyService;

public class VSAwareGenericAssayRepository implements GenericAssayRepository {

  private final VirtualStudyService virtualStudyService;
  private final GenericAssayRepository genericAssayRepository;

  public VSAwareGenericAssayRepository(
      VirtualStudyService virtualStudyService, GenericAssayRepository genericAssayRepository) {
    this.virtualStudyService = virtualStudyService;
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
