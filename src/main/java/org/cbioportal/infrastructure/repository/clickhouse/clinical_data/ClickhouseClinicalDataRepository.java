package org.cbioportal.infrastructure.repository.clickhouse.clinical_data;

import java.util.Collections;
import java.util.List;
import org.cbioportal.domain.clinical_data.repository.ClinicalDataRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.model.ClinicalDataCountItem;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class ClickhouseClinicalDataRepository implements ClinicalDataRepository {

  private static final List<String> FILTERED_CLINICAL_ATTR_VALUES = Collections.emptyList();

  private final ClickhouseClinicalDataMapper mapper;

  public ClickhouseClinicalDataRepository(ClickhouseClinicalDataMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public List<ClinicalData> getPatientClinicalDataFromStudyViewFilter(
      StudyViewFilterContext studyViewFilterContext, List<String> filteredAttributes) {
    return mapper.getPatientClinicalDataFromStudyViewFilter(
        studyViewFilterContext, filteredAttributes);
  }

  @Override
  public List<ClinicalData> getPatientClinicalData(
      List<String> uniquePatientIds, List<String> attributeIds, ProjectionType projectionType) {
    return mapper.getPatientClinicalData(uniquePatientIds, attributeIds, projectionType.toString());
  }

  @Override
  public List<ClinicalData> getSampleClinicalDataFromStudyViewFilter(
      StudyViewFilterContext studyViewFilterContext, List<String> filteredAttributes) {
    return mapper.getSampleClinicalDataFromStudyViewFilter(
        studyViewFilterContext, filteredAttributes);
  }

  @Override
  public List<ClinicalData> getSampleClinicalData(
      List<String> uniqueSampleIds, List<String> attributeIds, ProjectionType projectionType) {
    return mapper.getSampleClinicalData(uniqueSampleIds, attributeIds, projectionType.toString());
  }

  @Override
  public List<ClinicalDataCountItem> getClinicalDataCounts(
      StudyViewFilterContext studyViewFilterContext, List<String> filteredAttributes) {
    return mapper.getClinicalDataCounts(
        studyViewFilterContext, filteredAttributes, FILTERED_CLINICAL_ATTR_VALUES);
  }
}
