package org.cbioportal.service.impl;

import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.MolecularProfileRepository;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class MolecularProfileServiceImpl implements MolecularProfileService {

    @Autowired
    @Qualifier("molecularProfileMyBatisRepository")
    private MolecularProfileRepository molecularProfileRepository;
    @Autowired
    @Qualifier("molecularProfileSparkRepository")
    private MolecularProfileRepository molecularProfileSparkRepository;
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
    public MolecularProfile getMolecularProfile(String molecularProfileId) throws MolecularProfileNotFoundException {

        MolecularProfile molecularProfile = molecularProfileRepository.getMolecularProfile(molecularProfileId);
        if (molecularProfile == null) {
            throw new MolecularProfileNotFoundException(molecularProfileId);
        }

        return molecularProfile;
    }

    @Override
	public List<MolecularProfile> getMolecularProfiles(List<String> molecularProfileIds, String projection) {
        
        return molecularProfileRepository.getMolecularProfiles(molecularProfileIds, projection);
    }
    
    @Override
	public BaseMeta getMetaMolecularProfiles(List<String> molecularProfileIds) {
        
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
        
        return molecularProfileSparkRepository.getMolecularProfilesInStudies(studyIds, projection);
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
	public List<String> getFirstMutationProfileIds(List<String> studyIds, List<String> sampleIds) {
        
        List<String> molecularProfileIds = new ArrayList<>();
        Map<String, List<MolecularProfile>> mapByStudyId = getMolecularProfilesInStudies(studyIds, "SUMMARY")
            .stream().filter(m -> m.getMolecularAlterationType().equals(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED))
            .collect(Collectors.groupingBy(MolecularProfile::getCancerStudyIdentifier));
        int removedSampleCount = 0;
        for (int i = 0; i < studyIds.size(); i++) {
            String studyId = studyIds.get(i);
            if (mapByStudyId.containsKey(studyId)) {
                molecularProfileIds.add(mapByStudyId.get(studyId).get(0).getStableId());
            } else {
                sampleIds.remove(i - removedSampleCount);
                removedSampleCount++;
            }
        }
        return molecularProfileIds;
	}

    @Override
	public List<String> getFirstDiscreteCNAProfileIds(List<String> studyIds, List<String> sampleIds) {

        List<String> molecularProfileIds = new ArrayList<>();
        Map<String, List<MolecularProfile>> mapByStudyId = getMolecularProfilesInStudies(studyIds, "SUMMARY")
            .stream().filter(m -> m.getMolecularAlterationType().equals(MolecularProfile.MolecularAlterationType.COPY_NUMBER_ALTERATION) && 
            m.getDatatype().equals("DISCRETE")).collect(Collectors.groupingBy(MolecularProfile::getCancerStudyIdentifier));
        int removedSampleCount = 0;
        for (int i = 0; i < studyIds.size(); i++) {
            String studyId = studyIds.get(i);
            if (mapByStudyId.containsKey(studyId)) {
                molecularProfileIds.add(mapByStudyId.get(studyId).get(0).getStableId());
            } else {
                sampleIds.remove(i - removedSampleCount);
                removedSampleCount++;
            }
        }
        return molecularProfileIds;
	}
}
