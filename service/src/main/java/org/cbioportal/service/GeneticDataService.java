package org.cbioportal.service;

import org.cbioportal.model.GeneticData;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.cbioportal.service.exception.SampleNotFoundException;

import java.util.List;

public interface GeneticDataService {

    List<GeneticData> getGeneticData(String geneticProfileId, String sampleId, List<Integer> entrezGeneIds) 
        throws GeneticProfileNotFoundException, SampleNotFoundException;

    List<GeneticData> getGeneticDataOfAllSamplesOfGeneticProfile(String geneticProfileId, List<Integer> entrezGeneIds)
        throws GeneticProfileNotFoundException;
}
