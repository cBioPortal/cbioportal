package org.cbioportal.domain.sample.usecase;

import org.cbioportal.domain.sample.Sample;
import org.cbioportal.domain.sample.repository.SampleRepository;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("clickhouse")
public class GetAllSamplesUseCase {
    private final SampleRepository sampleRepository;

    public GetAllSamplesUseCase(SampleRepository sampleRepository) {
        this.sampleRepository = sampleRepository;
    }

    public List<Sample> execute(
        String keyword,
        List<String> studyIds,
        ProjectionType projection,
        Integer pageSize,
        Integer pageNumber,
        String sort,
        String direction
    ) {
        return sampleRepository.getAllSamples(
            keyword,
            studyIds,
            projection,
            pageSize,
            pageNumber,
            sort,
            direction
        );
    }
}
