package org.cbioportal.legacy.service.treatment;

import org.cbioportal.legacy.model.PatientTreatmentReport;
import org.cbioportal.legacy.model.SampleTreatmentReport;
import org.cbioportal.legacy.model.StudyViewFilterContext;
import org.cbioportal.legacy.web.parameter.CustomSampleIdentifier;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;

import java.util.List;

public interface TreatmentCountReportService {
    PatientTreatmentReport getPatientTreatmentReport(StudyViewFilterContext studyViewFilterContext);
    SampleTreatmentReport getSampleTreatmentReport(StudyViewFilterContext studyViewFilterContext);
}
