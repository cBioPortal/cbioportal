package org.cbioportal.infrastructure.repository.clickhouse.clinical_data;

import java.util.Collections;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
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
      List<String> uniqueIds,
      List<String> attributeIds,
      List<String> studyIds,
      ClinicalDataType clinicalDataType) {
    if (CollectionUtils.isEmpty(uniqueIds)) {
      return Collections.emptyList();
    }
    return mapper.fetchClinicalDataId(
        uniqueIds, attributeIds, studyIds, clinicalDataType.toString());
  }

  @Override
  public List<ClinicalData> fetchClinicalDataSummary(
      List<String> uniqueIds,
      List<String> attributeIds,
      List<String> studyIds,
      ClinicalDataType clinicalDataType) {
    if (CollectionUtils.isEmpty(uniqueIds)) {
      return Collections.emptyList();
    }
    return mapper.fetchClinicalDataSummary(
        uniqueIds, attributeIds, studyIds, clinicalDataType.toString());
  }

  @Override
  public List<ClinicalData> fetchClinicalDataDetailed(
      List<String> uniqueIds,
      List<String> attributeIds,
      List<String> studyIds,
      ClinicalDataType clinicalDataType) {
    if (CollectionUtils.isEmpty(uniqueIds)) {
      return Collections.emptyList();
    }
    return mapper.fetchClinicalDataDetailed(
        uniqueIds, attributeIds, studyIds, clinicalDataType.toString());
  }

  @Override
  public Integer fetchClinicalDataMeta(
      List<String> uniqueIds,
      List<String> attributeIds,
      List<String> studyIds,
      ClinicalDataType clinicalDataType) {
    if (CollectionUtils.isEmpty(uniqueIds)) {
      return 0;
    }
    Integer cnt =
        mapper.fetchClinicalDataMeta(
            uniqueIds, attributeIds, studyIds, clinicalDataType.toString());
    return cnt == null ? 0 : cnt;
  }
}
