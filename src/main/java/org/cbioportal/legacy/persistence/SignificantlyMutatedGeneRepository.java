package org.cbioportal.legacy.persistence;

import java.util.List;
import org.cbioportal.legacy.model.MutSig;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.springframework.cache.annotation.Cacheable;

public interface SignificantlyMutatedGeneRepository {

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  List<MutSig> getSignificantlyMutatedGenes(
      String studyId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  BaseMeta getMetaSignificantlyMutatedGenes(String studyId);
}
