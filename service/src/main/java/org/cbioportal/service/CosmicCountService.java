package org.cbioportal.service;


import java.util.List;
import org.cbioportal.model.CosmicCount;

public interface CosmicCountService {

    List<CosmicCount> getCOSMICCountsByKeywords(List<String> keywords);
}
