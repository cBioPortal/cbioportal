package org.cbioportal.infrastructure.repository.clinical_event;

import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalEventTypeCount;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class V2ClinicalEventRepository implements org.cbioportal.domain.clinical_event.repository.ClinicalEventRepository {

    private final V2ClinicalEventMapper mapper;

    public V2ClinicalEventRepository(V2ClinicalEventMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<ClinicalEventTypeCount> getClinicalEventTypeCounts(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getClinicalEventTypeCounts(studyViewFilterContext);
    }
}
