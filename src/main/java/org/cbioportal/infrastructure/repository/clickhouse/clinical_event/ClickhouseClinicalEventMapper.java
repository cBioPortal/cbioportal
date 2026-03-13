package org.cbioportal.infrastructure.repository.clickhouse.clinical_event;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalEvent;
import org.cbioportal.legacy.model.ClinicalEventTypeCount;

/**
 * Mapper interface for retrieving clinical event type data from ClickHouse. This interface provides
 * methods to fetch clinical event data from ClickHouse.
 */
public interface ClickhouseClinicalEventMapper {

  List<ClinicalEventTypeCount> getClinicalEventTypeCounts(
      @Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);

  List<ClinicalEvent> getPatientClinicalEvents(
      @Param("studyId") String studyId, @Param("patientId") String patientId);
}
