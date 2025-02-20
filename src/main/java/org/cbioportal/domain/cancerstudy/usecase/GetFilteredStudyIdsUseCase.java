package org.cbioportal.domain.cancerstudy.usecase;

import org.cbioportal.domain.cancerstudy.repository.CancerStudyRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("clickhouse")
public class GetFilteredStudyIdsUseCase {

    private final CancerStudyRepository studyRepository;

    public GetFilteredStudyIdsUseCase(CancerStudyRepository studyRepository) {
        this.studyRepository = studyRepository;
    }

    public List<String> execute(StudyViewFilterContext studyViewFilterContext) {
        return this.studyRepository.getFilteredStudyIds(studyViewFilterContext);
    }
}
