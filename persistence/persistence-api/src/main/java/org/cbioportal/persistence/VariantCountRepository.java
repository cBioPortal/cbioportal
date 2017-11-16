package org.cbioportal.persistence;

import org.cbioportal.model.VariantCount;

import java.util.List;

public interface VariantCountRepository {
    
    List<VariantCount> fetchVariantCounts(String molecularProfileId, List<Integer> entrezGeneIds, 
                                          List<String> keywords);
}
