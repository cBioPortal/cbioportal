package org.cbioportal.legacy.service;

import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.MolecularProfileCaseIdentifier;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;

import java.util.List;
import java.util.Set;

public interface MolecularProfileService {

    List<MolecularProfile> getAllMolecularProfiles(String projection, Integer pageSize, Integer pageNumber, 
                                                   String sortBy, String direction);

    BaseMeta getMetaMolecularProfiles();

    MolecularProfile getMolecularProfile(String molecularProfileId) throws MolecularProfileNotFoundException;

    List<MolecularProfile> getMolecularProfiles(Set<String> molecularProfileIds, String projection);

    BaseMeta getMetaMolecularProfiles(Set<String> molecularProfileIds);

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

    List<MolecularProfileCaseIdentifier> getMolecularProfileCaseIdentifiers(List<String> studyIds, List<String> sampleIds);
    List<MolecularProfileCaseIdentifier> getFirstMutationProfileCaseIdentifiers(List<String> studyIds, List<String> sampleIds);
    List<MolecularProfileCaseIdentifier> getMutationProfileCaseIdentifiers(List<String> studyIds, List<String> sampleIds);
    List<MolecularProfileCaseIdentifier> getFirstDiscreteCNAProfileCaseIdentifiers(List<String> studyIds, List<String> sampleIds);
    List<MolecularProfileCaseIdentifier> getFirstStructuralVariantProfileCaseIdentifiers(List<String> studyIds, List<String> sampleIds);
}
