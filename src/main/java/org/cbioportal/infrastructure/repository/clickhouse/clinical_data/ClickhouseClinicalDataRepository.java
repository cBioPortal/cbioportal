package org.cbioportal.infrastructure.repository.clickhouse.clinical_data;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.session.SqlSession;
import org.cbioportal.domain.clinical_data.ClinicalData;
import org.cbioportal.domain.clinical_data.ClinicalDataType;
import org.cbioportal.domain.clinical_data.repository.ClinicalDataRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalDataCountItem;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class ClickhouseClinicalDataRepository implements ClinicalDataRepository {

  private final ClickhouseClinicalDataMapper mapper;
  private final SqlSessionTemplate sqlSessionTemplate;

  public ClickhouseClinicalDataRepository(
      ClickhouseClinicalDataMapper mapper,
      @Qualifier("sqlColumnarSessionTemplate") SqlSessionTemplate sqlSessionTemplate) {
    this.mapper = mapper;
    this.sqlSessionTemplate = sqlSessionTemplate;
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

    // Execute both queries in the same session to support temp tables or session-level setup
    return sqlSessionTemplate.execute(
        (SqlSession session) -> {
          // Create parameters map
          Map<String, Object> params = new HashMap<>();
          params.put("studyViewFilterContext", studyViewFilterContext);
          params.put("sampleAttributeIds", sampleAttributeIds);
          params.put("patientAttributeIds", patientAttributeIds);
          params.put("conflictingAttributeIds", conflictingAttributeIds);

          // Step 1: Execute setup statement (e.g., create temp table, set session variables)
          session.update(
              "org.cbioportal.infrastructure.repository.clickhouse.clinical_data.ClickhouseClinicalDataMapper.setupClinicalDataCountsSession",
              params);

          // Step 2: Execute the main query that uses the temp table/session setup
          return session.selectList(
              "org.cbioportal.infrastructure.repository.clickhouse.clinical_data.ClickhouseClinicalDataMapper.getClinicalDataCounts",
              params);
        });
  }

  @Override
  public List<ClinicalData> fetchClinicalDataId(
      List<String> uniqueIds, List<String> attributeIds, ClinicalDataType clinicalDataType) {
    if (CollectionUtils.isEmpty(uniqueIds)) {
      return Collections.emptyList();
    }
    return mapper.fetchClinicalDataId(uniqueIds, attributeIds, clinicalDataType.toString());
  }

  @Override
  public List<ClinicalData> fetchClinicalDataSummary(
      List<String> uniqueIds, List<String> attributeIds, ClinicalDataType clinicalDataType) {
    if (CollectionUtils.isEmpty(uniqueIds)) {
      return Collections.emptyList();
    }
    return mapper.fetchClinicalDataSummary(uniqueIds, attributeIds, clinicalDataType.toString());
  }

  @Override
  public List<ClinicalData> fetchClinicalDataDetailed(
      List<String> uniqueIds, List<String> attributeIds, ClinicalDataType clinicalDataType) {
    if (CollectionUtils.isEmpty(uniqueIds)) {
      return Collections.emptyList();
    }
    return mapper.fetchClinicalDataDetailed(uniqueIds, attributeIds, clinicalDataType.toString());
  }

  @Override
  public Integer fetchClinicalDataMeta(
      List<String> uniqueIds, List<String> attributeIds, ClinicalDataType clinicalDataType) {
    if (CollectionUtils.isEmpty(uniqueIds)) {
      return 0;
    }
    Integer cnt =
        mapper.fetchClinicalDataMeta(uniqueIds, attributeIds, clinicalDataType.toString());
    return cnt == null ? 0 : cnt;
  }
}
