package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface MolecularProfileMapper {

    List<MolecularProfile> getAllMolecularProfiles(String studyId, String projection, Integer limit, Integer offset,
                                                   String sortBy, String direction);

    BaseMeta getMetaMolecularProfiles(String studyId);

    MolecularProfile getMolecularProfile(String molecularProfileId, String projection);

	List<MolecularProfile> getMolecularProfilesReferredBy(String referringMolecularProfileId, String projection);

	List<MolecularProfile> getMolecularProfilesReferringTo(String referredMolecularProfileId, String projection);
}
