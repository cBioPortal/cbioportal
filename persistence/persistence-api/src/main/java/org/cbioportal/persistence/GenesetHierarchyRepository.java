package org.cbioportal.persistence;

import java.util.List;

import org.cbioportal.model.GenesetAlteration;
import org.cbioportal.model.GenesetHierarchyInfo;

public interface GenesetHierarchyRepository {

    List<GenesetHierarchyInfo> getGenesetHierarchyItems(String geneticProfileId, List<GenesetAlteration> genesetScores, 
    		Integer percentile, Double scoreThreshold, Double pvalueThreshold);
}