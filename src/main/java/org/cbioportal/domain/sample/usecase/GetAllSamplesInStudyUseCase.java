package org.cbioportal.domain.sample.usecase;

import java.util.List;
import org.cbioportal.domain.sample.Sample;
import org.cbioportal.domain.sample.repository.SampleRepository;
import org.cbioportal.legacy.service.StudyService;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.stereotype.Service;

@Service
public class GetAllSamplesInStudyUseCase {
  private final StudyService studyService;
  private final SampleRepository sampleRepository;

  public GetAllSamplesInStudyUseCase(SampleRepository sampleRepository, StudyService studyService) {
    this.sampleRepository = sampleRepository;
    this.studyService = studyService;
  }

  public List<Sample> execute(
      String studyId,
      ProjectionType projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction)
      throws StudyNotFoundException {
    studyService.studyExists(studyId);

    return sampleRepository.getAllSamplesInStudy(
        studyId, projection, pageSize, pageNumber, sortBy, direction);
  }
}
