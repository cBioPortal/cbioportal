package org.cbioportal.persistence;

import java.util.List;

import org.cbioportal.model.meta.GenericAssayMeta;
import org.cbioportal.model.GenericAssayAdditionalProperty;
import org.springframework.cache.annotation.Cacheable;

public interface GenericAssayRepository {

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<GenericAssayMeta> getGenericAssayMeta(List<String> stableIds);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<GenericAssayAdditionalProperty> getGenericAssayAdditionalproperties(List<String> stableIds);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<String> getGenericAssayStableIdsByMolecularIds(List<String> molecularProfileIds);
}
