package org.cbioportal.persistence;

import org.cbioportal.model.TypeOfCancer;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface CancerTypeRepository {

    @Cacheable("GeneralRepositoryCache")
    List<TypeOfCancer> getAllCancerTypes(String projection, Integer pageSize, Integer pageNumber, String sortBy,
                                         String direction);
    @Cacheable("GeneralRepositoryCache")
    BaseMeta getMetaCancerTypes();

    @Cacheable("GeneralRepositoryCache")
    TypeOfCancer getCancerType(String cancerTypeId);
}
