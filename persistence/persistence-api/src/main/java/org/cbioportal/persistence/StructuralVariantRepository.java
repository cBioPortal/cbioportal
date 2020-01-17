package org.cbioportal.persistence;

import org.cbioportal.model.StructuralVariant;

import java.util.List;

public interface StructuralVariantRepository {

    List<StructuralVariant> fetchStructuralVariants(List<String> molecularProfileIds, 
            List<Integer> entrezGeneIds, List<String> sampleIds);
}
