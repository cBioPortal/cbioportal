package org.cbioportal.persistence.helper;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.persistence.enums.ClinicalAttributeDataSource;
import org.cbioportal.web.parameter.CategorizedClinicalDataCountFilter;
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
                                              @Nullable Map<ClinicalAttributeDataSource, List<ClinicalAttribute>> clinicalAttributesMap,
                                              @Nullable List<CustomSampleIdentifier> customDataSamples) {
        if (Objects.isNull(studyViewFilter)) {
            studyViewFilter = new StudyViewFilter();
        }
        if (Objects.isNull(clinicalAttributesMap)) {
            clinicalAttributesMap = new EnumMap<>(ClinicalAttributeDataSource.class);
        }
        if (Objects.isNull(customDataSamples)) {
            customDataSamples = new ArrayList<>();
        }
        return new StudyViewFilterHelper(studyViewFilter, clinicalAttributesMap, customDataSamples);
    }
    
    private final StudyViewFilter studyViewFilter;
    private final CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter;
    private final List<CustomSampleIdentifier> customDataSamples;
    private final boolean applyPatientIdFilters;

   
    private StudyViewFilterHelper(@NonNull StudyViewFilter studyViewFilter, @NonNull Map<ClinicalAttributeDataSource, List<ClinicalAttribute>> clinicalAttributesMap,
                                  @NonNull List<CustomSampleIdentifier> customDataSamples) {
        this.studyViewFilter = studyViewFilter;
        this.categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter, clinicalAttributesMap);
        this.customDataSamples = customDataSamples;
        this.applyPatientIdFilters = shouldApplyPatientIdFilters();
    }
    
    public StudyViewFilter studyViewFilter() {
        return studyViewFilter;
    }
    
    public CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter() {
        return categorizedClinicalDataCountFilter;
    }
    
    public List<CustomSampleIdentifier> customDataSamples() {
        return this.customDataSamples;
    }
    
    private CategorizedClinicalDataCountFilter extractClinicalDataCountFilters(final StudyViewFilter studyViewFilter, Map<ClinicalAttributeDataSource, List<ClinicalAttribute>> clinicalAttributesMap) {

        if (studyViewFilter.getClinicalDataFilters() == null || clinicalAttributesMap.isEmpty()) {
            return CategorizedClinicalDataCountFilter.getBuilder().build();
        }

        List<String> patientCategoricalAttributes = clinicalAttributesMap.get(ClinicalAttributeDataSource.PATIENT)
            .stream().filter(ca -> ca.getDatatype().equals("STRING"))
            .map(ClinicalAttribute::getAttrId)
            .toList();

        List<String> patientNumericalAttributes = clinicalAttributesMap.get(ClinicalAttributeDataSource.PATIENT)
            .stream().filter(ca -> ca.getDatatype().equals("NUMBER"))
            .map(ClinicalAttribute::getAttrId)
            .toList();

        List<String> sampleCategoricalAttributes = clinicalAttributesMap.get(ClinicalAttributeDataSource.SAMPLE)
            .stream().filter(ca -> ca.getDatatype().equals("STRING"))
            .map(ClinicalAttribute::getAttrId)
            .toList();

        List<String> sampleNumericalAttributes = clinicalAttributesMap.get(ClinicalAttributeDataSource.SAMPLE)
            .stream().filter(ca -> ca.getDatatype().equals("NUMBER"))
            .map(ClinicalAttribute::getAttrId)
            .toList();

        return CategorizedClinicalDataCountFilter.getBuilder()
            .setPatientCategoricalClinicalDataFilters(studyViewFilter.getClinicalDataFilters()
                .stream().filter(clinicalDataFilter -> patientCategoricalAttributes.contains(clinicalDataFilter.getAttributeId()))
                .toList())
            .setPatientNumericalClinicalDataFilters(studyViewFilter.getClinicalDataFilters().stream()
                .filter(clinicalDataFilter -> patientNumericalAttributes.contains(clinicalDataFilter.getAttributeId()))
                .toList())
            .setSampleCategoricalClinicalDataFilters(studyViewFilter.getClinicalDataFilters().stream()
                .filter(clinicalDataFilter -> sampleCategoricalAttributes.contains(clinicalDataFilter.getAttributeId()))
                .toList())
            .setSampleNumericalClinicalDataFilters(studyViewFilter.getClinicalDataFilters().stream()
                .filter(clinicalDataFilter -> sampleNumericalAttributes.contains(clinicalDataFilter.getAttributeId()))
                .toList())
            .build();
    }

    public boolean shouldApplyPatientIdFilters() {
        return studyViewFilter.getClinicalEventFilters() != null && !studyViewFilter.getClinicalEventFilters().isEmpty()
            || studyViewFilter.getPatientTreatmentFilters() != null && studyViewFilter.getPatientTreatmentFilters().getFilters()!= null && !studyViewFilter.getPatientTreatmentFilters().getFilters().isEmpty()
            || categorizedClinicalDataCountFilter.getPatientCategoricalClinicalDataFilters() != null && !categorizedClinicalDataCountFilter.getPatientCategoricalClinicalDataFilters().isEmpty()
            || categorizedClinicalDataCountFilter.getPatientNumericalClinicalDataFilters() != null && !categorizedClinicalDataCountFilter.getPatientNumericalClinicalDataFilters().isEmpty();
    }

}
