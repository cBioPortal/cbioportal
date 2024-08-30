package org.cbioportal.persistence.helper;

import org.cbioportal.model.MolecularProfile;
import org.cbioportal.persistence.enums.DataSource;
import org.cbioportal.web.parameter.ClinicalDataFilter;
import org.cbioportal.web.parameter.CategorizedGenericAssayDataCountFilter;
import org.cbioportal.web.parameter.CategorizedGenomicDataCountFilter;
import org.cbioportal.web.parameter.CustomSampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
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
            genericAssayProfilesMap = new HashMap<>();
        }
        if (Objects.isNull(customDataSamples)) {
            customDataSamples = new ArrayList<>();
        }
        return new StudyViewFilterHelper(studyViewFilter, genericAssayProfilesMap, customDataSamples);
    }

    private final StudyViewFilter studyViewFilter;
    private final CategorizedGenomicDataCountFilter categorizedGenomicDataCountFilter;
    private final CategorizedGenericAssayDataCountFilter categorizedGenericAssayDataCountFilter;
    private final List<CustomSampleIdentifier> customDataSamples;

    private StudyViewFilterHelper(@NonNull StudyViewFilter studyViewFilter, 
                                  @NonNull Map<DataSource, List<MolecularProfile>> genericAssayProfilesMap,
                                  @NonNull List<CustomSampleIdentifier> customDataSamples) {
        this.studyViewFilter = studyViewFilter;
        this.categorizedGenomicDataCountFilter = extractGenomicDataCountFilters(studyViewFilter);
        this.categorizedGenericAssayDataCountFilter = extractGenericAssayDataCountFilters(studyViewFilter, genericAssayProfilesMap);
        this.customDataSamples = customDataSamples;
    }

    public StudyViewFilter studyViewFilter() {
        return studyViewFilter;
    }
    
    public CategorizedGenomicDataCountFilter categorizedGenomicDataCountFilter() {
        return categorizedGenomicDataCountFilter;
    }

    public CategorizedGenericAssayDataCountFilter categorizedGenericAssayDataCountFilter() {
        return categorizedGenericAssayDataCountFilter;
    }
    
    public List<CustomSampleIdentifier> customDataSamples() {
        return this.customDataSamples;
    }
    
    private CategorizedGenomicDataCountFilter extractGenomicDataCountFilters(final StudyViewFilter studyViewFilter) {
        if (studyViewFilter.getGenomicDataFilters() == null)
        {
            return CategorizedGenomicDataCountFilter.getBuilder().build();
        }

        CategorizedGenomicDataCountFilter.Builder builder = CategorizedGenomicDataCountFilter.getBuilder();
        
        builder.setSampleNumericalGenomicDataFilters(studyViewFilter.getGenomicDataFilters().stream()
            .filter(genomicDataFilter -> !genomicDataFilter.getProfileType().equals("cna") && !genomicDataFilter.getProfileType().equals("gistic"))
            .toList());
        builder.setSampleCategoricalGenomicDataFilters(studyViewFilter.getGenomicDataFilters().stream()
            .filter(genomicDataFilter -> genomicDataFilter.getProfileType().equals("cna") || genomicDataFilter.getProfileType().equals("gistic"))
            .toList());
        return builder.build();
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
