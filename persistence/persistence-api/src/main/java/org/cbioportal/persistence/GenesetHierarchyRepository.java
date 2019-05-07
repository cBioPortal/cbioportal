package org.cbioportal.persistence;

import java.util.List;

import org.cbioportal.model.Geneset;
import org.cbioportal.model.GenesetHierarchyInfo;

import org.springframework.cache.annotation.Cacheable;

public interface GenesetHierarchyRepository {

    @Cacheable("GeneralRepositoryCache")
	List<GenesetHierarchyInfo> getGenesetHierarchyParents(List<String> genesetIds);

    @Cacheable("GeneralRepositoryCache")
	List<Geneset> getGenesetHierarchyGenesets(Integer nodeId);

    @Cacheable("GeneralRepositoryCache")
	List<GenesetHierarchyInfo> getGenesetHierarchySuperNodes(List<String> genesetIds);
}
