package org.cbioportal.persistence.clickhouse.mapper;

import org.cbioportal.model.CosmicMutation;

import java.util.List;

public interface CosmicCountMapper {

	List<CosmicMutation> getCosmicCountsByKeywords(List<String> keywords);
}
