package org.cbioportal.service.impl;

import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.MolecularProfileRepository;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.service.util.MolecularProfileUtil;
import org.cbioportal.web.parameter.Projection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

@Service
public class MolecularProfileServiceImpl implements MolecularProfileService {

    @Autowired
    private MolecularProfileRepository molecularProfileRepository;
    @Autowired
    private StudyService studyService;
    @Autowired
    private MolecularProfileUtil molecularProfileUtil;
    @Value("${authenticate:false}")
    private String AUTHENTICATE;

    @Override
    @PostFilter("hasPermission(filterObject, T(org.cbioportal.utils.security.AccessLevel).READ)")
    public List<MolecularProfile> getAllMolecularProfiles(String projection, Integer pageSize, Integer pageNumber,
                                                          String sortBy, String direction) {

        List<MolecularProfile> molecularProfiles = molecularProfileRepository.getAllMolecularProfiles(projection, pageSize, pageNumber, sortBy, direction);
        // copy the list before returning so @PostFilter doesn't taint the list stored in the persistence layer cache
        return (AUTHENTICATE.equals("false")) ? molecularProfiles : new ArrayList<MolecularProfile>(molecularProfiles);
    }

    @Override
    public BaseMeta getMetaMolecularProfiles() {

        return molecularProfileRepository.getMetaMolecularProfiles();
    }

    @Override
    public MolecularProfile getMolecularProfile(String molecularProfileId) throws MolecularProfileNotFoundException {

        MolecularProfile molecularProfile = molecularProfileRepository.getMolecularProfile(molecularProfileId);
        if (molecularProfile == null) {
            throw new MolecularProfileNotFoundException(molecularProfileId);
        }

        return molecularProfile;
    }

    @Override
	public List<MolecularProfile> getMolecularProfiles(Set<String> molecularProfileIds, String projection) {
        
        return molecularProfileRepository.getMolecularProfiles(molecularProfileIds, projection);
    }
    
    @Override
	public BaseMeta getMetaMolecularProfiles(Set<String> molecularProfileIds) {
        
        return molecularProfileRepository.getMetaMolecularProfiles(molecularProfileIds);
	}

    @Override
    public List<MolecularProfile> getAllMolecularProfilesInStudy(String studyId, String projection, Integer pageSize,
                                                                 Integer pageNumber, String sortBy, String direction) 
        throws StudyNotFoundException {

        studyService.getStudy(studyId);
        
        return molecularProfileRepository.getAllMolecularProfilesInStudy(studyId, projection, pageSize, pageNumber, 
            sortBy, direction);
    }

    @Override
    public BaseMeta getMetaMolecularProfilesInStudy(String studyId) throws StudyNotFoundException {

        studyService.getStudy(studyId);
        
        return molecularProfileRepository.getMetaMolecularProfilesInStudy(studyId);
    }

    @Override
	public List<MolecularProfile> getMolecularProfilesInStudies(List<String> studyIds, String projection) {
        
        return molecularProfileRepository.getMolecularProfilesInStudies(studyIds, projection);
	}

    @Override
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

    @Override
    public List<MolecularProfileCaseIdentifier> getMolecularProfileCaseIdentifiers(List<String> studyIds, List<String> sampleIds) {
        return getFilteredMolecularProfileCaseIdentifiers(studyIds, sampleIds, Optional.empty());
    }

    @Override
	public List<MolecularProfileCaseIdentifier> getFirstMutationProfileCaseIdentifiers(List<String> studyIds, List<String> sampleIds) {
        return getFirstFilteredMolecularProfileCaseIdentifiers(studyIds, sampleIds, Optional.of(molecularProfileUtil.isMutationProfile));
	}

    @Override
    public List<MolecularProfileCaseIdentifier> getMutationProfileCaseIdentifiers(List<String> studyIds, List<String> sampleIds) {
        return getFilteredMolecularProfileCaseIdentifiers(studyIds, sampleIds, Optional.of(molecularProfileUtil.isMutationProfile));
    }

	@Override
	public List<MolecularProfileCaseIdentifier> getFirstDiscreteCNAProfileCaseIdentifiers(List<String> studyIds, List<String> sampleIds) {
        return getFilteredMolecularProfileCaseIdentifiers(studyIds, sampleIds, Optional.of(molecularProfileUtil.isDiscreteCNAMolecularProfile));
	}

    @Override
    public List<MolecularProfileCaseIdentifier> getFirstStructuralVariantProfileCaseIdentifiers(List<String> studyIds, List<String> sampleIds) {
        return getFirstFilteredMolecularProfileCaseIdentifiers(studyIds, sampleIds, Optional.of(molecularProfileUtil.isStructuralVariantMolecularProfile));
    }

    private List<MolecularProfileCaseIdentifier> getFirstFilteredMolecularProfileCaseIdentifiers(List<String> studyIds, List<String> sampleIds, Optional<Predicate<MolecularProfile>> profileFilter) {
        List<MolecularProfile> molecularProfiles =
            getMolecularProfilesInStudies(studyIds.stream().distinct().toList(), Projection.SUMMARY.name());
        return molecularProfileUtil.getFirstFilteredMolecularProfileCaseIdentifiers(molecularProfiles, studyIds, sampleIds, profileFilter);
    }

    private List<MolecularProfileCaseIdentifier> getFilteredMolecularProfileCaseIdentifiers(List<String> studyIds, List<String> sampleIds, Optional<Predicate<MolecularProfile>> profileFilter) {
        List<MolecularProfile> molecularProfiles =
            getMolecularProfilesInStudies(studyIds.stream().distinct().toList(), Projection.SUMMARY.name());
        return molecularProfileUtil.getFilteredMolecularProfileCaseIdentifiers(molecularProfiles, studyIds, sampleIds, profileFilter);
    }
}
