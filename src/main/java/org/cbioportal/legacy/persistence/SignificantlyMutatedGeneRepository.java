package org.cbioportal.legacy.persistence;

import org.cbioportal.legacy.model.MutSig;
import org.cbioportal.legacy.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface SignificantlyMutatedGeneRepository {

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<MutSig> getSignificantlyMutatedGenes(String studyId, String projection, Integer pageSize, Integer pageNumber,
                                              String sortBy, String direction);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    BaseMeta getMetaSignificantlyMutatedGenes(String studyId);
}
