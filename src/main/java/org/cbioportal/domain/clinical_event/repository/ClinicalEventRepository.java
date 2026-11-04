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
   * Retrieves a patient's clinical events, including their attributes.
   *
   * @param studyId the study identifier
   * @param patientId the patient identifier within the study
   * @return the patient's clinical events
   */
  List<ClinicalEvent> getPatientClinicalEvents(String studyId, String patientId);
}
