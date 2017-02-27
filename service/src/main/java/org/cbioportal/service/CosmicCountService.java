package org.cbioportal.service;


import java.util.List;
import org.cbioportal.model.CosmicMutation;

public interface CosmicCountService {

    List<CosmicMutation> fetchCosmicCountsByKeywords(List<String> keywords);
}
