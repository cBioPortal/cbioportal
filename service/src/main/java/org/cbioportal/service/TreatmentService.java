package org.cbioportal.service;

import org.cbioportal.model.PatientTreatmentRow;
import org.cbioportal.model.SampleTreatmentRow;

import java.util.List;
import java.util.Set;

public interface TreatmentService {
    public List<SampleTreatmentRow> getAllTreatmentSampleRows(List<String> samples, List<String> studies, Set<String> treatments);
    public List<PatientTreatmentRow> getAllTreatmentPatientRows(List<String> samples, List<String> studies, Set<String> treatments);
}
