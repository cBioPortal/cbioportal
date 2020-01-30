package org.cbioportal.persistence;

import java.util.List;
import org.cbioportal.model.StructuralVariant;

public interface StructuralVariantRepository {
    List<StructuralVariant> fetchStructuralVariants(
        List<String> molecularProfileIds,
        List<Integer> entrezGeneIds,
        List<String> sampleIds
    );
}
