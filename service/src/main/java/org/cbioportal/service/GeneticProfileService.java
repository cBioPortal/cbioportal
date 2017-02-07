package org.cbioportal.service;

import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;

import java.util.List;

public interface GeneticProfileService {

    List<GeneticProfile> getAllGeneticProfiles(String projection, Integer pageSize, Integer pageNumber, String sortBy,
                                               String direction);

    BaseMeta getMetaGeneticProfiles();

    GeneticProfile getGeneticProfile(String geneticProfileId) throws GeneticProfileNotFoundException;

    List<GeneticProfile> getAllGeneticProfilesInStudy(String studyId, String projection, Integer pageSize,
                                                      Integer pageNumber, String sortBy, String direction);

    BaseMeta getMetaGeneticProfilesInStudy(String studyId);

	List<GeneticProfile> getGeneticProfilesReferredBy(String referringGeneticProfileId);
}
