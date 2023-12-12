package org.cbioportal.web.util.appliers;

import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.StructuralVariantFilterQuery;
import org.cbioportal.model.StudyViewStructuralVariantFilter;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.StructuralVariantService;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.util.StudyViewFilterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class StructuralVariantSubFilterApplier implements StudyViewSubFilterApplier {

    @Autowired
    private MolecularProfileService molecularProfileService;

    @Autowired
    private StructuralVariantService structuralVariantService;

    @Autowired
    private StudyViewFilterUtil studyViewFilterUtil;
    
    @Override
    public List<SampleIdentifier> filter(List<SampleIdentifier> toFilter, StudyViewFilter filters) {

        final List<StudyViewStructuralVariantFilter> structVarFilters = getStructVarFilters(filters);

        List<String> includedStudyIds = toFilter.stream()
            .map(SampleIdentifier::getStudyId)
            .distinct()
            .collect(Collectors.toList());

        List<MolecularProfile> molecularProfiles = molecularProfileService.getMolecularProfilesInStudies(includedStudyIds, "SUMMARY");
        
        Map<String, MolecularProfile> molecularProfileMap = molecularProfiles.stream()
            .collect(Collectors.toMap(MolecularProfile::getStableId, Function.identity()));

        List<SampleIdentifier> remainingSampleIdentifiers = toFilter;
        
        for (StudyViewStructuralVariantFilter structuralVariantFilter : structVarFilters) {

            // Collect molecular profiles referenced in gene filter.
            List<MolecularProfile> filteredMolecularProfiles = structuralVariantFilter
                .getMolecularProfileIds()
                .stream()
                .map(molecularProfileId -> molecularProfileMap.get(molecularProfileId))
                .collect(Collectors.toList());

            Map<String, List<MolecularProfile>> mapByStudyId = filteredMolecularProfiles
                .stream()
                .collect(Collectors.groupingBy(MolecularProfile::getCancerStudyIdentifier));

            for (List<StructuralVariantFilterQuery> structVarQueries: structuralVariantFilter.getStructVarQueries()) {

                // Remove samples in remainingSampleIdentifiers that belong to a study that is not accessible to the current user
                // (do not appear in the molecularProfileMap argument). 
                final List<SampleIdentifier> filteredSampleIdentifiers = remainingSampleIdentifiers.stream()
                    .filter(i -> mapByStudyId.containsKey(i.getStudyId())).collect(Collectors.toList());

                final List<String> molecularProfileIds = filteredSampleIdentifiers.stream()
                    .map(i -> mapByStudyId.get(i.getStudyId()).get(0).getStableId())
                    .distinct()
                    .collect(Collectors.toList());
                
                final List<String> sampleIds = filteredSampleIdentifiers.stream()
                        .map(SampleIdentifier::getSampleId)
                        .collect(Collectors.toList());

                final List<StructuralVariantFilterQuery> entrezIdEnhancedSvQueries = studyViewFilterUtil.resolveEntrezGeneIds(structVarQueries);
                remainingSampleIdentifiers = structuralVariantService
                    .fetchStructuralVariantsByStructVarQueries(molecularProfileIds, sampleIds, entrezIdEnhancedSvQueries)
                    .stream()
                    .map(m -> {
                        SampleIdentifier sampleIdentifier = new SampleIdentifier();
                        sampleIdentifier.setSampleId(m.getSampleId());
                        sampleIdentifier.setStudyId(m.getStudyId());
                        return sampleIdentifier;
                    })
                    .distinct()
                    .collect(Collectors.toList());
            }

        }
        return remainingSampleIdentifiers;
    }

    @Override
    public boolean shouldApplyFilter(StudyViewFilter studyViewFilter) {
        return !getStructVarFilters(studyViewFilter).isEmpty();
    }

    private static List<StudyViewStructuralVariantFilter> getStructVarFilters(StudyViewFilter filters) {
        final List<StudyViewStructuralVariantFilter> structuralVariantFilters = filters.getStructuralVariantFilters();
        if (structuralVariantFilters == null || structuralVariantFilters.isEmpty()) {
            return new ArrayList<>();
        }
        final List<StudyViewStructuralVariantFilter> structVarFilters = structuralVariantFilters.stream()
            .filter(structuralVariantFilter -> !structuralVariantFilter.getStructVarQueries().isEmpty())
            .collect(Collectors.toList());
        return structVarFilters;
    }
    
}
