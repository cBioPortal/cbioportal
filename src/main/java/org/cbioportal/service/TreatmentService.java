package org.cbioportal.service;

import java.util.List;

import org.cbioportal.model.PatientTreatmentRow;
import org.cbioportal.model.SampleTreatmentRow;
import org.cbioportal.model.ClinicalEventKeyCode;

public interface TreatmentService {
    public List<SampleTreatmentRow> getAllSampleTreatmentRows(List<String> samples, List<String> studies, ClinicalEventKeyCode key);
    public List<PatientTreatmentRow> getAllPatientTreatmentRows(List<String> samples, List<String> studies, ClinicalEventKeyCode key);
    public Boolean containsTreatmentData(List<String> studies, ClinicalEventKeyCode tier);
    public Boolean containsSampleTreatmentData(List<String> studyIds, ClinicalEventKeyCode tier);
}
