package org.cbioportal.infrastructure.repository.clinical_event;

import org.apache.ibatis.annotations.Param;
import org.cbioportal.legacy.model.ClinicalEventTypeCount;
import org.cbioportal.domain.studyview.StudyViewFilterContext;

import java.util.List;

/**
 * Mapper interface for retrieving clinical event type data from DB.
 * This interface provides a method to fetch clinical event type counts based on the study view filter context.
 */
public interface V2ClinicalEventMapper {

    /**
     * Retrieves the clinical event type counts based on the study view filter context.
     *
     * @param studyViewFilterContext the context of the study view filter
     * @return a list of clinical event type counts
     */
    List<ClinicalEventTypeCount> getClinicalEventTypeCounts(@Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);
}

