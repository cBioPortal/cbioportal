package org.cbioportal.service;

import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;

import java.util.List;

public interface MolecularProfileService {

    List<MolecularProfile> getAllMolecularProfiles(String projection, Integer pageSize, Integer pageNumber, 
                                                   String sortBy, String direction);

    BaseMeta getMetaMolecularProfiles();

    MolecularProfile getMolecularProfile(String molecularProfileId) throws MolecularProfileNotFoundException;

    List<MolecularProfile> getMolecularProfiles(List<String> molecularProfileIds, String projection);

    BaseMeta getMetaMolecularProfiles(List<String> molecularProfileIds);

    List<MolecularProfile> getAllMolecularProfilesInStudy(String studyId, String projection, Integer pageSize,
                                                          Integer pageNumber, String sortBy, String direction) 
        throws StudyNotFoundException;

    BaseMeta getMetaMolecularProfilesInStudy(String studyId) throws StudyNotFoundException;

    List<MolecularProfile> getMolecularProfilesInStudies(List<String> studyIds, String projection);

    BaseMeta getMetaMolecularProfilesInStudies(List<String> studyIds);

    List<MolecularProfile> getMolecularProfilesReferredBy(String referringMolecularProfileId) 
        throws MolecularProfileNotFoundException;

    List<MolecularProfile> getMolecularProfilesReferringTo(String referredMolecularProfileId) 
        throws MolecularProfileNotFoundException;

    List<String> getFirstMutationProfileIds(List<String> studyIds, List<String> sampleIds);
    List<String> getFirstDiscreteCNAProfileIds(List<String> studyIds, List<String> sampleIds);
}
