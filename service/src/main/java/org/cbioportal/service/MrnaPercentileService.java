package org.cbioportal.service;

import org.cbioportal.model.MrnaPercentile;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;

import java.util.List;

public interface MrnaPercentileService {
    
    List<MrnaPercentile> fetchMrnaPercentile(String geneticProfileId, String sampleId, List<Integer> entrezGeneIds) 
        throws GeneticProfileNotFoundException;
}
