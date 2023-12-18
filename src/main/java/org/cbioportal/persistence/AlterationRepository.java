package org.cbioportal.persistence;

import org.cbioportal.model.*;
import org.cbioportal.model.util.Select;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Set;

public interface AlterationRepository {

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<AlterationCountByGene> getSampleAlterationGeneCounts(Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                              Select<Integer> entrezGeneIds,
                                                              AlterationFilter alterationFilter);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<AlterationCountByGene> getPatientAlterationGeneCounts(Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                               Select<Integer> entrezGeneIds,
                                                               AlterationFilter alterationFilter);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<CopyNumberCountByGene> getSampleCnaGeneCounts(Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                       Select<Integer> entrezGeneIds,
                                                       AlterationFilter alterationFilter);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<CopyNumberCountByGene> getPatientCnaGeneCounts(Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                        Select<Integer> entrezGeneIds,
                                                        AlterationFilter alterationFilter);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<AlterationCountByStructuralVariant> getSampleStructuralVariantCounts(Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                              AlterationFilter alterationFilter);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<AlterationCountByStructuralVariant> getPatientStructuralVariantCounts(Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                               AlterationFilter alterationFilter);
    
}
