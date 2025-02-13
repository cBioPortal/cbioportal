package org.cbioportal.clinical_event.repository;

import org.cbioportal.legacy.model.ClinicalEventTypeCount;
import org.cbioportal.studyview.StudyViewFilterContext;

import java.util.List;

public interface ClinicalEventRepository {
    List<ClinicalEventTypeCount> getClinicalEventTypeCounts(StudyViewFilterContext studyViewFilterContext);
}
