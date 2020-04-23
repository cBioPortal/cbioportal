package org.cbioportal.persistence;

import java.util.List;

import org.cbioportal.model.ResourceDefinition;
import org.springframework.cache.annotation.Cacheable;

public interface ResourceDefinitionRepository {

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    ResourceDefinition getResourceDefinition(String studyId, String resourceId);

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    List<ResourceDefinition> getAllResourceDefinitionsInStudy(String studyId, String projection, Integer pageSize,
            Integer pageNumber, String sortBy, String direction);
}
