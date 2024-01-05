package org.cbioportal.persistence.mysql;

import org.cbioportal.model.CosmicMutation;
import org.springframework.context.annotation.Profile;

import java.util.List;

public interface CosmicCountMapper {

	List<CosmicMutation> getCosmicCountsByKeywords(List<String> keywords);
}
