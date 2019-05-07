package org.cbioportal.persistence;

import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface MolecularProfileRepository {

    @Cacheable("GeneralRepositoryCache")
    List<MolecularProfile> getAllMolecularProfiles(String projection, Integer pageSize, Integer pageNumber, 
                                                   String sortBy, String direction);

    @Cacheable("GeneralRepositoryCache")
    BaseMeta getMetaMolecularProfiles();

    @Cacheable("GeneralRepositoryCache")
    MolecularProfile getMolecularProfile(String molecularProfileId);

    @Cacheable("GeneralRepositoryCache")
    List<MolecularProfile> getMolecularProfiles(List<String> molecularProfileIds, String projection);

    @Cacheable("GeneralRepositoryCache")
    BaseMeta getMetaMolecularProfiles(List<String> molecularProfileIds);

    @Cacheable("GeneralRepositoryCache")
    List<MolecularProfile> getAllMolecularProfilesInStudy(String studyId, String projection, Integer pageSize,
                                                          Integer pageNumber, String sortBy, String direction);

    @Cacheable("GeneralRepositoryCache")
    BaseMeta getMetaMolecularProfilesInStudy(String studyId);

    @Cacheable("GeneralRepositoryCache")
    List<MolecularProfile> getMolecularProfilesInStudies(List<String> studyIds, String projection);

    @Cacheable("GeneralRepositoryCache")
    BaseMeta getMetaMolecularProfilesInStudies(List<String> studyIds);

    @Cacheable("GeneralRepositoryCache")
    List<MolecularProfile> getMolecularProfilesReferredBy(String referringMolecularProfileId);

    @Cacheable("GeneralRepositoryCache")
    List<MolecularProfile> getMolecularProfilesReferringTo(String referredMolecularProfileId);
}
