package org.cbioportal.service;

import org.cbioportal.model.ExpressionEnrichment;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;

import java.util.List;

public interface ExpressionEnrichmentService {
    
    List<ExpressionEnrichment> getExpressionEnrichments(String geneticProfileId, List<String> alteredSampleIds, 
                                                        List<String> unalteredSampleIds, String enrichmentType) 
        throws GeneticProfileNotFoundException;
}
