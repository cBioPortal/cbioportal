package org.cbioportal.infrastructure.repository.clickhouse.clinical_event;

import java.util.List;
import org.cbioportal.domain.clinical_event.repository.ClinicalEventRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalEventTypeCount;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class ClickhouseClinicalEventRepository implements ClinicalEventRepository {

  private final ClickhouseClinicalEventMapper mapper;

  public ClickhouseClinicalEventRepository(ClickhouseClinicalEventMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public List<ClinicalEventTypeCount> getClinicalEventTypeCounts(
      StudyViewFilterContext studyViewFilterContext) {
    return mapper.getClinicalEventTypeCounts(studyViewFilterContext);
  }
}
