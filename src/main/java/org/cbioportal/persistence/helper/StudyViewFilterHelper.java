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

            BigDecimal mergedStart = null;
            BigDecimal mergedEnd = null;
            for (DataFilterValue dataFilterValue : filter.getValues()) {
                // leave non-numerical values as they are
                if (dataFilterValue.getValue() != null) {
                    nonNumericalValues.add(dataFilterValue);
                }
                // merge adjacent numerical bins
                else {
                    hasNumericalValue = true;
                    BigDecimal start = dataFilterValue.getStart();
                    BigDecimal end = dataFilterValue.getEnd();

                    if (mergedStart == null && mergedEnd == null) {
                        mergedStart = start;
                        mergedEnd = end;
                    }
                    else if (mergedEnd.equals(start)) {
                        mergedEnd = end;
                    }
                    else {
                        mergedValues.add(new DataFilterValue(mergedStart, mergedEnd));
                        mergedStart = start;
                        mergedEnd = end;
                    }
                }
            }

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
        // check if filters are empty
        if (filters == null || filters.isEmpty()) {
            return false;
        }
        
        for (T filter : filters) {
            // check if each filter has value
            if (filter.getValues() == null || filter.getValues().isEmpty()) {
                return false;
            }
            
            BigDecimal start = null;
            BigDecimal end = null;
            for (DataFilterValue dataFilterValue : filter.getValues()) {
                // non-numerical value should not have numerical value
                if (dataFilterValue.getValue() != null) {
                    if (dataFilterValue.getStart() != null || dataFilterValue.getEnd() != null) {
                        return false;
                    }
                    continue;
                }
                // check if start < end
                if (dataFilterValue.getStart() != null && dataFilterValue.getEnd() != null) {
                    if (dataFilterValue.getStart().compareTo(dataFilterValue.getEnd()) >= 0) {
                        return false;
                    }
                }
                // check if start stays increasing
                if (dataFilterValue.getStart() != null) {
                    if (start != null && start.compareTo(dataFilterValue.getStart()) >= 0) {
                        return false;
                    }
                    start = dataFilterValue.getStart();
                    // no overlapping is allowed
                    if (end != null && start.compareTo(end) < 0) {
                        return false;
                    }
                }
                // check if end stays increasing
                if (dataFilterValue.getEnd() != null) {
                    if (end != null && end.compareTo(dataFilterValue.getEnd()) >= 0) {
                        return false;
                    }
                    end = dataFilterValue.getEnd();
                }
            }
        }
        return true;
    }
}
