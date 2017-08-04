package org.cbioportal.service;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;

import java.util.List;

public interface CopyNumberEnrichmentService {
    
    List<AlterationEnrichment> getCopyNumberEnrichments(String geneticProfileId, List<String> alteredIds, 
                                                        List<String> unalteredIds, List<Integer> alterationTypes, 
                                                        String enrichmentType) 
        throws GeneticProfileNotFoundException;
}