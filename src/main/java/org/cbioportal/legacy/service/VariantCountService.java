package org.cbioportal.legacy.service;

import org.cbioportal.legacy.model.VariantCount;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;

import java.util.List;

public interface VariantCountService {
    
    List<VariantCount> fetchVariantCounts(String molecularProfileId, List<Integer> entrezGeneIds, List<String> keywords) 
        throws MolecularProfileNotFoundException;
}
