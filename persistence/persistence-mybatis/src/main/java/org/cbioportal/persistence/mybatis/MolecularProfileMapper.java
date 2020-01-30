package org.cbioportal.persistence.mybatis;

import java.util.List;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.meta.BaseMeta;

public interface MolecularProfileMapper {
    List<MolecularProfile> getAllMolecularProfilesInStudies(
        List<String> studyIds,
        String projection,
        Integer limit,
        Integer offset,
        String sortBy,
        String direction
    );

    BaseMeta getMetaMolecularProfilesInStudies(List<String> studyIds);

    MolecularProfile getMolecularProfile(
        String molecularProfileId,
        String projection
    );

    List<MolecularProfile> getMolecularProfiles(
        List<String> molecularProfileIds,
        String projection
    );

    BaseMeta getMetaMolecularProfiles(List<String> molecularProfileIds);

    List<MolecularProfile> getMolecularProfilesReferredBy(
        String referringMolecularProfileId,
        String projection
    );

    List<MolecularProfile> getMolecularProfilesReferringTo(
        String referredMolecularProfileId,
        String projection
    );
}
