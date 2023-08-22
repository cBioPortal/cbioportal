package org.cbioportal.web.util;

import org.apache.commons.collections4.CollectionUtils;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.web.parameter.ClinicalDataType;
import org.cbioportal.web.parameter.Projection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ClinicalDataFetcher {

    @Autowired
    private ClinicalDataService clinicalDataService;
    
    public List<ClinicalData> fetchClinicalDataForSamples(
        List<String> studyIds,
        List<String> sampleIds,
        List<String> sampleAttributeIds
    ) {
        List<ClinicalData> filteredClinicalDataForSamples = Collections.emptyList();

        if (CollectionUtils.isNotEmpty(sampleAttributeIds)) {
            filteredClinicalDataForSamples = clinicalDataService.fetchClinicalData(
                studyIds,
                sampleIds,
                sampleAttributeIds,
                ClinicalDataType.SAMPLE.name(),
                Projection.SUMMARY.name()
            );
        }

        return filteredClinicalDataForSamples;
    }

    public List<ClinicalData> fetchClinicalDataForPatients(
        List<String> studyIdsOfPatients,
        List<String> patientIds,
        List<String> patientAttributeIds
    ) {
        List<ClinicalData> filteredClinicalDataForPatients = Collections.emptyList();

        if (CollectionUtils.isNotEmpty(patientAttributeIds)) {
            filteredClinicalDataForPatients = clinicalDataService.fetchClinicalData(
                studyIdsOfPatients,
                patientIds,
                patientAttributeIds,
                ClinicalDataType.PATIENT.name(),
                Projection.SUMMARY.name()
            );
        }

        return filteredClinicalDataForPatients;
    }

    public List<ClinicalData> fetchClinicalDataForConflictingPatientAttributes(
        List<String> studyIdsOfPatients,
        List<String> patientIds,
        List<String> conflictingPatientAttributes
    ) {
        List<ClinicalData> filteredClinicalDataForPatients = Collections.emptyList();

        if (CollectionUtils.isNotEmpty(conflictingPatientAttributes)) {
            filteredClinicalDataForPatients = clinicalDataService.getPatientClinicalDataDetailedToSample(
                studyIdsOfPatients,
                patientIds,
                conflictingPatientAttributes
            );
        }

        return filteredClinicalDataForPatients;
    }

    public List<ClinicalData> fetchClinicalData(
        List<String> studyIds,
        List<String> sampleIds,
        List<String> patientIds,
        List<String> studyIdsOfPatients,
        List<String> sampleAttributeIds,
        List<String> patientAttributeIds,
        List<String> conflictingPatientAttributes
    ) {
        List<ClinicalData> unfilteredClinicalDataForSamples = fetchClinicalDataForSamples(
            studyIds,
            sampleIds,
            sampleAttributeIds
        );

        List<ClinicalData> unfilteredClinicalDataForPatients = fetchClinicalDataForPatients(
            studyIdsOfPatients,
            patientIds,
            patientAttributeIds
        );

        List<ClinicalData> unfilteredClinicalDataForConflictingPatientAttributes = fetchClinicalDataForConflictingPatientAttributes(
            studyIdsOfPatients,
            patientIds,
            conflictingPatientAttributes
        );

        return Stream.of(
            unfilteredClinicalDataForSamples,
            unfilteredClinicalDataForPatients,
            unfilteredClinicalDataForConflictingPatientAttributes
        ).flatMap(Collection::stream).collect(Collectors.toList());
    }
}
