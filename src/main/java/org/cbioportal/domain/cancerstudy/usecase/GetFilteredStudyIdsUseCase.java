package org.cbioportal.domain.cancerstudy.usecase;

import java.util.List;
import org.cbioportal.domain.cancerstudy.repository.CancerStudyRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
public class GetFilteredStudyIdsUseCase {

  private final CancerStudyRepository studyRepository;

  public GetFilteredStudyIdsUseCase(CancerStudyRepository studyRepository) {
    this.studyRepository = studyRepository;
  }

  public List<String> execute(StudyViewFilterContext studyViewFilterContext) {
    return this.studyRepository.getFilteredStudyIds(studyViewFilterContext);
  }
}
