package org.cbioportal.service;

import org.cbioportal.model.FractionGenomeAltered;

import java.util.List;

public interface FractionGenomeAlteredService {
    
    List<FractionGenomeAltered> getFractionGenomeAltered(String studyId, String sampleListId);

    List<FractionGenomeAltered> fetchFractionGenomeAltered(List<String> studyIds, List<String> sampleIds);
}
