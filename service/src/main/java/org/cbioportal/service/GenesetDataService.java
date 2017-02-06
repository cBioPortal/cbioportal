package org.cbioportal.service;

import java.util.List;

import org.cbioportal.model.GenesetData;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;

public interface GenesetDataService {

    List<GenesetData> fetchGenesetData(String geneticProfileId, List<String> sampleIds, List<String> genesetIds) throws GeneticProfileNotFoundException;

	List<GenesetData> fetchGenesetData(String geneticProfileId, String sampleListId, List<String> genesetIds) throws GeneticProfileNotFoundException;
}
