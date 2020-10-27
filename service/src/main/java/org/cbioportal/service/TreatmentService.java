package org.cbioportal.service;

import java.util.List;

import org.cbioportal.model.PatientTreatmentRow;
import org.cbioportal.model.SampleTreatmentRow;

public interface TreatmentService {
    public List<SampleTreatmentRow> getAllSampleTreatmentRows(List<String> samples, List<String> studies);
    public List<PatientTreatmentRow> getAllPatientTreatmentRows(List<String> samples, List<String> studies);
    public Boolean containsTreatmentData(List<String> studies);
    public Boolean containsSampleTreatmentData(List<String> studyIds);
}
