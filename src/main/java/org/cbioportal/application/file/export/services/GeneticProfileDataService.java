package org.cbioportal.application.file.export.services;

import java.util.List;
import org.cbioportal.application.file.export.repositories.GeneticProfileDataRepository;
import org.cbioportal.application.file.model.GenericEntityProperty;
import org.cbioportal.application.file.model.GeneticProfileData;
import org.cbioportal.application.file.utils.CloseableIterator;

public class GeneticProfileDataService {
  private final GeneticProfileDataRepository geneticProfileDataRepository;

  public GeneticProfileDataService(GeneticProfileDataRepository geneticProfileDataRepository) {
    this.geneticProfileDataRepository = geneticProfileDataRepository;
  }

  public List<String> getSampleStableIds(String molecularProfileStableId) {
    return geneticProfileDataRepository.getSampleStableIds(molecularProfileStableId);
  }

  public CloseableIterator<GeneticProfileData> getData(String molecularProfileStableId) {
    return geneticProfileDataRepository.getData(molecularProfileStableId);
  }

  public List<String> getDistinctGenericEntityMetaPropertyNames(String molecularProfileStableId) {
    return geneticProfileDataRepository.getDistinctGenericEntityMetaPropertyNames(
        molecularProfileStableId);
  }

  public CloseableIterator<GenericEntityProperty> getGenericEntityMetaProperties(
      String molecularProfileStableId) {
    return geneticProfileDataRepository.getGenericEntityMetaProperties(molecularProfileStableId);
  }
}
