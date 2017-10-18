package org.cbioportal.service;

import org.cbioportal.model.FractionGenomeAltered;

import java.util.List;

public interface FractionGenomeAlteredService {
    
    List<FractionGenomeAltered> getFractionGenomeAltered(String profileId, String sampleListId, Double cutoff);

    List<FractionGenomeAltered> fetchFractionGenomeAltered(String profileId, List<String> sampleIds, Double cutoff);
}
