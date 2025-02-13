package org.cbioportal.treatment.repository;

import org.cbioportal.legacy.model.PatientTreatment;
import org.cbioportal.legacy.model.SampleTreatment;
import org.cbioportal.studyview.StudyViewFilterContext;

import java.util.List;

public interface TreatmentRepository {
    List<PatientTreatment> getPatientTreatments(StudyViewFilterContext studyViewFilterContext);
    int getTotalPatientTreatmentCount(StudyViewFilterContext studyViewFilterContext);
    List<SampleTreatment> getSampleTreatments(StudyViewFilterContext studyViewFilterContext);
    int getTotalSampleTreatmentCount(StudyViewFilterContext studyViewFilterContext);
}
