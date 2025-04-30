package org.cbioportal.domain.sample.usecase;

import org.cbioportal.domain.sample.Sample;
import org.cbioportal.domain.sample.repository.SampleRepository;
import org.cbioportal.legacy.service.StudyService;
import org.cbioportal.legacy.service.exception.SampleNotFoundException;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
public class GetSampleInStudyUseCase {
    private final StudyService studyService;
    private final SampleRepository sampleRepository;

    public GetSampleInStudyUseCase(
        SampleRepository sampleRepository,
        StudyService studyService
    ) {
        this.sampleRepository = sampleRepository;
        this.studyService = studyService;
    }

    public Sample execute(
        String studyId,
        String sampleId
    ) throws SampleNotFoundException, StudyNotFoundException {
        studyService.getStudy(studyId);
        Sample sample = sampleRepository.getSampleInStudy(studyId, sampleId);

        if (sample == null) {
            throw new SampleNotFoundException(studyId, sampleId);
        }

        return sample;
    }
}
