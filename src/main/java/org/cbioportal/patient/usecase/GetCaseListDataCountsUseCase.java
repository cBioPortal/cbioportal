package org.cbioportal.patient.usecase;

import org.cbioportal.legacy.model.CaseListDataCount;
import org.cbioportal.patient.repository.PatientRepository;
import org.cbioportal.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("clickhouse")
public class GetCaseListDataCountsUseCase {

    private final PatientRepository patientRepository;

    public GetCaseListDataCountsUseCase(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<CaseListDataCount> execute(StudyViewFilterContext studyViewFilterContext){
       return patientRepository.getCaseListDataCounts(studyViewFilterContext);
    }
}
