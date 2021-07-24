package org.cbioportal.service.util;

import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class MolecularProfileUtil {

    public final String MUTATION_PROFILE_SUFFIX = "_mutations";
    public final String FUSION_PROFILE_SUFFIX = "_fusion";
    public final String STRUCTURAL_VARIANT_PROFILE_SUFFIX = "_structural_variants";
    public final String FUSIONS_AS_MUTATIONS_DATATYPE = "FUSION";

    // TODO: Remove once fusions are removed from mutation table
    public Predicate<MolecularProfile> isStructuralVariantMolecularProfile =
        m -> m.getMolecularAlterationType().equals(MolecularProfile.MolecularAlterationType.FUSION) ||
            m.getMolecularAlterationType().equals(MolecularProfile.MolecularAlterationType.STRUCTURAL_VARIANT) || 
                (m.getMolecularAlterationType().equals(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED) 
                        && m.getDatatype().equals(FUSIONS_AS_MUTATIONS_DATATYPE));

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

    public List<MolecularProfileCaseIdentifier> getFilteredMolecularProfileCaseIdentifiers(List<MolecularProfile> molecularProfiles,
                                                                                           List<String> studyIds,
                                                                                           List<String> sampleIds,
                                                                                           Optional<Predicate<MolecularProfile>> profileFilter) {
        Map<String, List<MolecularProfile>> mapByStudyId = getFilteredMolecularProfilesByStudyId(molecularProfiles, profileFilter);
        List<MolecularProfileCaseIdentifier> caseIdentifiers = new ArrayList<>();
        for (int i = 0; i < studyIds.size(); i++) {
            String studyId = studyIds.get(i);
            if (mapByStudyId.containsKey(studyId)) {
                if (profileFilter.isPresent()) {
                    // only add identifier for one molecular profile
                    caseIdentifiers.add(new MolecularProfileCaseIdentifier(sampleIds.get(i), mapByStudyId.get(studyId).get(0).getStableId()));
                } else {
                    // add case identifiers for all molecular profiles
                    int finalI = i;
                    mapByStudyId
                        .getOrDefault(studyId, new ArrayList<>())
                        .forEach(molecularProfile -> {
                            caseIdentifiers.add(new MolecularProfileCaseIdentifier(sampleIds.get(finalI), molecularProfile.getStableId()));
                        });
                }
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
        Map<String, List<MolecularProfile>> studyMolecularProfilesSet = molecularProfileStream
            .collect(Collectors.groupingBy(MolecularProfile::getCancerStudyIdentifier))
            .entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    List<MolecularProfile> profilesToReturn = new ArrayList<>();
                    MolecularProfile structuralVariantProfile = null;
                    for (MolecularProfile molecularProfile : entry.getValue()) {
                        if (molecularProfile.getMolecularAlterationType().equals(MolecularProfile.MolecularAlterationType.FUSION)
                            || molecularProfile.getMolecularAlterationType()
                            .equals(MolecularProfile.MolecularAlterationType.STRUCTURAL_VARIANT)) {
                            if (structuralVariantProfile == null) {
                                structuralVariantProfile = molecularProfile;
                            } else if (!(molecularProfile.getMolecularAlterationType()
                                .equals(MolecularProfile.MolecularAlterationType.STRUCTURAL_VARIANT)
                                && molecularProfile.getDatatype().equals("SV"))) {
                                // replace structural variant profile with
                                // mutation profile having fusion data
                                structuralVariantProfile = molecularProfile;
                            }
                        } else {
                            profilesToReturn.add(molecularProfile);
                        }
                    }

                    if (structuralVariantProfile != null) {
                        profilesToReturn.add(structuralVariantProfile);
                    }

                    return profilesToReturn;
                }));
        return studyMolecularProfilesSet;
    }

    public String replaceFusionProfileWithMutationProfile(String profileId) {
        return profileId.replace(FUSION_PROFILE_SUFFIX, MUTATION_PROFILE_SUFFIX);
    }

}
