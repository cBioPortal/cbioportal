package org.cbioportal.infrastructure.repository.clickhouse.clinical_event;

import org.apache.ibatis.annotations.Param;
import org.cbioportal.legacy.model.ClinicalEventTypeCount;
import org.cbioportal.studyview.StudyViewFilterContext;

import java.util.List;

public interface ClickhouseClinicalEventMapper {
    List<ClinicalEventTypeCount> getClinicalEventTypeCounts(@Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);
}
