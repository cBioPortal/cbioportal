package org.cbioportal.service;

import org.cbioportal.model.VariantCount;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

import java.util.List;

public interface VariantCountService {
    
    List<VariantCount> fetchVariantCounts(String molecularProfileId, List<Integer> entrezGeneIds, List<String> keywords) 
        throws MolecularProfileNotFoundException;
}
