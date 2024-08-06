package org.cbioportal.service.treatment;

import org.cbioportal.model.PatientTreatmentReport;
import org.cbioportal.model.SampleTreatmentReport;
import org.cbioportal.model.SampleTreatmentRow;
import org.cbioportal.model.TemporalRelation;
import org.cbioportal.persistence.StudyViewRepository;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Stream;

@Service
public class TreatmentCountReportServiceImpl implements TreatmentCountReportService {
    
    private final StudyViewRepository studyViewRepository;
    
    @Autowired
    public TreatmentCountReportServiceImpl(StudyViewRepository studyViewRepository) {
        this.studyViewRepository = studyViewRepository;
    }

    @Override
    public PatientTreatmentReport getPatientTreatmentReport(StudyViewFilter studyViewFilter) {
        var patientTreatments = studyViewRepository.getPatientTreatments(studyViewFilter);
        var totalPatientTreatmentCount = studyViewRepository.getTotalPatientTreatmentCount(studyViewFilter);
        return new PatientTreatmentReport(totalPatientTreatmentCount, 0, patientTreatments);
    }

    @Override
    public SampleTreatmentReport getSampleTreatmentReport(StudyViewFilter studyViewFilter) {
        var sampleTreatments = studyViewRepository.getSampleTreatments(studyViewFilter)
            .stream()
            .flatMap(sampleTreatment -> 
                Stream.of(new SampleTreatmentRow(TemporalRelation.Pre, sampleTreatment.treatment(), sampleTreatment.preSampleCount(), Set.of()),
                    new SampleTreatmentRow(TemporalRelation.Post, sampleTreatment.treatment(), sampleTreatment.postSampleCount(), Set.of() ))
                    )
            .toList();
        var totalSampleTreatmentCount = studyViewRepository.getTotalSampleTreatmentCount(studyViewFilter);
        return new SampleTreatmentReport(totalSampleTreatmentCount, sampleTreatments);
    }
    
}
