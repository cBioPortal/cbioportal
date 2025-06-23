package org.cbioportal.application.file.export.services;

import java.util.Set;
import org.cbioportal.application.file.export.repositories.CnaSegmentRepository;
import org.cbioportal.application.file.model.CnaSegment;
import org.cbioportal.application.file.utils.CloseableIterator;

public class CnaSegmentService {
  private final CnaSegmentRepository cnaSegmentRepository;

  public CnaSegmentService(CnaSegmentRepository cnaSegmentRepository) {
    this.cnaSegmentRepository = cnaSegmentRepository;
  }

  public CloseableIterator<CnaSegment> getCnaSegments(String studyId, Set<String> sampleIds) {
    return cnaSegmentRepository.getCnaSegments(studyId, sampleIds);
  }

  public boolean hasCnaSegments(String studyId, Set<String> sampleIds) {
    return cnaSegmentRepository.hasCnaSegments(studyId, sampleIds);
  }
}
