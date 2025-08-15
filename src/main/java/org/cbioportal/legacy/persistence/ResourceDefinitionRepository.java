package org.cbioportal.legacy.persistence;

import java.util.List;
import org.cbioportal.legacy.model.ResourceDefinition;
import org.springframework.cache.annotation.Cacheable;

public interface ResourceDefinitionRepository {

  @Cacheable(
      cacheResolver = "staticRepositoryCacheOneResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  ResourceDefinition getResourceDefinition(String studyId, String resourceId);

  @Cacheable(
      cacheResolver = "staticRepositoryCacheOneResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  List<ResourceDefinition> fetchResourceDefinitions(
      List<String> studyIds,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction);
}
