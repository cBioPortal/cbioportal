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
  public List<ClinicalData> getPatientClinicalData(
      StudyViewFilterContext studyViewFilterContext, List<String> filteredAttributes) {
    return mapper.getPatientClinicalDataFromStudyViewFilter(
        studyViewFilterContext, filteredAttributes);
  }

  @Override
  public List<ClinicalData> getSampleClinicalData(
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
  public List<ClinicalData> fetchClinicalDataId(
      List<String> uniqueIds, List<String> attributeIds, ClinicalDataType clinicalDataType) {
    return mapper.fetchClinicalDataId(uniqueIds, attributeIds, clinicalDataType.toString());
  }

  @Override
  public List<ClinicalData> fetchClinicalDataSummary(
      List<String> uniqueIds, List<String> attributeIds, ClinicalDataType clinicalDataType) {
    return mapper.fetchClinicalDataSummary(uniqueIds, attributeIds, clinicalDataType.toString());
  }

  @Override
  public List<ClinicalData> fetchClinicalDataDetailed(
      List<String> uniqueIds, List<String> attributeIds, ClinicalDataType clinicalDataType) {
    return mapper.fetchClinicalDataDetailed(uniqueIds, attributeIds, clinicalDataType.toString());
  }

  @Override
  public Integer fetchClinicalDataMeta(
      List<String> uniqueIds, List<String> attributeIds, ClinicalDataType clinicalDataType) {
    return mapper.fetchClinicalDataMeta(uniqueIds, attributeIds, clinicalDataType.toString());
  }
}
