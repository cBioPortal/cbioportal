package org.cbioportal.legacy.persistence.mybatis;

import org.cbioportal.legacy.model.VariantCount;

import java.util.List;

public interface VariantCountMapper {
    
    List<VariantCount> fetchVariantCounts(String molecularProfileId, List<Integer> entrezGeneIds, 
                                          List<String> keywords);
}
