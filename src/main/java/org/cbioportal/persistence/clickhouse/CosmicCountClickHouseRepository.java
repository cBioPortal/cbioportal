package org.cbioportal.persistence.clickhouse;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.CosmicMutation;
import org.cbioportal.persistence.CosmicCountRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class CosmicCountClickHouseRepository implements CosmicCountRepository {

	@Override
	public List<CosmicMutation> fetchCosmicCountsByKeywords(List<String> keywords) {
		// TODO Auto-generated method stub
		return new ArrayList<CosmicMutation>();
	}

}
