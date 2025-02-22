package org.cbioportal.legacy.persistence;

import java.util.List;
import org.cbioportal.legacy.model.Geneset;
import org.cbioportal.legacy.model.GenesetHierarchyInfo;
import org.springframework.cache.annotation.Cacheable;

public interface GenesetHierarchyRepository {

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  List<GenesetHierarchyInfo> getGenesetHierarchyParents(List<String> genesetIds);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  List<Geneset> getGenesetHierarchyGenesets(Integer nodeId);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  List<GenesetHierarchyInfo> getGenesetHierarchySuperNodes(List<String> genesetIds);
}
