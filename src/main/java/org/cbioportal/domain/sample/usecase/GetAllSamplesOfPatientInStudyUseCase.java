package org.cbioportal.domain.sample.usecase;

import org.cbioportal.domain.sample.Sample;
import org.cbioportal.domain.sample.repository.SampleRepository;
import org.cbioportal.legacy.service.PatientService;
import org.cbioportal.legacy.service.exception.PatientNotFoundException;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("clickhouse")
public class GetAllSamplesOfPatientInStudyUseCase {
    private final PatientService patientService;
    private final SampleRepository sampleRepository;

    public GetAllSamplesOfPatientInStudyUseCase(
        SampleRepository sampleRepository,
        PatientService patientService
    ) {
        this.sampleRepository = sampleRepository;
        this.patientService = patientService;
    }

    public List<Sample> execute(
        String studyId,
        String patientId,
        ProjectionType projection,
        Integer pageSize,
        Integer pageNumber,
        String sortBy,
        String direction
    ) throws StudyNotFoundException, PatientNotFoundException {
        patientService.getPatientInStudy(studyId, patientId);

        return sampleRepository.getAllSamplesOfPatientInStudy(
            studyId,
            patientId,
            projection,
            pageSize,
            pageNumber,
            sortBy,
            direction
        );
    }
}
