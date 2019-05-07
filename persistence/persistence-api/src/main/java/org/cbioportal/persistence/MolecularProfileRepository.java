package org.cbioportal.persistence;

import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface MolecularProfileRepository {

    @Cacheable("RepositoryCache")
    List<MolecularProfile> getAllMolecularProfiles(String projection, Integer pageSize, Integer pageNumber, 
                                                   String sortBy, String direction);

    @Cacheable("RepositoryCache")
    BaseMeta getMetaMolecularProfiles();

    @Cacheable("RepositoryCache")
    MolecularProfile getMolecularProfile(String molecularProfileId);

    @Cacheable("RepositoryCache")
    List<MolecularProfile> getMolecularProfiles(List<String> molecularProfileIds, String projection);

    @Cacheable("RepositoryCache")
    BaseMeta getMetaMolecularProfiles(List<String> molecularProfileIds);

    @Cacheable("RepositoryCache")
    List<MolecularProfile> getAllMolecularProfilesInStudy(String studyId, String projection, Integer pageSize,
                                                          Integer pageNumber, String sortBy, String direction);

    @Cacheable("RepositoryCache")
    BaseMeta getMetaMolecularProfilesInStudy(String studyId);

    @Cacheable("RepositoryCache")
    List<MolecularProfile> getMolecularProfilesInStudies(List<String> studyIds, String projection);

    @Cacheable("RepositoryCache")
    BaseMeta getMetaMolecularProfilesInStudies(List<String> studyIds);

    @Cacheable("RepositoryCache")
    List<MolecularProfile> getMolecularProfilesReferredBy(String referringMolecularProfileId);

    @Cacheable("RepositoryCache")
    List<MolecularProfile> getMolecularProfilesReferringTo(String referredMolecularProfileId);
}
