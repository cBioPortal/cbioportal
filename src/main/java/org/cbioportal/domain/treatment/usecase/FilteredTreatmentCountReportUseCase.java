package org.cbioportal.domain.treatment.usecase;

import org.cbioportal.legacy.model.PatientTreatmentReport;
import org.cbioportal.legacy.model.SampleTreatmentReport;
import org.cbioportal.legacy.model.SampleTreatmentRow;
import org.cbioportal.legacy.model.TemporalRelation;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.domain.treatment.repository.TreatmentRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Stream;

@Service
@Profile("clickhouse")
public class FilteredTreatmentCountReportUseCase {

    private final TreatmentRepository treatmentRepository;

    public FilteredTreatmentCountReportUseCase(TreatmentRepository treatmentRepository) {
        this.treatmentRepository = treatmentRepository;
    }

    public PatientTreatmentReport getFilteredPatientTreatmentReport(StudyViewFilterContext studyViewFilterContext){
        var patientTreatments = treatmentRepository.getPatientTreatments(studyViewFilterContext);
        var totalPatientTreatmentCount = treatmentRepository.getTotalPatientTreatmentCount(studyViewFilterContext);
        return new PatientTreatmentReport(totalPatientTreatmentCount, 0, patientTreatments);
    }

    public SampleTreatmentReport getFilteredSampleTreatmentReport(StudyViewFilterContext studyViewFilterContext){
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
