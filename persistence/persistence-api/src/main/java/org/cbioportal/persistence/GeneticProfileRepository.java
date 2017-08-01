package org.cbioportal.persistence;

import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface GeneticProfileRepository {

    List<GeneticProfile> getAllGeneticProfiles(String projection, Integer pageSize, Integer pageNumber, String sortBy,
                                               String direction);

    BaseMeta getMetaGeneticProfiles();

    GeneticProfile getGeneticProfile(String geneticProfileId);

    List<GeneticProfile> getAllGeneticProfilesInStudy(String studyId, String projection, Integer pageSize,
                                                      Integer pageNumber, String sortBy, String direction);

    BaseMeta getMetaGeneticProfilesInStudy(String studyId);

	List<GeneticProfile> getGeneticProfilesReferredBy(String referringGeneticProfileId);

	List<GeneticProfile> getGeneticProfilesReferringTo(String referredGeneticProfileId);
}
