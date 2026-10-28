package org.cbioportal.domain.ascn.usecase;

import java.util.List;
import org.cbioportal.domain.ascn.AscnSegment;
import org.cbioportal.domain.ascn.repository.AscnSegmentRepository;
import org.cbioportal.legacy.persistence.mybatis.util.PaginationCalculator;
import org.cbioportal.legacy.service.StudyService;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class GetAscnSegmentsInStudyUseCase {
  private final AscnSegmentRepository ascnSegmentRepository;
  private final StudyService studyService;

  public GetAscnSegmentsInStudyUseCase(
      AscnSegmentRepository ascnSegmentRepository, StudyService studyService) {
    this.ascnSegmentRepository = ascnSegmentRepository;
    this.studyService = studyService;
  }

  public List<AscnSegment> execute(
      String studyId, String sampleId, Integer pageSize, Integer pageNumber)
      throws StudyNotFoundException {
    studyService.studyExists(studyId);

    return ascnSegmentRepository.getAscnSegments(
        List.of(studyId),
        sampleId == null ? null : List.of(sampleId),
        pageSize,
        PaginationCalculator.offset(pageSize, pageNumber));
  }
}
