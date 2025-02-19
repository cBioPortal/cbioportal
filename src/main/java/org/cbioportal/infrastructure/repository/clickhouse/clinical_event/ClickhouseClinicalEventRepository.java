package org.cbioportal.infrastructure.repository.clickhouse.clinical_event;

import org.cbioportal.domain.clinical_event.repository.ClinicalEventRepository;
import org.cbioportal.legacy.model.ClinicalEventTypeCount;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("clickhouse")
public class ClickhouseClinicalEventRepository implements ClinicalEventRepository {

    private final ClickhouseClinicalEventMapper mapper;

    public ClickhouseClinicalEventRepository(ClickhouseClinicalEventMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<ClinicalEventTypeCount> getClinicalEventTypeCounts(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getClinicalEventTypeCounts(studyViewFilterContext);
    }
}
