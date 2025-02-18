package org.cbioportal.domain.treatment.usecase;

import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.domain.treatment.repository.TreatmentRepository;
import org.cbioportal.legacy.model.SampleTreatmentReport;
import org.cbioportal.legacy.model.SampleTreatmentRow;
import org.cbioportal.legacy.model.TemporalRelation;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Stream;

@Service
@Profile("clickhouse")
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
    public SampleTreatmentReport execute(StudyViewFilterContext studyViewFilterContext){
        var sampleTreatments = treatmentRepository.getSampleTreatments(studyViewFilterContext)
                .stream()
                .flatMap(sampleTreatment ->
                        Stream.of(new SampleTreatmentRow(TemporalRelation.Pre, sampleTreatment.treatment(), sampleTreatment.preSampleCount(), Set.of()),
                                new SampleTreatmentRow(TemporalRelation.Post, sampleTreatment.treatment(), sampleTreatment.postSampleCount(), Set.of() ))
                )
                .filter(sampleTreatment -> sampleTreatment.getCount() > 0 )
                .toList();
        var totalSampleTreatmentCount = treatmentRepository.getTotalSampleTreatmentCount(studyViewFilterContext);
        return new SampleTreatmentReport(totalSampleTreatmentCount, sampleTreatments);
    }
}
