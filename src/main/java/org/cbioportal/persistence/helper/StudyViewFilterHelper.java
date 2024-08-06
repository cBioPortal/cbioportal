package org.cbioportal.persistence.helper;

import org.cbioportal.model.MolecularProfile;
import org.cbioportal.persistence.enums.ClinicalAttributeDataSource;
import org.cbioportal.web.parameter.CategorizedClinicalDataCountFilter;
import org.cbioportal.web.parameter.ClinicalDataFilter;
import org.cbioportal.web.parameter.CustomSampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class StudyViewFilterHelper {
    public static StudyViewFilterHelper build(@Nullable StudyViewFilter studyViewFilter,
            @Nullable Map<ClinicalAttributeDataSource, List<MolecularProfile>> genericAssayProfilesMap,
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
    private final List<CustomSampleIdentifier> customDataSamples;
    private final CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter;


    private StudyViewFilterHelper(@NonNull StudyViewFilter studyViewFilter,
            @NonNull Map<ClinicalAttributeDataSource, List<MolecularProfile>> genericAssayProfilesMap,
                                  @NonNull List<CustomSampleIdentifier> customDataSamples) {
        this.studyViewFilter = studyViewFilter;
        this.categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter, genericAssayProfilesMap);
        this.customDataSamples = customDataSamples;
    }

    public StudyViewFilter studyViewFilter() {
        return studyViewFilter;
    }

    public List<CustomSampleIdentifier> customDataSamples() {
        return this.customDataSamples;
    }
    
    private CategorizedClinicalDataCountFilter extractClinicalDataCountFilters(final StudyViewFilter studyViewFilter, Map<ClinicalAttributeDataSource, List<MolecularProfile>> genericAssayProfilesMap) {
        if ((studyViewFilter.getGenericAssayDataFilters() == null || genericAssayProfilesMap.isEmpty()) &&
            studyViewFilter.getGenomicDataFilters() == null)
        {
            return CategorizedClinicalDataCountFilter.getBuilder().build();
        }

        CategorizedClinicalDataCountFilter.Builder builder = CategorizedClinicalDataCountFilter.getBuilder();

        if (studyViewFilter.getGenomicDataFilters() != null) {
            builder.setSampleNumericalGenomicDataFilters(studyViewFilter.getGenomicDataFilters().stream()
                .filter(genomicDataFilter -> !genomicDataFilter.getProfileType().equals("cna") && !genomicDataFilter.getProfileType().equals("gistic"))
                .collect(Collectors.toList()));
            builder.setSampleCategoricalGenomicDataFilters(studyViewFilter.getGenomicDataFilters().stream()
                .filter(genomicDataFilter -> genomicDataFilter.getProfileType().equals("cna") || genomicDataFilter.getProfileType().equals("gistic"))
                .collect(Collectors.toList()));
        }
        if (studyViewFilter.getGenericAssayDataFilters() != null) {
            // TODO: Support patient level profiles and data filtering
            List<String> sampleCategoricalProfileTypes = genericAssayProfilesMap.get(ClinicalAttributeDataSource.SAMPLE)
                .stream().filter(profile -> profile.getDatatype().equals("CATEGORICAL") || profile.getDatatype().equals("BINARY"))
                .map(profile -> profile.getStableId().replace(profile.getCancerStudyIdentifier() + "_", ""))
                .toList();

            List<String> sampleNumericalProfileTypes = genericAssayProfilesMap.get(ClinicalAttributeDataSource.SAMPLE)
                .stream().filter(profile -> profile.getDatatype().equals("LIMIT-VALUE"))
                .map(profile -> profile.getStableId().replace(profile.getCancerStudyIdentifier() + "_", ""))
                .toList();
            builder.setSampleNumericalGenericAssayDataFilters(studyViewFilter.getGenericAssayDataFilters().stream()
                .filter(genericAssayDataFilter -> sampleNumericalProfileTypes.contains(genericAssayDataFilter.getProfileType()))
                .collect(Collectors.toList()));
            builder.setSampleCategoricalGenericAssayDataFilters(studyViewFilter.getGenericAssayDataFilters().stream()
                .filter(genericAssayDataFilter -> sampleCategoricalProfileTypes.contains(genericAssayDataFilter.getProfileType()))
                .collect(Collectors.toList()));
        }
        return builder.build();
    }

    public boolean isCategoricalClinicalDataFilter(ClinicalDataFilter clinicalDataFilter) {
        var filterValue = clinicalDataFilter.getValues().getFirst();
        return filterValue.getValue() != null;
    }

}
