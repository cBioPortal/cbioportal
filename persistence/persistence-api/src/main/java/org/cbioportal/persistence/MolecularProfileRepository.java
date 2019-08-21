package org.cbioportal.persistence;

import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface MolecularProfileRepository {

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    List<MolecularProfile> getAllMolecularProfiles(String projection, Integer pageSize, Integer pageNumber, 
                                                   String sortBy, String direction);

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    BaseMeta getMetaMolecularProfiles();

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    MolecularProfile getMolecularProfile(String molecularProfileId);

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    List<MolecularProfile> getMolecularProfiles(List<String> molecularProfileIds, String projection);

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    BaseMeta getMetaMolecularProfiles(List<String> molecularProfileIds);

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    List<MolecularProfile> getAllMolecularProfilesInStudy(String studyId, String projection, Integer pageSize,
                                                          Integer pageNumber, String sortBy, String direction);

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    BaseMeta getMetaMolecularProfilesInStudy(String studyId);

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    List<MolecularProfile> getMolecularProfilesInStudies(List<String> studyIds, String projection);

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    BaseMeta getMetaMolecularProfilesInStudies(List<String> studyIds);

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    List<MolecularProfile> getMolecularProfilesReferredBy(String referringMolecularProfileId);

    @Cacheable(cacheNames = "GeneralRepositoryCache", condition = "@cacheEnabledConfig.getEnabled()")
    List<MolecularProfile> getMolecularProfilesReferringTo(String referredMolecularProfileId);
}
