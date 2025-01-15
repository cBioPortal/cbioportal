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
                                              @Nullable List<CustomSampleIdentifier> customDataSamples,
                                              @Nullable List<String> involvedCancerStudies) {
        if (Objects.isNull(studyViewFilter)) {
            studyViewFilter = new StudyViewFilter();
        }
        if (Objects.isNull(genericAssayProfilesMap)) {
            genericAssayProfilesMap = new EnumMap<>(DataSource.class);
        }
        if (Objects.isNull(customDataSamples)) {
            customDataSamples = new ArrayList<>();
        }
        if (Objects.isNull(involvedCancerStudies)) {
            involvedCancerStudies = new ArrayList<>();
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
        return new StudyViewFilterHelper(studyViewFilter, genericAssayProfilesMap, customDataSamples, involvedCancerStudies);
    }

    private final StudyViewFilter studyViewFilter;
    private final CategorizedGenericAssayDataCountFilter categorizedGenericAssayDataCountFilter;
    private final List<CustomSampleIdentifier> customDataSamples;
    private final List<String> involvedCancerStudies;

    private StudyViewFilterHelper(@NonNull StudyViewFilter studyViewFilter, 
                                  @NonNull Map<DataSource, List<MolecularProfile>> genericAssayProfilesMap,
                                  @NonNull List<CustomSampleIdentifier> customDataSamples,
                                  @NonNull List<String> involvedCancerStudies) {
        this.studyViewFilter = studyViewFilter;
        this.categorizedGenericAssayDataCountFilter = extractGenericAssayDataCountFilters(studyViewFilter, genericAssayProfilesMap);
        this.customDataSamples = customDataSamples;
        this.involvedCancerStudies = involvedCancerStudies;
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
        if (studyViewFilter != null && studyViewFilter.getSampleIdentifiers() != null) {
            return studyViewFilter.getSampleIdentifiers().stream()
                .map(sampleIdentifier -> sampleIdentifier.getStudyId() + "_" + sampleIdentifier.getSampleId())
                .toArray(String[]::new);
        } else {
            return new String[0];
        }
    }
    
    public List<String> involvedCancerStudies() {
        return involvedCancerStudies;
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
        // this should throw error or move to all binning endpoints in the future for input validation
        if (!areValidFilters(filters)) {
            return filters;
        }
        
        boolean hasNumericalValue = false;
        List<T> mergedDataFilters = new ArrayList<>();

        for (T filter : filters) {
            List<DataFilterValue> mergedValues = new ArrayList<>();
            List<DataFilterValue> nonNumericalValues = new ArrayList<>();

            // record the start and end of current merging range
            BigDecimal mergedStart = null;
            BigDecimal mergedEnd = null;
            // for each value
            for (DataFilterValue dataFilterValue : filter.getValues()) {
                // if it is non-numerical, leave it as is
                if (dataFilterValue.getValue() != null) {
                    nonNumericalValues.add(dataFilterValue);
                    continue;
                }
                // else it is numerical so start merging process
                hasNumericalValue = true;
                BigDecimal start = dataFilterValue.getStart();
                BigDecimal end = dataFilterValue.getEnd();

                // if current merging range is null, we take current bin's range
                if (mergedStart == null && mergedEnd == null) {
                    mergedStart = start;
                    mergedEnd = end;
                }
                // else we already has a merging range, we check if this one is consecutive of our range
                else if (mergedEnd.equals(start)) {
                    // if true, we expand our range
                    mergedEnd = end;
                }
                else {
                    // otherwise it's a gap, so we save our current range first, and then use current bin to start the next range
                    mergedValues.add(new DataFilterValue(mergedStart, mergedEnd));
                    mergedStart = start;
                    mergedEnd = end;
                }
            }

            // in the end we need to save the final range, but if everything is non-numerical then no need to
            if (hasNumericalValue) {
                mergedValues.add(new DataFilterValue(mergedStart, mergedEnd));
            }
            mergedValues.addAll(nonNumericalValues);
            filter.setValues(mergedValues);
            mergedDataFilters.add(filter);
        }

        return mergedDataFilters;
    }

    public static <T extends DataFilter> boolean areValidFilters(List<T> filters) {
        if (filters == null || filters.isEmpty()) {
            return false;
        }
        
        for (T filter : filters) {
            if (!isValidFilter(filter)) {
                return false;
            }
        }
        return true;
    }

    private static <T extends DataFilter> boolean isValidFilter(T filter) {
        if (filter == null || filter.getValues() == null || filter.getValues().isEmpty()) {
            return false;
        }

        BigDecimal start = null;
        BigDecimal end = null;
        for (DataFilterValue value : filter.getValues()) {
            if (!validateDataFilterValue(value, start, end)) {
                return false;
            }
            // update start and end values to check next bin range
            if (value.getStart() != null) {
                start = value.getStart();
            }
            if (value.getEnd() != null) {
                end = value.getEnd();
            }
        }
        return true;
    }

    private static boolean validateDataFilterValue(DataFilterValue value, BigDecimal lastStart, BigDecimal lastEnd) {
        // non-numerical value should not have numerical value
        if (value.getValue() != null) {
            return value.getStart() == null && value.getEnd() == null;
        }

        // check if start < end
        if (value.getStart() != null && value.getEnd() != null
            && value.getStart().compareTo(value.getEnd()) >= 0) {
            return false;
        }

        // check if start stays increasing and no overlapping
        if (value.getStart() != null
            && ((lastStart != null && lastStart.compareTo(value.getStart()) >= 0)
            || (lastEnd != null && value.getStart().compareTo(lastEnd) < 0))) {
                return false;
        }
        
        // check if end stays increasing
        return value.getEnd() == null || lastEnd == null
            || lastEnd.compareTo(value.getEnd()) < 0;
    }
}
