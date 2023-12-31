package org.cbioportal.persistence.clickhouse;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.GeneFilterQuery;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCountByPosition;
import org.cbioportal.model.meta.MutationMeta;
import org.cbioportal.persistence.MutationRepository;
import org.cbioportal.persistence.clickhouse.mapper.MutationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class MutationClickHouseRepository implements MutationRepository {
	
	@Autowired
	private MutationMapper mutationMapper;

	@Override
	public List<Mutation> getMutationsInMolecularProfileBySampleListId(String molecularProfileId, String sampleListId,
			List<Integer> entrezGeneIds, Boolean snpOnly, String projection, Integer pageSize, Integer pageNumber,
			String sortBy, String direction) {
		// TODO Auto-generated method stub
		return new ArrayList<Mutation>();
	}

	@Override
	public MutationMeta getMetaMutationsInMolecularProfileBySampleListId(String molecularProfileId, String sampleListId,
			List<Integer> entrezGeneIds) {
		// TODO Auto-generated method stub
		return new MutationMeta();
	}

	@Override
	public List<Mutation> getMutationsInMultipleMolecularProfiles(List<String> molecularProfileIds,
			List<String> sampleIds, List<Integer> entrezGeneIds, String projection, Integer pageSize,
			Integer pageNumber, String sortBy, String direction) {
		// TODO Auto-generated method stub
		return new ArrayList<Mutation>();
	}

	@Override
	public List<Mutation> getMutationsInMultipleMolecularProfilesByGeneQueries(List<String> molecularProfileIds,
			List<String> sampleIds, List<GeneFilterQuery> geneQueries, String projection, Integer pageSize,
			Integer pageNumber, String sortBy, String direction) {
		// TODO Auto-generated method stub
		return new ArrayList<Mutation>();
	}

	@Override
	public MutationMeta getMetaMutationsInMultipleMolecularProfiles(List<String> molecularProfileIds,
			List<String> sampleIds, List<Integer> entrezGeneIds) {
		// TODO Auto-generated method stub
		return new MutationMeta();
	}

	@Override
	public List<Mutation> fetchMutationsInMolecularProfile(String molecularProfileId, List<String> sampleIds,
			List<Integer> entrezGeneIds, Boolean snpOnly, String projection, Integer pageSize, Integer pageNumber,
			String sortBy, String direction) {
		// TODO Auto-generated method stub
		return new ArrayList<Mutation>();
	}

	@Override
	public MutationMeta fetchMetaMutationsInMolecularProfile(String molecularProfileId, List<String> sampleIds,
			List<Integer> entrezGeneIds) {
		// TODO Auto-generated method stub
		return new MutationMeta();
	}

	@Override
	public MutationCountByPosition getMutationCountByPosition(Integer entrezGeneId, Integer proteinPosStart,
			Integer proteinPosEnd) {
		// TODO Auto-generated method stub
		return new MutationCountByPosition();
	}

}
