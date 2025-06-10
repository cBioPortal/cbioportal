package org.cbioportal.application.file.export.services;

import java.util.Set;
import org.cbioportal.application.file.export.mappers.CnaSegmentMapper;
import org.cbioportal.application.file.model.CnaSegment;
import org.cbioportal.application.file.utils.CloseableIterator;
import org.cbioportal.application.file.utils.CursorAdapter;

public class CnaSegmentService {
  private final CnaSegmentMapper cnaSegmentMapper;

  public CnaSegmentService(CnaSegmentMapper cnaSegmentMapper) {
    this.cnaSegmentMapper = cnaSegmentMapper;
  }

  public CloseableIterator<CnaSegment> getCnaSegments(String studyId, Set<String> sampleIds) {
    return new CursorAdapter<>(cnaSegmentMapper.getCnaSegments(studyId, sampleIds));
  }

  public boolean hasCnaSegments(String studyId, Set<String> sampleIds) {
    return cnaSegmentMapper.hasCnaSegments(studyId, sampleIds);
  }
}
