package org.cbioportal.service.treatment;

import org.cbioportal.model.PatientTreatmentReport;
import org.cbioportal.model.SampleTreatmentReport;
import org.cbioportal.web.parameter.StudyViewFilter;

public interface TreatmentCountReportService {
    PatientTreatmentReport getPatientTreatmentReport(StudyViewFilter studyViewFilter);
    SampleTreatmentReport getSampleTreatmentReport(StudyViewFilter studyViewFilter);
}
