package org.cbioportal.service.treatment;

import org.cbioportal.model.PatientTreatmentReport;
import org.cbioportal.model.SampleTreatmentReport;
import org.cbioportal.web.parameter.CustomSampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;

import java.util.List;

public interface TreatmentCountReportService {
    PatientTreatmentReport getPatientTreatmentReport(StudyViewFilter studyViewFilter, List<CustomSampleIdentifier> customDataSamples);
    SampleTreatmentReport getSampleTreatmentReport(StudyViewFilter studyViewFilter, List<CustomSampleIdentifier> customDataSamples);
}
