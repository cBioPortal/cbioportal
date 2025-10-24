package org.cbioportal.application.file.repositories.mybatis;

import java.util.Set;
import org.cbioportal.application.file.repositories.SVRepository;
import org.cbioportal.application.file.repositories.mybatis.utils.CursorAdapter;
import org.cbioportal.application.file.model.StructuralVariant;
import org.cbioportal.application.file.utils.CloseableIterator;

public class SVMyBatisRepository implements SVRepository {

  private final SVMapper mapper;

  public SVMyBatisRepository(SVMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public CloseableIterator<StructuralVariant> getStructuralVariants(
      String molecularProfileStableId, Set<String> sampleIds) {
    return new CursorAdapter<>(mapper.getStructuralVariants(molecularProfileStableId, sampleIds));
  }
}
