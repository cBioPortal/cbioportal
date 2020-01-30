package org.cbioportal.persistence;

import java.util.HashMap;
import java.util.List;
import org.cbioportal.model.meta.GenericAssayMeta;
import org.springframework.cache.annotation.Cacheable;

public interface GenericAssayRepository {
    @Cacheable(
        cacheNames = "GeneralRepositoryCache",
        condition = "@cacheEnabledConfig.getEnabled()"
    )
    List<GenericAssayMeta> getGenericAssayMeta(List<String> stableIds);

    @Cacheable(
        cacheNames = "GeneralRepositoryCache",
        condition = "@cacheEnabledConfig.getEnabled()"
    )
    List<String> getGenericAssayStableIdsByMolecularIds(
        List<String> molecularProfileIds
    );

    @Cacheable(
        cacheNames = "GeneralRepositoryCache",
        condition = "@cacheEnabledConfig.getEnabled()"
    )
    int getGeneticEntityIdByStableId(String stableId);

    @Cacheable(
        cacheNames = "GeneralRepositoryCache",
        condition = "@cacheEnabledConfig.getEnabled()"
    )
    List<HashMap<String, String>> getGenericAssayMetaPropertiesMap(
        int geneticEntityId
    );
}
