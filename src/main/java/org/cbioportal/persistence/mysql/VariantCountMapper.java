package org.cbioportal.persistence.mysql;

import org.cbioportal.model.VariantCount;
import org.springframework.context.annotation.Profile;

import java.util.List;

public interface VariantCountMapper {
    
    List<VariantCount> fetchVariantCounts(String molecularProfileId, List<Integer> entrezGeneIds, 
                                          List<String> keywords);
}
