package org.cbioportal.persistence.clickhouse;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneAlias;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GeneRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class GeneClickHouseRepository implements GeneRepository {

	@Override
	public List<Gene> getAllGenes(String keyword, String alias, String projection, Integer pageSize, Integer pageNumber,
			String sortBy, String direction) {
		// TODO Auto-generated method stub
		return new ArrayList<Gene>();
	}

	@Override
	public BaseMeta getMetaGenes(String keyword, String alias) {
		// TODO Auto-generated method stub
		return new BaseMeta();
	}

	@Override
	public Gene getGeneByGeneticEntityId(Integer geneticEntityId) {
		// TODO Auto-generated method stub
		return new Gene();
	}

	@Override
	public Gene getGeneByEntrezGeneId(Integer entrezGeneId) {
		// TODO Auto-generated method stub
		return new Gene();
	}

	@Override
	public Gene getGeneByHugoGeneSymbol(String hugoGeneSymbol) {
		// TODO Auto-generated method stub
		return new Gene();
	}

	@Override
	public List<String> getAliasesOfGeneByEntrezGeneId(Integer entrezGeneId) {
		// TODO Auto-generated method stub
		return new ArrayList<String>();
	}

	@Override
	public List<String> getAliasesOfGeneByHugoGeneSymbol(String hugoGeneSymbol) {
		// TODO Auto-generated method stub
		return new ArrayList<String>();
	}

	@Override
	public List<GeneAlias> getAllAliases() {
		// TODO Auto-generated method stub
		return new ArrayList<GeneAlias>();
	}

	@Override
	public List<Gene> fetchGenesByEntrezGeneIds(List<Integer> entrezGeneIds, String projection) {
		// TODO Auto-generated method stub
		return new ArrayList<Gene>();
	}

	@Override
	public List<Gene> fetchGenesByHugoGeneSymbols(List<String> hugoGeneSymbols, String projection) {
		// TODO Auto-generated method stub
		return new ArrayList<Gene>();
	}

	@Override
	public BaseMeta fetchMetaGenesByEntrezGeneIds(List<Integer> entrezGeneIds) {
		// TODO Auto-generated method stub
		return new BaseMeta();
	}

	@Override
	public BaseMeta fetchMetaGenesByHugoGeneSymbols(List<String> hugoGeneSymbols) {
		// TODO Auto-generated method stub
		return new BaseMeta();
	}

}
