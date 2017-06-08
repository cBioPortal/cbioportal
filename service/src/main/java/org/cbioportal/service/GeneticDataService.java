package org.cbioportal.service;

import org.cbioportal.model.GeneGeneticData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;

import java.util.List;

public interface GeneticDataService {

    List<GeneGeneticData> getGeneticData(String geneticProfileId, String sampleListId, List<Integer> entrezGeneIds,
                                         String projection) 
        throws GeneticProfileNotFoundException;

    BaseMeta getMetaGeneticData(String geneticProfileId, String sampleListId, List<Integer> entrezGeneIds) 
        throws GeneticProfileNotFoundException;
    
    List<GeneGeneticData> fetchGeneticData(String geneticProfileId, List<String> sampleIds, List<Integer> entrezGeneIds, 
                                       String projection) throws GeneticProfileNotFoundException;

    BaseMeta fetchMetaGeneticData(String geneticProfileId, List<String> sampleIds, List<Integer> entrezGeneIds) 
        throws GeneticProfileNotFoundException;
    
    Integer getNumberOfSamplesInGeneticProfile(String geneticProfileId);
}
