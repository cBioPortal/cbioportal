package org.cbioportal.application.file.repositories.mybatis;

import java.util.List;
import org.cbioportal.application.file.repositories.GeneticProfileDataRepository;
import org.cbioportal.application.file.repositories.mybatis.utils.CursorAdapter;
import org.cbioportal.application.file.model.GenericEntityProperty;
import org.cbioportal.application.file.model.GeneticProfileData;
import org.cbioportal.application.file.utils.CloseableIterator;

public class GeneticProfileDataMyBatisRepository implements GeneticProfileDataRepository {

  private final GeneticProfileDataMapper mapper;

  public GeneticProfileDataMyBatisRepository(GeneticProfileDataMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public List<String> getSampleStableIds(String molecularProfileStableId) {
    return mapper.getSampleStableIds(molecularProfileStableId);
  }

  @Override
  public CloseableIterator<GeneticProfileData> getData(String molecularProfileStableId) {
    return new CursorAdapter<>(mapper.getData(molecularProfileStableId));
  }

  @Override
  public List<String> getDistinctGenericEntityMetaPropertyNames(String molecularProfileStableId) {
    return mapper.getDistinctGenericEntityMetaPropertyNames(molecularProfileStableId);
  }

  @Override
  public CloseableIterator<GenericEntityProperty> getGenericEntityMetaProperties(
      String molecularProfileStableId) {
    return new CursorAdapter<>(mapper.getGenericEntityMetaProperties(molecularProfileStableId));
  }
}
