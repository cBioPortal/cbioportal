package org.cbioportal.persistence.helper;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.MolecularProfile;
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
import java.util.stream.Collectors;

public final class StudyViewFilterHelper {
    public static StudyViewFilterHelper build(@Nullable StudyViewFilter studyViewFilter,
                                              @Nullable Map<ClinicalAttributeDataSource, List<ClinicalAttribute>> clinicalAttributesMap,
                                              @Nullable Map<ClinicalAttributeDataSource, List<MolecularProfile>> genericAssayProfilesMap,
                                              @Nullable List<CustomSampleIdentifier> customDataSamples) {
        if (Objects.isNull(studyViewFilter)) {
            studyViewFilter = new StudyViewFilter();
        }
        if (Objects.isNull(clinicalAttributesMap)) {
            clinicalAttributesMap = new EnumMap<>(ClinicalAttributeDataSource.class);
        }
        if (Objects.isNull(genericAssayProfilesMap)) {
            genericAssayProfilesMap = new EnumMap<>(ClinicalAttributeDataSource.class);
        }
        if (Objects.isNull(customDataSamples)) {
            customDataSamples = new ArrayList<>();
        }
        return new StudyViewFilterHelper(studyViewFilter, clinicalAttributesMap, genericAssayProfilesMap, customDataSamples);
    }
    
    private final StudyViewFilter studyViewFilter;
    private final CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter;
    private final List<CustomSampleIdentifier> customDataSamples;
    private final boolean applyPatientIdFilters;

   
    private StudyViewFilterHelper(@NonNull StudyViewFilter studyViewFilter, @NonNull Map<ClinicalAttributeDataSource, List<ClinicalAttribute>> clinicalAttributesMap,
                                  @NonNull Map<ClinicalAttributeDataSource, List<MolecularProfile>> genericAssayProfilesMap,
                                  @NonNull List<CustomSampleIdentifier> customDataSamples) {
        this.studyViewFilter = studyViewFilter;
        this.categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter, clinicalAttributesMap, genericAssayProfilesMap);
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
    
    private CategorizedClinicalDataCountFilter extractClinicalDataCountFilters(final StudyViewFilter studyViewFilter, Map<ClinicalAttributeDataSource, List<ClinicalAttribute>> clinicalAttributesMap, Map<ClinicalAttributeDataSource, List<MolecularProfile>> genericAssayProfilesMap) {
        if ((studyViewFilter.getClinicalDataFilters() == null || clinicalAttributesMap.isEmpty()) &&
            (studyViewFilter.getGenericAssayDataFilters() == null || genericAssayProfilesMap.isEmpty()) &&
            studyViewFilter.getGenomicDataFilters() == null)
        {
            return CategorizedClinicalDataCountFilter.getBuilder().build();
        }

        CategorizedClinicalDataCountFilter.Builder builder = CategorizedClinicalDataCountFilter.getBuilder();

        if (studyViewFilter.getClinicalDataFilters() != null) {
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
            
            builder.setPatientCategoricalClinicalDataFilters(studyViewFilter.getClinicalDataFilters()
                    .stream().filter(clinicalDataFilter -> patientCategoricalAttributes.contains(clinicalDataFilter.getAttributeId()))
                    .collect(Collectors.toList()))
                .setPatientNumericalClinicalDataFilters(studyViewFilter.getClinicalDataFilters().stream()
                    .filter(clinicalDataFilter -> patientNumericalAttributes.contains(clinicalDataFilter.getAttributeId()))
                    .collect(Collectors.toList()))
                .setSampleCategoricalClinicalDataFilters(studyViewFilter.getClinicalDataFilters().stream()
                    .filter(clinicalDataFilter -> sampleCategoricalAttributes.contains(clinicalDataFilter.getAttributeId()))
                    .collect(Collectors.toList()))
                .setSampleNumericalClinicalDataFilters(studyViewFilter.getClinicalDataFilters().stream()
                    .filter(clinicalDataFilter -> sampleNumericalAttributes.contains(clinicalDataFilter.getAttributeId()))
                    .collect(Collectors.toList()));
        }
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

    public boolean shouldApplyPatientIdFilters() {
        return studyViewFilter.getClinicalEventFilters() != null && !studyViewFilter.getClinicalEventFilters().isEmpty()
            || studyViewFilter.getPatientTreatmentFilters() != null && studyViewFilter.getPatientTreatmentFilters().getFilters()!= null && !studyViewFilter.getPatientTreatmentFilters().getFilters().isEmpty()
            || categorizedClinicalDataCountFilter.getPatientCategoricalClinicalDataFilters() != null && !categorizedClinicalDataCountFilter.getPatientCategoricalClinicalDataFilters().isEmpty()
            || categorizedClinicalDataCountFilter.getPatientNumericalClinicalDataFilters() != null && !categorizedClinicalDataCountFilter.getPatientNumericalClinicalDataFilters().isEmpty();
    }

}
