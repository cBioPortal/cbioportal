package org.cbioportal.infrastructure.repository.clickhouse.treatment;

import java.util.List;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.domain.treatment.repository.TreatmentRepository;
import org.cbioportal.legacy.model.PatientTreatment;
import org.cbioportal.legacy.model.SampleTreatment;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.stereotype.Repository;

@Repository
public class ClickhouseTreatmentRepository implements TreatmentRepository {
  private final ClickhouseTreatmentMapper mapper;

  public ClickhouseTreatmentRepository(ClickhouseTreatmentMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public List<PatientTreatment> getPatientTreatments(
      StudyViewFilterContext studyViewFilterContext) {
    return mapper.getPatientTreatments(studyViewFilterContext);
  }

  @Override
  public int getTotalPatientTreatmentCount(StudyViewFilterContext studyViewFilterContext) {
    return mapper.getPatientTreatmentCounts(studyViewFilterContext);
  }

  @Override
  public List<SampleTreatment> getSampleTreatments(
      StudyViewFilterContext studyViewFilterContext, ProjectionType projection) {
    return mapper.getSampleTreatmentCounts(studyViewFilterContext, projection.name());
  }

  @Override
  public int getTotalSampleTreatmentCount(StudyViewFilterContext studyViewFilterContext) {
    return mapper.getTotalSampleTreatmentCounts(studyViewFilterContext);
  }
}
