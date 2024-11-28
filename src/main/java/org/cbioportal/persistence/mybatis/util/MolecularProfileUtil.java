package org.cbioportal.persistence.util;

import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.persistence.MolecularProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class MolecularProfileUtil {

    @Autowired
    private MolecularProfileRepository molecularProfileRepository;

    public Map<MolecularProfile.MolecularAlterationType, List<MolecularProfileCaseIdentifier>> groupIdentifiersByProfileType(
        Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers, String caseIdentifierType) {

        if (molecularProfileCaseIdentifiers == null || molecularProfileCaseIdentifiers.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<String> molecularProfileIds = molecularProfileCaseIdentifiers.stream()
            .map(MolecularProfileCaseIdentifier::getMolecularProfileId)
            .collect(Collectors.toSet());

        Map<String, MolecularProfile.MolecularAlterationType> profileTypeByProfileId = molecularProfileRepository
            .getMolecularProfiles(molecularProfileIds, "SUMMARY")
            .stream()
            .collect(Collectors.toMap(
                molecularProfile -> molecularProfile.getMolecularProfileId().toString(),
                MolecularProfile::getMolecularAlterationType
            ));

        return molecularProfileCaseIdentifiers.stream()
            .collect(Collectors.groupingBy(
                identifier -> profileTypeByProfileId.getOrDefault(identifier.getMolecularProfileId(), null)
            ));
    }
}
