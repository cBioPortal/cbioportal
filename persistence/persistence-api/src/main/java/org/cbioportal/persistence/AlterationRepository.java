package org.cbioportal.persistence;

import org.cbioportal.model.*;
import org.cbioportal.model.QueryElement;
import org.cbioportal.model.util.Select;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Set;

public interface AlterationRepository {

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<AlterationCountByGene> getSampleAlterationCounts(Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                          Select<Integer> entrezGeneIds,
                                                          QueryElement searchFusions,
                                                          AlterationFilter alterationFilter);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<AlterationCountByGene> getPatientAlterationCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                           Select<Integer> entrezGeneIds,
                                                           QueryElement searchFusions,
                                                           AlterationFilter alterationFilter);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<CopyNumberCountByGene> getSampleCnaCounts(Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                   Select<Integer> entrezGeneIds,
                                                   AlterationFilter alterationFilter);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<CopyNumberCountByGene> getPatientCnaCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                    Select<Integer> entrezGeneIds,
                                                    AlterationFilter alterationFilter);
    
}
