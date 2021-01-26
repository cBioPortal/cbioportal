package org.cbioportal.persistence;

import org.cbioportal.model.*;
import org.cbioportal.model.QueryElement;
import org.cbioportal.model.util.Select;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface AlterationRepository {

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    // TODO write javadoc
    List<AlterationCountByGene> getSampleAlterationCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                          Select<Integer> entrezGeneIds,
                                                          final Select<MutationEventType> mutationEventTypes,
                                                          final Select<CNA> cnaEventTypes,
                                                          QueryElement searchFusions,
                                                          boolean includeDriver,
                                                          boolean includeVUS,
                                                          boolean includeUnknownOncogenicity,
                                                          Select<String> selectedTiers,
                                                          boolean includeUnknownTier,
                                                          boolean includeGermline,
                                                          boolean includeSomatic,
                                                          boolean includeUnknownStatus);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    // TODO write javadoc
    List<AlterationCountByGene> getPatientAlterationCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                           Select<Integer> entrezGeneIds,
                                                           final Select<MutationEventType> mutationEventTypes,
                                                           final Select<CNA> cnaEventTypes,
                                                           QueryElement searchFusions,
                                                           boolean includeDriver,
                                                           boolean includeVUS,
                                                           boolean includeUnknownOncogenicity,
                                                           Select<String> selectedTiers,
                                                           boolean includeUnknownTier,
                                                           boolean includeGermline,
                                                           boolean includeSomatic,
                                                           boolean includeUnknownStatus);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<CopyNumberCountByGene> getSampleCnaCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                   Select<Integer> entrezGeneIds,
                                                   final Select<CNA> cnaEventTypes,
                                                   boolean includeDriver,
                                                   boolean includeVUS,
                                                   boolean includeUnknownOncogenicity,
                                                   Select<String> selectedTiers,
                                                   boolean includeUnknownTier);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<CopyNumberCountByGene> getPatientCnaCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                    Select<Integer> entrezGeneIds,
                                                    final Select<CNA> cnaEventTypes,
                                                    boolean includeDriver,
                                                    boolean includeVUS,
                                                    boolean includeUnknownOncogenicity,
                                                    Select<String> selectedTiers,
                                                    boolean includeUnknownTier);
    
}
