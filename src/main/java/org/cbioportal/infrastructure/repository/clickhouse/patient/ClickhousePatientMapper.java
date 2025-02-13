package org.cbioportal.infrastructure.repository.clickhouse.patient;

import org.cbioportal.legacy.model.CaseListDataCount;
import org.cbioportal.studyview.StudyViewFilterContext;

import java.util.List;

public interface ClickhousePatientMapper {
    int getFilteredPatientCount(StudyViewFilterContext studyViewFilterContext);
    List<CaseListDataCount> getCaseListDataCount(StudyViewFilterContext studyViewFilterContext);
}
