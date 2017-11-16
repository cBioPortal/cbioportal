package org.cbioportal.service;

import org.cbioportal.model.ExpressionEnrichment;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

import java.util.List;

public interface ExpressionEnrichmentService {
    
    List<ExpressionEnrichment> getExpressionEnrichments(String molecularProfileId, List<String> alteredSampleIds, 
                                                        List<String> unalteredSampleIds, String enrichmentType) 
        throws MolecularProfileNotFoundException;
}
