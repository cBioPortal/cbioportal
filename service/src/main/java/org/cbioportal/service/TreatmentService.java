package org.cbioportal.service;

import java.util.List;

import org.cbioportal.model.PatientTreatmentRow;
import org.cbioportal.model.SampleTreatmentRow;
import org.cbioportal.model.TreatmentSankeyGraph;

public interface TreatmentService {
    public TreatmentSankeyGraph getSequences(List<String> sampleIds, List<String> studyIds);
    public List<SampleTreatmentRow> getAllSampleTreatmentRows(List<String> samples, List<String> studies);
    public List<PatientTreatmentRow> getAllPatientTreatmentRows(List<String> samples, List<String> studies);
    public Boolean containsTreatmentData(List<String> studies);
    public Boolean containsSampleTreatmentData(List<String> studyIds);
}
