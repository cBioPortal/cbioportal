package org.cbioportal.service;

import java.util.List;

import org.cbioportal.model.GenesetHierarchyInfo;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;

public interface GenesetHierarchyService {

	List<GenesetHierarchyInfo> getGenesetHierarchyInfo(String geneticProfileId) throws GeneticProfileNotFoundException;

}
