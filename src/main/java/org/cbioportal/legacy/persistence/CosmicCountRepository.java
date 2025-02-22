package org.cbioportal.legacy.persistence;

import java.util.List;
import org.cbioportal.legacy.model.CosmicMutation;
import org.springframework.cache.annotation.Cacheable;

public interface CosmicCountRepository {

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  List<CosmicMutation> fetchCosmicCountsByKeywords(List<String> keywords);
}
