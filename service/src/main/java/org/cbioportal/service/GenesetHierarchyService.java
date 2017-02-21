package org.cbioportal.service;

import java.util.List;

import org.cbioportal.model.GenesetHierarchyInfo;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;

public interface GenesetHierarchyService {

	List<GenesetHierarchyInfo> fetchGenesetHierarchyInfo(String geneticProfileId, Integer percentile, Double scoreThreshold, Double pvalueThreshold) throws GeneticProfileNotFoundException;

	List<GenesetHierarchyInfo> fetchGenesetHierarchyInfo(String geneticProfileId, Integer percentile, Double scoreThreshold,
			Double pvalueThreshold, List<String> sampleIds) throws GeneticProfileNotFoundException;

	List<GenesetHierarchyInfo> fetchGenesetHierarchyInfo(String geneticProfileId, Integer percentile, Double scoreThreshold,
			Double pvalueThreshold, String sampleListId) throws GeneticProfileNotFoundException;

}
