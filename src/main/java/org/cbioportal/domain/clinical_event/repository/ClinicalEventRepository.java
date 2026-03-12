package org.cbioportal.domain.clinical_event.repository;

import java.util.List;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalEvent;
import org.cbioportal.legacy.model.ClinicalEventTypeCount;

/** Repository interface for retrieving clinical events and related data. */
public interface ClinicalEventRepository {

  /**
   * Retrieves counts of different clinical event types based on the given study view filter
   * context.
   *
   * @param studyViewFilterContext The filter criteria for the study view.
   * @return A list of {@link ClinicalEventTypeCount} representing the counts of clinical event
   *     types.
   */
  List<ClinicalEventTypeCount> getClinicalEventTypeCounts(
      StudyViewFilterContext studyViewFilterContext);

  /**
   * Retrieves all clinical events (with attributes) for a specific patient in a study.
   *
   * @param studyId The cancer study identifier.
   * @param patientId The patient stable ID.
   * @return A list of {@link ClinicalEvent} with nested attributes.
   */
  List<ClinicalEvent> getPatientClinicalEvents(String studyId, String patientId);
}
