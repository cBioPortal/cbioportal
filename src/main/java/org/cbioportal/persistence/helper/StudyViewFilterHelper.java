package org.cbioportal.persistence.helper;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.persistence.enums.DataSource;
import org.cbioportal.web.parameter.CategorizedClinicalDataCountFilter;
import org.cbioportal.web.parameter.CategorizedGenericAssayDataCountFilter;
import org.cbioportal.web.parameter.CategorizedGenomicDataCountFilter;
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
                                              @Nullable Map<DataSource, List<ClinicalAttribute>> clinicalAttributesMap,
                                              @Nullable Map<DataSource, List<MolecularProfile>> genericAssayProfilesMap,
                                              @Nullable List<CustomSampleIdentifier> customDataSamples) {
        if (Objects.isNull(studyViewFilter)) {
            studyViewFilter = new StudyViewFilter();
        }
        if (Objects.isNull(clinicalAttributesMap)) {
            clinicalAttributesMap = new EnumMap<>(DataSource.class);
        }
        if (Objects.isNull(genericAssayProfilesMap)) {
            genericAssayProfilesMap = new EnumMap<>(DataSource.class);
        }
        if (Objects.isNull(customDataSamples)) {
            customDataSamples = new ArrayList<>();
        }
        return new StudyViewFilterHelper(studyViewFilter, clinicalAttributesMap, genericAssayProfilesMap, customDataSamples);
    }
    
    private final StudyViewFilter studyViewFilter;
    private final CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter;

    private final CategorizedGenomicDataCountFilter categorizedGenomicDataCountFilter;

    private final CategorizedGenericAssayDataCountFilter categorizedGenericAssayDataCountFilter;

    private final List<CustomSampleIdentifier> customDataSamples;
    private final boolean applyPatientIdFilters;

   
    private StudyViewFilterHelper(@NonNull StudyViewFilter studyViewFilter, @NonNull Map<DataSource, List<ClinicalAttribute>> clinicalAttributesMap,
                                  @NonNull Map<DataSource, List<MolecularProfile>> genericAssayProfilesMap,
                                  @NonNull List<CustomSampleIdentifier> customDataSamples) {
        this.studyViewFilter = studyViewFilter;
        this.categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter, clinicalAttributesMap, genericAssayProfilesMap);
        this.categorizedGenomicDataCountFilter = extractGenomicDataCountFilters(studyViewFilter);
        this.categorizedGenericAssayDataCountFilter = extractGenericAssayDataCountFilters(studyViewFilter, genericAssayProfilesMap);
        this.customDataSamples = customDataSamples;
        this.applyPatientIdFilters = shouldApplyPatientIdFilters();
    }
    
    public StudyViewFilter studyViewFilter() {
        return studyViewFilter;
    }
    
    public CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter() {
        return categorizedClinicalDataCountFilter;
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
    
    private CategorizedClinicalDataCountFilter extractClinicalDataCountFilters(final StudyViewFilter studyViewFilter, Map<DataSource, List<ClinicalAttribute>> clinicalAttributesMap, Map<DataSource, List<MolecularProfile>> genericAssayProfilesMap) {
        if ((studyViewFilter.getClinicalDataFilters() == null || clinicalAttributesMap.isEmpty()))
        {
            return CategorizedClinicalDataCountFilter.getBuilder().build();
        }

        CategorizedClinicalDataCountFilter.Builder builder = CategorizedClinicalDataCountFilter.getBuilder();

        if (studyViewFilter.getClinicalDataFilters() != null) {
            List<String> patientCategoricalAttributes = clinicalAttributesMap.get(DataSource.PATIENT)
                .stream().filter(ca -> ca.getDatatype().equals("STRING"))
                .map(ClinicalAttribute::getAttrId)
                .toList();

            List<String> patientNumericalAttributes = clinicalAttributesMap.get(DataSource.PATIENT)
                .stream().filter(ca -> ca.getDatatype().equals("NUMBER"))
                .map(ClinicalAttribute::getAttrId)
                .toList();

            List<String> sampleCategoricalAttributes = clinicalAttributesMap.get(DataSource.SAMPLE)
                .stream().filter(ca -> ca.getDatatype().equals("STRING"))
                .map(ClinicalAttribute::getAttrId)
                .toList();

            List<String> sampleNumericalAttributes = clinicalAttributesMap.get(DataSource.SAMPLE)
                .stream().filter(ca -> ca.getDatatype().equals("NUMBER"))
                .map(ClinicalAttribute::getAttrId)
                .toList();
            
            builder.setPatientCategoricalClinicalDataFilters(studyViewFilter.getClinicalDataFilters()
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
                    .toList());
        }
        return builder.build();
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

    public boolean shouldApplyPatientIdFilters() {
        return studyViewFilter.getClinicalEventFilters() != null && !studyViewFilter.getClinicalEventFilters().isEmpty()
            || studyViewFilter.getPatientTreatmentFilters() != null && studyViewFilter.getPatientTreatmentFilters().getFilters()!= null && !studyViewFilter.getPatientTreatmentFilters().getFilters().isEmpty()
            || categorizedClinicalDataCountFilter.getPatientCategoricalClinicalDataFilters() != null && !categorizedClinicalDataCountFilter.getPatientCategoricalClinicalDataFilters().isEmpty()
            || categorizedClinicalDataCountFilter.getPatientNumericalClinicalDataFilters() != null && !categorizedClinicalDataCountFilter.getPatientNumericalClinicalDataFilters().isEmpty();
    }

}
