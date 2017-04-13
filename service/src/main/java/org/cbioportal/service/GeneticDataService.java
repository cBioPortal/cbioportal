package org.cbioportal.service;

import org.cbioportal.model.GeneGeneticData;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;

import java.util.List;

public interface GeneticDataService {

    List<GeneGeneticData> getGeneticData(String geneticProfileId, String sampleId, List<Integer> entrezGeneIds, 
                                     String projection) 
        throws GeneticProfileNotFoundException;
    
    List<GeneGeneticData> fetchGeneticData(String geneticProfileId, List<String> sampleIds, List<Integer> entrezGeneIds, 
                                       String projection) throws GeneticProfileNotFoundException;

    Integer getNumberOfSamplesInGeneticProfile(String geneticProfileId);
}
