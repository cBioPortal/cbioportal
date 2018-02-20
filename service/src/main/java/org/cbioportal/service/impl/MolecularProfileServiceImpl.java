package org.cbioportal.service.impl;

import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.MolecularProfileRepository;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;

@Service
public class MolecularProfileServiceImpl implements MolecularProfileService {

    @Autowired
    private MolecularProfileRepository molecularProfileRepository;
    @Autowired
    private StudyService studyService;
    @Value("${authenticate:false}")
    private String AUTHENTICATE;

    @Override
    @PostFilter("hasPermission(filterObject, 'read')")
    public List<MolecularProfile> getAllMolecularProfiles(String projection, Integer pageSize, Integer pageNumber,
                                                          String sortBy, String direction) {

        List<MolecularProfile> molecularProfiles = molecularProfileRepository.getAllMolecularProfiles(projection, pageSize, pageNumber, sortBy, direction);
        // copy the list before returning so @PostFilter doesn't taint the list stored in the mybatis second-level cache
        return (AUTHENTICATE.equals("false")) ? molecularProfiles : new ArrayList<MolecularProfile>(molecularProfiles);
    }

    @Override
    public BaseMeta getMetaMolecularProfiles() {

        return molecularProfileRepository.getMetaMolecularProfiles();
    }

    @Override
    @PreAuthorize("hasPermission(#molecularProfileId, 'MolecularProfile', 'read')")
    public MolecularProfile getMolecularProfile(String molecularProfileId) throws MolecularProfileNotFoundException {

        MolecularProfile molecularProfile = molecularProfileRepository.getMolecularProfile(molecularProfileId);
        if (molecularProfile == null) {
            throw new MolecularProfileNotFoundException(molecularProfileId);
        }

        return molecularProfile;
    }

    @Override
    @PreAuthorize("hasPermission(#molecularProfileIds, 'List<MolecularProfileId>', 'read')")
	public List<MolecularProfile> getMolecularProfiles(List<String> molecularProfileIds, String projection) {
        
        return molecularProfileRepository.getMolecularProfiles(molecularProfileIds, projection);
    }
    
    @Override
    @PreAuthorize("hasPermission(#molecularProfileIds, 'List<MolecularProfileId>', 'read')")
	public BaseMeta getMetaMolecularProfiles(List<String> molecularProfileIds) {
        
        return molecularProfileRepository.getMetaMolecularProfiles(molecularProfileIds);
	}

    @Override
    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    public List<MolecularProfile> getAllMolecularProfilesInStudy(String studyId, String projection, Integer pageSize,
                                                                 Integer pageNumber, String sortBy, String direction) 
        throws StudyNotFoundException {

        studyService.getStudy(studyId);
        
        return molecularProfileRepository.getAllMolecularProfilesInStudy(studyId, projection, pageSize, pageNumber, 
            sortBy, direction);
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    public BaseMeta getMetaMolecularProfilesInStudy(String studyId) throws StudyNotFoundException {

        studyService.getStudy(studyId);
        
        return molecularProfileRepository.getMetaMolecularProfilesInStudy(studyId);
    }

    @Override
    @PreAuthorize("hasPermission(#studyIds, 'List<CancerStudyId>', 'read')")
	public List<MolecularProfile> getMolecularProfilesInStudies(List<String> studyIds, String projection) {
        
        return molecularProfileRepository.getMolecularProfilesInStudies(studyIds, projection);
	}

    @Override
    @PreAuthorize("hasPermission(#studyIds, 'List<CancerStudyId>', 'read')")
	public BaseMeta getMetaMolecularProfilesInStudies(List<String> studyIds) {
        
        return molecularProfileRepository.getMetaMolecularProfilesInStudies(studyIds);
	}

	@Override
	public List<MolecularProfile> getMolecularProfilesReferredBy(String referringMolecularProfileId) 
        throws MolecularProfileNotFoundException {

        //validate (throws exception if profile not found):
        this.getMolecularProfile(referringMolecularProfileId);
        return molecularProfileRepository.getMolecularProfilesReferredBy(referringMolecularProfileId);
	}

	@Override
	public List<MolecularProfile> getMolecularProfilesReferringTo(String referredMolecularProfileId) 
        throws MolecularProfileNotFoundException {

        //validate (throws exception if profile not found):
        this.getMolecularProfile(referredMolecularProfileId);
        return molecularProfileRepository.getMolecularProfilesReferringTo(referredMolecularProfileId);
	}
}
