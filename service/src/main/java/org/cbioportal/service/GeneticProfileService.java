package org.cbioportal.service;

import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;

import java.util.List;

public interface GeneticProfileService {

    List<GeneticProfile> getAllGeneticProfiles(String projection, Integer pageSize, Integer pageNumber, String sortBy,
                                               String direction);

    BaseMeta getMetaGeneticProfiles();

    GeneticProfile getGeneticProfile(String geneticProfileId) throws GeneticProfileNotFoundException;

    List<GeneticProfile> getAllGeneticProfilesInStudy(String studyId, String projection, Integer pageSize,
                                                      Integer pageNumber, String sortBy, String direction) throws StudyNotFoundException;

    BaseMeta getMetaGeneticProfilesInStudy(String studyId) throws StudyNotFoundException;

	List<GeneticProfile> getGeneticProfilesReferredBy(String referringGeneticProfileId) throws GeneticProfileNotFoundException;
	
	List<GeneticProfile> getGeneticProfilesReferringTo(String referredGeneticProfileId) throws GeneticProfileNotFoundException;
}
