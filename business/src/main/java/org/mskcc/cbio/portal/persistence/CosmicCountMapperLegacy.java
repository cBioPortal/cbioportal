package org.mskcc.cbio.portal.persistence;

import org.mskcc.cbio.portal.model.CosmicCount;

import java.util.List;

public interface CosmicCountMapperLegacy {

	List<CosmicCount> getCOSMICCountsByKeywords(List<String> keywords);
}
