package org.cbioportal.persistence.clickhouse;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.MutSig;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.SignificantlyMutatedGeneRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class SignificantlyMutatedGeneClickHouseRepository implements SignificantlyMutatedGeneRepository {

	@Override
	public List<MutSig> getSignificantlyMutatedGenes(String studyId, String projection, Integer pageSize,
			Integer pageNumber, String sortBy, String direction) {
		// TODO Auto-generated method stub
		return new ArrayList<MutSig>();
	}

	@Override
	public BaseMeta getMetaSignificantlyMutatedGenes(String studyId) {
		// TODO Auto-generated method stub
		return new BaseMeta();
	}

}
