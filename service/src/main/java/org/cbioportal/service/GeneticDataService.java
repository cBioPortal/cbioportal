package org.cbioportal.service;

import org.cbioportal.model.GeneticData;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;

import java.util.List;

public interface GeneticDataService {

    List<GeneticData> getGeneticData(String geneticProfileId, String sampleId, List<Integer> entrezGeneIds, 
                                     String projection) 
        throws GeneticProfileNotFoundException;
    
    List<GeneticData> fetchGeneticData(String geneticProfileId, List<String> sampleIds, List<Integer> entrezGeneIds, 
                                       String projection) throws GeneticProfileNotFoundException;

    Integer getNumberOfSamplesInGeneticProfile(String geneticProfileId);
}
