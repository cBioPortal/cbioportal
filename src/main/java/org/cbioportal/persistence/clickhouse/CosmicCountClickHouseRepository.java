package org.cbioportal.persistence.clickhouse;

import java.util.List;

import org.cbioportal.model.CosmicMutation;
import org.cbioportal.persistence.CosmicCountRepository;
import org.cbioportal.persistence.clickhouse.mapper.CosmicCountMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class CosmicCountClickHouseRepository implements CosmicCountRepository {
	
	private CosmicCountMapper cosmicCountMapper;

	@Override
	public List<CosmicMutation> fetchCosmicCountsByKeywords(List<String> keywords) {
		return cosmicCountMapper.getCosmicCountsByKeywords(keywords);
	}

}
