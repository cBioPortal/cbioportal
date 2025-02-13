package org.cbioportal.patient.repository;

import org.cbioportal.legacy.model.CaseListDataCount;
import org.cbioportal.studyview.StudyViewFilterContext;

import java.util.List;

public interface PatientRepository {
    int getFilteredPatientCount(StudyViewFilterContext studyViewFilterContext);
    List<CaseListDataCount> getCaseListDataCounts(StudyViewFilterContext studyViewFilterContext);
}
