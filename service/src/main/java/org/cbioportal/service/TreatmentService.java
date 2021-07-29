package org.cbioportal.service;

import java.util.List;

import org.cbioportal.model.PatientTreatmentRow;
import org.cbioportal.model.SampleTreatmentRow;
import org.cbioportal.model.TreatmentClassificationTier;

public interface TreatmentService {
    public List<SampleTreatmentRow> getAllSampleTreatmentRows(List<String> samples, List<String> studies, TreatmentClassificationTier tier);
    public List<PatientTreatmentRow> getAllPatientTreatmentRows(List<String> samples, List<String> studies, TreatmentClassificationTier tier);
    public Boolean containsTreatmentData(List<String> studies);
    public Boolean containsSampleTreatmentData(List<String> studyIds);
}
