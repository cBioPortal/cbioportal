package org.cbioportal.persistence;

import org.cbioportal.model.TypeOfCancer;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface CancerTypeRepository {

    @Cacheable("RepositoryCache")
    List<TypeOfCancer> getAllCancerTypes(String projection, Integer pageSize, Integer pageNumber, String sortBy,
                                         String direction);
    @Cacheable("RepositoryCache")
    BaseMeta getMetaCancerTypes();

    @Cacheable("RepositoryCache")
    TypeOfCancer getCancerType(String cancerTypeId);
}
