package org.cbioportal.persistence;

import org.cbioportal.model.MutSig;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface SignificantlyMutatedGeneRepository {

    @Cacheable("RepositoryCache")
    List<MutSig> getSignificantlyMutatedGenes(String studyId, String projection, Integer pageSize, Integer pageNumber,
                                              String sortBy, String direction);

    @Cacheable("RepositoryCache")
    BaseMeta getMetaSignificantlyMutatedGenes(String studyId);
}
