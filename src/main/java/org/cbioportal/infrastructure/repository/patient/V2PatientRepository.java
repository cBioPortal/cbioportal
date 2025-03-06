package org.cbioportal.infrastructure.repository.patient;

import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.CaseListDataCount;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class V2PatientRepository implements org.cbioportal.domain.patient.repository.PatientRepository {

    private final V2PatientMapper mapper;

    public V2PatientRepository(V2PatientMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public int getFilteredPatientCount(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getPatientCount(studyViewFilterContext);
    }

    @Override
    public List<CaseListDataCount> getCaseListDataCounts(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getCaseListDataCounts(studyViewFilterContext);
    }
}
