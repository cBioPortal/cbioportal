package org.cbioportal.service;

import org.cbioportal.model.VariantCount;

import java.util.List;

public interface VariantCountService {
    
    List<VariantCount> fetchVariantCounts(String geneticProfileId, List<Integer> entrezGeneIds, List<String> keywords);
}
