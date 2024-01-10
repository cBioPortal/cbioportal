package org.cbioportal.persistence;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cbioportal.model.GeneMolecularAlteration;
import org.cbioportal.model.GenericAssayMolecularAlteration;
import org.cbioportal.model.GenesetMolecularAlteration;
import org.springframework.cache.annotation.Cacheable;
import org.cbioportal.model.MolecularProfileSamples;

public interface MolecularDataRepository {

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    MolecularProfileSamples getCommaSeparatedSampleIdsOfMolecularProfile(String molecularProfileId);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    Map<String, MolecularProfileSamples> commaSeparatedSampleIdsOfMolecularProfilesMap(Set<String> molecularProfileIds);

    // Not caching when entrezGeneIds is null or empty because the large response size sometimes crashes the cache
    @Cacheable(cacheResolver = "generalRepositoryCacheResolver",
               condition = "@cacheEnabledConfig.getEnabled() && #entrezGeneIds != null && #entrezGeneIds.size() != 0")
    List<GeneMolecularAlteration> getGeneMolecularAlterations(String molecularProfileId, List<Integer> entrezGeneIds,
                                                              String projection);

    Iterable<GeneMolecularAlteration> getGeneMolecularAlterationsIterable(String molecularProfileId, List<Integer> entrezGeneIds,
                                                                          String projection);

    // Same as getGeneMolecularAlterationsIterable above, except assumes that
    // entrezGeneIds is null or empty AND projection is "SUMMARY"
    Iterable<GeneMolecularAlteration> getGeneMolecularAlterationsIterableFast(String molecularProfileId);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<GeneMolecularAlteration> getGeneMolecularAlterationsInMultipleMolecularProfiles(Set<String> molecularProfileIds,
                                                                                         List<Integer> entrezGeneIds,
                                                                                         String projection);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<GenesetMolecularAlteration> getGenesetMolecularAlterations(String molecularProfileId, List<String> genesetIds,
                                                                    String projection);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    List<GenericAssayMolecularAlteration> getGenericAssayMolecularAlterations(String molecularProfileId, List<String> stableIds,
        String projection);

	Iterable<GenericAssayMolecularAlteration> getGenericAssayMolecularAlterationsIterable(String molecularProfileId,
			List<String> stableIds, String projection);

}
