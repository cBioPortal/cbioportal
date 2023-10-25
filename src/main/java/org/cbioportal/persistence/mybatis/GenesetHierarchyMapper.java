package org.cbioportal.persistence.mybatis;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.cbioportal.model.Geneset;
import org.cbioportal.model.GenesetHierarchyInfo;

public interface GenesetHierarchyMapper {

	List<GenesetHierarchyInfo> getGenesetHierarchyParents(@Param("genesetIds") List<String> genesetIds);

	List<Geneset> getGenesetHierarchyGenesets(Integer nodeId);

	List<GenesetHierarchyInfo> getGenesetHierarchySuperNodes(@Param("genesetIds") List<String> genesetIds);
}
