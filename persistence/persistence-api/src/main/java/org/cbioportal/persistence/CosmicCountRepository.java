package org.cbioportal.persistence;

import java.util.List;
import org.cbioportal.model.CosmicCount;

public interface CosmicCountRepository {
	List<CosmicCount> getCOSMICCountsByKeywords(List<String> keywords);
}
