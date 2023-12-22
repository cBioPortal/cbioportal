package org.cbioportal.persistence;

import java.util.List;

import org.cbioportal.model.Geneset;
import org.cbioportal.model.GenesetHierarchyInfo;

import org.springframework.cache.annotation.Cacheable;

public interface GenesetHierarchyRepository {

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
	List<GenesetHierarchyInfo> getGenesetHierarchyParents(List<String> genesetIds);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
	List<Geneset> getGenesetHierarchyGenesets(Integer nodeId);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
	List<GenesetHierarchyInfo> getGenesetHierarchySuperNodes(List<String> genesetIds);
}
