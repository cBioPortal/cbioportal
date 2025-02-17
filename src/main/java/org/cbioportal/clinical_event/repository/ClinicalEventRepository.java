package org.cbioportal.clinical_event.repository;

import org.cbioportal.legacy.model.ClinicalEventTypeCount;
import org.cbioportal.studyview.StudyViewFilterContext;

import java.util.List;

/**
 * Repository interface for retrieving clinical events and related data.
 */
public interface ClinicalEventRepository {

    /**
     * Retrieves counts of different clinical event types based on the given study view filter context.
     *
     * @param studyViewFilterContext The filter criteria for the study view.
     * @return A list of {@link ClinicalEventTypeCount} representing the counts of clinical event types.
     */
    List<ClinicalEventTypeCount> getClinicalEventTypeCounts(StudyViewFilterContext studyViewFilterContext);
}
