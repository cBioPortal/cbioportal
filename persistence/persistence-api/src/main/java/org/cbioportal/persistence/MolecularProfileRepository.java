package org.cbioportal.persistence;

import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface MolecularProfileRepository {

    List<MolecularProfile> getAllMolecularProfiles(String projection, Integer pageSize, Integer pageNumber, 
                                                   String sortBy, String direction);

    BaseMeta getMetaMolecularProfiles();

    MolecularProfile getMolecularProfile(String molecularProfileId);

    List<MolecularProfile> getMolecularProfiles(List<String> molecularProfileIds, String projection);

    BaseMeta getMetaMolecularProfiles(List<String> molecularProfileIds);

    List<MolecularProfile> getAllMolecularProfilesInStudy(String studyId, String projection, Integer pageSize,
                                                          Integer pageNumber, String sortBy, String direction);

    BaseMeta getMetaMolecularProfilesInStudy(String studyId);

    List<MolecularProfile> getMolecularProfilesInStudies(List<String> studyIds, String projection);
    
    BaseMeta getMetaMolecularProfilesInStudies(List<String> studyIds);

	List<MolecularProfile> getMolecularProfilesReferredBy(String referringMolecularProfileId);

	List<MolecularProfile> getMolecularProfilesReferringTo(String referredMolecularProfileId);
}
