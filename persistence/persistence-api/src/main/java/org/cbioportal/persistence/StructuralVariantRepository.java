package org.cbioportal.persistence;

import org.cbioportal.model.StructuralVariant;
import org.cbioportal.model.StructuralVariantCountByGene;

import java.util.List;

public interface StructuralVariantRepository {

    List<StructuralVariant> fetchStructuralVariants(List<String> molecularProfileIds, List<Integer> entrezGeneIds,
            List<String> sampleIds);

    List<StructuralVariantCountByGene> getSampleCountInMultipleMolecularProfiles(List<String> molecularProfileIds,
            List<String> sampleIds, List<Integer> entrezGeneIds);
}
