package org.cbioportal.infrastructure.repository.clickhouse.clinical_data;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
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

  private static boolean isEmpty(List<?> items) {
    return items == null || items.isEmpty();
  }

  private static String toTypeString(ClinicalDataType t) {
    // MyBatis XML expects 'sample' or 'patient'
    return t == null ? "" : t.name().toLowerCase(Locale.ROOT);
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
    if (isEmpty(uniqueIds)) {
      return Collections.emptyList();
    }
    return mapper.fetchClinicalDataId(uniqueIds, attributeIds, toTypeString(clinicalDataType));
  }

  @Override
  public List<ClinicalData> fetchClinicalDataSummary(
      List<String> uniqueIds, List<String> attributeIds, ClinicalDataType clinicalDataType) {
    if (isEmpty(uniqueIds)) {
      return Collections.emptyList();
    }
    return mapper.fetchClinicalDataSummary(uniqueIds, attributeIds, toTypeString(clinicalDataType));
  }

  @Override
  public List<ClinicalData> fetchClinicalDataDetailed(
      List<String> uniqueIds, List<String> attributeIds, ClinicalDataType clinicalDataType) {
    if (isEmpty(uniqueIds)) {
      return Collections.emptyList();
    }
    return mapper.fetchClinicalDataDetailed(
        uniqueIds, attributeIds, toTypeString(clinicalDataType));
  }

  @Override
  public Integer fetchClinicalDataMeta(
      List<String> uniqueIds, List<String> attributeIds, ClinicalDataType clinicalDataType) {
    if (isEmpty(uniqueIds)) {
      return 0;
    }
    Integer cnt =
        mapper.fetchClinicalDataMeta(uniqueIds, attributeIds, toTypeString(clinicalDataType));
    return cnt == null ? 0 : cnt;
  }
}
