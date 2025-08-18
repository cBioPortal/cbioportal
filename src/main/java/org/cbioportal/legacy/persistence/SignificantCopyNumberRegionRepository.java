package org.cbioportal.legacy.persistence;

import java.util.List;
import org.cbioportal.legacy.model.Gistic;
import org.cbioportal.legacy.model.GisticToGene;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.springframework.cache.annotation.Cacheable;

public interface SignificantCopyNumberRegionRepository {

  @Cacheable(
      cacheResolver = "staticRepositoryCacheOneResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  List<Gistic> getSignificantCopyNumberRegions(
      String studyId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction);

  @Cacheable(
      cacheResolver = "staticRepositoryCacheOneResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  BaseMeta getMetaSignificantCopyNumberRegions(String studyId);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  List<GisticToGene> getGenesOfRegions(List<Long> gisticRoiIds);
}
