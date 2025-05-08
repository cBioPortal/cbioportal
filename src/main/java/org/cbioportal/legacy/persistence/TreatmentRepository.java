package org.cbioportal.legacy.persistence;

import java.util.List;
import java.util.Map;
import org.cbioportal.legacy.model.ClinicalEventKeyCode;
import org.cbioportal.legacy.model.ClinicalEventSample;
import org.cbioportal.legacy.model.Treatment;
import org.springframework.cache.annotation.Cacheable;

public interface TreatmentRepository {
  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  public Map<String, List<Treatment>> getTreatmentsByPatientId(
      List<String> sampleIds, List<String> studyIds, ClinicalEventKeyCode key);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  public List<Treatment> getTreatments(
      List<String> sampleIds, List<String> studyIds, ClinicalEventKeyCode key);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  public Map<String, List<ClinicalEventSample>> getSamplesByPatientId(
      List<String> sampleIds, List<String> studyIds);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  public Map<String, List<ClinicalEventSample>> getShallowSamplesByPatientId(
      List<String> sampleIds, List<String> studyIds);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  public Boolean hasTreatmentData(List<String> studies, ClinicalEventKeyCode key);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  public Boolean hasSampleTimelineData(List<String> studies);
}
