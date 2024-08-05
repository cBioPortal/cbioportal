package org.cbioportal.service.treatment;

import org.cbioportal.model.PatientTreatmentReport;
import org.cbioportal.model.SampleTreatmentReport;
import org.cbioportal.persistence.StudyViewRepository;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TreatmentCountReportServiceImpl implements TreatmentCountReportService {
    
    private final StudyViewRepository studyViewRepository;
    
    @Autowired
    public TreatmentCountReportServiceImpl(StudyViewRepository studyViewRepository) {
        this.studyViewRepository = studyViewRepository;
    }

    @Override
    public PatientTreatmentReport getPatientTreatmentReport(StudyViewFilter studyViewFilter) {
        return studyViewRepository.getPatientTreatmentReport(studyViewFilter);
    }

    @Override
    public SampleTreatmentReport getSampleTreatmentReport(StudyViewFilter studyViewFilter) {
        var sampleTreatments = studyViewRepository.getSampleTreatments(studyViewFilter);
        var totalSampleTreatmentCount = studyViewRepository.getTotalSampleTreatmentCount(studyViewFilter);
        return new SampleTreatmentReport(totalSampleTreatmentCount, sampleTreatments);
    }
    
}
