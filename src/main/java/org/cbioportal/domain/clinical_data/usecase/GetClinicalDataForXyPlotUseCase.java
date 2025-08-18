package org.cbioportal.domain.clinical_data.usecase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.cbioportal.domain.clinical_data.ClinicalData;
import org.cbioportal.domain.sample.Sample;
import org.cbioportal.domain.sample.usecase.GetFilteredSamplesUseCase;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Use case for retrieving and combining clinical data for an XY plot. This class orchestrates the
 * fetching of clinical data for both patients and samples, then combines them based on the provided
 * context and filter options, preparing them for XY plot visualization.
 */
@Service
@Profile("clickhouse")
public class GetClinicalDataForXyPlotUseCase {

  private final GetPatientClinicalDataUseCase getPatientClinicalDataUseCase;
  private final GetSampleClinicalDataUseCase getSampleClinicalDataUseCase;
  private final GetFilteredSamplesUseCase getFilteredSamplesUseCase;

  /**
   * Constructs a {@code GetClinicalDataForXyPlotUseCase} with the provided use cases.
   *
   * @param getPatientClinicalDataUseCase the use case for retrieving patient clinical data
   * @param getSampleClinicalDataUseCase the use case for retrieving sample clinical data
   * @param getFilteredSamplesUseCase the use case for filtering samples
   */
  public GetClinicalDataForXyPlotUseCase(
      GetPatientClinicalDataUseCase getPatientClinicalDataUseCase,
      GetSampleClinicalDataUseCase getSampleClinicalDataUseCase,
      GetFilteredSamplesUseCase getFilteredSamplesUseCase) {
    this.getPatientClinicalDataUseCase = getPatientClinicalDataUseCase;
    this.getSampleClinicalDataUseCase = getSampleClinicalDataUseCase;
    this.getFilteredSamplesUseCase = getFilteredSamplesUseCase;
  }

  /**
   * Executes the use case to retrieve and combine clinical data for an XY plot.
   *
   * @param studyViewFilterContext the context of the study view filter to apply
   * @param attributeIds a list of attribute IDs to filter the clinical data
   * @param shouldFilterNonEmptyClinicalData flag indicating whether to filter out clinical data
   *     with empty values
   * @return a list of {@link ClinicalData} ready for XY plot visualization
   */
  public List<ClinicalData> execute(
      StudyViewFilterContext studyViewFilterContext,
      List<String> attributeIds,
      boolean shouldFilterNonEmptyClinicalData) {

    List<ClinicalData> sampleClinicalDataList =
        getSampleClinicalDataUseCase.execute(studyViewFilterContext, attributeIds);
    List<ClinicalData> patientClinicalDataList =
        getPatientClinicalDataUseCase.execute(studyViewFilterContext, attributeIds);

    List<Sample> samples = List.of();

    if (!patientClinicalDataList.isEmpty()) {
      // fetch samples for the given study view filter.
      // we need this to construct the complete patient to sample map.
      samples = getFilteredSamplesUseCase.execute(studyViewFilterContext);
    }

    return combineClinicalDataForXyPlot(
        sampleClinicalDataList, patientClinicalDataList, samples, shouldFilterNonEmptyClinicalData);
  }

  /**
   * Combines the clinical data for samples and patients into a single list, optionally filtering
   * non-empty data.
   *
   * @param sampleClinicalDataList a list of clinical data for samples
   * @param patientClinicalDataList a list of clinical data for patients
   * @param samples a list of samples to map patient data to
   * @param shouldFilterNonEmptyClinicalData flag indicating whether to filter out clinical data
   *     with empty values
   * @return a list of combined {@link ClinicalData} for XY plot visualization
   */
  private List<ClinicalData> combineClinicalDataForXyPlot(
      List<ClinicalData> sampleClinicalDataList,
      List<ClinicalData> patientClinicalDataList,
      List<Sample> samples,
      boolean shouldFilterNonEmptyClinicalData) {

    List<ClinicalData> combinedClinicalDataList;

    if (shouldFilterNonEmptyClinicalData) {
      sampleClinicalDataList = filterNonEmptyClinicalData(sampleClinicalDataList);
      patientClinicalDataList = filterNonEmptyClinicalData(patientClinicalDataList);
    }

    if (patientClinicalDataList.isEmpty()) {
      combinedClinicalDataList = sampleClinicalDataList;
    } else {
      combinedClinicalDataList =
          Stream.concat(
                  sampleClinicalDataList.stream(),
                  convertPatientClinicalDataToSampleClinicalData(patientClinicalDataList, samples)
                      .stream())
              .toList();
    }

    return combinedClinicalDataList;
  }

  /**
   * Filters out clinical data items with empty attribute values.
   *
   * @param clinicalData the list of clinical data to filter
   * @return a filtered list of {@link ClinicalData} containing only non-empty attribute values
   */
  private List<ClinicalData> filterNonEmptyClinicalData(List<ClinicalData> clinicalData) {
    return clinicalData.stream().filter(data -> !data.attrValue().isEmpty()).toList();
  }

  /**
   * Converts patient clinical data into sample clinical data, mapping each patient to their
   * corresponding samples.
   *
   * @param patientClinicalDataList a list of clinical data for patients
   * @param samplesWithoutNumericalFilter a list of samples to map patient data to
   * @return a list of {@link ClinicalData} representing the patient's clinical data in sample form
   */
  private List<ClinicalData> convertPatientClinicalDataToSampleClinicalData(
      List<ClinicalData> patientClinicalDataList, List<Sample> samplesWithoutNumericalFilter) {

    List<ClinicalData> sampleClinicalDataList = new ArrayList<>();

    Map<String, Map<String, List<Sample>>> patientToSamples =
        samplesWithoutNumericalFilter.stream()
            .collect(
                Collectors.groupingBy(
                    Sample::patientStableId, Collectors.groupingBy(Sample::cancerStudyIdentifier)));

    // Put all clinical data into sample form
    for (ClinicalData d : patientClinicalDataList) {
      List<Sample> samplesForPatient = patientToSamples.get(d.patientId()).get(d.studyId());
      if (samplesForPatient != null) {
        for (Sample s : samplesForPatient) {
          ClinicalData newData =
              new ClinicalData(
                  s.internalId(),
                  s.stableId(),
                  d.patientId(),
                  d.studyId(),
                  d.attrId(),
                  d.attrValue());
          sampleClinicalDataList.add(newData);
        }
      } else {
        // TODO: Ignoring for now rather than throwing an error
        // patient has no samples - this shouldn't happen and could affect the integrity
        // of the data analysis
        // return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }

    return sampleClinicalDataList;
  }
}
