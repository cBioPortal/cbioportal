package org.cbioportal.infrastructure.repository.clickhouse.clinical_event;

import java.util.List;
import org.cbioportal.domain.clinical_event.repository.ClinicalEventRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalEvent;
import org.cbioportal.legacy.model.ClinicalEventTypeCount;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.mybatis.util.PaginationCalculator;
import org.springframework.stereotype.Repository;

@Repository
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

  @Override
  public List<ClinicalEvent> getPatientClinicalEvents(
      String studyId,
      String patientId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    Integer offset = PaginationCalculator.offset(pageSize, pageNumber);
    if ("ID".equals(projection)) {
      return mapper.getPatientClinicalEventsIdProjection(studyId, patientId, pageSize, offset);
    }
    return mapper.getPatientClinicalEvents(studyId, patientId, pageSize, offset, sortBy, direction);
  }

  @Override
  public BaseMeta getMetaPatientClinicalEvents(String studyId, String patientId) {
    return mapper.getMetaPatientClinicalEvents(studyId, patientId);
  }
}
