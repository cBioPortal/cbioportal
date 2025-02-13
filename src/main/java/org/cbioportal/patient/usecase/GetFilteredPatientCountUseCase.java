package org.cbioportal.patient.usecase;

import org.cbioportal.patient.repository.PatientRepository;
import org.cbioportal.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
public class GetFilteredPatientCountUseCase {
    private final PatientRepository patientRepository;

    public GetFilteredPatientCountUseCase(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public int execute(StudyViewFilterContext studyViewFilterContext) {
        return patientRepository.getFilteredPatientCount(studyViewFilterContext);
    }
}
