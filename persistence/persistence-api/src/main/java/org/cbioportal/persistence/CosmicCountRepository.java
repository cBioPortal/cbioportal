package org.cbioportal.persistence;

import java.util.List;
import org.cbioportal.model.CosmicMutation;

public interface CosmicCountRepository {
    
	List<CosmicMutation> fetchCosmicCountsByKeywords(List<String> keywords);
}
