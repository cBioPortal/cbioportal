package org.cbioportal.service;

import org.cbioportal.model.CopyNumberCount;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;

import java.util.List;

public interface CopyNumberCountService {
    
    List<CopyNumberCount> fetchCopyNumberCounts(String geneticProfileId, List<Integer> entrezGeneIds, 
                                                List<Integer> alterations) throws GeneticProfileNotFoundException;
    
    
}
