package org.cbioportal.persistence.mysql;

import java.util.List;

import org.cbioportal.model.Geneset;
import org.cbioportal.model.GenesetHierarchyInfo;
import org.cbioportal.persistence.GenesetHierarchyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("mysql")
public class GenesetHierarchyMyBatisRepository implements GenesetHierarchyRepository {

	@Autowired
    private GenesetHierarchyMapper genesetHierarchyMapper;
	
	@Override
	public List<GenesetHierarchyInfo> getGenesetHierarchyParents(List<String> genesetIds) {

		return genesetHierarchyMapper.getGenesetHierarchyParents(genesetIds);
	}
	@Override
	public List<Geneset> getGenesetHierarchyGenesets(Integer nodeId) {

		return genesetHierarchyMapper.getGenesetHierarchyGenesets(nodeId);
	}
	@Override
	public List<GenesetHierarchyInfo> getGenesetHierarchySuperNodes(List<String> genesetIds) {

		return genesetHierarchyMapper.getGenesetHierarchySuperNodes(genesetIds);
	}
}
