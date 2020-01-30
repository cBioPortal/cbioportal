package org.mskcc.cbio.portal.persistence;

import java.util.List;
import org.mskcc.cbio.portal.model.CosmicCount;

public interface CosmicCountMapperLegacy {
    List<CosmicCount> getCOSMICCountsByKeywords(List<String> keywords);
}
