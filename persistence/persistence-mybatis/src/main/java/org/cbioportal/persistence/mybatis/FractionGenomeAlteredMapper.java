package org.cbioportal.persistence.mybatis;

import java.util.List;

import org.cbioportal.model.FractionGenomeAltered;

public interface FractionGenomeAlteredMapper {

    List<FractionGenomeAltered> getFractionGenomeAltered(List<String> studyIds, List<String> sampleIds);

    List<FractionGenomeAltered> getFractionGenomeAlteredBySampleListId(String studyId, String sampleListId);
}
