package org.cbioportal.infrastructure.repository.clickhouse.clinical_event;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalEvent;
import org.cbioportal.legacy.model.ClinicalEventTypeCount;
import org.cbioportal.legacy.model.meta.BaseMeta;

/**
 * MyBatis mapper interface for retrieving clinical events from ClickHouse. SUMMARY and DETAILED
 * projections both return full event data (dates + attributes); the ID projection is handled by a
 * separate, lighter query ({@link #getPatientClinicalEventsIdProjection}).
 */
public interface ClickhouseClinicalEventMapper {

  /**
   * Retrieves counts of distinct patients per clinical event type, filtered by the given study view
   * context.
   */
  List<ClinicalEventTypeCount> getClinicalEventTypeCounts(
      @Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);

  List<ClinicalEvent> getPatientClinicalEvents(
      @Param("studyId") String studyId,
      @Param("patientId") String patientId,
      @Param("limit") Integer limit,
      @Param("offset") Integer offset,
      @Param("sortBy") String sortBy,
      @Param("direction") String direction);

  List<ClinicalEvent> getPatientClinicalEventsIdProjection(
      @Param("studyId") String studyId,
      @Param("patientId") String patientId,
      @Param("limit") Integer limit,
      @Param("offset") Integer offset);

  BaseMeta getMetaPatientClinicalEvents(
      @Param("studyId") String studyId, @Param("patientId") String patientId);
}
