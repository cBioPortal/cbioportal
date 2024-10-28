package org.cbioportal.web.columnar.util;

import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.Sample;
import org.cbioportal.service.StudyViewColumnarService;
import org.cbioportal.web.parameter.StudyViewFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClinicalDataXyPlotUtil {
    public static List<ClinicalData> fetchClinicalDataForXyPlot(
        StudyViewColumnarService studyViewColumnarService,
        StudyViewFilter studyViewFilter,
        List<String> attributeIds,
        boolean shouldFilterNonEmptyClinicalData
    ) {
        List<ClinicalData> combinedClinicalDataList;
        List<ClinicalData> sampleClinicalDataList = studyViewColumnarService.getSampleClinicalData(studyViewFilter, attributeIds);
        List<ClinicalData> patientClinicalDataList = studyViewColumnarService.getPatientClinicalData(studyViewFilter, attributeIds);

        if (shouldFilterNonEmptyClinicalData) {
            sampleClinicalDataList = filterNonEmptyClinicalData(sampleClinicalDataList);
            patientClinicalDataList = filterNonEmptyClinicalData(patientClinicalDataList);
        }

        if (patientClinicalDataList.isEmpty()) {
            combinedClinicalDataList = sampleClinicalDataList;
        } else {
            // fetch samples for the given study view filter.
            // we need this to construct the complete patient to sample map. 
            List<Sample> samples = studyViewColumnarService.getFilteredSamples(studyViewFilter);

            combinedClinicalDataList = Stream.concat(
                sampleClinicalDataList.stream(),
                convertPatientClinicalDataToSampleClinicalData(patientClinicalDataList, samples).stream()
            ).toList();
        }

        return combinedClinicalDataList;
    }
    
    public static List<ClinicalData> filterNonEmptyClinicalData(List<ClinicalData> clinicalData) {
        return clinicalData
            .stream()
            .filter(data -> !data.getAttrValue().isEmpty())
            .toList();
    }
    
    public static List<ClinicalData> convertPatientClinicalDataToSampleClinicalData(
        List<ClinicalData> patientClinicalDataList,
        List<Sample> samplesWithoutNumericalFilter
    ) {
        List<ClinicalData> sampleClinicalDataList = new ArrayList<>();

        Map<String, Map<String, List<Sample>>> patientToSamples = samplesWithoutNumericalFilter
            .stream()
            .collect(Collectors.groupingBy(
                Sample::getPatientStableId,
                Collectors.groupingBy(Sample::getCancerStudyIdentifier)
            ));

        // put all clinical data into sample form
        for (ClinicalData d: patientClinicalDataList) {
            List<Sample> samplesForPatient = patientToSamples.get(d.getPatientId()).get(d.getStudyId());
            if (samplesForPatient != null) {
                for (Sample s: samplesForPatient) {
                    ClinicalData newData = new ClinicalData();

                    newData.setInternalId(s.getInternalId());
                    newData.setAttrId(d.getAttrId());
                    newData.setPatientId(d.getPatientId());
                    newData.setStudyId(d.getStudyId());
                    newData.setAttrValue(d.getAttrValue());
                    newData.setSampleId(s.getStableId());

                    sampleClinicalDataList.add(newData);
                }
            } else {
                // TODO ignoring for now rather than throwing an error
                // patient has no samples - this shouldn't happen and could affect the integrity
                //  of the data analysis
                // return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return sampleClinicalDataList;
    }
}
