package org.cbioportal.domain.sample.usecase;

import org.cbioportal.domain.sample.repository.SampleRepository;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.service.StudyService;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
public class GetMetaSamplesInStudyUseCase {
    private final SampleRepository sampleRepository;
    private final StudyService studyService;
    
    public GetMetaSamplesInStudyUseCase(
        SampleRepository sampleRepository,
        StudyService studyService
    ) {
        this.sampleRepository = sampleRepository;
        this.studyService = studyService;
    }
    
    public BaseMeta execute(String studyId) throws StudyNotFoundException {
        studyService.getStudy(studyId);

        return sampleRepository.getMetaSamplesInStudy(studyId);
    }
}
