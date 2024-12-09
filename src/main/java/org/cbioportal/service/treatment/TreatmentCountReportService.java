package org.cbioportal.service.treatment;

import org.cbioportal.model.PatientTreatmentReport;
import org.cbioportal.model.SampleTreatmentReport;
import org.cbioportal.model.StudyViewFilterContext;
import org.cbioportal.web.parameter.CustomSampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;

import java.util.List;

public interface TreatmentCountReportService {
    PatientTreatmentReport getPatientTreatmentReport(StudyViewFilterContext studyViewFilterContext);
    SampleTreatmentReport getSampleTreatmentReport(StudyViewFilterContext studyViewFilterContext);
}
