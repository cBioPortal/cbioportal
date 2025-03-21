package org.cbioportal.domain.sample.usecase;

import org.cbioportal.domain.sample.repository.SampleRepository;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.service.PatientService;
import org.cbioportal.legacy.service.exception.PatientNotFoundException;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
public class GetMetaSamplesOfPatientInStudyUseCase {
    private final SampleRepository sampleRepository;
    private final PatientService patientService;
    
    public GetMetaSamplesOfPatientInStudyUseCase(
        SampleRepository sampleRepository,
        PatientService patientService
    ) {
        this.sampleRepository = sampleRepository;
        this.patientService = patientService;
    }

    public BaseMeta execute(
        String studyId,
        String patientId
    ) throws StudyNotFoundException, PatientNotFoundException {
        patientService.getPatientInStudy(studyId, patientId);

        return sampleRepository.getMetaSamplesOfPatientInStudy(studyId, patientId);
    }
}
