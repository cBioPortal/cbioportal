package org.cbioportal.domain.sample.usecase;

import org.cbioportal.domain.sample.Sample;
import org.cbioportal.domain.sample.repository.SampleRepository;
import org.cbioportal.legacy.service.StudyService;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("clickhouse")
public class GetAllSamplesInStudyUseCase {
    private final StudyService studyService;
    private final SampleRepository sampleRepository;

    public GetAllSamplesInStudyUseCase(
        SampleRepository sampleRepository,
        StudyService studyService
    ) {
        this.sampleRepository = sampleRepository;
        this.studyService = studyService;
    }

    public List<Sample> execute(
        String studyId,
        ProjectionType projection,
        Integer pageSize,
        Integer pageNumber,
        String sortBy,
        String direction
    ) throws StudyNotFoundException {
        studyService.getStudy(studyId);

        return sampleRepository.getAllSamplesInStudy(
            studyId,
            projection,
            pageSize,
            pageNumber,
            sortBy,
            direction
        );
    }
}
