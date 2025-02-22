package org.cbioportal.legacy.service;

import java.util.List;
import org.cbioportal.legacy.model.ClinicalEventKeyCode;
import org.cbioportal.legacy.model.PatientTreatmentRow;
import org.cbioportal.legacy.model.SampleTreatmentRow;

public interface TreatmentService {
  public List<SampleTreatmentRow> getAllSampleTreatmentRows(
      List<String> samples, List<String> studies, ClinicalEventKeyCode key);

  public List<PatientTreatmentRow> getAllPatientTreatmentRows(
      List<String> samples, List<String> studies, ClinicalEventKeyCode key);

  public Boolean containsTreatmentData(List<String> studies, ClinicalEventKeyCode tier);

  public Boolean containsSampleTreatmentData(List<String> studyIds, ClinicalEventKeyCode tier);
}
