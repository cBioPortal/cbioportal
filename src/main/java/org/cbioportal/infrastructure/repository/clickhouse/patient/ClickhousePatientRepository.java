package org.cbioportal.infrastructure.repository.clickhouse.patient;

import java.util.List;
import org.cbioportal.domain.patient.repository.PatientRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.CaseListDataCount;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class ClickhousePatientRepository implements PatientRepository {

  private final ClickhousePatientMapper mapper;

  public ClickhousePatientRepository(ClickhousePatientMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public int getFilteredPatientCount(StudyViewFilterContext studyViewFilterContext) {
    return mapper.getPatientCount(studyViewFilterContext);
  }

  @Override
  public List<CaseListDataCount> getCaseListDataCounts(
      StudyViewFilterContext studyViewFilterContext) {
    return mapper.getCaseListDataCounts(studyViewFilterContext);
  }
}
