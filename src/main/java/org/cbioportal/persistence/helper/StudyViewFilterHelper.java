package org.cbioportal.persistence.helper;

import org.cbioportal.model.MolecularProfile;
import org.cbioportal.persistence.enums.DataSource;
import org.cbioportal.web.parameter.ClinicalDataFilter;
import org.cbioportal.web.parameter.CategorizedGenericAssayDataCountFilter;
import org.cbioportal.web.parameter.CustomSampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class StudyViewFilterHelper {
    public static StudyViewFilterHelper build(@Nullable StudyViewFilter studyViewFilter,
                                              @Nullable Map<DataSource, List<MolecularProfile>> genericAssayProfilesMap,
                                              @Nullable List<CustomSampleIdentifier> customDataSamples) {
        if (Objects.isNull(studyViewFilter)) {
            studyViewFilter = new StudyViewFilter();
        }
        if (Objects.isNull(genericAssayProfilesMap)) {
            genericAssayProfilesMap = new EnumMap<>(DataSource.class);
        }
        if (Objects.isNull(customDataSamples)) {
            customDataSamples = new ArrayList<>();
        }
        return new StudyViewFilterHelper(studyViewFilter, genericAssayProfilesMap, customDataSamples);
    }

    private final StudyViewFilter studyViewFilter;
    private final CategorizedGenericAssayDataCountFilter categorizedGenericAssayDataCountFilter;
    private final List<CustomSampleIdentifier> customDataSamples;
    private final String[] filteredSampleIdentifiers;

    private StudyViewFilterHelper(@NonNull StudyViewFilter studyViewFilter, 
                                  @NonNull Map<DataSource, List<MolecularProfile>> genericAssayProfilesMap,
                                  @NonNull List<CustomSampleIdentifier> customDataSamples) {
        this.studyViewFilter = studyViewFilter;
        this.categorizedGenericAssayDataCountFilter = extractGenericAssayDataCountFilters(studyViewFilter, genericAssayProfilesMap);
        this.customDataSamples = customDataSamples;
        if (studyViewFilter != null && studyViewFilter.getSampleIdentifiers() != null) {
            this.filteredSampleIdentifiers = studyViewFilter.getSampleIdentifiers().stream()
                .map(sampleIdentifier -> sampleIdentifier.getStudyId() + "_" + sampleIdentifier.getSampleId())
                .toArray(String[]::new);
        } else {
            this.filteredSampleIdentifiers = new String[0];
        }
    }

    public StudyViewFilter studyViewFilter() {
        return studyViewFilter;
    }
    
    public CategorizedGenericAssayDataCountFilter categorizedGenericAssayDataCountFilter() {
        return categorizedGenericAssayDataCountFilter;
    }
    
    public List<CustomSampleIdentifier> customDataSamples() {
        return this.customDataSamples;
    }
    
    public String[] filteredSampleIdentifiers() {
        return this.filteredSampleIdentifiers;
    }

    private CategorizedGenericAssayDataCountFilter extractGenericAssayDataCountFilters(final StudyViewFilter studyViewFilter, Map<DataSource, List<MolecularProfile>> genericAssayProfilesMap) {
        if ((studyViewFilter.getGenericAssayDataFilters() == null || genericAssayProfilesMap.isEmpty()))
        {
            return CategorizedGenericAssayDataCountFilter.getBuilder().build();
        }

        CategorizedGenericAssayDataCountFilter.Builder builder = CategorizedGenericAssayDataCountFilter.getBuilder();

        // TODO: Support patient level profiles and data filtering
        List<String> sampleCategoricalProfileTypes = genericAssayProfilesMap.get(DataSource.SAMPLE)
            .stream().filter(profile -> profile.getDatatype().equals("CATEGORICAL") || profile.getDatatype().equals("BINARY"))
            .map(profile -> profile.getStableId().replace(profile.getCancerStudyIdentifier() + "_", ""))
            .toList();
        List<String> sampleNumericalProfileTypes = genericAssayProfilesMap.get(DataSource.SAMPLE)
            .stream().filter(profile -> profile.getDatatype().equals("LIMIT-VALUE"))
            .map(profile -> profile.getStableId().replace(profile.getCancerStudyIdentifier() + "_", ""))
            .toList();
        builder.setSampleNumericalGenericAssayDataFilters(studyViewFilter.getGenericAssayDataFilters().stream()
            .filter(genericAssayDataFilter -> sampleNumericalProfileTypes.contains(genericAssayDataFilter.getProfileType()))
            .toList());
        builder.setSampleCategoricalGenericAssayDataFilters(studyViewFilter.getGenericAssayDataFilters().stream()
            .filter(genericAssayDataFilter -> sampleCategoricalProfileTypes.contains(genericAssayDataFilter.getProfileType()))
            .toList());
        return builder.build();
    }

    public boolean isCategoricalClinicalDataFilter(ClinicalDataFilter clinicalDataFilter) {
        var filterValue = clinicalDataFilter.getValues().getFirst();
        return filterValue.getValue() != null;
    }

}
