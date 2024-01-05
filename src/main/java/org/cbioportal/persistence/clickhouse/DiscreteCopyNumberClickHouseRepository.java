package org.cbioportal.persistence.clickhouse;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.GeneFilterQuery;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.DiscreteCopyNumberRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class DiscreteCopyNumberClickHouseRepository implements DiscreteCopyNumberRepository {

	@Override
	public List<DiscreteCopyNumberData> getDiscreteCopyNumbersInMolecularProfileBySampleListId(
			String molecularProfileId, String sampleListId, List<Integer> entrezGeneIds, List<Integer> alterationTypes,
			String projection) {
		// TODO Auto-generated method stub
		return new ArrayList<DiscreteCopyNumberData>();
	}

	@Override
	public BaseMeta getMetaDiscreteCopyNumbersInMolecularProfileBySampleListId(String molecularProfileId,
			String sampleListId, List<Integer> entrezGeneIds, List<Integer> alterationTypes) {
		// TODO Auto-generated method stub
		return new BaseMeta();
	}

	@Override
	public List<DiscreteCopyNumberData> fetchDiscreteCopyNumbersInMolecularProfile(String molecularProfileId,
			List<String> sampleIds, List<Integer> entrezGeneIds, List<Integer> alterationTypes, String projection) {
		// TODO Auto-generated method stub
		return new ArrayList<DiscreteCopyNumberData>();
	}

	@Override
	public List<DiscreteCopyNumberData> getDiscreteCopyNumbersInMultipleMolecularProfiles(
			List<String> molecularProfileIds, List<String> sampleIds, List<Integer> entrezGeneIds,
			List<Integer> alterationTypes, String projection) {
		// TODO Auto-generated method stub
		return new ArrayList<DiscreteCopyNumberData>();
	}

	@Override
	public List<DiscreteCopyNumberData> getDiscreteCopyNumbersInMultipleMolecularProfilesByGeneQueries(
			List<String> molecularProfileIds, List<String> sampleIds, List<GeneFilterQuery> geneFilterQuery,
			String projection) {
		// TODO Auto-generated method stub
		return new ArrayList<DiscreteCopyNumberData>();
	}

	@Override
	public BaseMeta fetchMetaDiscreteCopyNumbersInMolecularProfile(String molecularProfileId, List<String> sampleIds,
			List<Integer> entrezGeneIds, List<Integer> alterationTypes) {
		// TODO Auto-generated method stub
		return new BaseMeta();
	}

	@Override
	public List<CopyNumberCountByGene> getSampleCountByGeneAndAlterationAndSampleIds(String molecularProfileId,
			List<String> sampleIds, List<Integer> entrezGeneIds, List<Integer> alterations) {
		// TODO Auto-generated method stub
		return new ArrayList<CopyNumberCountByGene>();
	}

}
