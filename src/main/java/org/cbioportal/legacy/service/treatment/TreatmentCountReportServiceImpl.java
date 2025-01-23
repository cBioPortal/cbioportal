package org.cbioportal.legacy.service.treatment;

import org.cbioportal.legacy.model.PatientTreatmentReport;
import org.cbioportal.legacy.model.SampleTreatmentReport;
import org.cbioportal.legacy.model.SampleTreatmentRow;
import org.cbioportal.legacy.model.StudyViewFilterContext;
import org.cbioportal.legacy.model.TemporalRelation;
import org.cbioportal.legacy.persistence.StudyViewRepository;
import org.cbioportal.legacy.utils.config.annotation.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Stream;

@Service
@ConditionalOnProperty(name = "clickhouse_mode", havingValue = "true")
public class TreatmentCountReportServiceImpl implements TreatmentCountReportService {
    
    private final StudyViewRepository studyViewRepository;
    
    @Autowired
    public TreatmentCountReportServiceImpl(StudyViewRepository studyViewRepository) {
        this.studyViewRepository = studyViewRepository;
    }

    @Override
    public PatientTreatmentReport getPatientTreatmentReport(StudyViewFilterContext studyViewFilterContext) {
        var patientTreatments = studyViewRepository.getPatientTreatments(studyViewFilterContext);
        var totalPatientTreatmentCount = studyViewRepository.getTotalPatientTreatmentCount(studyViewFilterContext);
        return new PatientTreatmentReport(totalPatientTreatmentCount, 0, patientTreatments);
    }

    @Override
    public SampleTreatmentReport getSampleTreatmentReport(StudyViewFilterContext studyViewFilterContext) {
        var sampleTreatments = studyViewRepository.getSampleTreatments(studyViewFilterContext)
            .stream()
            .flatMap(sampleTreatment -> 
                Stream.of(new SampleTreatmentRow(TemporalRelation.Pre, sampleTreatment.treatment(), sampleTreatment.preSampleCount(), Set.of()),
                    new SampleTreatmentRow(TemporalRelation.Post, sampleTreatment.treatment(), sampleTreatment.postSampleCount(), Set.of() ))
                    )
            .filter(sampleTreatment -> sampleTreatment.getCount() > 0 )
            .toList();
        var totalSampleTreatmentCount = studyViewRepository.getTotalSampleTreatmentCount(studyViewFilterContext);
        return new SampleTreatmentReport(totalSampleTreatmentCount, sampleTreatments);
    }
    
}
