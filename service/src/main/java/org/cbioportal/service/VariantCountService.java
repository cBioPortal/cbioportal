package org.cbioportal.service;

import java.util.List;
import org.cbioportal.model.VariantCount;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;

public interface VariantCountService {
    List<VariantCount> fetchVariantCounts(
        String molecularProfileId,
        List<Integer> entrezGeneIds,
        List<String> keywords
    )
        throws MolecularProfileNotFoundException;
}
