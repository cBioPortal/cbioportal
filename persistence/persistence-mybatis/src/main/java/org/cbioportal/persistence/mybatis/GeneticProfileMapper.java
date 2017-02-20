package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface GeneticProfileMapper {

    List<GeneticProfile> getAllGeneticProfiles(String studyId, String projection, Integer limit, Integer offset, 
                                               String sortBy, String direction);

    BaseMeta getMetaGeneticProfiles(String studyId);

    GeneticProfile getGeneticProfile(String geneticProfileId, String projection);

	List<GeneticProfile> getGeneticProfilesReferredBy(String referringGeneticProfileId, String projection);

	List<GeneticProfile> getGeneticProfilesReferringTo(String referredGeneticProfileId, String projection);
}
