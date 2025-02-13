package org.cbioportal.clinical_data.usecase;

import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.sample.Sample;
import org.cbioportal.sample.usecase.GetFilteredSamplesUseCase;
import org.cbioportal.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Profile("clickhouse")
public class GetClinicalDataForXyPlotUseCase {
    private final GetPatientClinicalDataUseCase getPatientClinicalDataUseCase;
    private final GetSampleClinicalDataUseCase getSampleClinicalDataUseCase;
    private final GetFilteredSamplesUseCase getFilteredSamplesUseCase;

    public GetClinicalDataForXyPlotUseCase(GetPatientClinicalDataUseCase getPatientClinicalDataUseCase, GetSampleClinicalDataUseCase getSampleClinicalDataUseCase, GetFilteredSamplesUseCase getFilteredSamplesUseCase) {
        this.getPatientClinicalDataUseCase = getPatientClinicalDataUseCase;
        this.getSampleClinicalDataUseCase = getSampleClinicalDataUseCase;
        this.getFilteredSamplesUseCase = getFilteredSamplesUseCase;
    }

    public List<ClinicalData> execute(StudyViewFilterContext studyViewFilterContext, List<String> attributeIds,
                                      boolean shouldFilterNonEmptyClinicalData){

        List<ClinicalData> sampleClinicalDataList = getSampleClinicalDataUseCase.execute(studyViewFilterContext, attributeIds);
        List<ClinicalData> patientClinicalDataList = getPatientClinicalDataUseCase.execute(studyViewFilterContext, attributeIds);

        List<Sample> samples = List.of();

        if (!patientClinicalDataList.isEmpty()) {
            // fetch samples for the given study view filter.
            // we need this to construct the complete patient to sample map.
            samples = getFilteredSamplesUseCase.execute(studyViewFilterContext);
        }

        return combineClinicalDataForXyPlot(sampleClinicalDataList, patientClinicalDataList, samples, shouldFilterNonEmptyClinicalData);

    }

    private List<ClinicalData> combineClinicalDataForXyPlot(
            List<ClinicalData> sampleClinicalDataList,
            List<ClinicalData> patientClinicalDataList,
            List<Sample> samples,
            boolean shouldFilterNonEmptyClinicalData
    ) {
        List<ClinicalData> combinedClinicalDataList;

        if (shouldFilterNonEmptyClinicalData) {
            sampleClinicalDataList = filterNonEmptyClinicalData(sampleClinicalDataList);
            patientClinicalDataList = filterNonEmptyClinicalData(patientClinicalDataList);
        }

        if (patientClinicalDataList.isEmpty()) {
            combinedClinicalDataList = sampleClinicalDataList;
        } else {
            combinedClinicalDataList = Stream.concat(
                    sampleClinicalDataList.stream(),
                    convertPatientClinicalDataToSampleClinicalData(patientClinicalDataList, samples).stream()
            ).toList();
        }

        return combinedClinicalDataList;
    }

    private List<ClinicalData> filterNonEmptyClinicalData(List<ClinicalData> clinicalData) {
        return clinicalData
                .stream()
                .filter(data -> !data.getAttrValue().isEmpty())
                .toList();
    }

    private List<ClinicalData> convertPatientClinicalDataToSampleClinicalData(
            List<ClinicalData> patientClinicalDataList,
            List<Sample> samplesWithoutNumericalFilter
    ) {
        List<ClinicalData> sampleClinicalDataList = new ArrayList<>();

        Map<String, Map<String, List<Sample>>> patientToSamples = samplesWithoutNumericalFilter
                .stream()
                .collect(Collectors.groupingBy(
                       Sample::patientStableId,
                        Collectors.groupingBy(Sample::cancerStudyIdentifier)
                ));

        // put all clinical data into sample form
        for (ClinicalData d: patientClinicalDataList) {
            List<Sample> samplesForPatient = patientToSamples.get(d.getPatientId()).get(d.getStudyId());
            if (samplesForPatient != null) {
                for (Sample s: samplesForPatient) {
                    ClinicalData newData = new ClinicalData();

                    newData.setInternalId(s.internalId());
                    newData.setAttrId(d.getAttrId());
                    newData.setPatientId(d.getPatientId());
                    newData.setStudyId(d.getStudyId());
                    newData.setAttrValue(d.getAttrValue());
                    newData.setSampleId(s.stableId());

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
