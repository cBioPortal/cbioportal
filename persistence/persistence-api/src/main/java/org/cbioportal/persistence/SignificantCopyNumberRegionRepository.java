package org.cbioportal.persistence;

import java.util.List;
import org.cbioportal.model.Gistic;
import org.cbioportal.model.GisticToGene;
import org.cbioportal.model.meta.BaseMeta;
import org.springframework.cache.annotation.Cacheable;

public interface SignificantCopyNumberRegionRepository {
    @Cacheable(
        cacheNames = "GeneralRepositoryCache",
        condition = "@cacheEnabledConfig.getEnabled()"
    )
    List<Gistic> getSignificantCopyNumberRegions(
        String studyId,
        String projection,
        Integer pageSize,
        Integer pageNumber,
        String sortBy,
        String direction
    );

    @Cacheable(
        cacheNames = "GeneralRepositoryCache",
        condition = "@cacheEnabledConfig.getEnabled()"
    )
    BaseMeta getMetaSignificantCopyNumberRegions(String studyId);

    @Cacheable(
        cacheNames = "GeneralRepositoryCache",
        condition = "@cacheEnabledConfig.getEnabled()"
    )
    List<GisticToGene> getGenesOfRegions(List<Long> gisticRoiIds);
}
