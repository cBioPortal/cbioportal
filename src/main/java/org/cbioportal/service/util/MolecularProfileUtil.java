package org.cbioportal.service.util;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.springframework.stereotype.Component;

@Component
public class MolecularProfileUtil {

    public final String MUTATION_PROFILE_SUFFIX = "_mutations";
    public final String STRUCTURAL_VARIANT_PROFILE_SUFFIX = "_structural_variants";

    public Predicate<MolecularProfile> isStructuralVariantMolecularProfile =
        m -> m.getMolecularAlterationType().equals(MolecularProfile.MolecularAlterationType.STRUCTURAL_VARIANT);

    public Predicate<MolecularProfile> isDiscreteCNAMolecularProfile =
        m -> m.getMolecularAlterationType().equals(MolecularProfile.MolecularAlterationType.COPY_NUMBER_ALTERATION) && 
            m.getDatatype().equals("DISCRETE");

    public Predicate<MolecularProfile> isMutationProfile =
        m -> m.getMolecularAlterationType().equals(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED);

    public Map<String, List<MolecularProfile>> categorizeMolecularProfilesByStableIdSuffixes(List<MolecularProfile> molecularProfiles) {
        return molecularProfiles
            .stream()
            .collect(Collectors
                .groupingBy(molecularProfile ->
                    molecularProfile.getStableId().replace(molecularProfile.getCancerStudyIdentifier() + "_", "")));
    }

    public List<MolecularProfileCaseIdentifier> getFirstFilteredMolecularProfileCaseIdentifiers(List<MolecularProfile> molecularProfiles,
                                                                                           List<String> studyIds,
                                                                                           List<String> sampleIds,
                                                                                           Optional<Predicate<MolecularProfile>> profileFilter) {
        Map<String, List<MolecularProfile>> mapByStudyId = getFilteredMolecularProfilesByStudyId(molecularProfiles, profileFilter);
        List<MolecularProfileCaseIdentifier> caseIdentifiers = new ArrayList<>();
        for (int i = 0; i < studyIds.size(); i++) {
            String studyId = studyIds.get(i);
            if (mapByStudyId.containsKey(studyId)) {
                // only add identifier for one molecular profile
                caseIdentifiers.add(new MolecularProfileCaseIdentifier(sampleIds.get(i), mapByStudyId.get(studyId).get(0).getStableId()));
            }
        }
        return caseIdentifiers;
    }

    public List<MolecularProfileCaseIdentifier> getFilteredMolecularProfileCaseIdentifiers(List<MolecularProfile> molecularProfiles,
                                                                                           List<String> studyIds,
                                                                                           List<String> sampleIds,
                                                                                           Optional<Predicate<MolecularProfile>> profileFilter) {
        Map<String, List<MolecularProfile>> mapByStudyId = getFilteredMolecularProfilesByStudyId(molecularProfiles, profileFilter);
        List<MolecularProfileCaseIdentifier> caseIdentifiers = new ArrayList<>();
        for (int i = 0; i < studyIds.size(); i++) {
            String studyId = studyIds.get(i);
            if (mapByStudyId.containsKey(studyId)) {
                // add case identifiers for all molecular profiles
                int finalI = i;
                mapByStudyId
                    .getOrDefault(studyId, new ArrayList<>())
                    .forEach(molecularProfile -> {
                        caseIdentifiers.add(new MolecularProfileCaseIdentifier(sampleIds.get(finalI), molecularProfile.getStableId()));
                    });
            }
        }
        return caseIdentifiers;
    }

    private Map<String, List<MolecularProfile>> getFilteredMolecularProfilesByStudyId(List<MolecularProfile> molecularProfiles,
                                                                                      Optional<Predicate<MolecularProfile>> profileFilter) {
        Stream<MolecularProfile> molecularProfileStream = molecularProfiles.stream();
        if (profileFilter.isPresent()) {
            molecularProfileStream = molecularProfileStream.filter(profileFilter.get());
        }
        return molecularProfileStream
            .collect(Collectors.groupingBy(MolecularProfile::getCancerStudyIdentifier));
    }

}
