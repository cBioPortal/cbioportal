package org.cbioportal.legacy.service;


import java.util.List;
import org.cbioportal.legacy.model.CosmicMutation;

public interface CosmicCountService {

    List<CosmicMutation> fetchCosmicCountsByKeywords(List<String> keywords);
}
