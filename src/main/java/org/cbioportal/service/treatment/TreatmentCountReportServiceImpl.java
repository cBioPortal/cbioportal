package org.cbioportal.service.treatment;

import org.cbioportal.model.PatientTreatmentReport;
import org.cbioportal.model.SampleTreatmentReport;
import org.cbioportal.model.SampleTreatmentRow;
import org.cbioportal.model.TemporalRelation;
import org.cbioportal.persistence.StudyViewRepository;
import org.cbioportal.web.parameter.CustomSampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
    public PatientTreatmentReport getPatientTreatmentReport(StudyViewFilter studyViewFilter, List<CustomSampleIdentifier> customDataSamples) {
        var patientTreatments = studyViewRepository.getPatientTreatments(studyViewFilter, customDataSamples);
        var totalPatientTreatmentCount = studyViewRepository.getTotalPatientTreatmentCount(studyViewFilter, customDataSamples);
        return new PatientTreatmentReport(totalPatientTreatmentCount, 0, patientTreatments);
    }

    @Override
    public SampleTreatmentReport getSampleTreatmentReport(StudyViewFilter studyViewFilter, List<CustomSampleIdentifier> customDataSamples) {
        var sampleTreatments = studyViewRepository.getSampleTreatments(studyViewFilter, customDataSamples)
            .stream()
            .flatMap(sampleTreatment -> 
                Stream.of(new SampleTreatmentRow(TemporalRelation.Pre, sampleTreatment.treatment(), sampleTreatment.preSampleCount(), Set.of()),
                    new SampleTreatmentRow(TemporalRelation.Post, sampleTreatment.treatment(), sampleTreatment.postSampleCount(), Set.of() ))
                    )
            .toList();
        var totalSampleTreatmentCount = studyViewRepository.getTotalSampleTreatmentCount(studyViewFilter, customDataSamples);
        return new SampleTreatmentReport(totalSampleTreatmentCount, sampleTreatments);
    }
    
}
