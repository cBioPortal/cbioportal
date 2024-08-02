package org.cbioportal.service.studyview;

import org.cbioportal.model.SampleTreatmentReport;
import org.cbioportal.web.parameter.StudyViewFilter;

public interface TreatmentStudyViewService {
    SampleTreatmentReport getSampleTreatmentReport(StudyViewFilter studyViewFilter);
}
