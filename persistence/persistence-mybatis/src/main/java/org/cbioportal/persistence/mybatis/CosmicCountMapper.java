package org.cbioportal.persistence.mybatis;

import java.util.List;
import org.cbioportal.model.CosmicCount;

public interface CosmicCountMapper {

	List<CosmicCount> getCOSMICCountsByKeywords(List<String> keywords);
}
