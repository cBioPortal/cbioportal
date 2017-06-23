package org.cbioportal.service;

import java.util.List;

import org.cbioportal.model.GenesetGeneticData;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.cbioportal.service.exception.SampleListNotFoundException;

public interface GenesetDataService {

    List<GenesetGeneticData> fetchGenesetData(String geneticProfileId, List<String> sampleIds, List<String> genesetIds) throws GeneticProfileNotFoundException;

	List<GenesetGeneticData> fetchGenesetData(String geneticProfileId, String sampleListId, List<String> genesetIds) throws GeneticProfileNotFoundException, SampleListNotFoundException;
}
