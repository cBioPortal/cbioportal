package org.cbioportal.cancerstudy.usecase;

import org.cbioportal.cancerstudy.repository.CancerStudyRepository;
import org.cbioportal.studyview.StudyViewFilterContext;
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
