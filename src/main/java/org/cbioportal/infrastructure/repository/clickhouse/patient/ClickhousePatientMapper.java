package org.cbioportal.infrastructure.repository.clickhouse.patient;

import org.apache.ibatis.annotations.Param;
import org.cbioportal.legacy.model.CaseListDataCount;
import org.cbioportal.studyview.StudyViewFilterContext;

import java.util.List;

public interface ClickhousePatientMapper {
    int getPatientCount(@Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);
    List<CaseListDataCount> getCaseListDataCounts(@Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);
}
