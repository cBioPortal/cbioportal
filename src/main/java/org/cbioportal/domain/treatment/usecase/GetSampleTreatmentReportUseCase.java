package org.cbioportal.domain.treatment.usecase;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.domain.treatment.repository.TreatmentRepository;
import org.cbioportal.legacy.model.SampleTreatmentReport;
import org.cbioportal.legacy.model.SampleTreatmentRow;
import org.cbioportal.legacy.model.TemporalRelation;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.stereotype.Service;

@Service
public class GetSampleTreatmentReportUseCase {
  private final TreatmentRepository treatmentRepository;

  public GetSampleTreatmentReportUseCase(TreatmentRepository treatmentRepository) {
    this.treatmentRepository = treatmentRepository;
  }

  /**
   * Executes the use case to retrieve a sample treatment report based on the given filter context.
   *
   * @param studyViewFilterContext the filtering criteria for retrieving sample treatments
   * @return a {@link SampleTreatmentReport} containing treatment data for samples
   */
  public SampleTreatmentReport execute(
      StudyViewFilterContext studyViewFilterContext, ProjectionType projection) {
    var sampleTreatments =
        treatmentRepository.getSampleTreatments(studyViewFilterContext, projection).stream()
            .flatMap(
                sampleTreatment ->
                    Stream.of(
                        new SampleTreatmentRow(
                            TemporalRelation.Pre,
                            sampleTreatment.treatment(),
                            sampleTreatment.preSampleCount(),
                            sampleTreatment.preSamples() == null
                                ? Collections.emptySet()
                                : Set.copyOf(sampleTreatment.preSamples())),
                        new SampleTreatmentRow(
                            TemporalRelation.Post,
                            sampleTreatment.treatment(),
                            sampleTreatment.postSampleCount(),
                            sampleTreatment.postSamples() == null
                                ? Collections.emptySet()
                                : Set.copyOf(sampleTreatment.postSamples()))))
            .filter(sampleTreatment -> sampleTreatment.getCount() > 0)
            .toList();
    var totalSampleTreatmentCount =
        treatmentRepository.getTotalSampleTreatmentCount(studyViewFilterContext);
    return new SampleTreatmentReport(totalSampleTreatmentCount, sampleTreatments);
  }
}
