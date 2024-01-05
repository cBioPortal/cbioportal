package org.cbioportal.persistence.clickhouse;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.ReferenceGenomeGene;
import org.cbioportal.persistence.ReferenceGenomeGeneRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class ReferenceGenomeGeneClickHouseRepository implements ReferenceGenomeGeneRepository {

	@Override
	public List<ReferenceGenomeGene> getAllGenesByGenomeName(String genomeName) {
		// TODO Auto-generated method stub
		return new ArrayList<ReferenceGenomeGene>();
	}

	@Override
	public List<ReferenceGenomeGene> getGenesByHugoGeneSymbolsAndGenomeName(List<String> geneIds, String genomeName) {
		// TODO Auto-generated method stub
		return new ArrayList<ReferenceGenomeGene>();
	}

	@Override
	public List<ReferenceGenomeGene> getGenesByGenomeName(List<Integer> geneIds, String genomeName) {
		// TODO Auto-generated method stub
		return new ArrayList<ReferenceGenomeGene>();
	}

	@Override
	public ReferenceGenomeGene getReferenceGenomeGene(Integer geneId, String genomeName) {
		// TODO Auto-generated method stub
		return new ReferenceGenomeGene();
	}

	@Override
	public ReferenceGenomeGene getReferenceGenomeGeneByEntityId(Integer geneticEntityId, String genomeName) {
		// TODO Auto-generated method stub
		return new ReferenceGenomeGene();
	}

}
