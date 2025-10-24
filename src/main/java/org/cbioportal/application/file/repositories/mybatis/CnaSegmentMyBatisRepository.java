package org.cbioportal.application.file.repositories.mybatis;

import java.util.Set;
import org.cbioportal.application.file.repositories.CnaSegmentRepository;
import org.cbioportal.application.file.repositories.mybatis.utils.CursorAdapter;
import org.cbioportal.application.file.model.CnaSegment;
import org.cbioportal.application.file.utils.CloseableIterator;

public class CnaSegmentMyBatisRepository implements CnaSegmentRepository {
  private final CnaSegmentMapper mapper;

  public CnaSegmentMyBatisRepository(CnaSegmentMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public CloseableIterator<CnaSegment> getCnaSegments(String studyId, Set<String> sampleIds) {
    return new CursorAdapter<>(mapper.getCnaSegments(studyId, sampleIds));
  }

  @Override
  public boolean hasCnaSegments(String studyId, Set<String> sampleIds) {
    return mapper.hasCnaSegments(studyId, sampleIds);
  }
}
