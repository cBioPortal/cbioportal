package org.cbioportal.service.impl;

import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GeneticProfileRepository;
import org.cbioportal.service.GeneticProfileService;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GeneticProfileServiceImpl implements GeneticProfileService {

    @Autowired
    private GeneticProfileRepository geneticProfileRepository;
    @Autowired
    private StudyService studyService;

    @Override
    @PostFilter("hasPermission(filterObject, 'read')")
    public List<GeneticProfile> getAllGeneticProfiles(String projection, Integer pageSize, Integer pageNumber,
                                                      String sortBy, String direction) {

        return geneticProfileRepository.getAllGeneticProfiles(projection, pageSize, pageNumber, sortBy, direction);
    }

    @Override
    public BaseMeta getMetaGeneticProfiles() {

        return geneticProfileRepository.getMetaGeneticProfiles();
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public GeneticProfile getGeneticProfile(String geneticProfileId) throws GeneticProfileNotFoundException {

        GeneticProfile geneticProfile = geneticProfileRepository.getGeneticProfile(geneticProfileId);
        if (geneticProfile == null) {
            throw new GeneticProfileNotFoundException(geneticProfileId);
        }

        return geneticProfile;
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    public List<GeneticProfile> getAllGeneticProfilesInStudy(String studyId, String projection, Integer pageSize,
                                                             Integer pageNumber, String sortBy, String direction) 
        throws StudyNotFoundException {

        studyService.getStudy(studyId);
        
        return geneticProfileRepository.getAllGeneticProfilesInStudy(studyId, projection, pageSize, pageNumber, sortBy,
                direction);
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    public BaseMeta getMetaGeneticProfilesInStudy(String studyId) throws StudyNotFoundException {

        studyService.getStudy(studyId);
        
        return geneticProfileRepository.getMetaGeneticProfilesInStudy(studyId);
    }

	@Override
	public List<GeneticProfile> getGeneticProfilesReferredBy(String referringGeneticProfileId) throws GeneticProfileNotFoundException {

        //validate (throws exception if profile not found):
        this.getGeneticProfile(referringGeneticProfileId);
        return geneticProfileRepository.getGeneticProfilesReferredBy(referringGeneticProfileId);
	}

	@Override
	public List<GeneticProfile> getGeneticProfilesReferringTo(String referredGeneticProfileId) throws GeneticProfileNotFoundException {

        //validate (throws exception if profile not found):
        this.getGeneticProfile(referredGeneticProfileId);
        return geneticProfileRepository.getGeneticProfilesReferringTo(referredGeneticProfileId);
	}
}
