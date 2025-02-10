package org.cbioportal.legacy.persistence;

import org.cbioportal.legacy.model.VariantCount;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface VariantCountRepository {

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<VariantCount> fetchVariantCounts(String molecularProfileId, List<Integer> entrezGeneIds, 
                                          List<String> keywords);
}
