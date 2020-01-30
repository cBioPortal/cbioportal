package org.cbioportal.persistence.mybatis;

import java.util.List;
import org.cbioportal.model.VariantCount;

public interface VariantCountMapper {
    List<VariantCount> fetchVariantCounts(
        String molecularProfileId,
        List<Integer> entrezGeneIds,
        List<String> keywords
    );
}
