package org.cbioportal.infrastructure.repository.clickhouse.clinical_data;

import java.util.List;
import org.cbioportal.domain.clinical_data.ClinicalData;
import org.cbioportal.domain.clinical_data.ClinicalDataType;
import org.cbioportal.domain.clinical_data.repository.ClinicalDataRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalDataCountItem;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class ClickhouseClinicalDataRepository implements ClinicalDataRepository {

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
  public List<ClinicalData> getSampleClinicalDataFromStudyViewFilter(
      StudyViewFilterContext studyViewFilterContext, List<String> filteredAttributes) {
    return mapper.getSampleClinicalDataFromStudyViewFilter(
        studyViewFilterContext, filteredAttributes);
  }

  @Override
  public List<ClinicalDataCountItem> getClinicalDataCounts(
      StudyViewFilterContext studyViewFilterContext,
      List<String> sampleAttributeIds,
      List<String> patientAttributeIds,
      List<String> conflictingAttributeIds) {
    return mapper.getClinicalDataCounts(
        studyViewFilterContext, sampleAttributeIds, patientAttributeIds, conflictingAttributeIds);
  }

  @Override
  public List<ClinicalData> getClinicalDataId(
      List<String> uniqueIds, List<String> attributeIds, ClinicalDataType clinicalDataType) {
    return mapper.getClinicalDataId(uniqueIds, attributeIds, clinicalDataType.toString());
  }

  @Override
  public List<ClinicalData> getClinicalDataSummary(
      List<String> uniqueIds, List<String> attributeIds, ClinicalDataType clinicalDataType) {
    return mapper.getClinicalDataSummary(uniqueIds, attributeIds, clinicalDataType.toString());
  }

  @Override
  public List<ClinicalData> getClinicalDataDetailed(
      List<String> uniqueIds, List<String> attributeIds, ClinicalDataType clinicalDataType) {
    return mapper.getClinicalDataDetailed(uniqueIds, attributeIds, clinicalDataType.toString());
  }

  @Override
  public Integer getClinicalDataMeta(
      List<String> uniqueIds, List<String> attributeIds, ClinicalDataType clinicalDataType) {
    return mapper.getClinicalDataMeta(uniqueIds, attributeIds, clinicalDataType.toString());
  }
}
