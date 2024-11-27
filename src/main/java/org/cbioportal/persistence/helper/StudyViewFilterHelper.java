package org.cbioportal.persistence.helper;

import org.cbioportal.model.MolecularProfile;
import org.cbioportal.persistence.enums.DataSource;
import org.cbioportal.web.parameter.ClinicalDataFilter;
import org.cbioportal.web.parameter.CategorizedGenericAssayDataCountFilter;
import org.cbioportal.web.parameter.CustomSampleIdentifier;
import org.cbioportal.web.parameter.DataFilter;
import org.cbioportal.web.parameter.DataFilterValue;
import org.cbioportal.web.parameter.GenericAssayDataFilter;
import org.cbioportal.web.parameter.GenomicDataFilter;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
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
        if (studyViewFilter.getGenomicDataFilters() != null && !studyViewFilter.getGenomicDataFilters().isEmpty()) {
            List<GenomicDataFilter> mergedGenomicDataFilters = mergeDataFilters(studyViewFilter.getGenomicDataFilters());
            studyViewFilter.setGenomicDataFilters(mergedGenomicDataFilters);
        }
        if (studyViewFilter.getClinicalDataFilters() != null && !studyViewFilter.getClinicalDataFilters().isEmpty()) {
            List<ClinicalDataFilter> mergedClinicalDataFilters = mergeDataFilters(studyViewFilter.getClinicalDataFilters());
            studyViewFilter.setClinicalDataFilters(mergedClinicalDataFilters);
        }
        if (studyViewFilter.getGenericAssayDataFilters() != null && !studyViewFilter.getGenericAssayDataFilters().isEmpty()) {
            List<GenericAssayDataFilter> mergedGenericAssayDataFilters = mergeDataFilters(studyViewFilter.getGenericAssayDataFilters());
            studyViewFilter.setGenericAssayDataFilters(mergedGenericAssayDataFilters);
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

        // No BINARY in the database yet
        List<String> sampleNumericalProfileTypes = genericAssayProfilesMap.get(DataSource.SAMPLE)
            .stream().filter(profile -> profile.getDatatype().equals("LIMIT-VALUE"))
            .map(profile -> profile.getStableId().replace(profile.getCancerStudyIdentifier() + "_", ""))
            .toList();
        builder.setSampleNumericalGenericAssayDataFilters(studyViewFilter.getGenericAssayDataFilters().stream()
            .filter(genericAssayDataFilter -> sampleNumericalProfileTypes.contains(genericAssayDataFilter.getProfileType()))
            .toList());
        List<String> sampleCategoricalProfileTypes = genericAssayProfilesMap.get(DataSource.SAMPLE)
            .stream().filter(profile -> profile.getDatatype().equals("CATEGORICAL") || profile.getDatatype().equals("BINARY"))
            .map(profile -> profile.getStableId().replace(profile.getCancerStudyIdentifier() + "_", ""))
            .toList();
        builder.setSampleCategoricalGenericAssayDataFilters(studyViewFilter.getGenericAssayDataFilters().stream()
            .filter(genericAssayDataFilter -> sampleCategoricalProfileTypes.contains(genericAssayDataFilter.getProfileType()))
            .toList());
        List<String> patientCategoricalProfileTypes = genericAssayProfilesMap.get(DataSource.PATIENT)
            .stream().filter(profile -> profile.getDatatype().equals("CATEGORICAL") || profile.getDatatype().equals("BINARY"))
            .map(profile -> profile.getStableId().replace(profile.getCancerStudyIdentifier() + "_", ""))
            .toList();
        builder.setPatientCategoricalGenericAssayDataFilters(studyViewFilter.getGenericAssayDataFilters().stream()
            .filter(genericAssayDataFilter -> patientCategoricalProfileTypes.contains(genericAssayDataFilter.getProfileType()))
            .toList());
        return builder.build();
    }

    public boolean isCategoricalClinicalDataFilter(ClinicalDataFilter clinicalDataFilter) {
        var filterValue = clinicalDataFilter.getValues().getFirst();
        return filterValue.getValue() != null;
    }

    /**
     * Merge the range of numerical bins in DataFilters to reduce the number of scans that runs on the database when filtering.
     */
    public static <T extends DataFilter> List<T> mergeDataFilters(List<T> filters) {
        boolean isNonNumericalOnly = true;
        List<T> mergedDataFilters = new ArrayList<>();

        for (T filter : filters) {
            List<DataFilterValue> mergedValues = new ArrayList<>();
            List<DataFilterValue> nonNumericalValues = new ArrayList<>();

            BigDecimal mergedStart = null;
            BigDecimal mergedEnd = null;
            for (DataFilterValue dataFilterValue : filter.getValues()) {
                // leave non-numerical values as they are
                if (dataFilterValue.getValue() != null) {
                    nonNumericalValues.add(dataFilterValue);
                }
                // merge adjacent numerical bins
                else {
                    isNonNumericalOnly = false;
                    BigDecimal start = dataFilterValue.getStart();
                    BigDecimal end = dataFilterValue.getEnd();

                    if (mergedStart == null && mergedEnd == null) {
                        mergedStart = start;
                        mergedEnd = end;
                    }
                    else if (mergedEnd.equals(start)) {
                        mergedEnd = end;
                    } else {
                        mergedValues.add(new DataFilterValue(mergedStart, mergedEnd, null));
                        mergedStart = start;
                        mergedEnd = end;
                    }
                }
            }

            if (!isNonNumericalOnly) {
                mergedValues.add(new DataFilterValue(mergedStart, mergedEnd, null));
            }
            mergedValues.addAll(nonNumericalValues);
            filter.setValues(mergedValues);
            mergedDataFilters.add(filter);
        }

        return mergedDataFilters;
    }
}
