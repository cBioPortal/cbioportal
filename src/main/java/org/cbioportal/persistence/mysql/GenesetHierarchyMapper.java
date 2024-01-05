package org.cbioportal.persistence.mysql;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.cbioportal.model.Geneset;
import org.cbioportal.model.GenesetHierarchyInfo;
import org.springframework.context.annotation.Profile;

public interface GenesetHierarchyMapper {

	List<GenesetHierarchyInfo> getGenesetHierarchyParents(@Param("genesetIds") List<String> genesetIds);

	List<Geneset> getGenesetHierarchyGenesets(Integer nodeId);

	List<GenesetHierarchyInfo> getGenesetHierarchySuperNodes(@Param("genesetIds") List<String> genesetIds);
}
