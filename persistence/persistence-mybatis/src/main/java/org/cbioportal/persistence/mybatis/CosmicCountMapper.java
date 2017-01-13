package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.CosmicCount;

import java.util.List;

public interface CosmicCountMapper {

	List<CosmicCount> getCOSMICCountsByKeywords(List<String> keywords);
}
