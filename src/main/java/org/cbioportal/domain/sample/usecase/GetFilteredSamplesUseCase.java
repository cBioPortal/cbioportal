package org.cbioportal.domain.sample.usecase;

import org.cbioportal.domain.sample.Sample;
import org.cbioportal.domain.sample.repository.SampleRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("clickhouse")
public final class GetFilteredSamplesUseCase {

    private final SampleRepository sampleRepository;

    public GetFilteredSamplesUseCase(SampleRepository sampleRepository) {
        this.sampleRepository = sampleRepository;
    }

    public List<Sample> execute(StudyViewFilterContext studyViewFilterContext) {
        return this.sampleRepository.getFilteredSamples(studyViewFilterContext);
    }
}
