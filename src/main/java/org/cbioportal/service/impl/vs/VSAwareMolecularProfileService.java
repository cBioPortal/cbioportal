package org.cbioportal.service.impl.vs;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.service.util.MolecularProfileUtil;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.VirtualStudySamples;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VSAwareMolecularProfileService implements MolecularProfileService {

    private final MolecularProfileService molecularProfileService;
    private final PublishedVirtualStudyService publishedVirtualStudyService;
    private final MolecularProfileUtil molecularProfileUtil;

    public VSAwareMolecularProfileService(MolecularProfileService molecularProfileService, PublishedVirtualStudyService publishedVirtualStudyService, MolecularProfileUtil molecularProfileUtil) {
        this.molecularProfileService = molecularProfileService;
        this.publishedVirtualStudyService = publishedVirtualStudyService;
        this.molecularProfileUtil = molecularProfileUtil;
    }

    @Override
    public List<MolecularProfile> getAllMolecularProfiles(String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
        List<MolecularProfile> molecularProfiles = molecularProfileService.getAllMolecularProfiles(projection, pageSize, pageNumber, sortBy, direction);
        List<MolecularProfile> virtualMolecularProfiles = publishedVirtualStudyService.getAllVirtualMolecularProfiles(molecularProfiles); 

        Stream<MolecularProfile> resultStream = Stream.concat(
                molecularProfiles.stream(),
                virtualMolecularProfiles.stream()
        );

        if (sortBy != null) {
            resultStream = resultStream.sorted(buildComparator(sortBy, direction));
        }

        if (pageSize != null && pageNumber != null) {
            resultStream = resultStream.skip((long) pageSize * pageNumber).limit(pageSize);
        }

        return resultStream.toList(); 
    }

    private static Comparator<MolecularProfile> buildComparator(String sortBy, String direction) {
        Function<MolecularProfile, Comparable> getValue;
        //TODO add more fields to sort by
        if (sortBy.equalsIgnoreCase("name")) {
            getValue = MolecularProfile::getMolecularProfileId;
        } else {
            throw new IllegalArgumentException("Invalid sortBy value: " + sortBy);
        }
        if (direction != null && direction.equalsIgnoreCase("desc")) {
            return Comparator.comparing(getValue).reversed();
        }
        if (direction != null && direction.equalsIgnoreCase("asc")) {
            return Comparator.comparing(getValue);
        }
        throw new IllegalArgumentException("Invalid direction value: " + direction);
    }

    @Override
    public BaseMeta getMetaMolecularProfiles() {
        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(getAllMolecularProfiles(PersistenceConstants.ID_PROJECTION, null, null, null, null).size());
        return baseMeta;
    }

    @Override
    public MolecularProfile getMolecularProfile(String molecularProfileId) throws MolecularProfileNotFoundException {
        return getAllMolecularProfiles(PersistenceConstants.DETAILED_PROJECTION, null, null, null, null).stream()
                .filter(molecularProfile -> molecularProfile.getStableId().equals(molecularProfileId))
                .findFirst()
                .orElseThrow(() -> new MolecularProfileNotFoundException(molecularProfileId));
    }

    @Override
    public List<MolecularProfile> getMolecularProfiles(Set<String> molecularProfileIds, String projection) {
        return getAllMolecularProfiles(projection, null, null, null, null).stream()
                .filter(molecularProfile -> molecularProfileIds.contains(molecularProfile.getStableId()))
                .toList();
    }

    @Override
    public BaseMeta getMetaMolecularProfiles(Set<String> molecularProfileIds) {
        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(getAllMolecularProfiles(PersistenceConstants.ID_PROJECTION, null, null, null, null)
                .stream().filter(mp -> molecularProfileIds.contains(mp.getStableId())).toList().size());
        return baseMeta;
    }

    @Override
    public List<MolecularProfile> getAllMolecularProfilesInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
        return getAllMolecularProfiles(projection, pageSize, pageNumber, sortBy, direction).stream()
                .filter(molecularProfile -> molecularProfile.getCancerStudyIdentifier().equals(studyId))
                .toList();
    }

    @Override
    public BaseMeta getMetaMolecularProfilesInStudy(String studyId) {
        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(getAllMolecularProfilesInStudy(studyId, PersistenceConstants.ID_PROJECTION, null, null, null, null).size());
        return baseMeta;
    }

    @Override
    public List<MolecularProfile> getMolecularProfilesInStudies(List<String> studyIds, String projection) {
        return getAllMolecularProfiles(projection, null, null, null, null).stream()
                .filter(molecularProfile -> studyIds.contains(molecularProfile.getCancerStudyIdentifier()))
                .toList();
    }

    @Override
    public BaseMeta getMetaMolecularProfilesInStudies(List<String> studyIds) {
        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(getMolecularProfilesInStudies(studyIds, PersistenceConstants.ID_PROJECTION).size());
        return baseMeta;
    }

    @Override
    public List<MolecularProfile> getMolecularProfilesReferredBy(String referringMolecularProfileId) throws MolecularProfileNotFoundException {
        //TODO not supported for virtual molecular profiles?
        return molecularProfileService.getMolecularProfilesReferredBy(referringMolecularProfileId);
    }

    @Override
    public List<MolecularProfile> getMolecularProfilesReferringTo(String referredMolecularProfileId) throws MolecularProfileNotFoundException {
        //TODO not supported for virtual molecular profiles?
        return molecularProfileService.getMolecularProfilesReferringTo(referredMolecularProfileId);
    }

    @Override
    public List<MolecularProfileCaseIdentifier> getMolecularProfileCaseIdentifiers(List<String> studyIds, List<String> sampleIds) {
        return getFilteredMolecularProfileCaseIdentifiers(studyIds, sampleIds, Optional.empty());
    }

    @Override
    public List<MolecularProfileCaseIdentifier> getFirstMutationProfileCaseIdentifiers(List<String> studyIds, List<String> sampleIds) {
        return getFirstFilteredMolecularProfileCaseIdentifiers(studyIds, sampleIds, Optional.of(molecularProfileUtil.isMutationProfile));
    }

    @Override
    public List<MolecularProfileCaseIdentifier> getMutationProfileCaseIdentifiers(List<String> studyIds, List<String> sampleIds) {
        return getFilteredMolecularProfileCaseIdentifiers(studyIds, sampleIds, Optional.of(molecularProfileUtil.isMutationProfile));
    }

    @Override
    public List<MolecularProfileCaseIdentifier> getFirstDiscreteCNAProfileCaseIdentifiers(List<String> studyIds, List<String> sampleIds) {
        return getFilteredMolecularProfileCaseIdentifiers(studyIds, sampleIds, Optional.of(molecularProfileUtil.isDiscreteCNAMolecularProfile));
    }

    @Override
    public List<MolecularProfileCaseIdentifier> getFirstStructuralVariantProfileCaseIdentifiers(List<String> studyIds, List<String> sampleIds) {
        return getFirstFilteredMolecularProfileCaseIdentifiers(studyIds, sampleIds, Optional.of(molecularProfileUtil.isStructuralVariantMolecularProfile));
    }

    private List<MolecularProfileCaseIdentifier> getFirstFilteredMolecularProfileCaseIdentifiers(List<String> studyIds, List<String> sampleIds, Optional<Predicate<MolecularProfile>> profileFilter) {
        List<MolecularProfile> molecularProfiles =
                getMolecularProfilesInStudies(studyIds.stream().distinct().toList(), Projection.SUMMARY.name());
        return molecularProfileUtil.getFirstFilteredMolecularProfileCaseIdentifiers(molecularProfiles, studyIds, sampleIds, profileFilter);
    }

    private List<MolecularProfileCaseIdentifier> getFilteredMolecularProfileCaseIdentifiers(List<String> studyIds, List<String> sampleIds, Optional<Predicate<MolecularProfile>> profileFilter) {
        List<MolecularProfile> molecularProfiles =
                getMolecularProfilesInStudies(studyIds.stream().distinct().toList(), Projection.SUMMARY.name());
        return molecularProfileUtil.getFilteredMolecularProfileCaseIdentifiers(molecularProfiles, studyIds, sampleIds, profileFilter);
    }
}
