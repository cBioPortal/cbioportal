package org.cbioportal.service;

import org.cbioportal.model.PatientTreatmentRow;
import org.cbioportal.model.SampleTreatmentRow;

import java.util.List;
import java.util.Set;

public interface TreatmentService {
    public List<SampleTreatmentRow> getAllSampleTreatmentRows(List<String> samples, List<String> studies);
    public List<PatientTreatmentRow> getAllPatientTreatmentRows(List<String> samples, List<String> studies);
    public Boolean containsTreatmentData(List<String> samples, List<String> studies);
}
