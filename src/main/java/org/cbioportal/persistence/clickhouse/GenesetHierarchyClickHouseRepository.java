package org.cbioportal.persistence.clickhouse;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.Geneset;
import org.cbioportal.model.GenesetHierarchyInfo;
import org.cbioportal.persistence.GenesetHierarchyRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class GenesetHierarchyClickHouseRepository implements GenesetHierarchyRepository {

	@Override
	public List<GenesetHierarchyInfo> getGenesetHierarchyParents(List<String> genesetIds) {
		// TODO Auto-generated method stub
		return new ArrayList<GenesetHierarchyInfo>();
	}

	@Override
	public List<Geneset> getGenesetHierarchyGenesets(Integer nodeId) {
		// TODO Auto-generated method stub
		return new ArrayList<Geneset>();
	}

	@Override
	public List<GenesetHierarchyInfo> getGenesetHierarchySuperNodes(List<String> genesetIds) {
		// TODO Auto-generated method stub
		return new ArrayList<GenesetHierarchyInfo>();
	}

}
