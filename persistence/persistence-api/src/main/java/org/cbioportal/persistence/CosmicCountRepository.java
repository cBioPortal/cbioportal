package org.cbioportal.persistence;

import java.util.List;
import org.cbioportal.model.CosmicMutation;
import org.springframework.cache.annotation.Cacheable;

public interface CosmicCountRepository {
    @Cacheable(
        cacheNames = "GeneralRepositoryCache",
        condition = "@cacheEnabledConfig.getEnabled()"
    )
    List<CosmicMutation> fetchCosmicCountsByKeywords(List<String> keywords);
}
