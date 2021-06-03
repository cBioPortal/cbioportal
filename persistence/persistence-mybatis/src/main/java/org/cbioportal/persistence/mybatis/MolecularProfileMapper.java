package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;
import java.util.Set;

public interface MolecularProfileMapper {

    List<MolecularProfile> getAllMolecularProfilesInStudies(List<String> studyIds, String projection, Integer limit, Integer offset,
                                                   String sortBy, String direction);

    BaseMeta getMetaMolecularProfilesInStudies(List<String> studyIds);

    MolecularProfile getMolecularProfile(String molecularProfileId, String projection);

    List<MolecularProfile> getMolecularProfiles(Set<String> molecularProfileIds, String projection);

    BaseMeta getMetaMolecularProfiles(Set<String> molecularProfileIds);

	List<MolecularProfile> getMolecularProfilesReferredBy(String referringMolecularProfileId, String projection);

	List<MolecularProfile> getMolecularProfilesReferringTo(String referredMolecularProfileId, String projection);
}
