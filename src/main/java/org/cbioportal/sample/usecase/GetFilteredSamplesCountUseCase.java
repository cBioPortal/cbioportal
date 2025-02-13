package org.cbioportal.sample.usecase;

import org.cbioportal.sample.repository.SampleRepository;
import org.cbioportal.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;


@Service
@Profile("clickhouse")
public class GetFilteredSamplesCountUseCase {
    private final SampleRepository sampleRepository;

    public GetFilteredSamplesCountUseCase(SampleRepository sampleRepository) {
        this.sampleRepository = sampleRepository;
    }

    public int execute(StudyViewFilterContext studyViewFilterContext) {
        return sampleRepository.getFilteredSamplesCount(studyViewFilterContext);
    }
}
