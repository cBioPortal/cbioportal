package org.cbioportal.application.file.export.repositories.mybatis;

import java.util.List;
import java.util.Set;
import org.cbioportal.application.file.export.repositories.GenePanelMatrixRepository;
import org.cbioportal.application.file.export.repositories.mybatis.utils.CursorAdapter;
import org.cbioportal.application.file.model.GenePanelMatrixItem;
import org.cbioportal.application.file.utils.CloseableIterator;

public class GenePanelMatrixMyBatisRepository implements GenePanelMatrixRepository {
  private final GenePanelMatrixMapper mapper;

  public GenePanelMatrixMyBatisRepository(GenePanelMatrixMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public boolean hasGenePanelMatrix(String studyId, Set<String> sampleIds) {
    return mapper.hasGenePanelMatrix(studyId, sampleIds);
  }

  @Override
  public CloseableIterator<GenePanelMatrixItem> getGenePanelMatrix(
      String studyId, Set<String> sampleIds) {
    return new CursorAdapter<>(mapper.getGenePanelMatrix(studyId, sampleIds));
  }

  @Override
  public List<String> getDistinctGeneProfileIdsWithGenePanelMatrix(
      String studyId, Set<String> sampleIds) {
    return mapper.getDistinctGeneProfileIdsWithGenePanelMatrix(studyId, sampleIds);
  }
}
