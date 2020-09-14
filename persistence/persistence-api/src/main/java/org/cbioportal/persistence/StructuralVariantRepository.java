package org.cbioportal.persistence;

import java.util.List;

import org.cbioportal.model.StructuralVariant;
import org.cbioportal.model.StructuralVariantCountByGene;
import org.springframework.cache.annotation.Cacheable;

public interface StructuralVariantRepository {

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    List<StructuralVariant> fetchStructuralVariants(List<String> molecularProfileIds, List<Integer> entrezGeneIds,
            List<String> sampleIds);

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    List<StructuralVariantCountByGene> getSampleCountInMultipleMolecularProfiles(List<String> molecularProfileIds,
            List<String> sampleIds, List<Integer> entrezGeneIds);

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    List<StructuralVariantCountByGene> getPatientCountInMultipleMolecularProfiles(List<String> molecularProfileIds,
            List<String> patientIds, List<Integer> entrezGeneIds);
}
