package org.cbioportal.service;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;

import java.util.List;

public interface CopyNumberEnrichmentService {
    
    List<AlterationEnrichment> getCopyNumberEnrichments(String geneticProfileId, List<String> alteredSampleIds, 
                                                        List<String> unalteredSampleIds, List<Integer> entrezGeneIds, 
                                                        List<Integer> alterationTypes) 
        throws GeneticProfileNotFoundException;
}