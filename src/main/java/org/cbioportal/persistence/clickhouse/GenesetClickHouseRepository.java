package org.cbioportal.persistence.clickhouse;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.Gene;
import org.cbioportal.model.Geneset;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GenesetRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class GenesetClickHouseRepository implements GenesetRepository {

	@Override
	public List<Geneset> getAllGenesets(String projection, Integer pageSize, Integer pageNumber) {
		// TODO Auto-generated method stub
		return new ArrayList<Geneset>();
	}

	@Override
	public BaseMeta getMetaGenesets() {
		// TODO Auto-generated method stub
		return new BaseMeta();
	}

	@Override
	public Geneset getGeneset(String genesetId) {
		// TODO Auto-generated method stub
		return new Geneset();
	}

	@Override
	public List<Geneset> fetchGenesets(List<String> genesetIds) {
		// TODO Auto-generated method stub
		return new ArrayList<Geneset>();
	}

	@Override
	public List<Gene> getGenesByGenesetId(String genesetId) {
		// TODO Auto-generated method stub
		return new ArrayList<Gene>();
	}

	@Override
	public String getGenesetVersion() {
		// TODO Auto-generated method stub
		return "";
	}

}
