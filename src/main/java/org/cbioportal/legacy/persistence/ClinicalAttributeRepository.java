package org.cbioportal.legacy.persistence;

import java.util.List;
import org.cbioportal.legacy.model.ClinicalAttribute;
import org.cbioportal.legacy.model.ClinicalAttributeCount;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.springframework.cache.annotation.Cacheable;

public interface ClinicalAttributeRepository {

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  List<ClinicalAttribute> getAllClinicalAttributes(
      String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  BaseMeta getMetaClinicalAttributes();

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  ClinicalAttribute getClinicalAttribute(String studyId, String clinicalAttributeId);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  List<ClinicalAttribute> getAllClinicalAttributesInStudy(
      String studyId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  BaseMeta getMetaClinicalAttributesInStudy(String studyId);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  List<ClinicalAttribute> fetchClinicalAttributes(List<String> studyIds, String projection);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  BaseMeta fetchMetaClinicalAttributes(List<String> studyIds);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  List<ClinicalAttributeCount> getClinicalAttributeCountsBySampleIds(
      List<String> studyIds, List<String> sampleIds);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  List<ClinicalAttributeCount> getClinicalAttributeCountsBySampleListId(String sampleListId);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  List<ClinicalAttribute> getClinicalAttributesByStudyIdsAndAttributeIds(
      List<String> studyIds, List<String> attributeIds);
}
