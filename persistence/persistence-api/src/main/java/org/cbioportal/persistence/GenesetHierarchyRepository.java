package org.cbioportal.persistence;

import java.util.List;

import org.cbioportal.model.Geneset;
import org.cbioportal.model.GenesetHierarchyInfo;

import org.springframework.cache.annotation.Cacheable;

public interface GenesetHierarchyRepository {

    @Cacheable("RepositoryCache")
	List<GenesetHierarchyInfo> getGenesetHierarchyParents(List<String> genesetIds);

    @Cacheable("RepositoryCache")
	List<Geneset> getGenesetHierarchyGenesets(Integer nodeId);

    @Cacheable("RepositoryCache")
	List<GenesetHierarchyInfo> getGenesetHierarchySuperNodes(List<String> genesetIds);
}
