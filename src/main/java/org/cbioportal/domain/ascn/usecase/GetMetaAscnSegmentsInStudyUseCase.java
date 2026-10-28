package org.cbioportal.domain.ascn.usecase;

import java.util.List;
import org.cbioportal.domain.ascn.repository.AscnSegmentRepository;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.service.StudyService;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GetMetaAscnSegmentsInStudyUseCase {
  private final AscnSegmentRepository ascnSegmentRepository;
  private final StudyService studyService;

  public GetMetaAscnSegmentsInStudyUseCase(
      AscnSegmentRepository ascnSegmentRepository, StudyService studyService) {
    this.ascnSegmentRepository = ascnSegmentRepository;
    this.studyService = studyService;
  }

  public BaseMeta execute(String studyId, String sampleId) throws StudyNotFoundException {
    studyService.studyExists(studyId);

    return ascnSegmentRepository.getMetaAscnSegments(
        List.of(studyId), sampleId == null ? null : List.of(sampleId));
  }
}
