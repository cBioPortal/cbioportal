package org.cbioportal.legacy.persistence;

import org.cbioportal.legacy.model.Gistic;
import org.cbioportal.legacy.model.GisticToGene;
import org.cbioportal.legacy.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface SignificantCopyNumberRegionRepository {
   
    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<Gistic> getSignificantCopyNumberRegions(String studyId, String projection, Integer pageSize, 
                                                 Integer pageNumber, String sortBy, String direction);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    BaseMeta getMetaSignificantCopyNumberRegions(String studyId);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<GisticToGene> getGenesOfRegions(List<Long> gisticRoiIds);
}
