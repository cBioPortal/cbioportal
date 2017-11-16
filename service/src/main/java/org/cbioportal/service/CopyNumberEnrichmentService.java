package org.cbioportal.service;

import org.cbioportal.model.AlterationEnrichment;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

import java.util.List;

public interface CopyNumberEnrichmentService {
    
    List<AlterationEnrichment> getCopyNumberEnrichments(String molecularProfileId, List<String> alteredIds, 
                                                        List<String> unalteredIds, List<Integer> alterationTypes, 
                                                        String enrichmentType) 
        throws MolecularProfileNotFoundException;
}