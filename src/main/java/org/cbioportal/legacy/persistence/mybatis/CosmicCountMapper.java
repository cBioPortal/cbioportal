package org.cbioportal.legacy.persistence.mybatis;

import org.cbioportal.legacy.model.CosmicMutation;

import java.util.List;

public interface CosmicCountMapper {

	List<CosmicMutation> getCosmicCountsByKeywords(List<String> keywords);
}
