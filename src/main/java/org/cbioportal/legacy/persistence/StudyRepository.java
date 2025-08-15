package org.cbioportal.legacy.persistence;

import java.util.List;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.model.CancerStudyTags;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.springframework.cache.annotation.Cacheable;

public interface StudyRepository {

  @Cacheable(
      cacheResolver = "staticRepositoryCacheOneResolver",
      condition = "@cacheEnabledConfig.getEnabled() && #keyword == null")
  List<CancerStudy> getAllStudies(
      String keyword,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  BaseMeta getMetaStudies(String keyword);

  @Cacheable(
      cacheResolver = "staticRepositoryCacheOneResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  CancerStudy getStudy(String studyId, String projection);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  List<CancerStudy> fetchStudies(List<String> studyIds, String projection);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  BaseMeta fetchMetaStudies(List<String> studyIds);

  @Cacheable(
      cacheResolver = "staticRepositoryCacheOneResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  CancerStudyTags getTags(String studyId);

  @Cacheable(
      cacheResolver = "staticRepositoryCacheOneResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  List<CancerStudyTags> getTagsForMultipleStudies(List<String> studyIds);
}
