package org.cbioportal.persistence;

import java.util.List;

import org.cbioportal.model.FractionGenomeAltered;

public interface FractionGenomeAlteredRepository {

    List<FractionGenomeAltered> getFractionGenomeAltered(String studyId, String sampleListId);

    List<FractionGenomeAltered> fetchFractionGenomeAltered(List<String> studyIds, List<String> sampleIds);
}
