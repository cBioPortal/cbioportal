package org.cbioportal.persistence;

import org.cbioportal.model.MutSig;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface SignificantlyMutatedGeneRepository {

    @Cacheable("GeneralRepositoryCache")
    List<MutSig> getSignificantlyMutatedGenes(String studyId, String projection, Integer pageSize, Integer pageNumber,
                                              String sortBy, String direction);

    @Cacheable("GeneralRepositoryCache")
    BaseMeta getMetaSignificantlyMutatedGenes(String studyId);
}
