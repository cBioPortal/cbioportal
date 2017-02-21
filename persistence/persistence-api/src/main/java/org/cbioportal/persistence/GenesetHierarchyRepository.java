package org.cbioportal.persistence;

import java.util.List;

import org.cbioportal.model.Geneset;
import org.cbioportal.model.GenesetHierarchyInfo;

public interface GenesetHierarchyRepository {

	List<GenesetHierarchyInfo> getGenesetHierarchyParents(List<String> genesetIds);

	List<Geneset> getGenesetHierarchyGenesets(Integer nodeId);

	List<GenesetHierarchyInfo> getGenesetHierarchySuperNodes(List<String> genesetIds);
}