package org.cbioportal.persistence.helper;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.persistence.enums.ClinicalAttributeDataSource;
import org.cbioportal.web.parameter.CategorizedClinicalDataCountFilter;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StudyViewFilterHelper {
    public static StudyViewFilterHelper build(@Nullable StudyViewFilter studyViewFilter, @Nullable EnumMap<ClinicalAttributeDataSource, List<ClinicalAttribute>> clinicalAttributesMap) {
        if (Objects.isNull(studyViewFilter)) {
            studyViewFilter = new StudyViewFilter();
        }
        if (Objects.isNull(clinicalAttributesMap)) {
            clinicalAttributesMap = new EnumMap<>(ClinicalAttributeDataSource.class);
        }
        return new StudyViewFilterHelper(studyViewFilter, clinicalAttributesMap);
    }
    
    private final StudyViewFilter studyViewFilter;
    private final CategorizedClinicalDataCountFilter categorizedClinicalDataCountFilter;

   
    private StudyViewFilterHelper(@NonNull StudyViewFilter studyViewFilter, @NonNull Map<ClinicalAttributeDataSource, List<ClinicalAttribute>> clinicalAttributesMap ) {
        this.studyViewFilter = studyViewFilter;
        this.categorizedClinicalDataCountFilter = extractClinicalDataCountFilters(studyViewFilter, clinicalAttributesMap);
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
    

}
